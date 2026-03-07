package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.registry.ModEffects;
import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

import java.util.Comparator;
import java.util.List;

/**
 * 咒火投射物 - 追踪最近的敌人，施加咒火焚烧效果
 */
public class CurseFireProjectile extends AbstractArrow
{
    private final float curseFireDamage;
    private final Player shooter;
    private int lifeTime = 0;
    private final int maxLifeTime = 100; // 5秒生命周期
    private LivingEntity target = null;
    private static final double TRACKING_RANGE = 15.0; // 追踪范围15格
    
    public CurseFireProjectile(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        this.curseFireDamage = 0;
        this.shooter = null;
    }
    
    public CurseFireProjectile(Level level, Player shooter, float curseFireDamage) {
        super(ModEntities.CURSE_FIRE_PROJECTILE.get(), shooter, level);
        this.curseFireDamage = curseFireDamage;
        this.shooter = shooter;
        
        // 设置投射物属性
        this.setNoGravity(true); // 无视重力
        this.setBaseDamage(0); // 基础伤害设为0，使用自定义伤害计算
        this.setPierceLevel((byte) 0); // 穿透等级
        this.setKnockback(0); // 击退效果
    }
    
    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        
        if (!this.level().isClientSide()) {
            // 播放命中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.SOUL_ESCAPE, SoundSource.NEUTRAL, 1.0F, 1.0F);
            
            // 生成咒火爆炸粒子
            spawnCurseFireExplosionParticles();
            
            // 移除投射物
            this.discard();
        }
    }
    
    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        if (!this.level().isClientSide() && hitResult.getEntity() instanceof LivingEntity target) {
            // 应用咒火伤害
            if (curseFireDamage > 0) {
                boolean damageApplied = target.hurt(target.damageSources().magic(), curseFireDamage);
                
                if (damageApplied) {
                    // 应用咒火焚烧效果
                    applyCurseFireBurningEffect(target);
                    
                    // 播放咒火命中音效
                    this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundEvents.WITHER_SKELETON_HURT, SoundSource.NEUTRAL, 0.8F, 0.6F);
                }
            }
        }
        
        super.onHitEntity(hitResult);
    }
    
    /**
     * 应用咒火焚烧效果
     */
    private void applyCurseFireBurningEffect(LivingEntity target) {
        // 咒火焚烧效果：持续5秒，每秒造成伤害
        if (ModEffects.CURSE_FIRE_BURNING != null) {
            MobEffectInstance curseFireEffect = new MobEffectInstance(ModEffects.CURSE_FIRE_BURNING.get(), 100, 0);
            target.addEffect(curseFireEffect);
        }
        
        // 生成咒火粒子效果
        for (int i = 0; i < 8; i++) {
            double offsetX = (Math.random() - 0.5) * 1.0;
            double offsetY = Math.random() * 1.5;
            double offsetZ = (Math.random() - 0.5) * 1.0;
            
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    target.getX() + offsetX,
                    target.getY() + offsetY,
                    target.getZ() + offsetZ,
                    0, 0.1, 0);
        }
        
        // 生成紫色诅咒粒子
        for (int i = 0; i < 5; i++) {
            double offsetX = (Math.random() - 0.5) * 0.8;
            double offsetY = Math.random() * 1.2;
            double offsetZ = (Math.random() - 0.5) * 0.8;
            
            this.level().addParticle(ParticleTypes.WITCH,
                    target.getX() + offsetX,
                    target.getY() + offsetY,
                    target.getZ() + offsetZ,
                    0, 0.05, 0);
        }
    }
    
    /**
     * 生成咒火爆炸粒子效果
     */
    private void spawnCurseFireExplosionParticles() {
        Vec3 center = this.position();
        
        // 生成咒火爆炸粒子
        for (int i = 0; i < 25; i++) {
            double angle = Math.random() * Math.PI * 2;
            double radius = Math.random() * 2.0;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + (Math.random() - 0.5) * 2.0;
            
            // 生成灵魂火焰粒子
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    x, y, z,
                    (Math.random() - 0.5) * 0.3,
                    Math.random() * 0.2,
                    (Math.random() - 0.5) * 0.3);
            
            // 生成紫色魔法粒子
            if (i % 3 == 0) {
                this.level().addParticle(ParticleTypes.WITCH,
                        x, y, z,
                        (Math.random() - 0.5) * 0.2,
                        Math.random() * 0.1,
                        (Math.random() - 0.5) * 0.2);
            }
        }
    }
    
    @Override
    public void tick() {
        super.tick();
        lifeTime++;
        
        // 追踪最近的敌人
        if (!this.level().isClientSide() && target == null) {
            findNearestTarget();
        }
        
        // 如果找到目标，追踪目标
        if (target != null && target.isAlive()) {
            trackTarget();
        }
        
        // 在飞行过程中生成咒火轨迹粒子
        if (this.level().isClientSide()) {
            spawnFlightParticles();
        }
        
        // 检查生命周期
        if (lifeTime > maxLifeTime) {
            if (!this.level().isClientSide()) {
                // 生命周期结束时爆炸
                this.onHit(new BlockHitResult(this.position(), Direction.UP, this.blockPosition(), false));
            }
            this.discard();
        }
    }
    
    /**
     * 寻找最近的敌人作为目标
     */
    private void findNearestTarget() {
        Vec3 center = this.position();
        AABB searchArea = new AABB(
                center.x - TRACKING_RANGE, center.y - TRACKING_RANGE, center.z - TRACKING_RANGE,
                center.x + TRACKING_RANGE, center.y + TRACKING_RANGE, center.z + TRACKING_RANGE
        );
        
        List<Entity> entitiesInRange = this.level().getEntities(null, searchArea);
        
        // 过滤出活着的敌人（非玩家，非友方）
        List<LivingEntity> enemies = entitiesInRange.stream()
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity)
                .filter(entity -> entity != shooter && entity.isAlive())
                .sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(this)))
                .toList();
        
        if (!enemies.isEmpty()) {
            target = enemies.get(0); // 选择最近的敌人
        }
    }
    
    /**
     * 追踪目标
     */
    private void trackTarget() {
        Vec3 targetPos = target.getEyePosition();
        Vec3 currentPos = this.position();
        Vec3 direction = targetPos.subtract(currentPos).normalize();
        
        // 计算追踪速度（逐渐加速）
        double speed = Math.min(0.3, 0.1 + lifeTime * 0.002);
        
        // 设置新的运动方向
        this.setDeltaMovement(direction.scale(speed));
    }
    
    /**
     * 生成飞行粒子效果
     */
    private void spawnFlightParticles() {
        Vec3 pos = this.position();
        
        // 生成咒火轨迹粒子
        for (int i = 0; i < 2; i++) {
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    pos.x + (Math.random() - 0.5) * 0.3,
                    pos.y + (Math.random() - 0.5) * 0.3,
                    pos.z + (Math.random() - 0.5) * 0.3,
                    this.getDeltaMovement().x * -0.2,
                    this.getDeltaMovement().y * -0.2,
                    this.getDeltaMovement().z * -0.2);
        }
        
        // 生成紫色魔法轨迹粒子（每3tick生成一次）
        if (this.tickCount % 3 == 0) {
            this.level().addParticle(ParticleTypes.WITCH,
                    pos.x, pos.y, pos.z,
                    this.getDeltaMovement().x * -0.1,
                    this.getDeltaMovement().y * -0.1,
                    this.getDeltaMovement().z * -0.1);
        }
    }
    
    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY; // 不允许拾取
    }
}