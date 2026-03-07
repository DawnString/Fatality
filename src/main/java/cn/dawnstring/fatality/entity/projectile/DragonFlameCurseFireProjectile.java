package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.registry.ModEffects;
import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.joml.Vector3f;

/**
 * 龙炎诅咒火焰投射物
 * 特性：追踪最近目标，施加龙炎燃烧效果
 */
public class DragonFlameCurseFireProjectile extends AbstractArrow {
    
    private static final int MAX_LIFE_TIME = 100; // 最大生命周期100tick（5秒）
    private static final double TRACKING_RANGE = 20.0; // 追踪范围
    private static final double TRACKING_SPEED = 0.2; // 追踪速度
    private static final int DRAGONFIRE_DURATION = 100; // 龙炎燃烧持续时间5秒
    
    private int lifeTime = 0;
    private float baseDamage;
    private LivingEntity shooter;
    private LivingEntity target;
    private boolean hasTarget = false;
    
    public DragonFlameCurseFireProjectile(EntityType<? extends DragonFlameCurseFireProjectile> type, Level level) {
        super(type, level);
    }
    
    public DragonFlameCurseFireProjectile(Level level, LivingEntity shooter, float baseDamage) {
        super(ModEntities.DRAGON_FLAME_CURSE_FIRE_PROJECTILE.get(), shooter, level);
        this.shooter = shooter;
        this.baseDamage = baseDamage;
        
        // 设置投射物属性
        this.setNoGravity(false); // 受重力影响
        this.setBaseDamage(0); // 基础伤害设为0，使用自定义伤害计算
        this.setKnockback(2); // 中等击退效果
    }
    
    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        
        if (!this.level().isClientSide()) {
            // 播放火焰爆炸音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.FIRECHARGE_USE, SoundSource.NEUTRAL, 1.2F, 0.6F);
            
            // 生成火焰爆炸粒子效果
            spawnExplosionParticles();
            
            // 如果是击中实体，处理伤害
            if (hitResult.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityResult = (EntityHitResult) hitResult;
                Entity target = entityResult.getEntity();
                
                if (target instanceof LivingEntity) {
                    applyDamage((LivingEntity) target);
                }
            }
            
            // 移除投射物
            this.discard();
        }
    }
    
    /**
     * 对目标造成伤害
     */
    private void applyDamage(LivingEntity target) {
        float finalDamage = baseDamage;
        
        if (finalDamage > 0) {
            DamageSource damageSource = target.damageSources().indirectMagic(this, shooter);
            boolean damageApplied = target.hurt(damageSource, finalDamage);
            
            if (damageApplied) {
                // 施加龙炎燃烧效果
                applyDragonfireBurnEffect(target);
                
                // 播放伤害音效
                this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.BLAZE_HURT, SoundSource.NEUTRAL, 0.8F, 0.9F);
            }
        }
    }
    
    /**
     * 施加龙炎燃烧效果
     */
    private void applyDragonfireBurnEffect(LivingEntity target) {
        // 施加龙炎焚烧效果
        if (ModEffects.DRAGONFIRE_BURN != null) {
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    ModEffects.DRAGONFIRE_BURN.get(), DRAGONFIRE_DURATION, 0));
        }
    }
    
    /**
     * 生成爆炸粒子效果
     */
    private void spawnExplosionParticles() {
        if (this.level().isClientSide()) {
            Vec3 impactPos = this.position();
            
            // 生成龙炎爆炸粒子
            for (int i = 0; i < 40; i++) {
                double offsetX = (Math.random() - 0.5) * 4.0;
                double offsetY = (Math.random() - 0.5) * 4.0;
                double offsetZ = (Math.random() - 0.5) * 4.0;
                
                // 龙炎色粒子（深红色）
                Vector3f color = new Vector3f(0.8f, 0.2f, 0.1f);
                DustParticleOptions particle = new DustParticleOptions(color, 2.0f);
                
                this.level().addParticle(particle,
                        impactPos.x + offsetX,
                        impactPos.y + offsetY,
                        impactPos.z + offsetZ,
                        0, 0.1, 0);
            }
            
            // 生成火焰和烟雾粒子
            for (int i = 0; i < 30; i++) {
                this.level().addParticle(ParticleTypes.FLAME,
                        impactPos.x + (Math.random() - 0.5) * 3.0,
                        impactPos.y + (Math.random() - 0.5) * 3.0,
                        impactPos.z + (Math.random() - 0.5) * 3.0,
                        0, 0.1, 0);
                
                this.level().addParticle(ParticleTypes.SMOKE,
                        impactPos.x + (Math.random() - 0.5) * 2.0,
                        impactPos.y + (Math.random() - 0.5) * 2.0,
                        impactPos.z + (Math.random() - 0.5) * 2.0,
                        0, 0.05, 0);
            }
        }
    }
    
    @Override
    public void tick() {
        super.tick();
        lifeTime++;
        
        if (!this.level().isClientSide()) {
            // 追踪逻辑
            if (!hasTarget || target == null || !target.isAlive()) {
                findTarget();
            }
            
            if (hasTarget && target != null && target.isAlive()) {
                trackTarget();
            }
        }
        
        // 在飞行过程中生成龙炎粒子
        if (this.level().isClientSide()) {
            spawnFlightParticles();
        }
        
        // 检查生命周期
        if (lifeTime > MAX_LIFE_TIME) {
            if (!this.level().isClientSide()) {
                // 生命周期结束时爆炸
                this.onHit(new BlockHitResult(this.position(), net.minecraft.core.Direction.UP, this.blockPosition(), false));
            }
            this.discard();
        }
    }
    
    /**
     * 寻找最近的目标
     */
    private void findTarget() {
        // 寻找最近的敌对生物
        LivingEntity nearestTarget = this.level().getNearestEntity(
                this.level().getEntitiesOfClass(LivingEntity.class, 
                        new AABB(this.getX() - TRACKING_RANGE, this.getY() - TRACKING_RANGE, this.getZ() - TRACKING_RANGE,
                                 this.getX() + TRACKING_RANGE, this.getY() + TRACKING_RANGE, this.getZ() + TRACKING_RANGE)),
                TargetingConditions.forCombat().ignoreLineOfSight(),
                shooter,
                this.getX(), this.getY(), this.getZ()
        );
        
        // 优先选择敌对生物，如果没有则选择任何活着的生物（除了发射者）
        if (nearestTarget != null && nearestTarget != shooter && nearestTarget.isAlive()) {
            // 检查是否为敌对生物
            if (nearestTarget instanceof Enemy || nearestTarget instanceof Mob) {
                this.target = nearestTarget;
                this.hasTarget = true;
            } else {
                // 如果不是敌对生物，但也没有其他目标，则选择它
                this.target = nearestTarget;
                this.hasTarget = true;
            }
        }
    }
    
    /**
     * 追踪目标
     */
    private void trackTarget() {
        if (target != null && target.isAlive()) {
            Vec3 currentPos = this.position();
            Vec3 targetPos = target.position().add(0, target.getEyeHeight() / 2, 0);
            
            // 计算到目标的向量
            Vec3 toTarget = targetPos.subtract(currentPos);
            double distance = toTarget.length();
            
            // 如果距离很近，直接命中
            if (distance < 2.0) {
                this.onHit(new EntityHitResult(target));
                return;
            }
            
            // 计算新的方向向量
            Vec3 currentVelocity = this.getDeltaMovement();
            Vec3 newDirection = toTarget.normalize();
            
            // 平滑转向
            Vec3 smoothedDirection = currentVelocity.normalize().scale(1.0 - TRACKING_SPEED)
                    .add(newDirection.scale(TRACKING_SPEED))
                    .normalize();
            
            // 保持原有速度，只改变方向
            double speed = currentVelocity.length();
            this.setDeltaMovement(smoothedDirection.scale(speed));
            
            // 更新旋转角度
            this.setYRot((float)(Math.atan2(smoothedDirection.x, smoothedDirection.z) * (180.0 / Math.PI)));
            this.setXRot((float)(Math.atan2(smoothedDirection.y, Math.sqrt(smoothedDirection.x * smoothedDirection.x + smoothedDirection.z * smoothedDirection.z)) * (180.0 / Math.PI)));
        }
    }
    
    /**
     * 生成飞行粒子效果
     */
    private void spawnFlightParticles() {
        Vec3 pos = this.position();
        
        // 生成龙炎轨迹粒子
        if (this.tickCount % 2 == 0) {
            // 龙炎色粒子（深红色）
            Vector3f color = new Vector3f(0.8f, 0.2f, 0.1f);
            DustParticleOptions particle = new DustParticleOptions(color, 1.2f);
            
            this.level().addParticle(particle,
                    pos.x, pos.y, pos.z,
                    0, 0, 0);
            
            // 火焰粒子
            this.level().addParticle(ParticleTypes.FLAME,
                    pos.x, pos.y, pos.z,
                    0, 0.05, 0);
            
            // 烟雾粒子（追踪效果）
            this.level().addParticle(ParticleTypes.SMOKE,
                    pos.x, pos.y, pos.z,
                    0, 0.02, 0);
        }
    }
    
    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY; // 不允许拾取
    }
}