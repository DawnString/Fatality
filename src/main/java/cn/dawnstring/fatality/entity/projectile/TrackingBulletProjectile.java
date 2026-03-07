package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public  class TrackingBulletProjectile extends Projectile
{
    private LivingEntity target;
    private float damage;
    private Player shooter;
    private int ticksLived = 0;
    private static final int MAX_LIFETIME = 200; // 最大生存时间（10秒）
    private static final float TRACKING_STRENGTH = 0.1f; // 追踪强度
    private static final float HEAL_PERCENT = 0.01f; // 吸血百分比（1%）

    public TrackingBulletProjectile(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
        this.shooter = null;
        this.target = null;
        this.damage = 0;
    }

    public TrackingBulletProjectile(Level level, Player shooter, LivingEntity target, float damage) {
        this(ModEntities.TRACKING_BULLET.get(), level);
        this.shooter = shooter;
        this.target = target;
        this.damage = damage;
    }

    @Override
    public void tick() {
        super.tick();
        ticksLived++;

        if (ticksLived > MAX_LIFETIME || !isAlive()) {
            discard();
            return;
        }

        // 如果目标不存在或死亡，销毁子弹
        if (target == null || !target.isAlive()) {
            discard();
            return;
        }

        // 追踪目标
        if (ticksLived > 5) { // 前5个tick不追踪，让子弹先飞出去
            Vec3 targetPos = target.getEyePosition();
            Vec3 bulletPos = position();
            Vec3 direction = targetPos.subtract(bulletPos).normalize();

            // 应用追踪
            setDeltaMovement(getDeltaMovement().add(direction.scale(TRACKING_STRENGTH)).scale(0.95));
        }

        // 检查是否击中目标
        if (distanceTo(target) < 1.5) {
            onHitTarget();
            discard();
            return;
        }

        // 生成追踪粒子效果
        spawnTrackingParticles();
    }

    /**
     * 击中目标时的处理
     */
    private void onHitTarget() {
        // 造成伤害
        target.hurt(target.damageSources().playerAttack(shooter), damage);

        // 计算吸血量
        float healAmount = damage * HEAL_PERCENT;
        if (healAmount > 0) {
            float currentHealth = shooter.getHealth();
            float maxHealth = shooter.getMaxHealth();

            if (currentHealth < maxHealth) {
                shooter.setHealth(Math.min(maxHealth, currentHealth + healAmount));
            }
        }

        // 生成击中粒子效果
        spawnHitParticles();

        // 播放击中音效
        level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 0.8F, 0.6F);
    }

    /**
     * 生成追踪粒子效果
     */
    private void spawnTrackingParticles() {
        if (level().isClientSide()) {
            Vec3 pos = position();

            // 生成蓝色追踪粒子
            level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    pos.x, pos.y, pos.z,
                    (Math.random() - 0.5) * 0.02,
                    (Math.random() - 0.5) * 0.02,
                    (Math.random() - 0.5) * 0.02);

            // 生成红色血灵粒子
            if (ticksLived % 3 == 0) {
                level().addParticle(ParticleTypes.DRIPPING_LAVA,
                        pos.x, pos.y, pos.z,
                        getDeltaMovement().x * 0.1,
                        getDeltaMovement().y * 0.1,
                        getDeltaMovement().z * 0.1);
            }
        }
    }

    /**
     * 生成击中粒子效果
     */
    private void spawnHitParticles() {
        if (level().isClientSide()) {
            Vec3 hitPos = target.getEyePosition();

            // 生成爆炸粒子
            for (int i = 0; i < 20; i++) {
                level().addParticle(ParticleTypes.CRIT,
                        hitPos.x,
                        hitPos.y,
                        hitPos.z,
                        (Math.random() - 0.5) * 0.2,
                        (Math.random() - 0.5) * 0.2,
                        (Math.random() - 0.5) * 0.2);
            }

            // 生成吸血粒子（从目标到玩家）
            Vec3 shooterPos = shooter.getEyePosition();
            Vec3 direction = shooterPos.subtract(hitPos).normalize();

            for (int i = 0; i < 10; i++) {
                double progress = (double) i / 10.0;
                Vec3 particlePos = hitPos.add(direction.scale(distanceTo(shooter) * progress));

                level().addParticle(ParticleTypes.HEART,
                        particlePos.x,
                        particlePos.y,
                        particlePos.z,
                        direction.x * 0.1,
                        direction.y * 0.1,
                        direction.z * 0.1);
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        // 空实现，因为这是一个简化的投射物
    }
}
