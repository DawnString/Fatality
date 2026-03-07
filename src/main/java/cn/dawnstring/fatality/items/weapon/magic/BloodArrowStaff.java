package cn.dawnstring.fatality.items.weapon.magic;

import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import cn.dawnstring.fatality.system.ManaSystem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 血箭法杖 - 发射无视重力的血箭魔法武器
 * 特性：快速攻击速度、血箭特效、无视重力飞行
 */
public class BloodArrowStaff extends BaseWeapon
{
    private static final float MANA_COST = 5.0f; // 每次施法消耗5点魔法值
    private static final int COOLDOWN_TICKS = 5; // 冷却时间5tick（0.25秒）
    private static final float BASE_BLOOD_ARROW_DAMAGE = 34.0f; // 基础血箭伤害34

    public BloodArrowStaff() {
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
                return 0; // 法杖本身没有攻击伤害加成
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
        }, new Properties(), 0, 0.25f, 1f, 0.10f, 0.10f, 0.3f, WeaponEnum.MAGIC);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        // 检查玩家是否有足够的魔法值
        if (!ManaSystem.safeConsumeMana(player, MANA_COST)) {
            // 如果魔法值不足，提示玩家
            if (level.isClientSide()) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c魔法值不足！需要" + MANA_COST + "点魔法值"), true);
            }
            return InteractionResultHolder.fail(itemstack);
        }

        if (!level.isClientSide()) {
            // 计算血箭伤害
            float bloodArrowDamage = calculateBloodArrowDamage(player, itemstack);

            // 创建血箭投射物
            Arrow bloodArrow = new BloodArrow(level, player);

            // 设置血箭伤害（基于BaseWeapon计算的结果）
            bloodArrow.setBaseDamage(bloodArrowDamage);

            // 设置血箭位置和方向
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();

            bloodArrow.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
            bloodArrow.shoot(lookVec.x, lookVec.y, lookVec.z, 4.0F, 0.1F); // 4.0速度，极小散布

            // 添加到世界
            level.addFreshEntity(bloodArrow);

            // 播放血箭施法音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 0.8F, 1.2F); // 更高的音调
        } else {
            // 客户端：生成血箭发射粒子效果
            spawnBloodArrowParticles(level, player);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 生成血箭发射粒子效果
     */
    private void spawnBloodArrowParticles(Level level, Player player) {
        Vec3 lookVec = player.getLookAngle();
        Vec3 eyePos = player.getEyePosition();

        // 血箭发射起点位置
        Vec3 arrowStartPos = eyePos.add(lookVec.scale(0.5));

        // 生成血箭轨迹粒子
        for (int i = 0; i < 6; i++) {
            // 沿着发射方向生成粒子
            double offsetX = (Math.random() - 0.5) * 0.2;
            double offsetY = (Math.random() - 0.5) * 0.2;
            double offsetZ = (Math.random() - 0.5) * 0.2;

            // 粒子速度（沿着发射方向）
            double velocityX = lookVec.x * 0.2 + (Math.random() - 0.5) * 0.02;
            double velocityY = lookVec.y * 0.2 + (Math.random() - 0.5) * 0.02;
            double velocityZ = lookVec.z * 0.2 + (Math.random() - 0.5) * 0.02;

            // 生成血红色粒子（使用烟幕粒子，红色调）
            level.addParticle(ParticleTypes.SMOKE,
                    arrowStartPos.x + offsetX,
                    arrowStartPos.y + offsetY,
                    arrowStartPos.z + offsetZ,
                    velocityX, velocityY, velocityZ);

            // 生成灵魂火焰粒子（血红色效果）
            if (i % 2 == 0) {
                level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        arrowStartPos.x + offsetX * 0.8,
                        arrowStartPos.y + offsetY * 0.8,
                        arrowStartPos.z + offsetZ * 0.8,
                        velocityX * 0.8, velocityY * 0.8, velocityZ * 0.8);
            }
        }
    }

    /**
     * 计算血箭伤害（使用BaseWeapon相同的计算方法）
     */
    public float calculateBloodArrowDamage(Player player, ItemStack stack) {
        // 使用BaseWeapon的伤害计算逻辑，但基于血箭伤害
        float baseDamage = BASE_BLOOD_ARROW_DAMAGE;

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
     * 重写暴击特效，添加血箭特有的暴击效果
     */
    @Override
    protected void onCriticalHit(Player player, LivingEntity target, float damage) {
        super.onCriticalHit(player, target, damage);

        // 添加额外的血箭暴击特效
        if (player.level().isClientSide) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§4血箭暴击！ " + String.format("%.1f", damage) + " 伤害"),
                    true
            );
        }

        // 暴击时生成额外的血箭粒子
        if (player.level().isClientSide()) {
            spawnCriticalBloodParticles(player.level(), target);
        }
    }

    /**
     * 暴击时生成额外的血箭粒子
     */
    private void spawnCriticalBloodParticles(Level level, LivingEntity target) {
        // 在目标位置生成暴击粒子效果
        for (int i = 0; i < 10; i++) {
            double offsetX = (Math.random() - 0.5) * 1.0;
            double offsetY = (Math.random() - 0.5) * 1.0 + 1.0;
            double offsetZ = (Math.random() - 0.5) * 1.0;

            // 生成暴击血箭粒子
            level.addParticle(ParticleTypes.SMOKE,
                    target.getX() + offsetX,
                    target.getY() + offsetY,
                    target.getZ() + offsetZ,
                    0, 0.1, 0);

            // 生成灵魂火焰暴击粒子
            if (i % 3 == 0) {
                level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        target.getX() + offsetX * 0.9,
                        target.getY() + offsetY * 0.9,
                        target.getZ() + offsetZ * 0.9,
                        0, 0.08, 0);
            }
        }
    }

    /**
     * 获取武器特殊效果描述
     */
    @Override
    public String getSpecialEffectDescription() {
        return super.getSpecialEffectDescription() + " | 血箭：无视重力飞行，0.25秒极快攻击速度";
    }

    /**
     * 获取攻击距离（血箭法杖有较长的攻击距离）
     */
    @Override
    public double getAttackRange(Player player) {
        return 15.0; // 血箭有15格攻击距离
    }

    /**
     * 血箭类（内部类）- 自定义血箭投射物，无视重力
     */
    private static class BloodArrow extends Arrow {
        public BloodArrow(Level level, Player player) {
            super(level, player);
            // 设置血箭为无视重力
            this.setNoGravity(true);
        }

        /**
         * 重写tick方法，添加血箭飞行粒子效果
         */
        @Override
        public void tick() {
            super.tick();

            // 在飞行过程中生成血箭轨迹粒子
            if (this.level().isClientSide()) {
                spawnFlightParticles();
            }
        }

        /**
         * 生成血箭飞行粒子效果
         */
        private void spawnFlightParticles() {
            // 每5tick生成一次粒子（避免过多粒子）
            if (this.tickCount % 5 == 0) {
                Vec3 pos = this.position();

                // 生成血箭轨迹粒子
                for (int i = 0; i < 3; i++) {
                    double offsetX = (Math.random() - 0.5) * 0.15;
                    double offsetY = (Math.random() - 0.5) * 0.15;
                    double offsetZ = (Math.random() - 0.5) * 0.15;

                    // 粒子速度（与箭矢方向相反，形成拖尾效果）
                    Vec3 motion = this.getDeltaMovement();
                    double velocityX = -motion.x * 0.1 + (Math.random() - 0.5) * 0.01;
                    double velocityY = -motion.y * 0.1 + (Math.random() - 0.5) * 0.01;
                    double velocityZ = -motion.z * 0.1 + (Math.random() - 0.5) * 0.01;

                    // 生成血红色粒子
                    this.level().addParticle(ParticleTypes.SMOKE,
                            pos.x + offsetX,
                            pos.y + offsetY,
                            pos.z + offsetZ,
                            velocityX, velocityY, velocityZ);

                    // 生成灵魂火焰粒子增强血箭效果
                    if (i % 2 == 0) {
                        this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                                pos.x + offsetX * 0.7,
                                pos.y + offsetY * 0.7,
                                pos.z + offsetZ * 0.7,
                                velocityX * 0.7, velocityY * 0.7, velocityZ * 0.7);
                    }
                }
            }
        }

        /**
         * 重写击中实体时的效果
         */
        @Override
        protected void onHitEntity(net.minecraft.world.phys.EntityHitResult result) {
            super.onHitEntity(result);

            // 击中时生成血箭爆炸粒子效果
            if (this.level().isClientSide()) {
                spawnHitParticles(result.getLocation());
            }
        }

        /**
         * 生成血箭击中粒子效果
         */
        private void spawnHitParticles(Vec3 hitPos) {
            // 生成血箭击中爆炸粒子
            for (int i = 0; i < 8; i++) {
                double offsetX = (Math.random() - 0.5) * 0.6;
                double offsetY = (Math.random() - 0.5) * 0.6;
                double offsetZ = (Math.random() - 0.5) * 0.6;

                double velocityX = (Math.random() - 0.5) * 0.1;
                double velocityY = (Math.random() - 0.5) * 0.1 + 0.05;
                double velocityZ = (Math.random() - 0.5) * 0.1;

                // 生成血红色爆炸粒子
                this.level().addParticle(ParticleTypes.SMOKE,
                        hitPos.x + offsetX,
                        hitPos.y + offsetY,
                        hitPos.z + offsetZ,
                        velocityX, velocityY, velocityZ);

                // 生成灵魂火焰爆炸粒子
                if (i % 3 == 0) {
                    this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                            hitPos.x + offsetX * 0.8,
                            hitPos.y + offsetY * 0.8,
                            hitPos.z + offsetZ * 0.8,
                            velocityX * 0.8, velocityY * 0.8, velocityZ * 0.8);
                }
            }
        }
    }
}