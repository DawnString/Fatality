package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
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
import java.util.List;

/**
 * 海啸长矛投射物 - 具有海洋风格粒子效果和水波爆炸功能
 * 特性：无视重力，击中后产生水波爆炸效果
 */
public class TsunamiProjectile extends AbstractArrow {
    
    private final Player owner;
    private final float damage;
    private boolean hasHit = false;
    private static final int MAX_LIFETIME = 200; // 10秒最大生命周期
    
    public TsunamiProjectile(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        this.owner = null;
        this.damage = 0;
    }
    
    public TsunamiProjectile(Level level, Player owner, float damage) {
        super(ModEntities.TSUNAMI_PROJECTILE.get(), owner, level);
        this.owner = owner;
        this.damage = damage;
        
        // 设置投射物属性
        this.setNoGravity(true); // 无视重力
        this.setBaseDamage(0); // 基础伤害设为0，使用自定义伤害计算
        this.setPierceLevel((byte) 0); // 穿透等级
        this.setKnockback(0); // 击退效果
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // 生成海洋风格粒子效果
        if (this.level().isClientSide()) {
            spawnOceanParticles();
        }
        
        // 检查生命周期
        if (this.tickCount > MAX_LIFETIME) {
            this.discard();
        }
    }
    
    /**
     * 生成海洋风格粒子效果
     */
    private void spawnOceanParticles() {
        // 蓝色水花粒子轨迹
        for (int i = 0; i < 3; i++) {
            this.level().addParticle(ParticleTypes.BUBBLE,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                    this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                    (this.random.nextDouble() - 0.5) * 0.1,
                    (this.random.nextDouble() - 0.5) * 0.1,
                    (this.random.nextDouble() - 0.5) * 0.1);
        }
        
        // 蓝色光点粒子
        if (this.tickCount % 2 == 0) {
            this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
                    this.getY() + (this.random.nextDouble() - 0.5) * 0.2,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
                    0, 0, 0);
        }
        
        // 水雾拖尾效果
        if (this.tickCount % 3 == 0) {
            this.level().addParticle(ParticleTypes.CLOUD,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.15,
                    this.getY() + (this.random.nextDouble() - 0.5) * 0.15,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.15,
                    this.getDeltaMovement().x * -0.3,
                    this.getDeltaMovement().y * -0.3,
                    this.getDeltaMovement().z * -0.3);
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
                    
                    // 播放命中音效
                    this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.HOSTILE, 1.0F, 0.8F);
                    
                    // 产生水波爆炸效果
                    createWaterWaveExplosion(target.position());
                }
            } else if (result.getType() == HitResult.Type.BLOCK) {
                // 击中方块时也产生水波爆炸效果
                createWaterWaveExplosion(result.getLocation());
            }
            
            this.discard();
        }
    }
    
    /**
     * 创建水波爆炸效果
     */
    private void createWaterWaveExplosion(Vec3 center) {
        // 播放水波爆炸音效
        this.level().playSound(null, center.x, center.y, center.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 0.8F, 0.6F);
        
        // 生成水波爆炸粒子效果
        spawnWaterWaveParticles(center);
        
        // 对周围4格内的实体造成范围伤害
        AABB explosionBox = new AABB(
                center.x - 4, center.y - 4, center.z - 4,
                center.x + 4, center.y + 4, center.z + 4
        );
        
        List<LivingEntity> entitiesInRange = this.level().getEntitiesOfClass(
                LivingEntity.class, explosionBox,
                entity -> entity.isAlive() && entity != owner
        );
        
        // 范围伤害（基础伤害的40%）
        float explosionDamage = damage * 0.4f;
        
        for (LivingEntity entity : entitiesInRange) {
            // 计算距离衰减
            double distance = center.distanceTo(entity.position());
            float distanceMultiplier = (float) Math.max(0, 1.0 - distance / 4.0);
            float finalDamage = explosionDamage * distanceMultiplier;
            
            if (finalDamage > 0) {
                entity.hurt(entity.damageSources().playerAttack(owner), finalDamage);
                
                // 击退效果（向爆炸中心外推）
                Vec3 knockback = entity.position().subtract(center).normalize();
                entity.setDeltaMovement(knockback.scale(0.3));
            }
        }
    }
    
    /**
     * 生成水波爆炸粒子效果
     */
    private void spawnWaterWaveParticles(Vec3 center) {
        if (this.level().isClientSide()) {
            // 中心爆炸水花
            for (int i = 0; i < 30; i++) {
                this.level().addParticle(ParticleTypes.SPLASH,
                        center.x + (this.random.nextDouble() - 0.5) * 2.0,
                        center.y + (this.random.nextDouble() - 0.5) * 2.0,
                        center.z + (this.random.nextDouble() - 0.5) * 2.0,
                        (this.random.nextDouble() - 0.5) * 0.5,
                        0.2 + this.random.nextDouble() * 0.3,
                        (this.random.nextDouble() - 0.5) * 0.5);
            }
            
            // 蓝色气泡效果
            for (int i = 0; i < 20; i++) {
                this.level().addParticle(ParticleTypes.BUBBLE,
                        center.x + (this.random.nextDouble() - 0.5) * 3.0,
                        center.y + (this.random.nextDouble() - 0.5) * 3.0,
                        center.z + (this.random.nextDouble() - 0.5) * 3.0,
                        (this.random.nextDouble() - 0.5) * 0.3,
                        0.1 + this.random.nextDouble() * 0.2,
                        (this.random.nextDouble() - 0.5) * 0.3);
            }
            
            // 水波扩散效果（同心圆）
            for (int ring = 1; ring <= 3; ring++) {
                for (int i = 0; i < 16; i++) {
                    double angle = (double) i / 16.0 * Math.PI * 2;
                    double x = center.x + Math.cos(angle) * ring;
                    double z = center.z + Math.sin(angle) * ring;
                    double y = center.y;
                    
                    this.level().addParticle(ParticleTypes.GLOW,
                            x, y, z,
                            Math.cos(angle) * 0.1,
                            0.05,
                            Math.sin(angle) * 0.1);
                }
            }
        }
    }
    
    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY; // 不能拾取
    }
}