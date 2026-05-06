package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.entity.projectile.EclipseFurnaceSpearProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import cn.dawnstring.fatality.registry.ModEffects;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

/**
 * 日食熔炉长矛 - FurnaceSpear升级版
 * 特性：右键投掷火焰长矛，给予敌人灼烧效果，持续5秒
 * 伤害85 暴击率15 暴击伤害15 浮动0.3 攻击速度0.2s
 */
public class EclipseFurnaceSpear extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 4; // 冷却时间4tick（0.2秒）
    private static final float BASE_SPEAR_DAMAGE = 85.0f; // 基础长矛伤害85
    private static final int BURN_DURATION = 100; // 灼烧效果持续时间（5秒，100ticks）

    public EclipseFurnaceSpear()
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
        }, new Properties(), 85, 1.0f, 1f, 0.15f, 15.0f, 0.3f, WeaponEnum.MELEE);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算熔炉长矛伤害（使用BaseWeapon的伤害计算逻辑）
            float spearDamage = calculateSpearDamage(player, itemstack);

            // 创建日蚀熔炉长矛投射物
            EclipseFurnaceSpearProjectile spear = new EclipseFurnaceSpearProjectile(level, player, itemstack, spearDamage);

            // 设置长矛位置和方向
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();

            spear.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
            spear.shoot(lookVec.x, lookVec.y, lookVec.z, 3.0F, 0.1F); // 3.0速度，极小散布

            // 添加到世界
            level.addFreshEntity(spear);

            // 播放投掷音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);

            // 在客户端生成火焰粒子效果
            if (level.isClientSide()) {
                spawnFurnaceSpearParticles(level, player);
            }
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 重写伤害计算方法，应用熔炉长矛的特殊伤害计算和灼烧效果
     */
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            // 计算最终伤害（使用BaseWeapon的伤害计算逻辑）
            float finalDamage = calculateSpearDamage(player, stack, target);

            // 应用伤害
            boolean damageApplied = target.hurt(target.damageSources().playerAttack(player), finalDamage);

            if (damageApplied) {
                // 熔炉长矛命中时的特效（灼烧效果）
                onFurnaceSpearHit(player, target, stack, finalDamage);
            }

            return damageApplied;
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    /**
     * 计算熔炉长矛伤害（使用BaseWeapon相同的计算方法）
     */
    public float calculateSpearDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }

    /**
     * 计算熔炉长矛伤害（重载版本，包含目标参数）
     */
    public float calculateSpearDamage(Player player, ItemStack stack, LivingEntity target) {
        return calculateSpearDamage(player, stack);
    }

    /**
     * 熔炉长矛命中时的特效（灼烧效果）
     */
    protected void onFurnaceSpearHit(Player player, LivingEntity target, ItemStack stack, float damage) {
        // 应用灼烧效果
        applyBurnEffect(target);

        // 显示灼烧效果信息
        if (player.level().isClientSide) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§c灼烧效果已施加"),
                    true
            );
        }
    }

    /**
     * 给目标添加灼烧效果
     */
    private void applyBurnEffect(LivingEntity target) {
        // 灼烧效果：持续5秒，每秒造成伤害
        MobEffectInstance burnEffect = new MobEffectInstance(ModEffects.BURN.get(), BURN_DURATION, 0);
        target.addEffect(burnEffect);

        // 生成火焰粒子效果
        if (target.level().isClientSide()) {
            for (int i = 0; i < 5; i++) {
                double offsetX = (Math.random() - 0.5) * 1.0;
                double offsetY = Math.random() * 1.5;
                double offsetZ = (Math.random() - 0.5) * 1.0;

                target.level().addParticle(ParticleTypes.FLAME,
                        target.getX() + offsetX,
                        target.getY() + offsetY,
                        target.getZ() + offsetZ,
                        0, 0.1, 0);
            }
        }
    }

    /**
     * 生成熔炉长矛粒子效果（火焰投掷轨迹）
     */
    private void spawnFurnaceSpearParticles(Level level, Player player) {
        Vec3 lookVec = player.getLookAngle();
        Vec3 eyePos = player.getEyePosition();

        // 长矛投掷起点位置
        Vec3 throwPos = eyePos.add(lookVec.scale(0.5));

        // 生成火焰轨迹粒子
        for (int i = 0; i < 8; i++) {
            // 沿着投掷方向生成粒子
            double offsetX = (Math.random() - 0.5) * 0.3;
            double offsetY = (Math.random() - 0.5) * 0.3;
            double offsetZ = (Math.random() - 0.5) * 0.3;

            // 粒子速度（沿着投掷方向）
            double velocityX = lookVec.x * 0.3 + (Math.random() - 0.5) * 0.05;
            double velocityY = lookVec.y * 0.3 + (Math.random() - 0.5) * 0.05;
            double velocityZ = lookVec.z * 0.3 + (Math.random() - 0.5) * 0.05;

            // 生成火焰粒子
            level.addParticle(ParticleTypes.FLAME,
                    throwPos.x + offsetX,
                    throwPos.y + offsetY,
                    throwPos.z + offsetZ,
                    velocityX, velocityY, velocityZ);

            // 生成烟雾粒子（添加火焰特效）
            if (i % 2 == 0) {
                level.addParticle(ParticleTypes.SMOKE,
                        throwPos.x + offsetX * 0.5,
                        throwPos.y + offsetY * 0.5,
                        throwPos.z + offsetZ * 0.5,
                        velocityX * 0.5, velocityY * 0.5, velocityZ * 0.5);
            }
        }
    }

    /**
     * 重写暴击特效，添加熔炉长矛特有的暴击效果
     */
    @Override
    protected void onCriticalHit(Player player, LivingEntity target, float damage) {
        super.onCriticalHit(player, target, damage);

        // 添加额外的熔炉长矛暴击特效
        if (player.level().isClientSide) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§c熔炉暴击！ " + String.format("%.1f", damage) + " 伤害"),
                    true
            );
        }

        // 暴击时应用更高级的灼烧效果
        if (!player.level().isClientSide()) {
            MobEffectInstance burnEffect = new MobEffectInstance(ModEffects.BURN.get(), BURN_DURATION, 1);
            target.addEffect(burnEffect);
        }

        // 暴击时生成额外的火焰粒子
        if (player.level().isClientSide()) {
            spawnCriticalBurnParticles(player.level(), target);
        }
    }

    /**
     * 暴击时生成额外的火焰粒子
     */
    private void spawnCriticalBurnParticles(Level level, LivingEntity target) {
        // 在目标位置生成暴击火焰粒子效果
        for (int i = 0; i < 15; i++) {
            double offsetX = (Math.random() - 0.5) * 1.5;
            double offsetY = (Math.random() - 0.5) * 1.5 + 1.0;
            double offsetZ = (Math.random() - 0.5) * 1.5;

            // 生成暴击火焰粒子
            level.addParticle(ParticleTypes.FLAME,
                    target.getX() + offsetX,
                    target.getY() + offsetY,
                    target.getZ() + offsetZ,
                    0, 0.15, 0);

            // 生成暴击烟雾粒子
            if (i % 2 == 0) {
                level.addParticle(ParticleTypes.LARGE_SMOKE,
                        target.getX() + offsetX * 0.7,
                        target.getY() + offsetY * 0.7,
                        target.getZ() + offsetZ * 0.7,
                        0, 0.12, 0);
            }
        }
    }

    /**
     * 获取攻击距离（熔炉长矛有较长的攻击距离）
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
        return "日食熔炉长矛 - 右键投掷火焰长矛，命中目标施加灼烧效果，暴击时灼烧效果增强";
    }
}