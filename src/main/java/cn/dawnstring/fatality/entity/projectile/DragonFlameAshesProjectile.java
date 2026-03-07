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
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.joml.Vector3f;

/**
 * 龙炎灰烬投射物
 * 特性：击中后子弹散射，对锥形区域造成距离递减伤害，施加龙炎燃烧效果
 */
public class DragonFlameAshesProjectile extends AbstractArrow {
    
    private static final int MAX_LIFE_TIME = 40; // 最大生命周期40tick（2秒）
    private static final float CONE_ANGLE = 30.0f; // 锥形散射角度
    private static final float MAX_CONE_RANGE = 12.0f; // 最大锥形范围
    private static final int DRAGONFIRE_DURATION = 100; // 龙炎燃烧持续时间5秒
    
    private int lifeTime = 0;
    private float baseDamage;
    private LivingEntity shooter;
    private boolean hasHit = false;
    
    public DragonFlameAshesProjectile(EntityType<? extends DragonFlameAshesProjectile> type, Level level) {
        super(type, level);
    }
    
    public DragonFlameAshesProjectile(Level level, LivingEntity shooter, float baseDamage) {
        super(ModEntities.DRAGON_FLAME_ASHES_PROJECTILE.get(), shooter, level);
        this.shooter = shooter;
        this.baseDamage = baseDamage;
        
        // 设置投射物属性
        this.setNoGravity(false); // 受重力影响
        this.setBaseDamage(0); // 基础伤害设为0，使用自定义伤害计算
        this.setKnockback(1); // 轻微击退效果
    }
    
    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        
        if (!this.level().isClientSide() && !hasHit) {
            hasHit = true;
            
            // 播放子弹命中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.FIRECHARGE_USE, SoundSource.NEUTRAL, 0.8F, 1.2F);
            
            // 生成命中粒子效果
            spawnHitParticles();
            
            // 如果是击中实体，处理伤害和散射逻辑
            if (hitResult.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityResult = (EntityHitResult) hitResult;
                Entity target = entityResult.getEntity();
                
                if (target instanceof LivingEntity) {
                    // 对主要目标造成伤害
                    applyDirectDamage((LivingEntity) target);
                    
                    // 生成锥形散射伤害
                    createConeScatterDamage(target.position());
                }
            } else if (hitResult.getType() == HitResult.Type.BLOCK) {
                // 击中方块时也生成锥形散射
                BlockHitResult blockResult = (BlockHitResult) hitResult;
                createConeScatterDamage(blockResult.getLocation());
            }
            
            // 移除投射物
            this.discard();
        }
    }
    
    /**
     * 对主要目标造成直接伤害
     */
    private void applyDirectDamage(LivingEntity target) {
        float finalDamage = baseDamage;
        
        if (finalDamage > 0) {
            DamageSource damageSource = target.damageSources().indirectMagic(this, shooter);
            boolean damageApplied = target.hurt(damageSource, finalDamage);
            
            if (damageApplied) {
                // 施加龙炎燃烧效果
                applyDragonfireBurnEffect(target);
                
                // 播放伤害音效
                this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.BLAZE_HURT, SoundSource.NEUTRAL, 0.5F, 1.1F);
            }
        }
    }
    
    /**
     * 生成锥形散射伤害
     */
    private void createConeScatterDamage(Vec3 impactPos) {
        // 获取投射物方向
        Vec3 direction = this.getDeltaMovement().normalize();
        
        // 在锥形区域内寻找其他实体
        this.level().getEntitiesOfClass(LivingEntity.class, 
                new AABB(impactPos.x - MAX_CONE_RANGE, impactPos.y - MAX_CONE_RANGE, impactPos.z - MAX_CONE_RANGE,
                         impactPos.x + MAX_CONE_RANGE, impactPos.y + MAX_CONE_RANGE, impactPos.z + MAX_CONE_RANGE))
                .stream()
                .filter(entity -> entity != shooter && entity.isAlive())
                .forEach(entity -> {
                    // 计算实体到冲击点的向量
                    Vec3 toEntity = entity.position().subtract(impactPos);
                    
                    // 计算角度（点积）
                    double dotProduct = direction.dot(toEntity.normalize());
                    double angle = Math.acos(Math.max(-1, Math.min(1, dotProduct))) * (180.0 / Math.PI);
                    
                    // 如果在锥形角度内
                    if (angle <= CONE_ANGLE / 2) {
                        double distance = toEntity.length();
                        if (distance <= MAX_CONE_RANGE) {
                            // 计算距离衰减的伤害
                            float distanceMultiplier = (float) (1.0 - (distance / MAX_CONE_RANGE));
                            float scatterDamage = baseDamage * 0.3f * distanceMultiplier; // 散射伤害为基础伤害的30%
                            
                            if (scatterDamage > 0) {
                                DamageSource damageSource = entity.damageSources().indirectMagic(this, shooter);
                                boolean damageApplied = entity.hurt(damageSource, scatterDamage);
                                
                                if (damageApplied) {
                                    // 施加龙炎燃烧效果
                                    applyDragonfireBurnEffect(entity);
                                }
                            }
                        }
                    }
                });
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
     * 生成命中粒子效果
     */
    private void spawnHitParticles() {
        if (this.level().isClientSide()) {
            Vec3 impactPos = this.position();
            
            // 生成子弹命中粒子
            for (int i = 0; i < 20; i++) {
                double offsetX = (Math.random() - 0.5) * 2.0;
                double offsetY = (Math.random() - 0.5) * 2.0;
                double offsetZ = (Math.random() - 0.5) * 2.0;
                
                // 龙炎色粒子（灰红色）
                Vector3f color = new Vector3f(0.6f, 0.3f, 0.2f);
                DustParticleOptions particle = new DustParticleOptions(color, 0.8f);
                
                this.level().addParticle(particle,
                        impactPos.x + offsetX,
                        impactPos.y + offsetY,
                        impactPos.z + offsetZ,
                        0, 0.05, 0);
            }
            
            // 生成火焰和火花粒子
            for (int i = 0; i < 15; i++) {
                this.level().addParticle(ParticleTypes.FLAME,
                        impactPos.x + (Math.random() - 0.5) * 1.5,
                        impactPos.y + (Math.random() - 0.5) * 1.5,
                        impactPos.z + (Math.random() - 0.5) * 1.5,
                        0, 0.05, 0);
                
                this.level().addParticle(ParticleTypes.SMOKE,
                        impactPos.x + (Math.random() - 0.5) * 1.0,
                        impactPos.y + (Math.random() - 0.5) * 1.0,
                        impactPos.z + (Math.random() - 0.5) * 1.0,
                        0, 0.02, 0);
            }
        }
    }
    
    @Override
    public void tick() {
        super.tick();
        lifeTime++;
        
        // 在飞行过程中生成子弹轨迹粒子
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
     * 生成飞行粒子效果
     */
    private void spawnFlightParticles() {
        Vec3 pos = this.position();
        
        // 生成子弹轨迹粒子
        if (this.tickCount % 3 == 0) {
            // 龙炎色粒子（灰红色）
            Vector3f color = new Vector3f(0.6f, 0.3f, 0.2f);
            DustParticleOptions particle = new DustParticleOptions(color, 0.6f);
            
            this.level().addParticle(particle,
                    pos.x, pos.y, pos.z,
                    0, 0, 0);
            
            // 火焰粒子
            this.level().addParticle(ParticleTypes.FLAME,
                    pos.x, pos.y, pos.z,
                    0, 0.02, 0);
        }
    }
    
    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY; // 不允许拾取
    }
}