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
 * 通用榴弹投射物
 * 特性：受重力影响，击中目标或方块后爆炸，对周围造成范围伤害
 */
public class GrenadeProjectile extends AbstractArrow {
    
    private final float damage;
    private final Player owner;
    private int lifeTicks = 0;
    private static final int MAX_LIFE_TICKS = 100; // 最大生存时间5秒
    private static final int EXPLOSION_RADIUS = 3; // 爆炸半径3格
    
    public GrenadeProjectile(Level level, Player owner, float damage) {
        super(EntityType.ARROW, level);
        this.damage = damage;
        this.owner = owner;
        
        // 设置投射物属性
        this.setNoGravity(false); // 受重力影响
        this.setPierceLevel((byte) 0); // 不穿透
        this.setBaseDamage(damage);
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // 生成粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 2; i++) {
                this.level().addParticle(ParticleTypes.SMOKE,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                        0, 0.05, 0);
            }
        }
        
        // 检查生存时间
        lifeTicks++;
        if (lifeTicks > MAX_LIFE_TICKS) {
            explode();
            return;
        }
        
        // 检查是否击中方块或超出世界边界
        if (this.horizontalCollision || this.verticalCollision || this.isInWall()) {
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
                net.minecraft.sounds.SoundSource.NEUTRAL, 0.8F, 1.0F);
        
        // 生成爆炸粒子效果
        for (int i = 0; i < 20; i++) {
            double offsetX = (this.random.nextDouble() - 0.5) * EXPLOSION_RADIUS * 2;
            double offsetY = (this.random.nextDouble() - 0.5) * EXPLOSION_RADIUS * 2;
            double offsetZ = (this.random.nextDouble() - 0.5) * EXPLOSION_RADIUS * 2;
            
            this.level().addParticle(ParticleTypes.EXPLOSION,
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
            
            // 计算距离伤害衰减
            double distance = entity.distanceTo(this);
            float distanceMultiplier = Math.max(0.1f, (float)(1.0 - (distance / EXPLOSION_RADIUS)));
            float finalDamage = damage * distanceMultiplier;
            
            // 应用伤害
            entity.hurt(entity.damageSources().explosion(this, owner), finalDamage);
            
            // 生成命中效果
            if (this.level().isClientSide()) {
                for (int i = 0; i < 3; i++) {
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
        return false; // 受重力影响
    }
}