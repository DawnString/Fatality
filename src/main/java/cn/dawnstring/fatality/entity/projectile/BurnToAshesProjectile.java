package cn.dawnstring.fatality.entity.projectile;

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
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 焚尽火球投射物类
 * 实现火球飞行功能，击中目标后施加灵火灼烧效果
 */
public class BurnToAshesProjectile extends AbstractArrow {
    private final float fireballDamage;
    private final Player shooter;
    private int lifeTime = 0;
    private final int maxLifeTime = 200; // 10秒生命周期
    
    public BurnToAshesProjectile(Level level, Player shooter, float damage) {
        super(EntityType.ARROW, level);
        this.shooter = shooter;
        this.fireballDamage = damage;
        
        // 设置投射物属性
        this.setOwner(shooter);
        this.setBaseDamage(damage);
        this.setNoGravity(true); // 无视重力
        this.pickup = AbstractArrow.Pickup.DISALLOWED; // 不允许拾取
        
        // 设置位置和方向
        Vec3 shooterPos = shooter.getEyePosition();
        Vec3 lookVec = shooter.getLookAngle();
        
        this.setPos(shooterPos.x, shooterPos.y, shooterPos.z);
        this.shoot(lookVec.x, lookVec.y, lookVec.z, 2.0f, 1.0f);
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // 生成飞行粒子效果
        if (this.level().isClientSide()) {
            spawnFlightParticles();
        }
        
        // 检查是否超时（10秒后消失）
        if (this.tickCount > maxLifeTime) {
            this.discard();
        }
    }
    
    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        
        if (!this.level().isClientSide()) {
            // 处理击中逻辑
            if (result.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityResult = (EntityHitResult) result;
                Entity target = entityResult.getEntity();
                
                if (target instanceof LivingEntity) {
                    // 应用灵火灼烧效果
                    applySpiritualFireBurn((LivingEntity) target);
                }
            }
            
            // 播放爆炸音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 0.8F);
            
            // 生成爆炸粒子效果
            spawnExplosionParticles();
            
            // 销毁投射物
            this.discard();
        }
    }
    
    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        if (!this.level().isClientSide() && hitResult.getEntity() instanceof LivingEntity target) {
            // 应用直接伤害
            if (fireballDamage > 0) {
                float directDamage = fireballDamage * 0.8f; // 直接伤害为80%
                
                // 使用正确的伤害源
                boolean damageApplied = target.hurt(target.damageSources().arrow(this, shooter), directDamage);
                
                if (damageApplied) {
                    // 应用灵火灼烧效果
                    applySpiritualFireBurn(target);
                    
                    // 播放火焰命中音效
                    this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundEvents.BLAZE_HURT, SoundSource.NEUTRAL, 0.5F, 1.0F);
                }
            }
        }
        
        super.onHitEntity(hitResult);
    }
    
    /**
     * 应用灵火灼烧效果
     */
    private void applySpiritualFireBurn(LivingEntity target) {
        // 灵火灼烧效果：持续5秒，每2秒造成一次伤害，并阻止生命恢复
        MobEffectInstance spiritualFireBurn = new MobEffectInstance(
                MobEffects.WITHER, // 使用凋零效果模拟灵火灼烧
                100, // 5秒持续时间（20tick/秒）
                1, // 等级2
                false, // 不显示粒子
                true // 显示图标
        );
        
        // 添加额外的伤害效果
        MobEffectInstance fireDamage = new MobEffectInstance(
                MobEffects.HARM, // 伤害效果
                5, // 立即生效
                0, // 等级1
                false, // 不显示粒子
                false // 不显示图标
        );
        
        // 阻止生命恢复
        MobEffectInstance noRegeneration = new MobEffectInstance(
                MobEffects.WEAKNESS, // 虚弱效果，阻止自然恢复
                100, // 5秒持续时间
                0, // 等级1
                false, // 不显示粒子
                false // 不显示图标
        );
        
        target.addEffect(spiritualFireBurn);
        target.addEffect(fireDamage);
        target.addEffect(noRegeneration);
        
        // 生成灵火灼烧粒子效果
        if (this.level().isClientSide()) {
            spawnSpiritualFireParticles(target);
        }
    }
    
    /**
     * 生成飞行粒子效果
     */
    private void spawnFlightParticles() {
        Vec3 pos = this.position();
        
        // 生成火焰粒子
        for (int i = 0; i < 3; i++) {
            this.level().addParticle(ParticleTypes.FLAME,
                    pos.x + (Math.random() - 0.5) * 0.2,
                    pos.y + (Math.random() - 0.5) * 0.2,
                    pos.z + (Math.random() - 0.5) * 0.2,
                    (Math.random() - 0.5) * 0.02,
                    (Math.random() - 0.5) * 0.02,
                    (Math.random() - 0.5) * 0.02);
        }
        
        // 生成灵魂火焰粒子（灵火特效）
        if (this.tickCount % 3 == 0) {
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    pos.x, pos.y, pos.z,
                    this.getDeltaMovement().x * -0.1,
                    this.getDeltaMovement().y * -0.1,
                    this.getDeltaMovement().z * -0.1);
        }
        
        // 生成发光粒子（灵火特效）
        if (this.tickCount % 5 == 0) {
            this.level().addParticle(ParticleTypes.GLOW,
                    pos.x, pos.y, pos.z,
                    0, 0.05, 0);
        }
    }
    
    /**
     * 生成爆炸粒子效果
     */
    private void spawnExplosionParticles() {
        Vec3 center = this.position();
        
        // 生成爆炸粒子
        for (int i = 0; i < 20; i++) {
            double angle = Math.random() * Math.PI * 2;
            double radius = Math.random() * 2.0;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + (Math.random() - 0.5) * 2.0;
            
            // 生成火焰粒子
            this.level().addParticle(ParticleTypes.FLAME,
                    x, y, z,
                    (Math.random() - 0.5) * 0.2,
                    Math.random() * 0.1,
                    (Math.random() - 0.5) * 0.2);
            
            // 生成灵魂火焰粒子
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    x, y, z,
                    (Math.random() - 0.5) * 0.1,
                    Math.random() * 0.05,
                    (Math.random() - 0.5) * 0.1);
        }
        
        // 生成冲击波粒子
        for (int ring = 1; ring <= 2; ring++) {
            for (int i = 0; i < 8; i++) {
                double angle = (double) i / 8.0 * Math.PI * 2;
                double x = center.x + Math.cos(angle) * ring;
                double z = center.z + Math.sin(angle) * ring;
                double y = center.y;
                
                this.level().addParticle(ParticleTypes.GLOW,
                        x, y, z,
                        Math.cos(angle) * 0.1,
                        0.1,
                        Math.sin(angle) * 0.1);
            }
        }
    }
    
    /**
     * 生成灵火灼烧粒子效果
     */
    private void spawnSpiritualFireParticles(LivingEntity target) {
        Vec3 targetPos = target.position();
        
        // 在目标周围生成灵火粒子
        for (int i = 0; i < 15; i++) {
            double x = targetPos.x + (Math.random() - 0.5) * target.getBbWidth();
            double y = targetPos.y + Math.random() * target.getBbHeight();
            double z = targetPos.z + (Math.random() - 0.5) * target.getBbWidth();
            
            // 生成灵魂火焰粒子
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    x, y, z,
                    (Math.random() - 0.5) * 0.05,
                    Math.random() * 0.05,
                    (Math.random() - 0.5) * 0.05);
            
            // 生成发光粒子
            this.level().addParticle(ParticleTypes.GLOW,
                    x, y, z,
                    0, 0.02, 0);
        }
    }
    
    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY; // 不允许拾取
    }
}