package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.core.particles.DustParticleOptions;
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
 * 蚀浪长矛投射物 - 具有侵蚀风格粒子效果和腐蚀爆炸功能
 * 特性：无视重力，击中后产生腐蚀爆炸效果，具有侵蚀主题
 */
public class ErodingWaveProjectile extends AbstractArrow {
    
    private final Player owner;
    private final float damage;
    private boolean hasHit = false;
    private static final int MAX_LIFETIME = 200; // 10秒最大生命周期
    
    public ErodingWaveProjectile(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        this.owner = null;
        this.damage = 0;
    }
    
    public ErodingWaveProjectile(Level level, Player owner, float damage) {
        super(ModEntities.ERODING_WAVE_PROJECTILE.get(), owner, level);
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
        
        // 生成侵蚀风格粒子效果
        if (this.level().isClientSide()) {
            spawnErosionParticles();
        }
        
        // 检查生命周期
        if (this.tickCount > MAX_LIFETIME) {
            this.discard();
        }
    }
    
    /**
     * 生成侵蚀风格粒子效果
     */
    private void spawnErosionParticles() {
        // 暗红色腐蚀粒子轨迹
        for (int i = 0; i < 4; i++) {
            // 暗红色粒子（类似血液效果）
            this.level().addParticle(ParticleTypes.DAMAGE_INDICATOR,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.4,
                    this.getY() + (this.random.nextDouble() - 0.5) * 0.4,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.4,
                    (this.random.nextDouble() - 0.5) * 0.15,
                    (this.random.nextDouble() - 0.5) * 0.15,
                    (this.random.nextDouble() - 0.5) * 0.15);
        }
        
        // 紫色腐蚀光点粒子
        if (this.tickCount % 2 == 0) {
            this.level().addParticle(ParticleTypes.WITCH,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                    this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                    0, 0, 0);
        }
        
        // 暗色烟雾拖尾效果
        if (this.tickCount % 3 == 0) {
            this.level().addParticle(ParticleTypes.SMOKE,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
                    this.getY() + (this.random.nextDouble() - 0.5) * 0.2,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
                    this.getDeltaMovement().x * -0.4,
                    this.getDeltaMovement().y * -0.4,
                    this.getDeltaMovement().z * -0.4);
        }
        
        // 偶尔生成绿色腐蚀粒子
        if (this.tickCount % 5 == 0) {
            this.level().addParticle(ParticleTypes.ITEM_SLIME,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                    this.getY() + (this.random.nextDouble() - 0.5) * 0.5,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                    (this.random.nextDouble() - 0.5) * 0.2,
                    (this.random.nextDouble() - 0.5) * 0.2,
                    (this.random.nextDouble() - 0.5) * 0.2);
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
                    
                    // 播放腐蚀命中音效
                    this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundEvents.GENERIC_EAT, SoundSource.HOSTILE, 1.0F, 0.6F);
                    
                    // 产生腐蚀爆炸效果
                    createCorrosionExplosion(target.position());
                }
            } else if (result.getType() == HitResult.Type.BLOCK) {
                // 击中方块时也产生腐蚀爆炸效果
                createCorrosionExplosion(result.getLocation());
            }
            
            this.discard();
        }
    }
    
    /**
     * 创建腐蚀爆炸效果
     */
    private void createCorrosionExplosion(Vec3 center) {
        // 播放腐蚀爆炸音效
        this.level().playSound(null, center.x, center.y, center.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 0.7F, 0.5F);
        
        // 生成腐蚀爆炸粒子效果
        spawnCorrosionParticles(center);
        
        // 对周围3.5格内的实体造成范围伤害（比Tsunami范围稍小但更集中）
        AABB explosionBox = new AABB(
                center.x - 3.5, center.y - 3.5, center.z - 3.5,
                center.x + 3.5, center.y + 3.5, center.z + 3.5
        );
        
        List<LivingEntity> entitiesInRange = this.level().getEntitiesOfClass(
                LivingEntity.class, explosionBox,
                entity -> entity.isAlive() && entity != owner
        );
        
        // 范围伤害（基础伤害的45%，比Tsunami稍高）
        float explosionDamage = damage * 0.45f;
        
        for (LivingEntity entity : entitiesInRange) {
            // 计算距离衰减
            double distance = center.distanceTo(entity.position());
            float distanceMultiplier = (float) Math.max(0, 1.0 - distance / 3.5);
            float finalDamage = explosionDamage * distanceMultiplier;
            
            if (finalDamage > 0) {
                entity.hurt(entity.damageSources().playerAttack(owner), finalDamage);
                
                // 击退效果（向爆炸中心外推）
                Vec3 knockback = entity.position().subtract(center).normalize();
                entity.setDeltaMovement(knockback.scale(0.4)); // 比Tsunami更强的击退
            }
        }
    }
    
    /**
     * 生成腐蚀爆炸粒子效果
     */
    private void spawnCorrosionParticles(Vec3 center) {
        if (this.level().isClientSide()) {
            // 中心暗红色腐蚀粒子
            for (int i = 0; i < 25; i++) {
                this.level().addParticle(ParticleTypes.DAMAGE_INDICATOR,
                        center.x + (this.random.nextDouble() - 0.5) * 2.5,
                        center.y + (this.random.nextDouble() - 0.5) * 2.5,
                        center.z + (this.random.nextDouble() - 0.5) * 2.5,
                        (this.random.nextDouble() - 0.5) * 0.6,
                        0.2 + this.random.nextDouble() * 0.4,
                        (this.random.nextDouble() - 0.5) * 0.6);
            }
            
            // 绿色腐蚀气泡效果
            for (int i = 0; i < 15; i++) {
                this.level().addParticle(ParticleTypes.ITEM_SLIME,
                        center.x + (this.random.nextDouble() - 0.5) * 2.0,
                        center.y + (this.random.nextDouble() - 0.5) * 2.0,
                        center.z + (this.random.nextDouble() - 0.5) * 2.0,
                        (this.random.nextDouble() - 0.5) * 0.4,
                        0.1 + this.random.nextDouble() * 0.3,
                        (this.random.nextDouble() - 0.5) * 0.4);
            }
            
            // 紫色腐蚀光环效果
            for (int ring = 1; ring <= 2; ring++) {
                for (int i = 0; i < 12; i++) {
                    double angle = (double) i / 12.0 * Math.PI * 2;
                    double x = center.x + Math.cos(angle) * ring;
                    double z = center.z + Math.sin(angle) * ring;
                    double y = center.y;
                    
                    this.level().addParticle(ParticleTypes.WITCH,
                            x, y, z,
                            Math.cos(angle) * 0.15,
                            0.08,
                            Math.sin(angle) * 0.15);
                }
            }
            
            // 暗色烟雾效果
            for (int i = 0; i < 10; i++) {
                this.level().addParticle(ParticleTypes.SMOKE,
                        center.x + (this.random.nextDouble() - 0.5) * 1.5,
                        center.y + (this.random.nextDouble() - 0.5) * 1.5,
                        center.z + (this.random.nextDouble() - 0.5) * 1.5,
                        (this.random.nextDouble() - 0.5) * 0.3,
                        0.05 + this.random.nextDouble() * 0.2,
                        (this.random.nextDouble() - 0.5) * 0.3);
            }
        }
    }
    
    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY; // 不能拾取
    }
}