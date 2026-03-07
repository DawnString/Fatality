package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 祸乱弹幕投射物 - 在目标周围生成并攻击最近生物
 */
public class ChaosBarrageProjectile extends AbstractArrow {
    
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(ChaosBarrageProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(ChaosBarrageProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_TRACKING = SynchedEntityData.defineId(ChaosBarrageProjectile.class, EntityDataSerializers.BOOLEAN);
    
    private LivingEntity target;
    private int age = 0;
    private static final int MAX_LIFETIME = 100; // 5秒生命周期
    private static final double TRACKING_RANGE = 10.0; // 追踪范围10格
    
    public ChaosBarrageProjectile(EntityType<? extends ChaosBarrageProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true); // 无重力
        this.setPierceLevel((byte) 0); // 不穿透
    }
    
    public ChaosBarrageProjectile(Level level, LivingEntity owner, float damage, LivingEntity target) {
        this(ModEntities.CHAOS_BARRAGE_PROJECTILE.get(), level);
        this.setOwner(owner);
        this.setDamage(damage);
        this.target = target;
        this.setLifetime(MAX_LIFETIME);
        this.setTracking(false); // 初始不追踪
        
        // 设置初始位置在目标周围随机位置
        if (target != null) {
            double offsetX = (random.nextDouble() - 0.5) * 3.0;
            double offsetY = (random.nextDouble() - 0.5) * 3.0;
            double offsetZ = (random.nextDouble() - 0.5) * 3.0;
            
            this.setPos(target.getX() + offsetX, target.getY() + offsetY, target.getZ() + offsetZ);
        }
    }
    
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DAMAGE, 0.0f);
        this.entityData.define(LIFETIME, MAX_LIFETIME);
        this.entityData.define(IS_TRACKING, false);
    }
    
    @Override
    public void tick() {
        super.tick();
        age++;
        
        if (!this.level().isClientSide()) {
            // 检查生命周期
            if (age >= this.getLifetime()) {
                this.discard();
                return;
            }
            
            // 1秒后开始追踪
            if (age >= 20 && !this.isTracking()) {
                this.setTracking(true);
            }
            
            // 追踪逻辑
            if (this.isTracking()) {
                LivingEntity nearestTarget = findNearestTarget();
                if (nearestTarget != null) {
                    // 追踪最近目标
                    Vec3 targetPos = nearestTarget.position().add(0, nearestTarget.getEyeHeight() / 2, 0);
                    Vec3 currentPos = this.position();
                    Vec3 direction = targetPos.subtract(currentPos).normalize();
                    
                    // 设置速度朝向目标
                    this.setDeltaMovement(direction.scale(0.3)); // 较慢的追踪速度
                    
                    // 检查是否击中目标
                    if (currentPos.distanceTo(targetPos) < 1.5) {
                        onHitTarget(nearestTarget);
                        return;
                    }
                } else {
                    // 没有找到目标，随机移动
                    if (this.random.nextInt(20) == 0) {
                        double motionX = (random.nextDouble() - 0.5) * 0.1;
                        double motionY = (random.nextDouble() - 0.5) * 0.1;
                        double motionZ = (random.nextDouble() - 0.5) * 0.1;
                        this.setDeltaMovement(motionX, motionY, motionZ);
                    }
                }
            }
            
            // 生成红色粒子效果
            if (this.level().isClientSide()) {
                for (int i = 0; i < 2; i++) {
                    double offsetX = (random.nextDouble() - 0.5) * 0.2;
                    double offsetY = (random.nextDouble() - 0.5) * 0.2;
                    double offsetZ = (random.nextDouble() - 0.5) * 0.2;
                    
                    this.level().addParticle(ParticleTypes.FLAME,
                            this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ,
                            0, 0, 0);
                }
            }
        }
    }
    
    /**
     * 寻找最近的生物目标（除玩家外）
     */
    private LivingEntity findNearestTarget() {
        AABB searchArea = this.getBoundingBox().inflate(TRACKING_RANGE);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, searchArea);
        
        LivingEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        
        for (LivingEntity entity : entities) {
            // 排除玩家和发射者
            if (entity instanceof Player || entity == this.getOwner()) {
                continue;
            }
            
            double distance = this.distanceToSqr(entity);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = entity;
            }
        }
        
        return nearest;
    }
    
    /**
     * 击中目标时的处理
     */
    private void onHitTarget(LivingEntity target) {
        if (!this.level().isClientSide()) {
            // 造成伤害
            if (this.getOwner() instanceof LivingEntity) {
                target.hurt(this.damageSources().indirectMagic(this, (LivingEntity) this.getOwner()), this.getDamage());
            } else {
                target.hurt(this.damageSources().magic(), this.getDamage());
            }
            
            // 播放击中效果
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    net.minecraft.sounds.SoundEvents.FIREWORK_ROCKET_BLAST, 
                    net.minecraft.sounds.SoundSource.NEUTRAL, 0.5F, 1.2F);
            
            // 生成爆炸粒子
            if (this.level().isClientSide()) {
                for (int i = 0; i < 10; i++) {
                    double offsetX = (random.nextDouble() - 0.5) * 2.0;
                    double offsetY = (random.nextDouble() - 0.5) * 2.0;
                    double offsetZ = (random.nextDouble() - 0.5) * 2.0;
                    
                    this.level().addParticle(ParticleTypes.FLAME,
                            target.getX() + offsetX, target.getY() + offsetY, target.getZ() + offsetZ,
                            0, 0.1, 0);
                }
            }
            
            this.discard();
        }
    }
    
    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY; // 不能被捡起
    }
    
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Damage")) {
            this.setDamage(compound.getFloat("Damage"));
        }
        if (compound.contains("Lifetime")) {
            this.setLifetime(compound.getInt("Lifetime"));
        }
        if (compound.contains("Tracking")) {
            this.setTracking(compound.getBoolean("Tracking"));
        }
    }
    
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("Damage", this.getDamage());
        compound.putInt("Lifetime", this.getLifetime());
        compound.putBoolean("Tracking", this.isTracking());
    }
    
    // Getter和Setter方法
    public float getDamage() {
        return this.entityData.get(DAMAGE);
    }
    
    public void setDamage(float damage) {
        this.entityData.set(DAMAGE, damage);
    }
    
    public int getLifetime() {
        return this.entityData.get(LIFETIME);
    }
    
    public void setLifetime(int lifetime) {
        this.entityData.set(LIFETIME, lifetime);
    }
    
    public boolean isTracking() {
        return this.entityData.get(IS_TRACKING);
    }
    
    public void setTracking(boolean tracking) {
        this.entityData.set(IS_TRACKING, tracking);
    }
}