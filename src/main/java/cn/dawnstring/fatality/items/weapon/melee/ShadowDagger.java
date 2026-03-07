package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.entity.projectile.BulletProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 暗影匕首 - 快速近战投掷武器
 * 特性：快速攻击速度、暗影粒子效果、投掷攻击方式
 */
public class ShadowDagger extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 4; // 冷却时间4tick（0.2秒）
    private static final float BASE_DAGGER_DAMAGE = 35.0f; // 基础匕首伤害35

    public ShadowDagger()
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
                return null;
            }
        }, new Properties(), 0, 0.2f, 1f, 0.10f, 0.10f, 0.2f, WeaponEnum.MELEE);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算匕首伤害（使用BaseWeapon的伤害计算逻辑）
            float daggerDamage = calculateDaggerDamage(player, itemstack);

            // 创建匕首投射物（使用BulletProjectile作为基础）
            BulletProjectile dagger = new BulletProjectile(level, player, itemstack, daggerDamage);

            // 设置匕首位置和方向
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();

            dagger.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
            dagger.shoot(lookVec.x, lookVec.y, lookVec.z, 5.0F, 0.2F); // 5.0速度，极小散布

            // 添加到世界
            level.addFreshEntity(dagger);

            // 播放暗影投掷音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8F, 1.5F); // 更高的音调

            // 在客户端生成暗影粒子效果
            if (level.isClientSide()) {
                spawnShadowParticles(level, player);
            }
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 生成暗影粒子效果（匕首投掷轨迹）
     */
    private void spawnShadowParticles(Level level, Player player) {
        Vec3 lookVec = player.getLookAngle();
        Vec3 eyePos = player.getEyePosition();

        // 匕首投掷起点位置
        Vec3 throwPos = eyePos.add(lookVec.scale(0.5));

        // 生成暗影轨迹粒子
        for (int i = 0; i < 8; i++) {
            // 沿着投掷方向生成粒子
            double offsetX = (Math.random() - 0.5) * 0.3;
            double offsetY = (Math.random() - 0.5) * 0.3;
            double offsetZ = (Math.random() - 0.5) * 0.3;

            // 粒子速度（沿着投掷方向）
            double velocityX = lookVec.x * 0.3 + (Math.random() - 0.5) * 0.05;
            double velocityY = lookVec.y * 0.3 + (Math.random() - 0.5) * 0.05;
            double velocityZ = lookVec.z * 0.3 + (Math.random() - 0.5) * 0.05;

            // 生成暗影粒子（使用烟幕粒子）
            level.addParticle(ParticleTypes.SMOKE,
                    throwPos.x + offsetX,
                    throwPos.y + offsetY,
                    throwPos.z + offsetZ,
                    velocityX, velocityY, velocityZ);

            // 生成灵魂火焰粒子（添加暗影特效）
            if (i % 3 == 0) {
                level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        throwPos.x + offsetX * 0.5,
                        throwPos.y + offsetY * 0.5,
                        throwPos.z + offsetZ * 0.5,
                        velocityX * 0.5, velocityY * 0.5, velocityZ * 0.5);
            }
        }
    }

    /**
     * 计算匕首伤害（使用BaseWeapon相同的计算方法）
     */
    public float calculateDaggerDamage(Player player, ItemStack stack) {
        // 使用BaseWeapon的伤害计算逻辑，但基于匕首伤害
        float baseDamage = BASE_DAGGER_DAMAGE;

        // 计算基础伤害加成（基于饰品）
        float accessoryBaseBonus = calculateAccessoryBaseBonus(player);

        // 计算其他伤害加成（饰品、药水等）
        float otherBonus = calculateOtherBonus(player);

        // 计算伤害浮动值
        float fluctuation = calculateDamageFluctuation();

        // 判断是否暴击
        boolean isCritical = isCriticalHit(player);

        float finalDamage;
        if (isCritical) {
            // 暴击伤害公式（与BaseWeapon保持一致）
            float criticalBonus = getCriticalDamageMultiplier(player);
            finalDamage = baseDamage * accessoryBaseBonus * otherBonus * 0.8f * criticalBonus * fluctuation;
        } else {
            // 普通伤害公式（与BaseWeapon保持一致）
            finalDamage = baseDamage * accessoryBaseBonus * otherBonus * 0.9f * fluctuation;
        }

        return Math.max(0, finalDamage);
    }

    /**
     * 重写暴击特效，添加暗影匕首特有的暴击效果
     */
    @Override
    protected void onCriticalHit(Player player, LivingEntity target, float damage) {
        super.onCriticalHit(player, target, damage);

        // 添加额外的暗影匕首暴击特效
        if (player.level().isClientSide) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§8暗影暴击！ " + String.format("%.1f", damage) + " 伤害"),
                    true
            );
        }

        // 暴击时生成额外的暗影粒子
        if (player.level().isClientSide()) {
            spawnCriticalShadowParticles(player.level(), target);
        }
    }

    /**
     * 暴击时生成额外的暗影粒子
     */
    private void spawnCriticalShadowParticles(Level level, LivingEntity target) {
        // 在目标位置生成暴击粒子效果
        for (int i = 0; i < 12; i++) {
            double offsetX = (Math.random() - 0.5) * 1.0;
            double offsetY = (Math.random() - 0.5) * 1.0 + 1.0;
            double offsetZ = (Math.random() - 0.5) * 1.0;

            // 生成暴击暗影粒子
            level.addParticle(ParticleTypes.SMOKE,
                    target.getX() + offsetX,
                    target.getY() + offsetY,
                    target.getZ() + offsetZ,
                    0, 0.1, 0);

            // 生成灵魂火焰暴击粒子
            if (i % 4 == 0) {
                level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        target.getX() + offsetX * 0.8,
                        target.getY() + offsetY * 0.8,
                        target.getZ() + offsetZ * 0.8,
                        0, 0.08, 0);
            }
        }
    }

    /**
     * 获取攻击距离（匕首有较短的攻击距离）
     */
    @Override
    public double getAttackRange(Player player) {
        return 2.5; // 匕首有2.5格攻击距离
    }
}