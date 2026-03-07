package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.entity.projectile.ShadowSpearProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
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
 * 暗影长矛 - 黑暗的近战投掷武器
 * 特性：右键投掷暗影长矛，击中目标后，目标周围出现黑色粒子构成的触手包围目标，造成二次伤害
 * 伤害888 暴击率20 暴击伤害25 浮动0.2 攻击速度1s
 */
public class ShadowSpear extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 20; // 冷却时间20tick（1秒）
    private static final float BASE_SPEAR_DAMAGE = 888.0f; // 基础长矛伤害888
    private static final int TENTACLE_DURATION = 100; // 触手持续时间（5秒，100ticks）

    public ShadowSpear()
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
        }, new Properties().stacksTo(1), 888, 1.0f, 1.0f, 0.20f, 25.0f, 0.2f, WeaponEnum.MELEE);
        
        setStory("一把充满暗影能量的长矛，投掷后会在命中目标周围生成暗影触手，对敌人造成持续的二次伤害。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算长矛伤害（使用BaseWeapon的伤害计算逻辑）
            float spearDamage = calculateSpearDamage(player, itemstack);

            // 创建暗影长矛投射物
            ShadowSpearProjectile spear = new ShadowSpearProjectile(level, player, itemstack, spearDamage);

            // 设置长矛位置和方向
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();

            spear.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
            spear.shoot(lookVec.x, lookVec.y, lookVec.z, 3.0F, 0.1F); // 3.0速度，极小散布

            // 添加到世界
            level.addFreshEntity(spear);

            // 播放投掷音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.0F, 0.8F);

            // 在客户端生成暗影粒子效果
            if (level.isClientSide()) {
                spawnShadowSpearParticles(level, player);
            }
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 生成暗影长矛粒子效果（投掷轨迹）
     */
    private void spawnShadowSpearParticles(Level level, Player player) {
        Vec3 lookVec = player.getLookAngle();
        Vec3 eyePos = player.getEyePosition();

        // 长矛投掷起点位置
        Vec3 throwPos = eyePos.add(lookVec.scale(0.5));

        // 生成暗影轨迹粒子
        for (int i = 0; i < 12; i++) {
            // 沿着投掷方向生成粒子
            double offsetX = (Math.random() - 0.5) * 0.4;
            double offsetY = (Math.random() - 0.5) * 0.4;
            double offsetZ = (Math.random() - 0.5) * 0.4;

            // 粒子速度（沿着投掷方向）
            double velocityX = lookVec.x * 0.4 + (Math.random() - 0.5) * 0.08;
            double velocityY = lookVec.y * 0.4 + (Math.random() - 0.5) * 0.08;
            double velocityZ = lookVec.z * 0.4 + (Math.random() - 0.5) * 0.08;

            // 生成暗影粒子（使用烟幕粒子）
            level.addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE,
                    throwPos.x + offsetX,
                    throwPos.y + offsetY,
                    throwPos.z + offsetZ,
                    velocityX, velocityY, velocityZ);

            // 生成灵魂火焰粒子（添加暗影特效）
            if (i % 3 == 0) {
                level.addParticle(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                        throwPos.x + offsetX * 0.6,
                        throwPos.y + offsetY * 0.6,
                        throwPos.z + offsetZ * 0.6,
                        velocityX * 0.6, velocityY * 0.6, velocityZ * 0.6);
            }
        }
    }

    /**
     * 计算长矛伤害
     */
    private float calculateSpearDamage(Player player, ItemStack itemstack) {
        return calculateFinalDamage(player, itemstack, null);
    }

    /**
     * 获取攻击距离（长矛有较长的攻击距离）
     */
    @Override
    public double getAttackRange(Player player) {
        return 4.0; // 长矛有4格攻击距离
    }

    /**
     * 获取武器特殊效果描述
     */
    @Override
    public String getSpecialEffectDescription() {
        return "暗影长矛 - 右键投掷暗影长矛，命中目标后生成暗影触手造成二次伤害";
    }
}