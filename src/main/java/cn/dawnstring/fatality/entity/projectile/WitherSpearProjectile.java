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
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

/**
 * 凋零长矛投射物
 */
public class WitherSpearProjectile extends AbstractArrow {
    
    private final float damage;
    private final Player owner;
    private final int chargeLevel;
    private int lifeTicks = 0;
    private static final int MAX_LIFE_TICKS = 200; // 最大生存时间10秒
    private static final int TRAIL_PARTICLE_INTERVAL = 5; // 尾焰粒子生成间隔
    private final List<TrailParticle> trailParticles = new ArrayList<>();
    
    public WitherSpearProjectile(Level level, Player owner, float damage, int chargeLevel) {
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
            for (int i = 0; i < 2; i++) {
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.1,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.1,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.1,
                        0, 0, 0);
            }
        }
        
        // 检查生存时间
        lifeTicks++;
        if (lifeTicks > MAX_LIFE_TICKS) {
            this.discard();
            return;
        }
        
        // 二级和三级蓄力生成尾焰粒子
        if (chargeLevel >= 1 && lifeTicks % TRAIL_PARTICLE_INTERVAL == 0) {
            createTrailParticle();
        }
        
        // 更新尾焰粒子
        updateTrailParticles();
        
        // 检查是否击中方块或超出世界边界
        if (this.horizontalCollision || this.verticalCollision || this.isInWall()) {
            this.discard();
        }
    }
    
    /**
     * 创建尾焰粒子
     */
    private void createTrailParticle() {
        if (!this.level().isClientSide()) {
            TrailParticle trail = new TrailParticle(
                    this.getX(), this.getY(), this.getZ(),
                    this.level(), owner, damage * 0.6f // 尾焰伤害为基础伤害的60%
            );
            trailParticles.add(trail);
        }
    }
    
    /**
     * 更新尾焰粒子
     */
    private void updateTrailParticles() {
        if (this.level().isClientSide()) {
            return;
        }
        
        // 移除过期的尾焰粒子
        trailParticles.removeIf(trail -> trail.isExpired());
        
        // 更新存活的尾焰粒子
        for (TrailParticle trail : trailParticles) {
            trail.tick();
        }
    }
    
    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        
        if (!this.level().isClientSide()) {
            // 播放命中音效和粒子效果
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    net.minecraft.sounds.SoundEvents.WITHER_SHOOT, 
                    net.minecraft.sounds.SoundSource.NEUTRAL, 0.8F, 1.2F);
            
            // 生成爆炸粒子
            for (int i = 0; i < 10; i++) {
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.5,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                        0, 0, 0);
            }
            
            this.discard();
        }
    }
    
    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        
        // 不伤害自己
        if (target == owner) {
            return;
        }
        
        // 计算伤害
        float finalDamage = damage;
        
        // 应用伤害
        if (target instanceof LivingEntity) {
            LivingEntity livingTarget = (LivingEntity) target;
            
            // 创建伤害源
            DamageSource damageSource = this.damageSources().arrow(this, owner);
            
            // 造成伤害
            if (livingTarget.hurt(damageSource, finalDamage)) {
                // 播放伤害音效
                this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                        net.minecraft.sounds.SoundEvents.WITHER_HURT, 
                        net.minecraft.sounds.SoundSource.NEUTRAL, 0.6F, 1.0F);
                
                // 生成伤害粒子
                for (int i = 0; i < 5; i++) {
                    this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                            target.getX() + (this.random.nextDouble() - 0.5) * target.getBbWidth(),
                            target.getY() + this.random.nextDouble() * target.getBbHeight(),
                            target.getZ() + (this.random.nextDouble() - 0.5) * target.getBbWidth(),
                            0, 0.1, 0);
                }
                
                // 施加凋零效果（2秒）
                // livingTarget.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0));
            }
        }
        
        super.onHitEntity(result);
    }
    
    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY; // 不能拾取
    }
    
    @Override
    public boolean isNoGravity() {
        return true; // 不受重力影响
    }
    
    /**
     * 尾焰粒子类
     */
    private static class TrailParticle {
        private final double x, y, z;
        private final Level level;
        private final Player owner;
        private final float damage;
        private int lifeTicks = 0;
        private static final int MAX_LIFE_TICKS = 100; // 尾焰粒子持续5秒
        
        public TrailParticle(double x, double y, double z, Level level, Player owner, float damage) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.level = level;
            this.owner = owner;
            this.damage = damage;
        }
        
        public void tick() {
            lifeTicks++;
            
            // 生成粒子效果
            if (level.isClientSide()) {
                for (int i = 0; i < 2; i++) {
                    level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                            x + (Math.random() - 0.5) * 0.3,
                            y + (Math.random() - 0.5) * 0.3,
                            z + (Math.random() - 0.5) * 0.3,
                            0, 0.05, 0);
                }
            }
            
            // 检查附近实体并造成伤害
            if (!level.isClientSide() && lifeTicks % 10 == 0) { // 每0.5秒检查一次
                checkNearbyEntities();
            }
        }
        
        public boolean isExpired() {
            return lifeTicks > MAX_LIFE_TICKS;
        }
        
        /**
         * 检查附近实体并造成伤害
         */
        private void checkNearbyEntities() {
            // 搜索半径1.5格内的实体
            net.minecraft.world.phys.AABB searchArea = new net.minecraft.world.phys.AABB(
                    x - 1.5, y - 1.5, z - 1.5,
                    x + 1.5, y + 1.5, z + 1.5
            );
            
            List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class, searchArea);
            
            for (LivingEntity entity : nearbyEntities) {
                // 不伤害玩家和自己
                if (entity == owner || entity instanceof Player) {
                    continue;
                }
                
                // 计算距离衰减的伤害
                double distance = Math.sqrt(
                        Math.pow(entity.getX() - x, 2) +
                        Math.pow(entity.getY() - y, 2) +
                        Math.pow(entity.getZ() - z, 2)
                );
                
                if (distance <= 1.5) {
                    double damageMultiplier = 1.0 - (distance / 1.5);
                    float finalDamage = damage * (float)damageMultiplier;
                    
                    // 创建伤害源
                    DamageSource damageSource = level.damageSources().magic();
                    
                    // 造成伤害
                    if (entity.hurt(damageSource, finalDamage)) {
                        // 播放伤害音效
                        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                                net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH, 
                                net.minecraft.sounds.SoundSource.NEUTRAL, 0.3F, 1.5F);
                        
                        // 生成伤害粒子
                        for (int i = 0; i < 3; i++) {
                            level.addParticle(ParticleTypes.SMOKE,
                                    entity.getX() + (Math.random() - 0.5) * entity.getBbWidth(),
                                    entity.getY() + Math.random() * entity.getBbHeight(),
                                    entity.getZ() + (Math.random() - 0.5) * entity.getBbWidth(),
                                    0, 0.1, 0);
                        }
                    }
                }
            }
        }
    }
}