package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.effects.FreezeEffect;
import cn.dawnstring.fatality.registry.ModEffects;
import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;

/**
 * 日蚀冰刺投射物 - EclipseIceSpikeSpear的专属投射物
 * 特性：更强的冻结效果、冰爆范围伤害、更华丽的冰系特效
 */
public class EclipseIceSpikeProjectile extends AbstractArrow
{
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(EclipseIceSpikeProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_FREEZE_LEVEL = SynchedEntityData.defineId(EclipseIceSpikeProjectile.class, EntityDataSerializers.INT);

    private int lifeTime = 0;
    private final int maxLifeTime = 150; // 7.5秒生命周期（比基础版更长）
    private boolean hasHit = false;

    public EclipseIceSpikeProjectile(EntityType<? extends EclipseIceSpikeProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true); // 冰刺无重力，直线飞行
        this.entityData.set(DATA_DAMAGE, 20.0f); // 默认伤害更高
        this.entityData.set(DATA_FREEZE_LEVEL, 2); // 默认冻结等级更高
    }

    public EclipseIceSpikeProjectile(Level level, Player shooter, float damage, int freezeLevel) {
        this(ModEntities.ECLIPSE_ICE_SPIKE_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.setBaseDamage(damage); // 使用传入的伤害值
        this.entityData.set(DATA_DAMAGE, damage); // 同步伤害数据
        this.entityData.set(DATA_FREEZE_LEVEL, freezeLevel); // 同步冻结等级
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    /**
     * 获取日蚀冰刺伤害（从同步数据中读取）
     */
    public float getIceSpikeDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    /**
     * 获取冻结等级（从同步数据中读取）
     */
    public int getFreezeLevel() {
        return this.entityData.get(DATA_FREEZE_LEVEL);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 20.0f);
        this.entityData.define(DATA_FREEZE_LEVEL, 2);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 生成日蚀冰刺飞行粒子效果（更华丽）
        if (this.level().isClientSide()) {
            // 冰晶粒子效果（更密集）
            for (int i = 0; i < 5; i++) {
                this.level().addParticle(ParticleTypes.ITEM_SNOWBALL,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getDeltaMovement().x * -0.1,
                        this.getDeltaMovement().y * -0.1,
                        this.getDeltaMovement().z * -0.1);
            }

            // 雪花粒子效果（更频繁）
            if (this.random.nextFloat() < 0.6f) {
                this.level().addParticle(ParticleTypes.SNOWFLAKE,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.4,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.4,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.4,
                        (this.random.nextDouble() - 0.5) * 0.08,
                        (this.random.nextDouble() - 0.5) * 0.08,
                        (this.random.nextDouble() - 0.5) * 0.08);
            }

            // 冰雾拖尾效果（更明显）
            if (this.tickCount % 1 == 0) { // 每帧都生成
                this.level().addParticle(ParticleTypes.CLOUD,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.15,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.15,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.15,
                        this.getDeltaMovement().x * -0.3,
                        this.getDeltaMovement().y * -0.3,
                        this.getDeltaMovement().z * -0.3);
            }

            // 日蚀特效：蓝色火焰粒子
            if (this.tickCount % 4 == 0) {
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
                        0, 0.05, 0);
            }
        }

        // 检查生命周期结束
        if (lifeTime >= maxLifeTime && !hasHit) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide() && !hasHit) {
            hasHit = true;

            // 播放日蚀冰刺破碎音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.2F, 1.0F);

            // 生成冰爆效果，对周围敌人造成伤害
            applyIceExplosion();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide() && !hasHit && result.getEntity() instanceof LivingEntity target) {
            hasHit = true;

            // 应用日蚀冰刺伤害（伤害提升）
            float damage = getIceSpikeDamage() * 1.3f; // 伤害提升30%
            if (damage > 0) {
                boolean damageApplied = target.hurt(target.damageSources().arrow(this, (Player) this.getOwner()), damage);

                if (damageApplied) {
                    // 应用日蚀冻结效果（更强）
                    applyEclipseFreezeEffect(target);

                    // 播放日蚀冰刺命中音效
                    this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 0.8F, 1.3F);

                    // 对冻结目标造成额外伤害
                    if (target.hasEffect(ModEffects.FREEZE.get())) {
                        float bonusDamage = damage * 0.4f; // 40%额外伤害
                        target.hurt(target.damageSources().magic(), bonusDamage);
                    }
                }
            }

