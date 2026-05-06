package cn.dawnstring.fatality.items.weapon.magic;

import cn.dawnstring.fatality.entity.projectile.CurseFireProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import cn.dawnstring.fatality.system.ManaSystem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

/**
 * 咒火 - 魔法武器
 * 特性：右键生成追踪火球，命中目标后施加咒火焚烧效果
 */
public class CurseFire extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 20;
    private static final float MANA_COST = 5.0f;

    public CurseFire()
    {
        super(new Tier() {
            @Override
            public int getUses() {
                return 0; // 耐久度
            }

            @Override
            public float getSpeed() {
                return 0; // 挖掘速度
            }

            @Override
            public float getAttackDamageBonus() {
                return 0; // 基础攻击伤害加成
            }

            @Override
            public int getLevel() {
                return 0; // 材料等级（铁级）
            }

            @Override
            public int getEnchantmentValue() {
                return 0; // 附魔能力
            }

            @Override
            public Ingredient getRepairIngredient() {
                // 使用烈焰棒修复
                return null;
            }
        }, new Properties(), 482, 1.0f, 1f, 0.16f, 1.7f, 0.4f, WeaponEnum.MAGIC);

        setStory("咒火法杖，蕴含着诅咒的火焰之力。\n" +
                "释放的火焰会追踪最近的敌人，\n" +
                "被命中的目标将遭受咒火焚烧之苦。");
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

            float curseFireDamage = calculateCurseFireDamage(player, itemstack);
            
            // 创建咒火投射物
            CurseFireProjectile fireball = new CurseFireProjectile(level, player, curseFireDamage);
            
            // 设置投射物位置和方向
            fireball.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            fireball.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            
            level.addFreshEntity(fireball);
            
            // 播放施法音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        // 在客户端生成粒子效果
        if (level.isClientSide()) {
            spawnCastParticles(level, player);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 计算咒火伤害
     */
    public float calculateCurseFireDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }

    /**
     * 生成施法粒子效果
     */
    private void spawnCastParticles(Level level, Player player) {
        // 生成咒火粒子效果
        for (int i = 0; i < 15; i++) {
            double offsetX = (Math.random() - 0.5) * 0.8;
            double offsetY = (Math.random() - 0.5) * 0.8;
            double offsetZ = (Math.random() - 0.5) * 0.8;
            
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    player.getX() + offsetX,
                    player.getY() + player.getEyeHeight() + offsetY,
                    player.getZ() + offsetZ,
                    0, 0, 0);
        }
        
        // 生成紫色魔法粒子
        for (int i = 0; i < 8; i++) {
            double offsetX = (Math.random() - 0.5) * 0.5;
            double offsetY = (Math.random() - 0.5) * 0.5;
            double offsetZ = (Math.random() - 0.5) * 0.5;
            
            level.addParticle(ParticleTypes.WITCH,
                    player.getX() + offsetX,
                    player.getY() + player.getEyeHeight() + offsetY,
                    player.getZ() + offsetZ,
                    0, 0.1, 0);
        }
    }
}