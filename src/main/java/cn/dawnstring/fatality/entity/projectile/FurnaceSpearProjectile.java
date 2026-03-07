package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.registry.ModEffects;
import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;

/**
 * 熔炉长矛投射物 - 投掷出的火焰长矛
 * 特性：命中时造成灼烧效果，持续伤害
 */
public class FurnaceSpearProjectile extends AbstractArrow
{
    private final float spearDamage;
    private final Player shooter;
    private final ItemStack weaponItem;
    private int lifeTime = 0;
    private final int maxLifeTime = 200; // 10秒生命周期
    
    public FurnaceSpearProjectile(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        this.spearDamage = 0;
        this.shooter = null;
        this.weaponItem = ItemStack.EMPTY;
    }
    
    public FurnaceSpearProjectile(Level level, Player shooter, ItemStack weaponItem, float spearDamage) {
        super(ModEntities.FURNACE_SPEAR_PROJECTILE.get(), shooter, level);
        this.spearDamage = spearDamage;
        this.shooter = shooter;
        this.weaponItem = weaponItem;
        
        // 设置投射物属性
        this.setNoGravity(true); // 无视重力
        this.setBaseDamage(0); // 基础伤害设为0，使用自定义伤害计算
        this.setPierceLevel((byte) 0); // 穿透等级
        this.setKnockback(0); // 击退效果
    }
    
    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        
        if (!this.level().isClientSide) {
            // 播放命中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 1.0F, 1.0F);
            
            // 生成火焰爆炸粒子
            for (int i = 0; i < 8; i++) {
                double offsetX = (Math.random() - 0.5) * 0.5;
                double offsetY = (Math.random() - 0.5) * 0.5;
                double offsetZ = (Math.random() - 0.5) * 0.5;
                
                this.level().addParticle(ParticleTypes.FLAME,
                        this.getX() + offsetX,
                        this.getY() + offsetY,
                        this.getZ() + offsetZ,
                        0, 0, 0);
            }
            
            // 移除投射物
            this.discard();
        }
    }
    
    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        if (!this.level().isClientSide && hitResult.getEntity() instanceof LivingEntity target) {
            // 应用伤害
            if (spearDamage > 0) {
                float effectiveDamage = Math.max(0.5f, spearDamage);
                
                // 使用正确的伤害源
                boolean damageApplied = target.hurt(target.damageSources().arrow(this, shooter), effectiveDamage);
                
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
     * 应用灼烧效果
     */
    private void applyBurnEffect(LivingEntity target) {
        // 灼烧效果：持续5秒，每秒造成伤害
        MobEffectInstance burnEffect = new MobEffectInstance(ModEffects.BURN.get(), 100, 0);
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
    
    @Override
    public void tick() {
        super.tick();
        lifeTime++;
        
        // 在飞行过程中生成火焰轨迹粒子
        if (this.level().isClientSide && this.tickCount % 2 == 0) {
            this.level().addParticle(ParticleTypes.FLAME,
                    this.getX(), this.getY(), this.getZ(),
                    0, 0, 0);
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