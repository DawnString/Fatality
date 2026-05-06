package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import cn.dawnstring.fatality.registry.ModEffects;
import cn.dawnstring.fatality.entity.projectile.IceSpikeProjectile;
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

public class IceSpikeSpear extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 20; // 冷却时间20tick（1秒）
    private static final float BASE_SPEAR_DAMAGE = 390.0f; // 基础长矛伤害390
    private static final int FREEZE_DURATION = 100; // 冻结效果持续时间（5秒，100ticks）

    public IceSpikeSpear()
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
        }, new Properties(), (int)BASE_SPEAR_DAMAGE, 1.0f, 1f, 0.15f, 17.0f, 0.3f, WeaponEnum.MELEE);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算冰刺伤害（使用BaseWeapon的伤害计算逻辑）
            float spearDamage = calculateSpearDamage(player, itemstack);

            // 创建冰刺投射物（使用IceSpikeProjectile作为基础）
            // 冻结等级默认为1级，每次攻击都会叠加
            IceSpikeProjectile iceSpike = new IceSpikeProjectile(level, player, spearDamage, 1);

            // 设置冰刺位置和方向
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();

            iceSpike.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
            iceSpike.shoot(lookVec.x, lookVec.y, lookVec.z, 3.0F, 0.1F); // 3.0速度，极小散布

            // 添加到世界
            level.addFreshEntity(iceSpike);

            // 播放冰刺投掷音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.5F);

            // 在客户端生成冰晶粒子效果
            if (level.isClientSide()) {
                spawnIceSpikeParticles(level, player);
            }
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 重写伤害计算方法，应用冰刺长矛的特殊伤害计算和冻结效果
     */
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            // 计算最终伤害（使用BaseWeapon的伤害计算逻辑）
            float finalDamage = calculateSpearDamage(player, stack, target);

            // 应用伤害
            if (finalDamage > 0) {
                // 确保伤害值足够大，避免被游戏忽略
                float effectiveDamage = Math.max(0.5f, finalDamage);

                // 使用正确的伤害源
                boolean damageApplied = target.hurt(target.damageSources().playerAttack(player), effectiveDamage);

                // 如果伤害应用失败，尝试使用不同的伤害源
                if (!damageApplied) {
                    damageApplied = target.hurt(target.damageSources().generic(), effectiveDamage);
                }

                // 如果伤害应用成功，触发特效
                if (damageApplied) {
                    // 触发暴击特效
                    if (isCriticalHit(player)) {
                        onCriticalHit(player, target, finalDamage);
                    }

                    // 触发冰刺长矛特效（冻结效果）
                    onIceSpikeHit(player, target, stack, finalDamage);
                }

                return damageApplied;
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    /**
     * 生成冰晶粒子效果（冰刺投掷轨迹）
     */
    private void spawnIceSpikeParticles(Level level, Player player) {
        Vec3 lookVec = player.getLookAngle();
        Vec3 eyePos = player.getEyePosition();

        // 冰刺投掷起点位置
        Vec3 throwPos = eyePos.add(lookVec.scale(0.5));

        // 生成冰晶轨迹粒子
        for (int i = 0; i < 12; i++) {
            // 沿着投掷方向生成粒子
            double offsetX = (Math.random() - 0.5) * 0.3;
            double offsetY = (Math.random() - 0.5) * 0.3;
            double offsetZ = (Math.random() - 0.5) * 0.3;

            // 粒子速度（沿着投掷方向）
            double velocityX = lookVec.x * 0.3 + (Math.random() - 0.5) * 0.05;
            double velocityY = lookVec.y * 0.3 + (Math.random() - 0.5) * 0.05;
            double velocityZ = lookVec.z * 0.3 + (Math.random() - 0.5) * 0.05;

            // 生成冰晶粒子（使用雪球粒子）
            level.addParticle(ParticleTypes.ITEM_SNOWBALL,
                    throwPos.x + offsetX,
                    throwPos.y + offsetY,
                    throwPos.z + offsetZ,
                    velocityX, velocityY, velocityZ);

            // 生成雪花粒子（添加冰冻特效）
            if (i % 3 == 0) {
                level.addParticle(ParticleTypes.SNOWFLAKE,
                        throwPos.x + offsetX * 0.5,
                        throwPos.y + offsetY * 0.5,
                        throwPos.z + offsetZ * 0.5,
                        velocityX * 0.5, velocityY * 0.5, velocityZ * 0.5);
            }
        }
    }

    /**
     * 计算冰刺长矛伤害（使用BaseWeapon相同的计算方法）
     */
    public float calculateSpearDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }

    /**
     * 计算冰刺长矛伤害（重载版本，包含目标参数）
     */
    public float calculateSpearDamage(Player player, ItemStack stack, LivingEntity target) {
        return calculateSpearDamage(player, stack);
    }

    /**
     * 冰刺长矛命中时的特效（冻结效果）
     */
    protected void onIceSpikeHit(Player player, LivingEntity target, ItemStack stack, float damage) {
        // 播放冰冻音效
        player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.8F, 1.2F);

        // 发送冰刺长矛命中消息
        if (player.level().isClientSide) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§b冰刺命中！ " + String.format("%.1f", damage) + " 伤害"),
                    true
            );
        }

        // 应用冻结效果
        applyFreezeEffect(target, player);

        // 生成冰冻粒子效果
        if (player.level().isClientSide()) {
            spawnFreezeParticles(player.level(), target);
        }
    }

    /**
     * 应用冻结效果
     */
    private void applyFreezeEffect(LivingEntity target, Player player) {
        // 检查目标是否已经有冻结效果
        MobEffectInstance existingFreeze = target.getEffect(ModEffects.FREEZE.get());
        
        int amplifier = 0;
        if (existingFreeze != null) {
            // 如果已有冻结效果，等级+1（最大5级）
            amplifier = Math.min(4, existingFreeze.getAmplifier() + 1);
        }
        
        // 应用冻结效果，持续5秒，每攻击一次效果等级+1
        target.addEffect(new MobEffectInstance(ModEffects.FREEZE.get(), FREEZE_DURATION, amplifier));
        
        // 显示冻结效果信息
        if (player.level().isClientSide) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§b冻结效果等级：" + (amplifier + 1)),
                    true
            );
        }
    }

    /**
     * 生成冰冻粒子效果
     */
    private void spawnFreezeParticles(Level level, LivingEntity target) {
        // 在目标位置生成冰冻粒子效果
        for (int i = 0; i < 15; i++) {
            double offsetX = (Math.random() - 0.5) * 1.0;
            double offsetY = (Math.random() - 0.5) * 1.0 + 1.0;
            double offsetZ = (Math.random() - 0.5) * 1.0;

            // 生成冰冻粒子
            level.addParticle(ParticleTypes.ITEM_SNOWBALL,
                    target.getX() + offsetX,
                    target.getY() + offsetY,
                    target.getZ() + offsetZ,
                    0, 0.1, 0);

            // 生成雪花粒子
            if (i % 3 == 0) {
                level.addParticle(ParticleTypes.SNOWFLAKE,
                        target.getX() + offsetX * 0.8,
                        target.getY() + offsetY * 0.8,
                        target.getZ() + offsetZ * 0.8,
                        0, 0.08, 0);
            }
        }
    }

    /**
     * 重写暴击特效，添加冰刺长矛特有的暴击效果
     */
    @Override
    protected void onCriticalHit(Player player, LivingEntity target, float damage) {
        super.onCriticalHit(player, target, damage);

        // 添加额外的冰刺长矛暴击特效
        if (player.level().isClientSide) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§b冰刺暴击！ " + String.format("%.1f", damage) + " 伤害"),
                    true
            );
        }

        // 暴击时应用更高级的冻结效果
        if (!player.level().isClientSide()) {
            MobEffectInstance existingFreeze = target.getEffect(ModEffects.FREEZE.get());
            int amplifier = existingFreeze != null ? Math.min(4, existingFreeze.getAmplifier() + 2) : 1;
            target.addEffect(new MobEffectInstance(ModEffects.FREEZE.get(), FREEZE_DURATION, amplifier));
        }

        // 暴击时生成额外的冰冻粒子
        if (player.level().isClientSide()) {
            spawnCriticalFreezeParticles(player.level(), target);
        }
    }

    /**
     * 暴击时生成额外的冰冻粒子
     */
    private void spawnCriticalFreezeParticles(Level level, LivingEntity target) {
        // 在目标位置生成暴击冰冻粒子效果
        for (int i = 0; i < 20; i++) {
            double offsetX = (Math.random() - 0.5) * 1.5;
            double offsetY = (Math.random() - 0.5) * 1.5 + 1.0;
            double offsetZ = (Math.random() - 0.5) * 1.5;

            // 生成暴击冰冻粒子
            level.addParticle(ParticleTypes.ITEM_SNOWBALL,
                    target.getX() + offsetX,
                    target.getY() + offsetY,
                    target.getZ() + offsetZ,
                    0, 0.15, 0);

            // 生成暴击雪花粒子
            if (i % 2 == 0) {
                level.addParticle(ParticleTypes.SNOWFLAKE,
                        target.getX() + offsetX * 0.7,
                        target.getY() + offsetY * 0.7,
                        target.getZ() + offsetZ * 0.7,
                        0, 0.12, 0);
            }
        }
    }

    /**
     * 获取攻击距离（冰刺长矛有较长的攻击距离）
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
        return "冰刺长矛 - 右键投掷冰刺，命中目标施加冻结效果，每攻击一次冻结等级+1（最高5级）";
    }
}