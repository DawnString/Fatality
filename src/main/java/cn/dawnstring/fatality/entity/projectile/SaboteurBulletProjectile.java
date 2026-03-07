package cn.dawnstring.fatality.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
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
 * 破坏者子弹投射物
 */
public class SaboteurBulletProjectile extends AbstractArrow {
    
    private final float damage;
    private final Player owner;
    private int lifeTicks = 0;
    private static final int MAX_LIFE_TICKS = 100; // 最大生存时间5秒
    
    public SaboteurBulletProjectile(Level level, Player owner, float damage) {
        super(EntityType.ARROW, level);
        this.damage = damage;
        this.owner = owner;
        
        // 设置投射物属性
        this.setNoGravity(true); // 不受重力影响
        this.setPierceLevel((byte) 0); // 不穿透
        this.setBaseDamage(damage);
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // 生成粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 2; i++) {
                this.level().addParticle(ParticleTypes.SMOKE,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.1,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.1,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.1,
                        0, 0, 0);
            }
        }
        
        // 检查生存时间
        lifeTicks++;
        if (lifeTicks > MAX_LIFE_TICKS) {
            this.discard();
            return;
        }
        
        // 检查是否击中方块或超出世界边界
        if (this.horizontalCollision || this.verticalCollision || this.isInWall()) {
            this.discard();
        }
    }
    
    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        
        if (!this.level().isClientSide()) {
            // 播放命中音效和粒子效果
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE, 
                    net.minecraft.sounds.SoundSource.NEUTRAL, 0.3F, 1.5F);
            
            // 生成爆炸粒子
            for (int i = 0; i < 5; i++) {
                this.level().addParticle(ParticleTypes.EXPLOSION,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.5,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                        0, 0, 0);
            }
            
            this.discard();
        }
    }
    
    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        
        // 不伤害自己
        if (target == owner) {
            return;
        }
        
        // 计算伤害
        float finalDamage = damage;
        
        // 应用伤害
        if (target instanceof LivingEntity) {
            LivingEntity livingTarget = (LivingEntity) target;
            
            // 创建伤害源
            DamageSource damageSource = this.damageSources().arrow(this, owner);
            
            // 造成伤害
            if (livingTarget.hurt(damageSource, finalDamage)) {
                // 播放伤害音效
                this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                        net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE, 
                        net.minecraft.sounds.SoundSource.NEUTRAL, 0.5F, 1.0F);
                
                // 生成伤害粒子
                for (int i = 0; i < 3; i++) {
                    this.level().addParticle(ParticleTypes.CRIT,
                            target.getX() + (this.random.nextDouble() - 0.5) * target.getBbWidth(),
                            target.getY() + this.random.nextDouble() * target.getBbHeight(),
                            target.getZ() + (this.random.nextDouble() - 0.5) * target.getBbWidth(),
                            0, 0.1, 0);
                }
            }
        }
        
        super.onHitEntity(result);
    }
    
    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY; // 不能拾取
    }
    
    @Override
    public boolean isNoGravity() {
        return true; // 不受重力影响
    }
}