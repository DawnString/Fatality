package cn.dawnstring.fatality.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.List;

/**
 * 末影长矛投射物 - 实现吸附和爆炸功能
 */
public class EnderLanceProjectile extends Projectile {
    
    private final Player owner;
    private final float damage;
    private boolean hasHit = false;
    
    public EnderLanceProjectile(Level level, Player owner, float damage) {
        super(EntityType.ARROW, level); // 使用箭矢实体类型作为基础
        this.owner = owner;
        this.damage = damage;
        this.setNoGravity(true); // 不受重力影响
    }
    
    @Override
    protected void defineSynchedData() {
        // 不需要同步数据
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // 生成末影粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 2; i++) {
                this.level().addParticle(ParticleTypes.PORTAL,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
                        0, 0, 0);
            }
        }
        
        // 检查是否存活时间过长
        if (this.tickCount > 200) { // 10秒后消失
            this.discard();
        }
    }
    
    @Override
    protected void onHit(HitResult result) {
        if (!this.level().isClientSide() && !hasHit) {
            hasHit = true;
            
            if (result.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityResult = (EntityHitResult) result;
                Entity hitEntity = entityResult.getEntity();
                
                if (hitEntity instanceof LivingEntity target && target != owner) {
                    // 对目标造成伤害
                    target.hurt(target.damageSources().playerAttack(owner), damage);
                    
                    // 吸附周围5格内的实体
                    attractNearbyEntities(target);
                    
                    // 播放吸附音效
                    this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 0.8F);
                    
                    // 延迟爆炸（1秒后）
                    this.level().getServer().execute(() -> {
                        if (!target.isRemoved()) {
                            explodeAtTarget(target);
                        }
                    });
                }
            }
            
            this.discard();
        }
    }
    
    /**
     * 吸附目标周围5格内的实体
     */
    private void attractNearbyEntities(LivingEntity target) {
        Vec3 center = target.position();
        AABB attractionBox = new AABB(
                center.x - 5, center.y - 5, center.z - 5,
                center.x + 5, center.y + 5, center.z + 5
        );
        
        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(
                LivingEntity.class, attractionBox,
                entity -> entity != owner && 
                         entity != target && 
                         entity.isAlive() && 
                         !(entity instanceof Player) && 
                         !entity.getType().getDescription().getString().toLowerCase().contains("boss")
        );
        
        for (LivingEntity entity : nearbyEntities) {
            // 将实体拉向目标
            Vec3 direction = center.subtract(entity.position()).normalize();
            entity.setDeltaMovement(direction.scale(0.5)); // 中等速度吸附
            
            // 播放吸附粒子效果
            if (this.level().isClientSide()) {
                for (int i = 0; i < 5; i++) {
                    this.level().addParticle(ParticleTypes.PORTAL,
                            entity.getX(), entity.getY() + 1, entity.getZ(),
                            direction.x * 0.1, direction.y * 0.1, direction.z * 0.1);
                }
            }
        }
    }
    
    /**
     * 在目标位置爆炸
     */
    private void explodeAtTarget(LivingEntity target) {
        Vec3 center = target.position();
        AABB explosionBox = new AABB(
                center.x - 3, center.y - 3, center.z - 3,
                center.x + 3, center.y + 3, center.z + 3
        );
        
        List<LivingEntity> entitiesInRange = this.level().getEntitiesOfClass(
                LivingEntity.class, explosionBox,
                entity -> entity.isAlive() && entity != owner
        );
        
        // 爆炸伤害（基础伤害的50%）
        float explosionDamage = damage * 0.5f;
        
        for (LivingEntity entity : entitiesInRange) {
            // 计算距离衰减
            double distance = center.distanceTo(entity.position());
            float distanceMultiplier = (float) Math.max(0, 1.0 - distance / 3.0);
            float finalDamage = explosionDamage * distanceMultiplier;
            
            if (finalDamage > 0) {
                entity.hurt(entity.damageSources().playerAttack(owner), finalDamage);
                
                // 击退效果
                Vec3 knockback = entity.position().subtract(center).normalize();
                entity.setDeltaMovement(knockback.scale(0.5));
            }
        }
        
        // 播放爆炸音效和粒子效果
        this.level().playSound(null, center.x, center.y, center.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0F, 1.0F);
        
        if (this.level().isClientSide()) {
            for (int i = 0; i < 20; i++) {
                this.level().addParticle(ParticleTypes.EXPLOSION,
                        center.x + (this.random.nextDouble() - 0.5) * 2,
                        center.y + (this.random.nextDouble() - 0.5) * 2,
                        center.z + (this.random.nextDouble() - 0.5) * 2,
                        0, 0, 0);
            }
        }
    }
}