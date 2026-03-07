package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 灾厄长矛投射物类
 * 实现长矛投掷功能，受重力影响，击中目标后爆炸，造成范围伤害
 */
public class CalamityProjectile extends AbstractArrow {
    private static final int MAX_LIFETIME = 200; // 最大生命周期10秒
    private static final float EXPLOSION_RADIUS = 5.0f; // 爆炸半径5格
    private static final float DAMAGE_REDUCTION_PER_BLOCK = 0.1f; // 每格伤害衰减10%
    
    private Player shooter;
    private ItemStack weaponStack;
    private float baseDamage;

    public CalamityProjectile(EntityType<? extends CalamityProjectile> type, Level level) {
        super(type, level);
        this.shooter = null;
        this.weaponStack = ItemStack.EMPTY;
        this.baseDamage = 0.0f;
        this.setNoGravity(false); // 受重力影响
        this.pickup = AbstractArrow.Pickup.DISALLOWED; // 不允许拾取
    }

    public CalamityProjectile(Level level, Player shooter, ItemStack weaponStack, float damage) {
        this(ModEntities.CALAMITY_PROJECTILE.get(), level);
        this.shooter = shooter;
        this.weaponStack = weaponStack;
        this.baseDamage = damage;

        // 设置投射物属性
        this.setOwner(shooter);
        this.setBaseDamage(damage);

        // 设置位置和方向
        Vec3 shooterPos = shooter.getEyePosition();
        Vec3 lookVec = shooter.getLookAngle();

        this.setPos(shooterPos.x, shooterPos.y, shooterPos.z);
        this.shoot(lookVec.x, lookVec.y, lookVec.z, 2.0f, 1.0f);
    }

    @Override
    public void tick() {
        super.tick();

        // 生成黑色粒子效果（飞行轨迹）
        if (this.level().isClientSide()) {
            spawnFlightParticles();
        }

        // 检查是否超时（10秒后消失）
        if (this.tickCount > MAX_LIFETIME) {
            this.explode();
            this.discard();
        }
    }

    /**
     * 生成飞行粒子效果
     */
    private void spawnFlightParticles() {
        // 生成黑色粒子（烟幕粒子）
        for (int i = 0; i < 2; i++) {
            this.level().addParticle(ParticleTypes.SMOKE,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                    this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                    (this.random.nextDouble() - 0.5) * 0.02,
                    (this.random.nextDouble() - 0.5) * 0.02,
                    (this.random.nextDouble() - 0.5) * 0.02);
        }
        
        // 生成灵魂火焰粒子（黑色效果）
        if (this.tickCount % 3 == 0) {
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
                    this.getY() + (this.random.nextDouble() - 0.5) * 0.2,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
                    0, 0, 0);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        
        if (!this.level().isClientSide()) {
            // 触发爆炸效果
            this.explode();
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        
        // 不伤害自己
        if (target == this.shooter) {
            return;
        }
        
        if (!this.level().isClientSide()) {
            // 对直接命中的目标造成全额伤害
            if (target instanceof LivingEntity livingTarget) {
                DamageSource damageSource = this.damageSources().playerAttack(this.shooter);
                livingTarget.hurt(damageSource, this.baseDamage);
            }
            
            // 触发爆炸效果
            this.explode();
            this.discard();
        }
    }

    /**
     * 爆炸效果 - 对周围目标造成范围伤害
     */
    private void explode() {
        if (this.level().isClientSide()) {
            return;
        }

        // 播放爆炸音效
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1.0F, 0.8F);

        // 生成爆炸粒子效果
        if (this.level().isClientSide()) {
            spawnExplosionParticles();
        }

        // 造成范围伤害
        Vec3 center = this.position();
        AABB explosionArea = new AABB(
                center.x - EXPLOSION_RADIUS, center.y - EXPLOSION_RADIUS, center.z - EXPLOSION_RADIUS,
                center.x + EXPLOSION_RADIUS, center.y + EXPLOSION_RADIUS, center.z + EXPLOSION_RADIUS
        );

        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, explosionArea)) {
            if (entity != this.shooter) {
                // 计算距离衰减伤害
                double distance = center.distanceTo(entity.position());
                float distanceMultiplier = (float) Math.max(0.1, 1.0 - (distance / EXPLOSION_RADIUS));
                float explosionDamage = this.baseDamage * 0.6f * distanceMultiplier; // 爆炸伤害为60%

                if (explosionDamage > 0) {
                    DamageSource damageSource = this.damageSources().explosion(null, this.shooter);
                    entity.hurt(damageSource, explosionDamage);
                }
            }
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
            double radius = Math.random() * EXPLOSION_RADIUS;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + (Math.random() - 0.5) * EXPLOSION_RADIUS;
            
            // 生成烟幕粒子（黑色爆炸效果）
            this.level().addParticle(ParticleTypes.SMOKE,
                    x, y, z,
                    (Math.random() - 0.5) * 0.1,
                    (Math.random() - 0.5) * 0.1,
                    (Math.random() - 0.5) * 0.1);
            
            // 生成灵魂火焰粒子
            if (i % 3 == 0) {
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        x, y, z,
                        0, 0.1, 0);
            }
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }
}