package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.joml.Vector3f;

/**
 * 丛林权杖剑气投射物
 * 特性：竖着的绿色粒子剑气，中等飞行速度，能够穿透敌人造成伤害
 */
public class JungleSwordWaveProjectile extends AbstractArrow {
    
    private static final int MAX_LIFE_TIME = 100; // 最大生命周期100tick（5秒）
    private static final float SWORD_WAVE_DAMAGE = 1.0f; // 剑气伤害倍率
    private static final float PIERCING_DAMAGE_REDUCTION = 0.7f; // 穿透伤害衰减
    private static final int MAX_PIERCED_ENTITIES = 3; // 最大穿透敌人数量
    
    private int lifeTime = 0;
    private float swordWaveDamage;
    private LivingEntity shooter;
    private int piercedEntities = 0;
    
    public JungleSwordWaveProjectile(EntityType<? extends JungleSwordWaveProjectile> type, Level level) {
        super(type, level);
    }
    
    public JungleSwordWaveProjectile(Level level, LivingEntity shooter, float swordWaveDamage) {
        super(ModEntities.JUNGLE_SWORD_WAVE_PROJECTILE.get(), shooter, level);
        this.shooter = shooter;
        this.swordWaveDamage = swordWaveDamage;
        
        // 设置投射物属性
        this.setNoGravity(true); // 无视重力
        this.setBaseDamage(0); // 基础伤害设为0，使用自定义伤害计算
        this.setPierceLevel((byte) MAX_PIERCED_ENTITIES); // 穿透等级
        this.setKnockback(0); // 无击退效果
    }
    
    @Override
    protected void onHit(HitResult hitResult) {
        // 不调用super.onHit(hitResult)，避免默认的碰撞处理逻辑
        
        if (!this.level().isClientSide()) {
            // 播放剑气命中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.NEUTRAL, 0.8F, 1.2F);
            
            // 生成命中粒子效果
            spawnHitParticles();
            
            // 如果是击中实体，处理伤害逻辑
            if (hitResult.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityResult = (EntityHitResult) hitResult;
                Entity target = entityResult.getEntity();
                
                if (target instanceof LivingEntity) {
                    // 应用穿透伤害衰减
                    float damageMultiplier = (float) Math.pow(PIERCING_DAMAGE_REDUCTION, piercedEntities);
                    float finalDamage = swordWaveDamage * damageMultiplier;
                    
                    if (finalDamage > 0) {
                        DamageSource damageSource = target.damageSources().indirectMagic(this, shooter);
                        boolean damageApplied = target.hurt(damageSource, finalDamage);
                        
                        if (damageApplied) {
                            piercedEntities++;
                            
                            // 播放伤害音效
                            this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.NEUTRAL, 0.6F, 1.0F);
                        }
                    }
                }
            }
            
            // 如果达到最大穿透数量或生命周期结束，移除投射物
            if (piercedEntities >= MAX_PIERCED_ENTITIES || lifeTime >= MAX_LIFE_TIME) {
                this.discard();
            }
        }
    }
    
    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        // 不调用父类方法，避免重复伤害计算
        // 伤害逻辑在onHit方法中处理
    }
    
    /**
     * 生成命中粒子效果
     */
    private void spawnHitParticles() {
        if (this.level().isClientSide()) {
            Vec3 hitPos = this.position();
            
            // 生成绿色剑气爆炸粒子
            for (int i = 0; i < 15; i++) {
                double offsetX = (Math.random() - 0.5) * 1.5;
                double offsetY = (Math.random() - 0.5) * 1.5;
                double offsetZ = (Math.random() - 0.5) * 1.5;
                
                // 绿色粒子效果
                Vector3f color = new Vector3f(0.2f, 1.0f, 0.1f); // 绿色
                DustParticleOptions particle = new DustParticleOptions(color, 1.5f);
                
                this.level().addParticle(particle,
                        hitPos.x + offsetX,
                        hitPos.y + offsetY,
                        hitPos.z + offsetZ,
                        0, 0.1, 0);
            }
            
            // 生成树叶粒子效果
            for (int i = 0; i < 10; i++) {
                this.level().addParticle(ParticleTypes.CHERRY_LEAVES,
                        hitPos.x + (Math.random() - 0.5) * 2.0,
                        hitPos.y + (Math.random() - 0.5) * 2.0,
                        hitPos.z + (Math.random() - 0.5) * 2.0,
                        0, 0.05, 0);
            }
        }
    }
    
    @Override
    public void tick() {
        super.tick();
        lifeTime++;
        
        // 在飞行过程中生成剑气粒子
        if (this.level().isClientSide()) {
            spawnFlightParticles();
        }
        
        // 检查生命周期
        if (lifeTime > MAX_LIFE_TIME) {
            if (!this.level().isClientSide()) {
                // 生命周期结束时播放消失音效
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.5F, 1.0F);
            }
            this.discard();
        }
    }
    
    /**
     * 生成飞行粒子效果（竖着的绿色剑气）
     */
    private void spawnFlightParticles() {
        Vec3 pos = this.position();
        Vec3 motion = this.getDeltaMovement();
        
        // 生成竖着的绿色剑气粒子
        for (int i = 0; i < 5; i++) {
            // 竖着的粒子排列（垂直于运动方向）
            double verticalOffset = (Math.random() - 0.5) * 2.0; // 垂直方向偏移
            double horizontalOffset = (Math.random() - 0.5) * 0.5; // 水平方向偏移
            
            // 计算垂直于运动方向的向量
            Vec3 perpendicular = new Vec3(-motion.z, 0, motion.x).normalize();
            
            double particleX = pos.x + perpendicular.x * horizontalOffset;
            double particleY = pos.y + verticalOffset;
            double particleZ = pos.z + perpendicular.z * horizontalOffset;
            
            // 绿色粒子效果
            Vector3f color = new Vector3f(0.1f, 0.8f, 0.05f); // 深绿色
            DustParticleOptions particle = new DustParticleOptions(color, 2.0f);
            
            this.level().addParticle(particle,
                    particleX, particleY, particleZ,
                    motion.x * -0.1,
                    motion.y * -0.1,
                    motion.z * -0.1);
        }
        
        // 生成剑气轨迹粒子（绿色粒子）
        if (this.tickCount % 2 == 0) {
            this.level().addParticle(ParticleTypes.HAPPY_VILLAGER,
                    pos.x, pos.y, pos.z,
                    motion.x * -0.2,
                    motion.y * -0.2,
                    motion.z * -0.2);
        }
        
        // 生成树叶轨迹粒子
        if (this.tickCount % 3 == 0) {
            this.level().addParticle(ParticleTypes.CHERRY_LEAVES,
                    pos.x + (Math.random() - 0.5) * 0.5,
                    pos.y + (Math.random() - 0.5) * 0.5,
                    pos.z + (Math.random() - 0.5) * 0.5,
                    0, 0.02, 0);
        }
    }
    
    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY; // 不允许拾取
    }
}