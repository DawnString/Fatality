package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.entity.VortexEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.List;

/**
 * 漩涡爆弹投射物 - 实现漩涡吸附和持续伤害功能
 */
public class VortexBombProjectile extends Projectile {
    
    private final Player owner;
    private final float baseDamage;
    private boolean hasHit = false;
    
    public VortexBombProjectile(Level level, Player owner, float damage) {
        super(EntityType.ARROW, level); // 使用箭矢实体类型作为基础
        this.owner = owner;
        this.baseDamage = damage;
        this.setNoGravity(true); // 不受重力影响
    }
    
    @Override
    protected void defineSynchedData() {
        // 不需要同步数据
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // 生成漩涡粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 3; i++) {
                this.level().addParticle(ParticleTypes.BUBBLE,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                        0, 0.1, 0);
            }
        }
        
        // 检查是否存活时间过长
        if (this.tickCount > 200) { // 10秒后消失
            this.discard();
        }
    }
    
    @Override
    protected void onHit(HitResult result) {
        if (!this.level().isClientSide() && !hasHit) {
            hasHit = true;
            
            // 创建漩涡效果
            createVortexEffect(result.getLocation());
            
            // 播放爆炸音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 0.8F, 0.9F);
            
            // 如果命中实体，造成基础伤害
            if (result.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityResult = (EntityHitResult) result;
                Entity hitEntity = entityResult.getEntity();
                
                if (hitEntity instanceof LivingEntity target && target != owner) {
                    // 对目标造成基础伤害
                    target.hurt(target.damageSources().playerAttack(owner), baseDamage);
                }
            }
            
            this.discard();
        }
    }
    
    /**
     * 创建漩涡效果
     */
    private void createVortexEffect(Vec3 position) {
        // 创建漩涡实体
        VortexEntity vortex = new VortexEntity(this.level(), owner, baseDamage * 0.5f);
        vortex.setPos(position.x, position.y, position.z);
        this.level().addFreshEntity(vortex);
        
        // 播放漩涡生成音效
        this.level().playSound(null, position.x, position.y, position.z,
                SoundEvents.WATER_AMBIENT, SoundSource.HOSTILE, 1.0F, 0.6F);
    }
}