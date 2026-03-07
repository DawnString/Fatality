package cn.dawnstring.fatality.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.List;

/**
 * 破坏者追踪榴弹投射物
 */
public class SaboteurGrenadeProjectile extends AbstractArrow {
    
    private final float damage;
    private final Player owner;
    private int lifeTicks = 0;
    private static final int MAX_LIFE_TICKS = 200; // 最大生存时间10秒
    private static final int EXPLOSION_RADIUS = 5; // 爆炸半径5格
    private LivingEntity targetEntity = null;
    private static final double TRACKING_SPEED = 0.1; // 追踪速度
    
    public SaboteurGrenadeProjectile(Level level, Player owner, float damage) {
        super(EntityType.ARROW, level);
        this.damage = damage;
        this.owner = owner;
        
        // 设置投射物属性
        this.setNoGravity(true); // 不受重力影响
        this.setPierceLevel((byte) 0); // 不穿透
        this.setBaseDamage(damage);
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // 生成粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 3; i++) {
                this.level().addParticle(ParticleTypes.FLAME,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
                        0, 0.1, 0);
            }
        }
        
        // 检查生存时间
        lifeTicks++;
        if (lifeTicks > MAX_LIFE_TICKS) {
            explode();
            return;
        }
        
        // 寻找并追踪目标
        if (targetEntity == null || !targetEntity.isAlive() || targetEntity.distanceTo(this) > 20) {
            findNearestTarget();
        }
        
        // 追踪目标
        if (targetEntity != null && targetEntity.isAlive()) {
            trackTarget();
        }
        
        // 检查是否击中方块或超出世界边界
        if (this.horizontalCollision || this.verticalCollision || this.isInWall()) {
            explode();
        }
    }
    
    /**
     * 寻找最近的目标
     */
    private void findNearestTarget() {
        // 搜索半径20格内的实体
        AABB searchArea = new AABB(
                this.getX() - 20, this.getY() - 20, this.getZ() - 20,
                this.getX() + 20, this.getY() + 20, this.getZ() + 20
        );
        
        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(LivingEntity.class, searchArea);
        
        LivingEntity nearestTarget = null;
        double nearestDistance = Double.MAX_VALUE;
        
        for (LivingEntity entity : nearbyEntities) {
            // 不追踪玩家和自己
            if (entity == owner || entity instanceof Player) {
                continue;
            }
            
            double distance = entity.distanceTo(this);
            if (distance < nearestDistance) {
                nearestTarget = entity;
                nearestDistance = distance;
            }
        }
        
        targetEntity = nearestTarget;
    }
    
    /**
     * 追踪目标
     */
    private void trackTarget() {
        Vec3 targetPos = targetEntity.position();
        Vec3 currentPos = this.position();
        
        // 计算方向向量
        Vec3 direction = targetPos.subtract(currentPos).normalize();
        
        // 计算当前速度方向
        Vec3 currentVelocity = this.getDeltaMovement();
        
        // 计算新的速度方向（平滑追踪）
        Vec3 newVelocity = currentVelocity.scale(0.9).add(direction.scale(TRACKING_SPEED));
        
        // 设置新的速度
        this.setDeltaMovement(newVelocity.normalize().scale(currentVelocity.length()));
        
        // 更新旋转角度
        this.setYRot((float)(Math.atan2(newVelocity.x, newVelocity.z) * (180 / Math.PI)));
        this.setXRot((float)(Math.atan2(newVelocity.y, Math.sqrt(newVelocity.x * newVelocity.x + newVelocity.z * newVelocity.z)) * (180 / Math.PI)));
        
        // 如果距离目标很近，直接爆炸
        if (targetEntity.distanceTo(this) < 2.0) {
            explode();
        }
    }
    
    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        
        if (!this.level().isClientSide()) {
            explode();
        }
    }
    
    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        
        // 不伤害自己
        if (target == owner) {
            return;
        }
        
        if (!this.level().isClientSide()) {
            explode();
        }
    }
    
    /**
     * 爆炸方法
     */
    private void explode() {
        if (this.level().isClientSide()) {
            return;
        }
        
        // 播放爆炸音效
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE, 
                net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);
        
        // 生成爆炸粒子效果
        for (int i = 0; i < 30; i++) {
            double offsetX = (this.random.nextDouble() - 0.5) * EXPLOSION_RADIUS * 2;
            double offsetY = (this.random.nextDouble() - 0.5) * EXPLOSION_RADIUS * 2;
            double offsetZ = (this.random.nextDouble() - 0.5) * EXPLOSION_RADIUS * 2;
            
            this.level().addParticle(ParticleTypes.EXPLOSION_EMITTER,
                    this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ,
                    0, 0, 0);
        }
        
        // 对爆炸范围内的实体造成伤害
        AABB explosionArea = new AABB(
                this.getX() - EXPLOSION_RADIUS, this.getY() - EXPLOSION_RADIUS, this.getZ() - EXPLOSION_RADIUS,
                this.getX() + EXPLOSION_RADIUS, this.getY() + EXPLOSION_RADIUS, this.getZ() + EXPLOSION_RADIUS
        );
        
        List<LivingEntity> affectedEntities = this.level().getEntitiesOfClass(LivingEntity.class, explosionArea);
        
        for (LivingEntity entity : affectedEntities) {
            // 不伤害自己
            if (entity == owner) {
                continue;
            }
            
            // 计算距离衰减的伤害
            double distance = entity.distanceTo(this);
            double damageMultiplier = 1.0 - (distance / EXPLOSION_RADIUS);
            damageMultiplier = Math.max(0.1, damageMultiplier); // 最小10%伤害
            
            float finalDamage = damage * (float)damageMultiplier;
            
            // 创建伤害源
            DamageSource damageSource = this.damageSources().explosion(this, owner);
            
            // 造成伤害
            if (entity.hurt(damageSource, finalDamage)) {
                // 击退效果
                Vec3 knockbackDirection = entity.position().subtract(this.position()).normalize();
                entity.setDeltaMovement(knockbackDirection.scale(0.5));
                
                // 生成伤害粒子
                for (int i = 0; i < 5; i++) {
                    this.level().addParticle(ParticleTypes.CRIT,
                            entity.getX() + (this.random.nextDouble() - 0.5) * entity.getBbWidth(),
                            entity.getY() + this.random.nextDouble() * entity.getBbHeight(),
                            entity.getZ() + (this.random.nextDouble() - 0.5) * entity.getBbWidth(),
                            0, 0.1, 0);
                }
            }
        }
        
        // 销毁投射物
        this.discard();
    }
    
    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY; // 不能拾取
    }
    
    @Override
    public boolean isNoGravity() {
        return true; // 不受重力影响
    }
}