            // 生成冰爆效果
            applyIceExplosion();
        }
    }

    /**
     * 应用日蚀冻结效果（更强版本）
     */
    private void applyEclipseFreezeEffect(LivingEntity target) {
        // 日蚀冻结效果：持续时间更长，等级更高
        int freezeDuration = 120 + (getFreezeLevel() * 40); // 基础6秒 + 每级2秒
        MobEffectInstance freezeEffect = new MobEffectInstance(ModEffects.FREEZE.get(), freezeDuration, getFreezeLevel());
        target.addEffect(freezeEffect);

        // 生成日蚀冻结粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 12; i++) {
                this.level().addParticle(ParticleTypes.ITEM_SNOWBALL,
                        target.getX() + (this.random.nextDouble() - 0.5) * 2.0,
                        target.getY() + this.random.nextDouble() * 2.0,
                        target.getZ() + (this.random.nextDouble() - 0.5) * 2.0,
                        (this.random.nextDouble() - 0.5) * 0.2,
                        0.1,
                        (this.random.nextDouble() - 0.5) * 0.2);
            }

            // 蓝色灵魂火焰粒子（日蚀特效）
            for (int i = 0; i < 8; i++) {
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        target.getX() + (this.random.nextDouble() - 0.5) * 1.5,
                        target.getY() + this.random.nextDouble() * 1.5,
                        target.getZ() + (this.random.nextDouble() - 0.5) * 1.5,
                        0, 0.08, 0);
            }
        }
    }

    /**
     * 应用冰爆效果，对周围敌人造成范围伤害
     */
    private void applyIceExplosion() {
        // 获取周围4格内的所有实体
        for (Entity entity : this.level().getEntities(this, this.getBoundingBox().inflate(4.0))) {
            if (entity instanceof LivingEntity target && target != this.getOwner()) {
                // 对周围敌人造成40%的冰爆伤害
                float explosionDamage = getIceSpikeDamage() * 0.4f;
                if (explosionDamage > 0) {
                    boolean damageApplied = target.hurt(target.damageSources().magic(), explosionDamage);

                    if (damageApplied) {
                        // 对周围敌人也应用冻结效果（等级较低）
                        MobEffectInstance splashFreezeEffect = new MobEffectInstance(ModEffects.FREEZE.get(), 60, 0);
                        target.addEffect(splashFreezeEffect);

                        // 生成冰爆炸粒子效果
                        if (this.level().isClientSide()) {
                            this.level().addParticle(ParticleTypes.SNOWFLAKE,
                                    target.getX(),
                                    target.getY() + 1.0,
                                    target.getZ(),
                                    0, 0.1, 0);
                        }
                    }
                }
            }
        }

        // 生成日蚀冰爆粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 25; i++) {
                this.level().addParticle(ParticleTypes.ITEM_SNOWBALL,
                        this.getX() + (this.random.nextDouble() - 0.5) * 2.0,
                        this.getY() + (this.random.nextDouble() - 0.5) * 2.0,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 2.0,
                        (this.random.nextDouble() - 0.5) * 0.4,
                        (this.random.nextDouble() - 0.5) * 0.4,
                        (this.random.nextDouble() - 0.5) * 0.4);
            }

            // 冰雾爆炸效果
            for (int i = 0; i < 20; i++) {
                this.level().addParticle(ParticleTypes.CLOUD,
                        this.getX() + (this.random.nextDouble() - 0.5) * 1.5,
                        this.getY() + (this.random.nextDouble() - 0.5) * 1.5,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 1.5,
                        (this.random.nextDouble() - 0.5) * 0.3,
                        0.15,
                        (this.random.nextDouble() - 0.5) * 0.3);
            }

            // 日蚀特效粒子
            for (int i = 0; i < 15; i++) {
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX() + (this.random.nextDouble() - 0.5) * 1.2,
                        this.getY() + (this.random.nextDouble() - 0.5) * 1.2,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 1.2,
                        0, 0.1, 0);
            }
        }

        this.discard();
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.lifeTime = compound.getInt("LifeTime");
        this.hasHit = compound.getBoolean("HasHit");
        this.entityData.set(DATA_DAMAGE, compound.getFloat("IceSpikeDamage"));
        this.entityData.set(DATA_FREEZE_LEVEL, compound.getInt("FreezeLevel"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putBoolean("HasHit", hasHit);
        compound.putFloat("IceSpikeDamage", getIceSpikeDamage());
        compound.putInt("FreezeLevel", getFreezeLevel());
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY; // 不允许拾取
    }
}