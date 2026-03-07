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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/**
 * 灵火散射子弹 - SpiritFireJudgment的散射子弹
 * 特性：锥形区域伤害、距离递减伤害
 */
public class SpiritFireScatterProjectile extends AbstractArrow {
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(SpiritFireScatterProjectile.class, EntityDataSerializers.FLOAT);
    private static final float CONE_ANGLE = 45.0f; // 锥形角度
    private static final float MAX_RANGE = 8.0f; // 最大射程
    
    private Player shooter;
    private ItemStack weaponItem;
    private Vec3 originalDirection;
    private int lifeTime = 0;
    private final int maxLifeTime = 40; // 2秒生命周期
    
    public SpiritFireScatterProjectile(EntityType<? extends SpiritFireScatterProjectile> type, Level level) {
        super(type, level);
        this.shooter = null;
        this.weaponItem = ItemStack.EMPTY;
        this.originalDirection = Vec3.ZERO;
        this.setNoGravity(true);
        this.entityData.set(DATA_DAMAGE, 0.0f);
    }
    
    public SpiritFireScatterProjectile(Level level, Player shooter, ItemStack weaponItem, float damage, Vec3 direction) {
        this(ModEntities.SPIRIT_FIRE_SCATTER_PROJECTILE.get(), level);
        this.shooter = shooter;
        this.weaponItem = weaponItem;
        this.originalDirection = direction;
        
        // 设置投射物属性
        this.setOwner(shooter);
        this.setBaseDamage(damage);
        this.entityData.set(DATA_DAMAGE, damage);
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }
    
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 0.0f);
    }
    
    @Override
    public void tick() {
        super.tick();
        lifeTime++;
        
        // 生成散射子弹粒子效果
        if (this.level().isClientSide()) {
            spawnScatterFlightParticles();
        }
        
        // 检查生命周期结束
        if (lifeTime >= maxLifeTime) {
            this.discard();
        }
    }
    
    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        
        if (!this.level().isClientSide()) {
            // 处理锥形区域伤害
            applyConeAreaDamage();
            
            // 播放命中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.8F, 1.2F);
            
            // 销毁投射物
            this.discard();
        }
    }
    
    /**
     * 应用锥形区域伤害
     */
    private void applyConeAreaDamage() {
        Vec3 hitPos = this.position();
        Vec3 direction = this.originalDirection;
        
        // 计算锥形区域
        AABB coneArea = calculateConeArea(hitPos, direction);
        
        // 获取锥形区域内的所有实体
        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, coneArea)) {
            // 跳过施法者
            if (entity == this.shooter) continue;
            
            // 检查实体是否在锥形区域内
            if (isEntityInCone(entity, hitPos, direction)) {
                // 计算距离衰减伤害
                double distance = entity.distanceTo(this);
                float damageMultiplier = calculateDistanceDamageMultiplier(distance);
                float finalDamage = this.entityData.get(DATA_DAMAGE) * damageMultiplier;
                
                // 造成伤害
                if (finalDamage > 0) {
                    entity.hurt(entity.damageSources().arrow(this, shooter), finalDamage);
                    
                    // 生成命中粒子效果
                    spawnHitParticles(entity);
                }
            }
        }
    }
    
    /**
     * 计算锥形区域
     */
    private AABB calculateConeArea(Vec3 center, Vec3 direction) {
        // 锥形区域长度
        double length = MAX_RANGE;
        
        // 计算锥形区域的边界
        Vec3 endPoint = center.add(direction.scale(length));
        
        // 计算锥形半径（基于角度和距离）
        double maxRadius = length * Math.tan(Math.toRadians(CONE_ANGLE / 2.0));
        
        // 创建锥形区域的AABB
        return new AABB(
                Math.min(center.x, endPoint.x) - maxRadius,
                Math.min(center.y, endPoint.y) - maxRadius,
                Math.min(center.z, endPoint.z) - maxRadius,
                Math.max(center.x, endPoint.x) + maxRadius,
                Math.max(center.y, endPoint.y) + maxRadius,
                Math.max(center.z, endPoint.z) + maxRadius
        );
    }
    
    /**
     * 检查实体是否在锥形区域内
     */
    private boolean isEntityInCone(LivingEntity entity, Vec3 coneOrigin, Vec3 coneDirection) {
        Vec3 entityPos = entity.position();
        Vec3 toEntity = entityPos.subtract(coneOrigin);
        
        // 检查距离
        double distance = toEntity.length();
        if (distance > MAX_RANGE) return false;
        
        // 检查角度
        double dotProduct = toEntity.normalize().dot(coneDirection.normalize());
        double angle = Math.acos(dotProduct);
        double maxAngle = Math.toRadians(CONE_ANGLE / 2.0);
        
        return angle <= maxAngle;
    }
    
    /**
     * 计算距离衰减伤害倍率
     */
    private float calculateDistanceDamageMultiplier(double distance) {
        // 距离越远，伤害越低（线性衰减）
        float multiplier = 1.0f - (float)(distance / MAX_RANGE);
        return Math.max(0.1f, multiplier); // 最低10%伤害
    }
    
    /**
     * 生成散射子弹飞行粒子效果
     */
    private void spawnScatterFlightParticles() {
        Vec3 pos = this.position();
        
        // 生成小型火焰粒子
        if (this.tickCount % 3 == 0) {
            this.level().addParticle(ParticleTypes.FLAME,
                    pos.x, pos.y, pos.z,
                    (Math.random() - 0.5) * 0.1,
                    (Math.random() - 0.5) * 0.1,
                    (Math.random() - 0.5) * 0.1);
        }
        
        // 生成小型烟雾粒子
        if (this.tickCount % 5 == 0) {
            this.level().addParticle(ParticleTypes.SMOKE,
                    pos.x, pos.y, pos.z,
                    (Math.random() - 0.5) * 0.05,
                    (Math.random() - 0.5) * 0.05,
                    (Math.random() - 0.5) * 0.05);
        }
    }
    
    /**
     * 生成命中粒子效果
     */
    private void spawnHitParticles(LivingEntity entity) {
        Vec3 entityPos = entity.position();
        
        // 在实体位置生成小型火焰粒子
        for (int i = 0; i < 3; i++) {
            this.level().addParticle(ParticleTypes.FLAME,
                    entityPos.x + (Math.random() - 0.5) * 0.5,
                    entityPos.y + Math.random() * 1.5,
                    entityPos.z + (Math.random() - 0.5) * 0.5,
                    0, 0.1, 0);
        }
    }
    
    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        // 实体命中逻辑已经在onHit中处理
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
        if (compound.contains("ScatterDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("ScatterDamage"));
        }
    }
    
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putFloat("ScatterDamage", this.entityData.get(DATA_DAMAGE));
    }
}