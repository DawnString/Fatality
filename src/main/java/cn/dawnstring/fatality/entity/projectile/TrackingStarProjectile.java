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

public class TrackingStarProjectile extends Projectile
{
    private LivingEntity target;
    private float damage;
    private Player shooter;
    private int ticksLived = 0;
    private static final int MAX_LIFETIME = 120; // 6秒最大生存时间
    private static final float TRACKING_STRENGTH = 0.08f; // 追踪强度

    public TrackingStarProjectile(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
        this.shooter = null;
        this.target = null;
        this.damage = 0;
    }

    public TrackingStarProjectile(Level level, Player shooter, LivingEntity target, float damage) {
        this(ModEntities.TRACKING_STAR.get(), level);
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

        // 如果目标不存在或死亡，销毁星星弹幕
        if (target == null || !target.isAlive()) {
            discard();
            return;
        }

        // 追踪目标
        if (ticksLived > 3) { // 前3个tick不追踪，让星星先飞出去
            Vec3 targetPos = target.getEyePosition();
            Vec3 starPos = position();
            Vec3 direction = targetPos.subtract(starPos).normalize();

            // 应用追踪
            setDeltaMovement(getDeltaMovement().add(direction.scale(TRACKING_STRENGTH)).scale(0.97));
        }

        // 检查是否击中目标
        if (distanceTo(target) < 1.8) {
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

        // 生成击中粒子效果
        spawnHitParticles();

        // 播放击中音效
        level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.8F, 0.8F);
    }

    /**
     * 生成追踪粒子效果
     */
    private void spawnTrackingParticles() {
        if (level().isClientSide()) {
            Vec3 pos = position();

            // 生成星星主体粒子
            level().addParticle(ParticleTypes.END_ROD,
                    pos.x, pos.y, pos.z,
                    (Math.random() - 0.5) * 0.01,
                    (Math.random() - 0.5) * 0.01,
                    (Math.random() - 0.5) * 0.01);

            // 生成闪烁光晕粒子
            if (ticksLived % 4 == 0) {
                level().addParticle(ParticleTypes.GLOW,
                        pos.x + (Math.random() - 0.5) * 0.3,
                        pos.y + (Math.random() - 0.5) * 0.3,
                        pos.z + (Math.random() - 0.5) * 0.3,
                        0, 0.05, 0);
            }

            // 生成追踪轨迹粒子
            if (ticksLived % 2 == 0) {
                level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        pos.x - getDeltaMovement().x * 0.2,
                        pos.y - getDeltaMovement().y * 0.2,
                        pos.z - getDeltaMovement().z * 0.2,
                        0, 0, 0);
            }
        }
    }

    /**
     * 生成击中粒子效果
     */
    private void spawnHitParticles() {
        if (level().isClientSide()) {
            Vec3 hitPos = target.getEyePosition();

            // 生成星星爆炸粒子
            for (int i = 0; i < 15; i++) {
                level().addParticle(ParticleTypes.GLOW_SQUID_INK,
                        hitPos.x,
                        hitPos.y,
                        hitPos.z,
                        (Math.random() - 0.5) * 0.3,
                        (Math.random() - 0.5) * 0.3,
                        (Math.random() - 0.5) * 0.3);
            }

            // 生成星光粒子
            for (int i = 0; i < 10; i++) {
                level().addParticle(ParticleTypes.END_ROD,
                        hitPos.x,
                        hitPos.y,
                        hitPos.z,
                        (Math.random() - 0.5) * 0.4,
                        (Math.random() - 0.5) * 0.4,
                        (Math.random() - 0.5) * 0.4);
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        // 空实现，因为这是一个简化的投射物
    }
}