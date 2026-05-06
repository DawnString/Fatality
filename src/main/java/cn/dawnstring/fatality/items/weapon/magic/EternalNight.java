package cn.dawnstring.fatality.items.weapon.magic;

import cn.dawnstring.fatality.entity.projectile.EternalNightProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import cn.dawnstring.fatality.system.ManaSystem;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 永恒之夜 - 黑暗魔法武器
 * 特性：右键向前方发射一个黑色法球，法球碰撞到目标后，目标周围产生黑雾，黑雾过1s会包裹目标，并造成伤害
 * 伤害225 暴击率18 暴击伤害24 浮动0.4 攻击速度0.25s
 */
public class EternalNight extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 5;
    private static final float BASE_SPELL_DAMAGE = 225.0f;
    private static final float MANA_COST = 5.0f;
    private static final int FOG_DELAY_TICKS = 20;

    public EternalNight()
    {
        super(new Tier() {
            @Override
            public int getUses() {
                return 0;
            }

            @Override
            public float getSpeed() {
                return 0;
            }

            @Override
            public float getAttackDamageBonus() {
                return 0;
            }

            @Override
            public int getLevel() {
                return 0;
            }

            @Override
            public int getEnchantmentValue() {
                return 0;
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.of(net.minecraft.world.item.Items.NETHERITE_INGOT); // 修复材料：下界合金锭
            }
        }, new Properties(), (int)BASE_SPELL_DAMAGE, 0.25f, 1f, 0.18f, 24.0f, 0.4f, WeaponEnum.MAGIC);
        
        setStory("一把蕴含永恒黑暗力量的魔法武器，发射的黑色法球会在命中目标后产生致命黑雾，吞噬一切光明。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            if (!ManaSystem.safeConsumeMana(player, MANA_COST)) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§c魔力不足！"),
                        true
                );
                return InteractionResultHolder.fail(itemstack);
            }

            float spellDamage = calculateSpellDamage(player, itemstack);

            // 创建永恒之夜投射物
            EternalNightProjectile spell = new EternalNightProjectile(level, player, itemstack, spellDamage, FOG_DELAY_TICKS);

            // 设置法术位置和方向
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();

            spell.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
            spell.shoot(lookVec.x, lookVec.y, lookVec.z, 2.5F, 0.05F); // 2.5速度，极小散布

            // 添加到世界
            level.addFreshEntity(spell);

            // 播放施法音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.7F, 0.6F);

            // 在客户端生成黑暗粒子效果
            if (level.isClientSide()) {
                spawnEternalNightParticles(level, player);
            }
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 生成永恒之夜粒子效果（法术轨迹）
     */
    private void spawnEternalNightParticles(Level level, Player player) {
        Vec3 lookVec = player.getLookAngle();
        Vec3 eyePos = player.getEyePosition();

        // 法术发射起点位置
        Vec3 castPos = eyePos.add(lookVec.scale(0.5));

        // 生成黑暗轨迹粒子
        for (int i = 0; i < 15; i++) {
            // 沿着发射方向生成粒子
            double offsetX = (Math.random() - 0.5) * 0.3;
            double offsetY = (Math.random() - 0.5) * 0.3;
            double offsetZ = (Math.random() - 0.5) * 0.3;

            // 粒子速度（沿着发射方向）
            double velocityX = lookVec.x * 0.25 + (Math.random() - 0.5) * 0.04;
            double velocityY = lookVec.y * 0.25 + (Math.random() - 0.5) * 0.04;
            double velocityZ = lookVec.z * 0.25 + (Math.random() - 0.5) * 0.04;

            // 生成暗影粒子（使用烟幕粒子）
            level.addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE,
                    castPos.x + offsetX,
                    castPos.y + offsetY,
                    castPos.z + offsetZ,
                    velocityX, velocityY, velocityZ);

            // 生成灵魂火焰粒子（添加黑暗特效）
            if (i % 2 == 0) {
                level.addParticle(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                        castPos.x + offsetX * 0.7,
                        castPos.y + offsetY * 0.7,
                        castPos.z + offsetZ * 0.7,
                        velocityX * 0.7, velocityY * 0.7, velocityZ * 0.7);
            }

            // 生成末影粒子（添加魔法特效）
            if (i % 3 == 0) {
                level.addParticle(net.minecraft.core.particles.ParticleTypes.PORTAL,
                        castPos.x + offsetX * 0.5,
                        castPos.y + offsetY * 0.5,
                        castPos.z + offsetZ * 0.5,
                        velocityX * 0.5, velocityY * 0.5, velocityZ * 0.5);
            }
        }
    }

    /**
     * 计算法术伤害
     */
    private float calculateSpellDamage(Player player, ItemStack itemstack) {
        return calculateFinalDamage(player, itemstack, null);
    }

    /**
     * 获取武器特殊效果描述
     */
    @Override
    public String getSpecialEffectDescription() {
        return "永恒之夜 - 右键发射黑暗法球，命中目标后产生黑雾，1秒后包裹目标造成二次伤害";
    }
}