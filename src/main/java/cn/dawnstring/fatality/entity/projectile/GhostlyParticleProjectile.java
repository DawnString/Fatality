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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;

/**
 * 幽灵粒子投射物 - 用于GhostlyGrimoire的白色粒子追踪攻击
 * 特性：白色粒子球体、白色尾缀、曲线轨迹追踪、自动寻找最近目标
 */
public class GhostlyParticleProjectile extends AbstractArrow
{
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(GhostlyParticleProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_TRACKING_MODE = SynchedEntityData.defineId(GhostlyParticleProjectile.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_CURVE_AMPLITUDE = SynchedEntityData.defineId(GhostlyParticleProjectile.class, EntityDataSerializers.FLOAT);

    private int lifeTime = 0;
    private final int maxLifeTime = 200; // 10秒生命周期（200tick）
    private Vec3 targetPos;
    private Vec3 startPos;
    private boolean hasHit = false;
    private boolean isInitialFlightComplete = false;
    private int trackingInterval = 0;
    private LivingEntity currentTarget = null;
    private final double searchRadius = 15.0; // 搜索半径15格

    public GhostlyParticleProjectile(EntityType<? extends GhostlyParticleProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true); // 幽灵粒子无视重力
        this.entityData.set(DATA_DAMAGE, 30.0f); // 默认伤害
        this.entityData.set(DATA_TRACKING_MODE, false); // 默认非追踪模式
        this.entityData.set(DATA_CURVE_AMPLITUDE, 1.5f); // 默认曲线幅度
    }

    public GhostlyParticleProjectile(Level level, LivingEntity shooter, Vec3 startPos, Vec3 targetPos, float damage, float curveAmplitude) {
        this(ModEntities.GHOSTLY_PARTICLE_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.setBaseDamage(damage);
        this.entityData.set(DATA_DAMAGE, damage);
        this.entityData.set(DATA_CURVE_AMPLITUDE, curveAmplitude);
        this.startPos = startPos;
        this.targetPos = targetPos;
        this.pickup = AbstractArrow.Pickup.DISALLOWED;

        // 设置初始位置
        this.setPos(startPos.x, startPos.y, startPos.z);

        // 计算初始方向（指向目标）
        Vec3 direction = targetPos.subtract(startPos).normalize();
        this.shoot(direction.x, direction.y, direction.z, 1.2F, 0.0F); // 较慢速度，便于追踪
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
     * 获取幽灵伤害
     */
    public float getGhostDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    /**
     * 获取曲线幅度
     */
    public float getCurveAmplitude() {
        return this.entityData.get(DATA_CURVE_AMPLITUDE);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 30.0f);
        this.entityData.define(DATA_TRACKING_MODE, false);
        this.entityData.define(DATA_CURVE_AMPLITUDE, 1.5f);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 检查生命周期结束
        if (lifeTime >= maxLifeTime) {
            if (!hasHit) {
                this.discard();
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

        // 生成白色粒子效果
        if (this.level().isClientSide()) {
            spawnGhostlyParticles();
        }
    }

    /**
     * 处理初始飞行阶段（向前飞行5格）
     */
    private void handleInitialFlight() {
        if (startPos == null || targetPos == null) return;

        // 计算当前进度
        Vec3 currentPos = this.position();
        double distanceTraveled = currentPos.distanceTo(startPos);

        // 如果飞行了5格，切换到追踪模式
        if (distanceTraveled >= 5.0 && !isInitialFlightComplete) {
            isInitialFlightComplete = true;
            setTrackingMode(true);
        }

        // 继续直线飞行
        if (!isInitialFlightComplete) {
            Vec3 direction = targetPos.subtract(startPos).normalize();
            this.setDeltaMovement(direction.scale(1.2));
        }
    }

    /**
     * 处理追踪逻辑
     */
    private void handleTrackingLogic() {
        trackingInterval++;

        // 每3tick更新一次目标
        if (trackingInterval >= 3) {
            trackingInterval = 0;
            updateTarget();
        }

        // 如果有目标，追踪目标
        if (currentTarget != null && currentTarget.isAlive()) {
            Vec3 targetPos = currentTarget.position().add(0, currentTarget.getEyeHeight() * 0.5, 0);
            Vec3 currentPos = this.position();
            Vec3 direction = targetPos.subtract(currentPos).normalize();

            // 使用曲线轨迹追踪（增加曲线运动）
            Vec3 currentMovement = this.getDeltaMovement();
            Vec3 newMovement = direction.scale(1.2);
            
            // 添加曲线运动
            double curveFactor = getCurveAmplitude() * Math.sin(lifeTime * 0.1);
            Vec3 curveDirection = new Vec3(-direction.z, 0, direction.x).normalize();
            newMovement = newMovement.add(curveDirection.scale(curveFactor * 0.1));

            this.setDeltaMovement(newMovement);

            // 如果距离目标很近，直接命中
            if (currentPos.distanceTo(targetPos) <= 1.5) {
                this.onHitTarget(currentTarget);
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
        if (this.level().isClientSide()) return;

        // 获取附近的所有生物
        List<Entity> nearbyEntities = this.level().getEntities(this, 
                this.getBoundingBox().inflate(searchRadius));

        LivingEntity closestTarget = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity livingEntity && 
                !(livingEntity instanceof Player) &&
                livingEntity.isAlive() && 
                livingEntity != this.getOwner()) {
                
                double distance = this.distanceTo(livingEntity);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestTarget = livingEntity;
                }
            }
        }

        currentTarget = closestTarget;
    }

    /**
     * 生成幽灵粒子效果
     */
    private void spawnGhostlyParticles() {
        Vec3 pos = this.position();
        
        // 主粒子球体（白色）
        for (int i = 0; i < 3; i++) {
            double offsetX = (this.random.nextDouble() - 0.5) * 0.3;
            double offsetY = (this.random.nextDouble() - 0.5) * 0.3;
            double offsetZ = (this.random.nextDouble() - 0.5) * 0.3;
            
            this.level().addParticle(ParticleTypes.WHITE_ASH, 
                    pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 
                    0, 0, 0);
        }
        
        // 尾缀粒子（白色烟雾）
        Vec3 movement = this.getDeltaMovement();
        Vec3 trailPos = pos.subtract(movement.normalize().scale(0.5));
        
        this.level().addParticle(ParticleTypes.CLOUD, 
                trailPos.x, trailPos.y, trailPos.z, 
                -movement.x * 0.1, -movement.y * 0.1, -movement.z * 0.1);
    }

    /**
     * 命中目标时的处理
     */
    private void onHitTarget(LivingEntity target) {
        if (hasHit || this.level().isClientSide()) return;
        
        hasHit = true;
        
        // 造成伤害
        if (this.getOwner() instanceof LivingEntity owner) {
            target.hurt(this.damageSources().indirectMagic(this, owner), getGhostDamage());
        } else {
            target.hurt(this.damageSources().magic(), getGhostDamage());
        }
        
        // 播放命中音效
        this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.SOUL_ESCAPE, SoundSource.NEUTRAL, 0.4F, 1.5F);
        
        // 生成命中粒子
        spawnHitParticles(target.position());
        
        this.discard();
    }

    /**
     * 生成命中粒子效果
     */
    private void spawnHitParticles(Vec3 hitPos) {
        if (this.level().isClientSide()) {
            for (int i = 0; i < 8; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 1.5;
                double offsetY = (this.random.nextDouble() - 0.5) * 1.5;
                double offsetZ = (this.random.nextDouble() - 0.5) * 1.5;
                
                this.level().addParticle(ParticleTypes.WHITE_ASH, 
                        hitPos.x + offsetX, hitPos.y + offsetY, hitPos.z + offsetZ, 
                        0.1, 0.1, 0.1);
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        
        if (!hasHit && result.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityResult = (EntityHitResult) result;
            if (entityResult.getEntity() instanceof LivingEntity livingEntity) {
                onHitTarget(livingEntity);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        // 已经在onHit中处理
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("GhostDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("GhostDamage"));
        }
        if (compound.contains("TrackingMode")) {
            this.entityData.set(DATA_TRACKING_MODE, compound.getBoolean("TrackingMode"));
        }
        if (compound.contains("CurveAmplitude")) {
            this.entityData.set(DATA_CURVE_AMPLITUDE, compound.getFloat("CurveAmplitude"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("GhostDamage", this.entityData.get(DATA_DAMAGE));
        compound.putBoolean("TrackingMode", this.entityData.get(DATA_TRACKING_MODE));
        compound.putFloat("CurveAmplitude", this.entityData.get(DATA_CURVE_AMPLITUDE));
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}