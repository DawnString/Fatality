package cn.dawnstring.fatality.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.List;

/**
 * 空间坍缩实体 - 实现空间坍缩吸附和黑色粒子伤害功能
 */
public class SpaceCollapseEntity extends Entity {
    
    private final Player owner;
    private final float collapseDamage;
    private final double collapseRadius;
    private final int maxLifetime;
    private int lifeTicks = 0;
    private int damageTickCounter = 0;
    private static final int DAMAGE_INTERVAL = 10; // 伤害间隔10tick（0.5秒）
    private static final float ATTRACTION_FORCE = 0.6f; // 吸附力大小（比漩涡更强）

    public SpaceCollapseEntity(Level level, Player owner, float damage, double radius, int duration) {
        super(EntityType.AREA_EFFECT_CLOUD, level); // 使用区域效果云实体类型作为基础
        this.owner = owner;
        this.collapseDamage = damage;
        this.collapseRadius = radius;
        this.maxLifetime = duration;
        
        // 设置坍缩区域大小
        this.setBoundingBox(new AABB(-radius, -radius, -radius, radius, radius, radius));
    }
    
    @Override
    protected void defineSynchedData() {
        // 不需要同步数据
    }
    
    @Override
    public void tick() {
        super.tick();
        lifeTicks++;
        
        // 生成空间坍缩粒子效果
        if (this.level().isClientSide()) {
            spawnCollapseParticles();
        }
        
        // 检查生命周期结束
        if (lifeTicks >= maxLifetime) {
            this.discard();
            return;
        }
        
        // 每tick执行吸附逻辑
        performAttraction();
        
        // 每0.5秒执行一次伤害逻辑
        if (lifeTicks % DAMAGE_INTERVAL == 0) {
            performDamage();
        }
    }
    
    /**
     * 生成空间坍缩粒子效果
     */
    private void spawnCollapseParticles() {
        // 生成坍缩核心粒子（黑色烟雾和火焰）
        for (int i = 0; i < 12; i++) {
            double angle = (lifeTicks * 0.3 + i * 0.524) % (2 * Math.PI);
            double radius = (this.random.nextDouble() * 0.7 + 0.3) * collapseRadius;
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;
            
            // 黑色烟雾粒子（核心）
            this.level().addParticle(ParticleTypes.SMOKE,
                    this.getX() + offsetX,
                    this.getY() + this.random.nextDouble() * collapseRadius * 2,
                    this.getZ() + offsetZ,
                    0, -0.05, 0);
            
            // 黑色火焰粒子（外围）
            if (i % 3 == 0) {
                this.level().addParticle(ParticleTypes.FLAME,
                        this.getX() + offsetX * 1.2,
                        this.getY() + this.random.nextDouble() * collapseRadius * 2,
                        this.getZ() + offsetZ * 1.2,
                        0, -0.03, 0);
            }
        }
        
        // 生成坍缩边缘粒子（灵魂火焰）
        for (int i = 0; i < 8; i++) {
            double angle = (lifeTicks * 0.25 + i * 0.785) % (2 * Math.PI);
            double radius = collapseRadius * 0.9;
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;

            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX() + offsetX,
                    this.getY() + this.random.nextDouble() * collapseRadius * 1.5,
                    this.getZ() + offsetZ,
                    0, -0.02, 0);
        }
        
        // 生成坍缩中心粒子（末影粒子）
        if (lifeTicks % 5 == 0) {
            for (int i = 0; i < 3; i++) {
                this.level().addParticle(ParticleTypes.PORTAL,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                        this.getY() + this.random.nextDouble() * 0.5,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                        0, 0.1, 0);
            }
        }
    }
    
    /**
     * 执行吸附逻辑
     */
    private void performAttraction() {
        if (this.level().isClientSide()) {
            return;
        }
        
        // 计算吸附区域
        AABB attractionBox = new AABB(
                this.getX() - collapseRadius, this.getY() - collapseRadius, this.getZ() - collapseRadius,
                this.getX() + collapseRadius, this.getY() + collapseRadius, this.getZ() + collapseRadius
        );
        
        // 获取区域内的所有实体
        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(
                LivingEntity.class, attractionBox,
                entity -> entity != owner && 
                         entity.isAlive() && 
                         !entity.getType().getDescription().getString().toLowerCase().contains("boss")
        );
        
        for (LivingEntity entity : nearbyEntities) {
            // 吸附实体向坍缩中心
            attractEntity(entity);
        }
    }
    
    /**
     * 吸附实体向坍缩中心
     */
    private void attractEntity(LivingEntity entity) {
        Vec3 collapsePos = this.position();
        Vec3 entityPos = entity.position();
        
        // 计算吸附方向
        Vec3 attractionDirection = collapsePos.subtract(entityPos).normalize();
        
        // 计算距离，距离越近吸附力越大（与漩涡相反，坍缩是越近吸力越强）
        double distance = collapsePos.distanceTo(entityPos);
        double forceMultiplier = Math.max(0.1, 1.0 - (distance / collapseRadius));
        
        // 应用吸附力
        Vec3 currentMotion = entity.getDeltaMovement();
        Vec3 attractionMotion = attractionDirection.scale(ATTRACTION_FORCE * forceMultiplier);
        
        entity.setDeltaMovement(currentMotion.add(attractionMotion));
        
        // 播放吸附粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 3; i++) {
                this.level().addParticle(ParticleTypes.SMOKE,
                        entity.getX(), entity.getY() + 0.5, entity.getZ(),
                        attractionDirection.x * 0.2, attractionDirection.y * 0.2, attractionDirection.z * 0.2);
            }
        }
    }
    
    /**
     * 执行伤害逻辑
     */
    private void performDamage() {
        if (this.level().isClientSide()) {
            return;
        }
        
        // 计算伤害区域
        AABB damageBox = new AABB(
                this.getX() - collapseRadius, this.getY() - collapseRadius, this.getZ() - collapseRadius,
                this.getX() + collapseRadius, this.getY() + collapseRadius, this.getZ() + collapseRadius
        );
        
        // 获取区域内的所有实体
        List<LivingEntity> entitiesInRange = this.level().getEntitiesOfClass(
                LivingEntity.class, damageBox,
                entity -> entity.isAlive() && entity != owner
        );
        
        for (LivingEntity entity : entitiesInRange) {
            // 对实体造成伤害
            damageEntity(entity);
        }
    }
    
    /**
     * 对实体造成伤害
     */
    private void damageEntity(LivingEntity entity) {
        // 创建伤害源（魔法伤害）
        var damageSource = entity.damageSources().indirectMagic(this, this.owner);
        
        // 造成伤害
        if (entity.hurt(damageSource, collapseDamage)) {
            // 生成伤害粒子
            if (this.level().isClientSide()) {
                for (int i = 0; i < 5; i++) {
                    this.level().addParticle(ParticleTypes.DAMAGE_INDICATOR,
                            entity.getX() + (this.random.nextDouble() - 0.5) * entity.getBbWidth(),
                            entity.getY() + this.random.nextDouble() * entity.getBbHeight(),
                            entity.getZ() + (this.random.nextDouble() - 0.5) * entity.getBbWidth(),
                            0, 0.15, 0);
                }
            }
            
            // 播放伤害音效
            this.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.ENDERMAN_HURT, SoundSource.HOSTILE, 0.6F, 0.7F);
        }
    }
    
    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        // 不需要保存数据
    }
    
    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        // 不需要保存数据
    }
}