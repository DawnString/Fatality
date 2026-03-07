package cn.dawnstring.fatality.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 末影飞刀投射物 - 实现分裂功能
 */
public class EnderFlyingKnifeProjectile extends Projectile {
    
    private final Player owner;
    private final float damage;
    private final int splitLevel; // 分裂等级（0为初始，1为第一次分裂，2为第二次分裂）
    private boolean hasSplit = false;
    
    public EnderFlyingKnifeProjectile(Level level, Player owner, float damage, int splitLevel) {
        super(EntityType.ARROW, level); // 使用箭矢实体类型作为基础
        this.owner = owner;
        this.damage = damage;
        this.splitLevel = splitLevel;
        this.setNoGravity(true); // 不受重力影响
    }
    
    @Override
    protected void defineSynchedData() {
        // 不需要同步数据
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // 生成末影粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 2; i++) {
                this.level().addParticle(ParticleTypes.PORTAL,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.1,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.1,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.1,
                        0, 0, 0);
            }
        }
        
        // 检查是否飞行了10格（分裂条件）
        if (!hasSplit && splitLevel == 0 && this.tickCount > 20) { // 大约飞行10格后分裂
            double distanceTraveled = this.position().distanceTo(new Vec3(
                    this.xo, this.yo, this.zo));
            
            if (distanceTraveled >= 10.0) {
                splitIntoThreeKnives();
                hasSplit = true;
            }
        }
        
        // 检查是否存活时间过长
        if (this.tickCount > 100) { // 5秒后消失
            this.discard();
        }
    }
    
    @Override
    protected void onHit(HitResult result) {
        if (!this.level().isClientSide()) {
            if (result.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityResult = (EntityHitResult) result;
                Entity hitEntity = entityResult.getEntity();
                
                if (hitEntity instanceof LivingEntity target && target != owner) {
                    // 对目标造成伤害
                    target.hurt(target.damageSources().playerAttack(owner), damage);
                    
                    // 播放命中音效
                    this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundEvents.ENDERMAN_HURT, SoundSource.HOSTILE, 0.8F, 1.2F);
                }
            }
            
            this.discard();
        }
    }
    
    /**
     * 分裂为3把新飞刀
     */
    private void splitIntoThreeKnives() {
        if (splitLevel >= 1) return; // 只允许分裂一次
        
        Vec3 currentPos = this.position();
        Vec3 currentDirection = this.getDeltaMovement().normalize();
        
        // 分裂为3把新飞刀，每把伤害为原伤害的70%
        float splitDamage = damage * 0.7f;
        
        for (int i = 0; i < 3; i++) {
            // 计算分裂方向（锥形散布）
            Vec3 splitDirection = calculateSplitDirection(currentDirection, i);
            
            // 创建新飞刀投射物
            EnderFlyingKnifeProjectile newKnife = new EnderFlyingKnifeProjectile(
                    this.level(), owner, splitDamage, splitLevel + 1);
            
            newKnife.setPos(currentPos.x, currentPos.y, currentPos.z);
            newKnife.setDeltaMovement(splitDirection.scale(2.0)); // 保持速度
            
            // 添加到世界
            this.level().addFreshEntity(newKnife);
        }
        
        // 播放分裂音效
        this.level().playSound(null, currentPos.x, currentPos.y, currentPos.z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.6F, 1.5F);
        
        // 播放分裂粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 15; i++) {
                this.level().addParticle(ParticleTypes.PORTAL,
                        currentPos.x + (this.random.nextDouble() - 0.5) * 0.5,
                        currentPos.y + (this.random.nextDouble() - 0.5) * 0.5,
                        currentPos.z + (this.random.nextDouble() - 0.5) * 0.5,
                        (this.random.nextDouble() - 0.5) * 0.2,
                        (this.random.nextDouble() - 0.5) * 0.2,
                        (this.random.nextDouble() - 0.5) * 0.2);
            }
        }
        
        this.discard();
    }
    
    /**
     * 计算分裂方向
     */
    private Vec3 calculateSplitDirection(Vec3 baseDirection, int index) {
        // 基础方向
        Vec3 direction = baseDirection.normalize();
        
        // 计算散布角度（30度锥形）
        double spreadAngle = Math.toRadians(30);
        
        // 计算每个飞刀的偏移角度
        double angleOffset = (index - 1) * spreadAngle / 2;
        
        // 计算旋转后的方向
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = direction.cross(up).normalize();
        
        // 绕右向量旋转
        double cosAngle = Math.cos(angleOffset);
        double sinAngle = Math.sin(angleOffset);
        
        Vec3 rotatedDirection = direction.scale(cosAngle)
                .add(up.cross(direction).normalize().scale(sinAngle));
        
        return rotatedDirection.normalize();
    }
}