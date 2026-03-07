package cn.dawnstring.fatality.entity.projectile;

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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;

/**
 * 暗影投射物 - 用于BookOfShadowStealing的追踪弹幕攻击
 * 特性：黑色粒子球体、紫色尾缀、追踪最近的怪物、20秒存在时间
 */
public class ShadowProjectile extends AbstractArrow
{
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(ShadowProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_CURVE_AMPLITUDE = SynchedEntityData.defineId(ShadowProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_PHASE_OFFSET = SynchedEntityData.defineId(ShadowProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_TRACKING_MODE = SynchedEntityData.defineId(ShadowProjectile.class, EntityDataSerializers.BOOLEAN);

    private int lifeTime = 0;
    private final int maxLifeTime = 400; // 20秒生命周期（400tick）
    private Vec3 targetPos;
    private Vec3 startPos;
    private boolean hasHit = false;
    private boolean isInitialFlightComplete = false;
    private int trackingInterval = 0;
    private LivingEntity currentTarget = null;

    public ShadowProjectile(EntityType<? extends ShadowProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true); // 暗影投射物无视重力
        this.entityData.set(DATA_DAMAGE, 30.0f); // 默认伤害
        this.entityData.set(DATA_CURVE_AMPLITUDE, 2.5f); // 默认曲线幅度
        this.entityData.set(DATA_PHASE_OFFSET, 0.0f); // 默认相位偏移
        this.entityData.set(DATA_TRACKING_MODE, false); // 默认非追踪模式
    }

    public ShadowProjectile(Level level, LivingEntity shooter, Vec3 startPos, Vec3 targetPos, float damage, float curveAmplitude, float phaseOffset) {
        this(ModEntities.SHADOW_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.setBaseDamage(damage);
        this.entityData.set(DATA_DAMAGE, damage);
        this.entityData.set(DATA_CURVE_AMPLITUDE, curveAmplitude);
        this.entityData.set(DATA_PHASE_OFFSET, phaseOffset);
        this.startPos = startPos;
        this.targetPos = targetPos;
        this.pickup = AbstractArrow.Pickup.DISALLOWED;

        // 设置初始位置
        this.setPos(startPos.x, startPos.y, startPos.z);

        // 计算初始方向（指向目标）
        Vec3 direction = targetPos.subtract(startPos).normalize();
        this.shoot(direction.x, direction.y, direction.z, 1.5F, 0.0F); // 较慢速度，便于追踪
    }

    /**
     * 设置追踪模式
     */
    public void setTrackingMode(boolean tracking) {
        this.entityData.set(DATA_TRACKING_MODE, tracking);
    }

    /**
     * 获取是否处于追踪模式
     */
    public boolean isTrackingMode() {
        return this.entityData.get(DATA_TRACKING_MODE);
    }

    /**
     * 获取暗影伤害
     */
    public float getShadowDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    /**
     * 获取曲线幅度
     */
    public float getCurveAmplitude() {
        return this.entityData.get(DATA_CURVE_AMPLITUDE);
    }

    /**
     * 获取相位偏移
     */
    public float getPhaseOffset() {
        return this.entityData.get(DATA_PHASE_OFFSET);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 30.0f);
        this.entityData.define(DATA_CURVE_AMPLITUDE, 2.5f);
        this.entityData.define(DATA_PHASE_OFFSET, 0.0f);
        this.entityData.define(DATA_TRACKING_MODE, false);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 检查生命周期结束
        if (lifeTime >= maxLifeTime) {
            if (!hasHit) {
                this.explode();
            }
            return;
        }

        // 追踪逻辑
        if (isTrackingMode()) {
            handleTrackingLogic();
        } else {
            // 初始飞行阶段
            handleInitialFlight();
        }

        // 生成粒子效果
        if (this.level().isClientSide()) {
            spawnShadowParticles();
        }
    }

    /**
     * 处理初始飞行阶段（向前飞行10格）
     */
    private void handleInitialFlight() {
        if (startPos == null || targetPos == null) return;

        // 计算当前进度
        Vec3 currentPos = this.position();
        double distanceTraveled = currentPos.distanceTo(startPos);

        // 如果飞行了10格，切换到追踪模式
        if (distanceTraveled >= 10.0 && !isInitialFlightComplete) {
            isInitialFlightComplete = true;
            setTrackingMode(true);
        }

        // 继续直线飞行
        if (!isInitialFlightComplete) {
            Vec3 direction = targetPos.subtract(startPos).normalize();
            this.setDeltaMovement(direction.scale(1.5));
        }
    }

    /**
     * 处理追踪逻辑
     */
    private void handleTrackingLogic() {
        trackingInterval++;

        // 每5tick更新一次目标
        if (trackingInterval >= 5) {
            trackingInterval = 0;
            updateTarget();
        }

        // 如果有目标，追踪目标
        if (currentTarget != null && currentTarget.isAlive()) {
            Vec3 targetPos = currentTarget.position().add(0, currentTarget.getEyeHeight() * 0.5, 0);
            Vec3 currentPos = this.position();
            Vec3 direction = targetPos.subtract(currentPos).normalize();

            // 瞬间转向追踪（直接设置为目标方向）
            this.setDeltaMovement(direction.scale(1.5));

            // 如果距离目标很近，直接命中
            if (currentPos.distanceTo(targetPos) <= 1.5) {
                this.explode();
            }
        } else {
            // 没有目标，继续直线飞行
            currentTarget = null;
        }
    }

    /**
     * 更新追踪目标
     */
    private void updateTarget() {
        if (this.getOwner() == null || !(this.getOwner() instanceof LivingEntity owner)) return;

        Vec3 currentPos = this.position();
        double searchRadius = 15.0; // 15格搜索半径

        // 获取范围内的所有生物
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(searchRadius));

        LivingEntity closestTarget = null;
        double closestDistance = Double.MAX_VALUE;

        for (LivingEntity entity : entities) {
            // 排除玩家和已死亡的实体
            if (entity == owner || !entity.isAlive()) continue;

            double distance = currentPos.distanceTo(entity.position());
            if (distance < closestDistance) {
                closestDistance = distance;
                closestTarget = entity;
            }
        }

        currentTarget = closestTarget;
    }

    /**
     * 生成黑色粒子球体和紫色尾缀效果
     */
    private void spawnShadowParticles() {
        Vec3 position = this.position();
        Vec3 motion = this.getDeltaMovement();

        // 黑色粒子球体（核心）
        for (int i = 0; i < 6; i++) {
            double offsetX = (Math.random() - 0.5) * 0.6;
            double offsetY = (Math.random() - 0.5) * 0.6;
            double offsetZ = (Math.random() - 0.5) * 0.6;

            // 黑色烟幕粒子
            this.level().addParticle(ParticleTypes.SMOKE,
                    position.x + offsetX,
                    position.y + offsetY,
                    position.z + offsetZ,
                    0, 0.01, 0);

            // 灵魂火焰粒子（增强暗影效果）
            if (i % 2 == 0) {
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        position.x + offsetX * 0.8,
                        position.y + offsetY * 0.8,
                        position.z + offsetZ * 0.8,
                        0, 0.008, 0);
            }
        }

        // 紫色尾缀效果
        for (int i = 0; i < 3; i++) {
            // 在弹幕后方生成紫色粒子
            double trailOffset = -0.3 * (i + 1);
            Vec3 trailPos = position.add(motion.normalize().scale(trailOffset));

            // 紫色龙息粒子
            this.level().addParticle(ParticleTypes.DRAGON_BREATH,
                    trailPos.x + (Math.random() - 0.5) * 0.3,
                    trailPos.y + (Math.random() - 0.5) * 0.3,
                    trailPos.z + (Math.random() - 0.5) * 0.3,
                    -motion.x * 0.1, -motion.y * 0.1, -motion.z * 0.1);

            // 紫色火焰粒子
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    trailPos.x + (Math.random() - 0.5) * 0.2,
                    trailPos.y + (Math.random() - 0.5) * 0.2,
                    trailPos.z + (Math.random() - 0.5) * 0.2,
                    -motion.x * 0.08, -motion.y * 0.08, -motion.z * 0.08);
        }
    }

    /**
     * 爆炸效果（命中目标或生命周期结束）
     */
    private void explode() {
        if (hasHit) return;
        hasHit = true;

        // 播放暗影爆炸音效
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1.0F, 0.8F);

        // 生成爆炸粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 15; i++) {
                double offsetX = (Math.random() - 0.5) * 1.2;
                double offsetY = (Math.random() - 0.5) * 1.2;
                double offsetZ = (Math.random() - 0.5) * 1.2;

                this.level().addParticle(ParticleTypes.SMOKE,
                        this.getX() + offsetX,
                        this.getY() + offsetY,
                        this.getZ() + offsetZ,
                        0, 0.08, 0);

                if (i % 3 == 0) {
                    this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                            this.getX() + offsetX * 0.7,
                            this.getY() + offsetY * 0.7,
                            this.getZ() + offsetZ * 0.7,
                            0, 0.06, 0);
                }
            }
        }

        // 造成伤害
        if (!this.level().isClientSide()) {
            Vec3 center = this.position();
            float explosionRadius = 1.5f; // 1.5格爆炸半径

            for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class,
                    this.getBoundingBox().inflate(explosionRadius))) {
                if (entity != this.getOwner() && entity.isAlive()) {
                    double distance = entity.distanceToSqr(center);
                    if (distance <= explosionRadius * explosionRadius) {
                        // 直接造成伤害
                        entity.hurt(this.damageSources().magic(), getShadowDamage());
                    }
                }
            }
        }

        this.discard();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide() && !hasHit) {
            this.explode();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();

        if (entity instanceof LivingEntity livingEntity && !hasHit) {
            // 播放实体命中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 0.8F, 1.0F);
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.lifeTime = compound.getInt("LifeTime");
        if (compound.contains("ShadowDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("ShadowDamage"));
        }
        if (compound.contains("CurveAmplitude")) {
            this.entityData.set(DATA_CURVE_AMPLITUDE, compound.getFloat("CurveAmplitude"));
        }
        if (compound.contains("PhaseOffset")) {
            this.entityData.set(DATA_PHASE_OFFSET, compound.getFloat("PhaseOffset"));
        }
        if (compound.contains("TrackingMode")) {
            this.entityData.set(DATA_TRACKING_MODE, compound.getBoolean("TrackingMode"));
        }
        this.isInitialFlightComplete = compound.getBoolean("InitialFlightComplete");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putFloat("ShadowDamage", getShadowDamage());
        compound.putFloat("CurveAmplitude", getCurveAmplitude());
        compound.putFloat("PhaseOffset", getPhaseOffset());
        compound.putBoolean("TrackingMode", isTrackingMode());
        compound.putBoolean("InitialFlightComplete", isInitialFlightComplete);
    }
}