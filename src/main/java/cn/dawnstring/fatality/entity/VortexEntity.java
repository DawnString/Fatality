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
 * 漩涡实体 - 实现吸附和持续伤害功能
 */
public class VortexEntity extends Entity {
    
    private static final int MAX_LIFETIME = 100; // 最大生命周期5秒
    private static final float ATTRACTION_RADIUS = 2.0f; // 吸附半径2格
    private static final int DAMAGE_INTERVAL = 20; // 伤害间隔20tick（1秒）
    private static final float ATTRACTION_FORCE = 0.4f; // 吸附力大小
    
    private final Player owner;
    private final float vortexDamage;
    private int lifeTicks = 0;
    private int damageTickCounter = 0;

    public VortexEntity(Level level, Player owner, float damage) {
        super(EntityType.AREA_EFFECT_CLOUD, level); // 使用区域效果云实体类型作为基础
        this.owner = owner;
        this.vortexDamage = damage;
        
        // 设置漩涡大小
        this.setBoundingBox(new AABB(-1, -1, -1, 1, 1, 1));
    }
    
    @Override
    protected void defineSynchedData() {
        // 不需要同步数据
    }
    
    @Override
    public void tick() {
        super.tick();
        lifeTicks++;
        
        // 生成漩涡粒子效果
        if (this.level().isClientSide()) {
            spawnVortexParticles();
        }
        
        // 检查生命周期结束
        if (lifeTicks >= MAX_LIFETIME) {
            this.discard();
            return;
        }
        
        // 每tick执行吸附逻辑
        performAttraction();
        
        // 每1秒执行一次伤害逻辑
        if (lifeTicks % DAMAGE_INTERVAL == 0) {
            performDamage();
        }
    }
    
    /**
     * 生成漩涡粒子效果
     */
    private void spawnVortexParticles() {
        // 生成漩涡核心粒子（水泡粒子）
        for (int i = 0; i < 8; i++) {
            double angle = (lifeTicks * 0.2 + i * 0.785) % (2 * Math.PI);
            double radius = 0.5 + this.random.nextDouble() * 0.3;
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;
            
            this.level().addParticle(ParticleTypes.BUBBLE,
                    this.getX() + offsetX,
                    this.getY() + this.random.nextDouble() * 1.5,
                    this.getZ() + offsetZ,
                    0, 0.1, 0);
        }
        
        // 生成漩涡外围粒子（水花粒子）
        for (int i = 0; i < 5; i++) {
            double angle = (lifeTicks * 0.15 + i * 1.57) % (2 * Math.PI);
            double radius = 1.0 + this.random.nextDouble() * 0.5;
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;

            this.level().addParticle(ParticleTypes.DRIPPING_DRIPSTONE_WATER,
                    this.getX() + offsetX,
                    this.getY() + this.random.nextDouble() * 2.0,
                    this.getZ() + offsetZ,
                    0, 0.05, 0);
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
                this.getX() - ATTRACTION_RADIUS, this.getY() - ATTRACTION_RADIUS, this.getZ() - ATTRACTION_RADIUS,
                this.getX() + ATTRACTION_RADIUS, this.getY() + ATTRACTION_RADIUS, this.getZ() + ATTRACTION_RADIUS
        );
        
        // 获取区域内的所有实体
        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(
                LivingEntity.class, attractionBox,
                entity -> entity != owner && 
                         entity.isAlive() && 
                         !(entity instanceof Player) && 
                         !entity.getType().getDescription().getString().toLowerCase().contains("boss")
        );
        
        for (LivingEntity entity : nearbyEntities) {
            // 吸附实体向漩涡中心
            attractEntity(entity);
        }
    }
    
    /**
     * 吸附实体向漩涡中心
     */
    private void attractEntity(LivingEntity entity) {
        Vec3 vortexPos = this.position();
        Vec3 entityPos = entity.position();
        
        // 计算吸附方向
        Vec3 attractionDirection = vortexPos.subtract(entityPos).normalize();
        
        // 计算距离，距离越近吸附力越小
        double distance = vortexPos.distanceTo(entityPos);
        double forceMultiplier = Math.min(1.0, distance / ATTRACTION_RADIUS);
        
        // 应用吸附力
        Vec3 currentMotion = entity.getDeltaMovement();
        Vec3 attractionMotion = attractionDirection.scale(ATTRACTION_FORCE * forceMultiplier);
        
        entity.setDeltaMovement(currentMotion.add(attractionMotion));
        
        // 播放吸附粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 2; i++) {
                this.level().addParticle(ParticleTypes.BUBBLE_POP,
                        entity.getX(), entity.getY() + 0.5, entity.getZ(),
                        attractionDirection.x * 0.1, attractionDirection.y * 0.1, attractionDirection.z * 0.1);
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
                this.getX() - ATTRACTION_RADIUS, this.getY() - ATTRACTION_RADIUS, this.getZ() - ATTRACTION_RADIUS,
                this.getX() + ATTRACTION_RADIUS, this.getY() + ATTRACTION_RADIUS, this.getZ() + ATTRACTION_RADIUS
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
        // 创建伤害源
        var damageSource = entity.damageSources().indirectMagic(this, this.owner);
        
        // 造成伤害
        if (entity.hurt(damageSource, vortexDamage)) {
            // 生成伤害粒子
            if (this.level().isClientSide()) {
                for (int i = 0; i < 3; i++) {
                    this.level().addParticle(ParticleTypes.CRIT,
                            entity.getX() + (this.random.nextDouble() - 0.5) * entity.getBbWidth(),
                            entity.getY() + this.random.nextDouble() * entity.getBbHeight(),
                            entity.getZ() + (this.random.nextDouble() - 0.5) * entity.getBbWidth(),
                            0, 0.1, 0);
                }
            }
            
            // 播放伤害音效
            this.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.WATER_AMBIENT, SoundSource.HOSTILE, 0.5F, 0.8F);
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