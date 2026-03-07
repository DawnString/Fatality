package cn.dawnstring.fatality.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
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
import java.util.List;

/**
 * 衰败球体投射物
 */
public class DecayOrbProjectile extends AbstractArrow {
    
    private final float damage;
    private final Player owner;
    private final int chargeLevel;
    private int lifeTicks = 0;
    private static final int MAX_LIFE_TICKS = 200; // 最大生存时间10秒
    private static final int AOE_DAMAGE_INTERVAL = 10; // 范围伤害间隔（0.5秒）
    private static final float AOE_RADIUS = 2.0f; // 范围伤害半径
    
    public DecayOrbProjectile(Level level, Player owner, float damage, int chargeLevel) {
        super(EntityType.ARROW, level);
        this.damage = damage;
        this.owner = owner;
        this.chargeLevel = chargeLevel;
        
        // 设置投射物属性
        this.setNoGravity(true); // 不受重力影响
        this.setPierceLevel((byte) 0); // 不穿透
        this.setBaseDamage(damage);
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // 生成主粒子效果
        if (this.level().isClientSide()) {
            // 根据蓄力等级生成不同数量的粒子
            int particleCount = 3 + chargeLevel * 2;
            for (int i = 0; i < particleCount; i++) {
                this.level().addParticle(ParticleTypes.DRAGON_BREATH,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                        0, 0, 0);
            }
            
            // 高级蓄力生成额外粒子
            if (chargeLevel >= 1) {
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
                        0, 0.05, 0);
            }
        }
        
        // 检查生存时间
        lifeTicks++;
        if (lifeTicks > MAX_LIFE_TICKS) {
            explode();
            return;
        }
        
        // 定期对周围造成范围伤害
        if (!this.level().isClientSide() && lifeTicks % AOE_DAMAGE_INTERVAL == 0) {
            applyAreaOfEffectDamage();
        }
        
        // 检查是否击中方块或超出世界边界
        if (this.horizontalCollision || this.verticalCollision || this.isInWall()) {
            explode();
        }
    }
    
    /**
     * 对周围造成范围伤害
     */
    private void applyAreaOfEffectDamage() {
        // 搜索半径内的实体
        AABB searchArea = new AABB(
                this.getX() - AOE_RADIUS, this.getY() - AOE_RADIUS, this.getZ() - AOE_RADIUS,
                this.getX() + AOE_RADIUS, this.getY() + AOE_RADIUS, this.getZ() + AOE_RADIUS
        );
        
        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(LivingEntity.class, searchArea);
        
        for (LivingEntity entity : nearbyEntities) {
            // 不伤害自己
            if (entity == owner) {
                continue;
            }
            
            // 计算距离衰减的伤害
            double distance = entity.distanceTo(this);
            if (distance <= AOE_RADIUS) {
                double damageMultiplier = 1.0 - (distance / AOE_RADIUS);
                damageMultiplier = Math.max(0.2, damageMultiplier); // 最小20%伤害
                
                float aoeDamage = damage * 0.3f * (float)damageMultiplier; // 范围伤害为基础伤害的30%
                
                // 创建伤害源
                DamageSource damageSource = this.damageSources().magic();
                
                // 造成伤害
                if (entity.hurt(damageSource, aoeDamage)) {
                    // 生成伤害粒子
                    if (this.level().isClientSide()) {
                        for (int i = 0; i < 2; i++) {
                            this.level().addParticle(ParticleTypes.SMOKE,
                                    entity.getX() + (this.random.nextDouble() - 0.5) * entity.getBbWidth(),
                                    entity.getY() + this.random.nextDouble() * entity.getBbHeight(),
                                    entity.getZ() + (this.random.nextDouble() - 0.5) * entity.getBbWidth(),
                                    0, 0.1, 0);
                        }
                    }
                }
            }
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
                net.minecraft.sounds.SoundEvents.WITHER_SHOOT, 
                net.minecraft.sounds.SoundSource.NEUTRAL, 0.8F, 1.0F);
        
        // 生成爆炸粒子效果
        int explosionParticleCount = 15 + chargeLevel * 5;
        for (int i = 0; i < explosionParticleCount; i++) {
            double offsetX = (this.random.nextDouble() - 0.5) * 2.0;
            double offsetY = (this.random.nextDouble() - 0.5) * 2.0;
            double offsetZ = (this.random.nextDouble() - 0.5) * 2.0;
            
            this.level().addParticle(ParticleTypes.DRAGON_BREATH,
                    this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ,
                    0, 0.1, 0);
        }
        
        // 对爆炸范围内的实体造成伤害
        float explosionRadius = 3.0f + chargeLevel * 0.5f; // 爆炸半径随蓄力等级增加
        AABB explosionArea = new AABB(
                this.getX() - explosionRadius, this.getY() - explosionRadius, this.getZ() - explosionRadius,
                this.getX() + explosionRadius, this.getY() + explosionRadius, this.getZ() + explosionRadius
        );
        
        List<LivingEntity> affectedEntities = this.level().getEntitiesOfClass(LivingEntity.class, explosionArea);
        
        for (LivingEntity entity : affectedEntities) {
            // 不伤害自己
            if (entity == owner) {
                continue;
            }
            
            // 计算距离衰减的伤害
            double distance = entity.distanceTo(this);
            if (distance <= explosionRadius) {
                double damageMultiplier = 1.0 - (distance / explosionRadius);
                damageMultiplier = Math.max(0.1, damageMultiplier); // 最小10%伤害
                
                float finalDamage = damage * (float)damageMultiplier;
                
                // 创建伤害源
                DamageSource damageSource = this.damageSources().explosion(this, owner);
                
                // 造成伤害
                if (entity.hurt(damageSource, finalDamage)) {
                    // 击退效果
                    Vec3 knockbackDirection = entity.position().subtract(this.position()).normalize();
                    entity.setDeltaMovement(knockbackDirection.scale(0.3));
                    
                    // 生成伤害粒子
                    for (int i = 0; i < 3; i++) {
                        this.level().addParticle(ParticleTypes.SMOKE,
                                entity.getX() + (this.random.nextDouble() - 0.5) * entity.getBbWidth(),
                                entity.getY() + this.random.nextDouble() * entity.getBbHeight(),
                                entity.getZ() + (this.random.nextDouble() - 0.5) * entity.getBbWidth(),
                                0, 0.1, 0);
                    }
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