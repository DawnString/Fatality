package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.registry.ModEffects;
import cn.dawnstring.fatality.registry.ModEntities;
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
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * 日蚀熔炉长矛投射物 - EclipseFurnaceSpear的专属投射物
 * 特性：更强的灼烧效果、更大的爆炸范围、更华丽的火焰特效
 */
public class EclipseFurnaceSpearProjectile extends AbstractArrow
{
    private final float spearDamage;
    private final Player shooter;
    private final ItemStack weaponItem;
    private int lifeTime = 0;
    private final int maxLifeTime = 300; // 15秒生命周期（比基础版更长）
    
    public EclipseFurnaceSpearProjectile(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        this.spearDamage = 0;
        this.shooter = null;
        this.weaponItem = ItemStack.EMPTY;
    }
    
    public EclipseFurnaceSpearProjectile(Level level, Player shooter, ItemStack weaponItem, float spearDamage) {
        super(ModEntities.ECLIPSE_FURNACE_SPEAR_PROJECTILE.get(), shooter, level);
        this.spearDamage = spearDamage;
        this.shooter = shooter;
        this.weaponItem = weaponItem;
        
        // 设置投射物属性
        this.setNoGravity(true); // 无视重力
        this.setBaseDamage(0); // 基础伤害设为0，使用自定义伤害计算
        this.setPierceLevel((byte) 1); // 穿透等级提升到1（可穿透一个敌人）
        this.setKnockback(1); // 轻微击退效果
    }
    
    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        
        if (!this.level().isClientSide) {
            // 播放日蚀火焰爆炸音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.FIRECHARGE_USE, SoundSource.NEUTRAL, 1.5F, 0.8F);
            
            // 生成日蚀火焰爆炸粒子（更大范围）
            for (int i = 0; i < 15; i++) {
                double offsetX = (Math.random() - 0.5) * 1.0;
                double offsetY = (Math.random() - 0.5) * 1.0;
                double offsetZ = (Math.random() - 0.5) * 1.0;
                
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX() + offsetX,
                        this.getY() + offsetY,
                        this.getZ() + offsetZ,
                        0, 0, 0);
            }
            
            // 生成额外的火焰粒子
            for (int i = 0; i < 10; i++) {
                double offsetX = (Math.random() - 0.5) * 1.5;
                double offsetY = (Math.random() - 0.5) * 1.5;
                double offsetZ = (Math.random() - 0.5) * 1.5;
                
                this.level().addParticle(ParticleTypes.FLAME,
                        this.getX() + offsetX,
                        this.getY() + offsetY,
                        this.getZ() + offsetZ,
                        0, 0.1, 0);
            }
            
            // 移除投射物
            this.discard();
        }
    }
    
    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        if (!this.level().isClientSide && hitResult.getEntity() instanceof LivingEntity target) {
            // 应用伤害（日蚀版伤害更高）
            if (spearDamage > 0) {
                float effectiveDamage = Math.max(0.5f, spearDamage * 1.2f); // 伤害提升20%
                
                // 使用正确的伤害源
                boolean damageApplied = target.hurt(target.damageSources().arrow(this, shooter), effectiveDamage);
                
                if (damageApplied) {
                    // 应用日蚀灼烧效果（更强）
                    applyEclipseBurnEffect(target);
                    
                    // 播放日蚀火焰命中音效
                    this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundEvents.BLAZE_HURT, SoundSource.NEUTRAL, 0.8F, 0.9F);
                    
                    // 对周围敌人造成溅射伤害
                    applySplashDamage(target);
                }
            }
        }
        
        super.onHitEntity(hitResult);
    }
    
    /**
     * 应用日蚀灼烧效果（更强版本）
     */
    private void applyEclipseBurnEffect(LivingEntity target) {
        // 日蚀灼烧效果：持续8秒，每秒造成伤害，等级更高
        MobEffectInstance burnEffect = new MobEffectInstance(ModEffects.BURN.get(), 160, 1); // 等级2，持续时间更长
        target.addEffect(burnEffect);
        
        // 生成日蚀火焰粒子效果
        for (int i = 0; i < 8; i++) {
            double offsetX = (Math.random() - 0.5) * 1.5;
            double offsetY = Math.random() * 2.0;
            double offsetZ = (Math.random() - 0.5) * 1.5;
            
            // 灵魂火焰粒子
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    target.getX() + offsetX,
                    target.getY() + offsetY,
                    target.getZ() + offsetZ,
                    0, 0.15, 0);
            
            // 普通火焰粒子
            this.level().addParticle(ParticleTypes.FLAME,
                    target.getX() + offsetX * 0.8,
                    target.getY() + offsetY * 0.8,
                    target.getZ() + offsetZ * 0.8,
                    0, 0.1, 0);
        }
    }
    
    /**
     * 对周围敌人造成溅射伤害
     */
    private void applySplashDamage(LivingEntity mainTarget) {
        // 获取周围3格内的所有实体
        for (Entity entity : this.level().getEntities(mainTarget, mainTarget.getBoundingBox().inflate(3.0))) {
            if (entity instanceof LivingEntity nearbyTarget && nearbyTarget != mainTarget) {
                // 对周围敌人造成50%的溅射伤害
                float splashDamage = spearDamage * 0.5f;
                if (splashDamage > 0) {
                    boolean damageApplied = nearbyTarget.hurt(nearbyTarget.damageSources().arrow(this, shooter), splashDamage);
                    
                    if (damageApplied) {
                        // 对周围敌人也应用灼烧效果（等级较低）
                        MobEffectInstance splashBurnEffect = new MobEffectInstance(ModEffects.BURN.get(), 80, 0);
                        nearbyTarget.addEffect(splashBurnEffect);
                        
                        // 生成溅射粒子效果
                        this.level().addParticle(ParticleTypes.FLAME,
                                nearbyTarget.getX(),
                                nearbyTarget.getY() + 1.0,
                                nearbyTarget.getZ(),
                                0, 0.1, 0);
                    }
                }
            }
        }
    }
    
    @Override
    public void tick() {
        super.tick();
        lifeTime++;
        
        // 在飞行过程中生成日蚀火焰轨迹粒子（更华丽）
        if (this.level().isClientSide) {
            // 每帧都生成粒子，创造更密集的轨迹
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX(), this.getY(), this.getZ(),
                    0, 0, 0);
            
            // 额外生成普通火焰粒子
            if (this.tickCount % 2 == 0) {
                this.level().addParticle(ParticleTypes.FLAME,
                        this.getX(), this.getY(), this.getZ(),
                        0, 0, 0);
            }
            
            // 生成火花粒子效果
            if (this.tickCount % 3 == 0) {
                this.level().addParticle(ParticleTypes.LAVA,
                        this.getX() + (Math.random() - 0.5) * 0.3,
                        this.getY() + (Math.random() - 0.5) * 0.3,
                        this.getZ() + (Math.random() - 0.5) * 0.3,
                        0, 0.05, 0);
            }
        }
        
        // 检查生命周期结束
        if (lifeTime >= maxLifeTime) {
            this.discard();
        }
    }
    
    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY; // 无法拾取
    }
}