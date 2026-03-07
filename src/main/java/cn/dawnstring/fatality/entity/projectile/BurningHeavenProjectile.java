package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

import java.util.List;

/**
 * 燃烧天堂长矛投射物 - 投掷出的天堂火焰长矛
 * 特性：无视重力，击中目标后产生爆炸效果，对周围敌人造成范围伤害
 */
public class BurningHeavenProjectile extends AbstractArrow
{
    private final float spearDamage;
    private final Player shooter;
    private int lifeTime = 0;
    private final int maxLifeTime = 200; // 10秒生命周期
    private static final float EXPLOSION_RADIUS = 5.0f; // 爆炸半径5格
    
    public BurningHeavenProjectile(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        this.spearDamage = 0;
        this.shooter = null;
    }
    
    public BurningHeavenProjectile(Level level, Player shooter, float spearDamage) {
        super(ModEntities.BURNING_HEAVEN_PROJECTILE.get(), shooter, level);
        this.spearDamage = spearDamage;
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
            // 播放爆炸音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 1.0F, 1.0F);
            
            // 生成爆炸粒子效果
            spawnExplosionParticles();
            
            // 对周围敌人造成范围伤害
            createExplosionDamage();
            
            // 移除投射物
            this.discard();
        }
    }
    
    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        if (!this.level().isClientSide() && hitResult.getEntity() instanceof LivingEntity target) {
            // 应用直接伤害
            if (spearDamage > 0) {
                float directDamage = spearDamage * 0.8f; // 直接伤害为80%
                
                // 使用正确的伤害源
                boolean damageApplied = target.hurt(target.damageSources().arrow(this, shooter), directDamage);
                
                if (damageApplied) {
                    // 应用灼烧效果
                    applyBurnEffect(target);
                    
                    // 播放火焰命中音效
                    this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundEvents.BLAZE_HURT, SoundSource.NEUTRAL, 0.5F, 1.0F);
                }
            }
        }
        
        super.onHitEntity(hitResult);
    }
    
    /**
     * 创建爆炸范围伤害
     */
    private void createExplosionDamage() {
        Vec3 center = this.position();
        AABB explosionArea = new AABB(
                center.x - EXPLOSION_RADIUS, center.y - EXPLOSION_RADIUS, center.z - EXPLOSION_RADIUS,
                center.x + EXPLOSION_RADIUS, center.y + EXPLOSION_RADIUS, center.z + EXPLOSION_RADIUS
        );
        
        List<Entity> entitiesInRange = this.level().getEntities(null, explosionArea);
        
        for (Entity entity : entitiesInRange) {
            if (entity instanceof LivingEntity target && entity != shooter) {
                // 计算距离衰减伤害
                double distance = center.distanceTo(entity.position());
                float distanceMultiplier = (float) Math.max(0.1, 1.0 - (distance / EXPLOSION_RADIUS));
                float explosionDamage = spearDamage * 0.6f * distanceMultiplier; // 爆炸伤害为60%
                
                if (explosionDamage > 0) {
                    DamageSource damageSource = target.damageSources().explosion(null, shooter);
                    target.hurt(damageSource, explosionDamage);
                    
                    // 对爆炸范围内的敌人应用灼烧效果
                    applyBurnEffect(target);
                }
            }
        }
    }
    
    /**
     * 应用灼烧效果
     */
    private void applyBurnEffect(LivingEntity target) {
        // 灼烧效果：持续5秒，每秒造成伤害
        MobEffectInstance burnEffect = new MobEffectInstance(MobEffects.WITHER, 100, 0); // 使用凋零效果模拟灼烧
        target.addEffect(burnEffect);
        
        // 生成火焰粒子效果
        for (int i = 0; i < 5; i++) {
            double offsetX = (Math.random() - 0.5) * 1.0;
            double offsetY = Math.random() * 1.5;
            double offsetZ = (Math.random() - 0.5) * 1.0;
            
            this.level().addParticle(ParticleTypes.FLAME,
                    target.getX() + offsetX,
                    target.getY() + offsetY,
                    target.getZ() + offsetZ,
                    0, 0.1, 0);
        }
    }
    
    /**
     * 生成爆炸粒子效果
     */
    private void spawnExplosionParticles() {
        Vec3 center = this.position();
        
        // 生成爆炸粒子
        for (int i = 0; i < 30; i++) {
            double angle = Math.random() * Math.PI * 2;
            double radius = Math.random() * EXPLOSION_RADIUS;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + (Math.random() - 0.5) * EXPLOSION_RADIUS;
            
            // 生成火焰粒子
            this.level().addParticle(ParticleTypes.FLAME,
                    x, y, z,
                    (Math.random() - 0.5) * 0.2,
                    Math.random() * 0.1,
                    (Math.random() - 0.5) * 0.2);
            
            // 生成烟雾粒子
            this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                    x, y, z,
                    (Math.random() - 0.5) * 0.1,
                    Math.random() * 0.05,
                    (Math.random() - 0.5) * 0.1);
        }
        
        // 生成冲击波粒子
        for (int ring = 1; ring <= 3; ring++) {
            for (int i = 0; i < 12; i++) {
                double angle = (double) i / 12.0 * Math.PI * 2;
                double x = center.x + Math.cos(angle) * ring;
                double z = center.z + Math.sin(angle) * ring;
                double y = center.y;
                
                this.level().addParticle(ParticleTypes.CRIT,
                        x, y, z,
                        Math.cos(angle) * 0.1,
                        0.1,
                        Math.sin(angle) * 0.1);
            }
        }
    }
    
    @Override
    public void tick() {
        super.tick();
        lifeTime++;
        
        // 在飞行过程中生成火焰轨迹粒子
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
     * 生成飞行粒子效果
     */
    private void spawnFlightParticles() {
        Vec3 pos = this.position();
        
        // 生成火焰轨迹粒子
        for (int i = 0; i < 3; i++) {
            this.level().addParticle(ParticleTypes.FLAME,
                    pos.x + (Math.random() - 0.5) * 0.2,
                    pos.y + (Math.random() - 0.5) * 0.2,
                    pos.z + (Math.random() - 0.5) * 0.2,
                    this.getDeltaMovement().x * -0.1,
                    this.getDeltaMovement().y * -0.1,
                    this.getDeltaMovement().z * -0.1);
        }
        
        // 生成天堂光芒粒子（每5tick生成一次）
        if (this.tickCount % 5 == 0) {
            this.level().addParticle(ParticleTypes.GLOW,
                    pos.x, pos.y, pos.z,
                    0, 0, 0);
        }
    }
    
    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY; // 不允许拾取
    }
}