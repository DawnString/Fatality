package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 光暗追踪投射物 - 用于BookOfLightAndDarkness的追踪粒子攻击
 * 特性：根据光暗类型生成不同颜色的粒子、追踪最近的敌人、造成光暗伤害
 */
public class LightDarkTrackingProjectile extends Projectile
{
    private LivingEntity target;
    private float damage;
    private Player shooter;
    private boolean isLight;
    private int ticksLived = 0;
    private static final int MAX_LIFETIME = 200; // 最大生存时间（10秒）
    private static final float TRACKING_STRENGTH = 0.15f; // 追踪强度
    private static final double TRACKING_RANGE = 15.0; // 追踪范围

    public LightDarkTrackingProjectile(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
        this.shooter = null;
        this.target = null;
        this.damage = 0;
        this.isLight = true;
    }

    public LightDarkTrackingProjectile(Level level, Player shooter, LivingEntity target, float damage, boolean isLight) {
        this(ModEntities.LIGHT_DARK_TRACKING_PROJECTILE.get(), level);
        this.shooter = shooter;
        this.target = target;
        this.damage = damage;
        this.isLight = isLight;
        this.setNoGravity(true); // 无视重力
    }

    @Override
    public void tick() {
        super.tick();
        ticksLived++;

        // 生命周期检查
        if (ticksLived > MAX_LIFETIME || !isAlive()) {
            discard();
            return;
        }

        // 如果目标不存在或死亡，寻找新目标
        if (target == null || !target.isAlive()) {
            findNewTarget();
            if (target == null) {
                discard();
                return;
            }
        }

        // 追踪目标
        if (ticksLived > 3) { // 前3个tick不追踪，让投射物先飞出去
            Vec3 targetPos = target.getEyePosition();
            Vec3 projectilePos = position();
            Vec3 direction = targetPos.subtract(projectilePos).normalize();

            // 应用追踪
            setDeltaMovement(getDeltaMovement().add(direction.scale(TRACKING_STRENGTH)).scale(0.95));
        }

        // 检查是否击中目标
        if (target != null && distanceTo(target) < 1.5) {
            onHitTarget();
            discard();
            return;
        }

        // 生成追踪粒子效果
        spawnTrackingParticles();
    }

    /**
     * 寻找新目标
     */
    private void findNewTarget() {
        if (this.shooter == null) return;

        Vec3 currentPos = this.position();
        
        // 获取范围内的所有生物
        var entities = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(TRACKING_RANGE));

        LivingEntity closestTarget = null;
        double closestDistance = Double.MAX_VALUE;

        for (LivingEntity entity : entities) {
            // 排除玩家和已死亡的实体
            if (entity == shooter || !entity.isAlive()) continue;

            double distance = currentPos.distanceTo(entity.position());
            if (distance < closestDistance) {
                closestDistance = distance;
                closestTarget = entity;
            }
        }

        target = closestTarget;
    }

    /**
     * 击中目标时的处理
     */
    private void onHitTarget() {
        if (target == null || shooter == null) return;

        // 造成伤害
        target.hurt(target.damageSources().indirectMagic(this, shooter), damage);

        // 生成击中粒子效果
        spawnHitParticles();

        // 播放击中音效
        level().playSound(null, target.getX(), target.getY(), target.getZ(),
                isLight ? net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT : net.minecraft.sounds.SoundEvents.WITHER_SHOOT,
                net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, isLight ? 1.2F : 0.8F);
    }

    /**
     * 生成追踪粒子效果
     */
    private void spawnTrackingParticles() {
        if (level().isClientSide()) {
            Vec3 pos = position();

            // 根据光暗类型生成不同颜色的粒子
            if (isLight) {
                // 光球：白色发光粒子
                level().addParticle(ParticleTypes.END_ROD,
                        pos.x, pos.y, pos.z,
                        (Math.random() - 0.5) * 0.02,
                        (Math.random() - 0.5) * 0.02,
                        (Math.random() - 0.5) * 0.02);

                // 光球尾迹
                if (ticksLived % 2 == 0) {
                    level().addParticle(ParticleTypes.GLOW,
                            pos.x - getDeltaMovement().x * 0.2,
                            pos.y - getDeltaMovement().y * 0.2,
                            pos.z - getDeltaMovement().z * 0.2,
                            0, 0, 0);
                }
            } else {
                // 暗球：黑色烟雾粒子
                level().addParticle(ParticleTypes.SMOKE,
                        pos.x, pos.y, pos.z,
                        (Math.random() - 0.5) * 0.02,
                        (Math.random() - 0.5) * 0.02,
                        (Math.random() - 0.5) * 0.02);

                // 暗球尾迹
                if (ticksLived % 2 == 0) {
                    level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                            pos.x - getDeltaMovement().x * 0.2,
                            pos.y - getDeltaMovement().y * 0.2,
                            pos.z - getDeltaMovement().z * 0.2,
                            0, 0, 0);
                }
            }
        }
    }

    /**
     * 生成击中粒子效果
     */
    private void spawnHitParticles() {
        if (level().isClientSide() && target != null) {
            Vec3 hitPos = target.getEyePosition();

            // 根据光暗类型生成不同击中粒子
            if (isLight) {
                // 光球击中：白色爆炸粒子
                for (int i = 0; i < 15; i++) {
                    level().addParticle(ParticleTypes.END_ROD,
                            hitPos.x,
                            hitPos.y,
                            hitPos.z,
                            (Math.random() - 0.5) * 0.2,
                            (Math.random() - 0.5) * 0.2,
                            (Math.random() - 0.5) * 0.2);
                }
            } else {
                // 暗球击中：黑色爆炸粒子
                for (int i = 0; i < 15; i++) {
                    level().addParticle(ParticleTypes.SMOKE,
                            hitPos.x,
                            hitPos.y,
                            hitPos.z,
                            (Math.random() - 0.5) * 0.2,
                            (Math.random() - 0.5) * 0.2,
                            (Math.random() - 0.5) * 0.2);
                }

                // 暗球额外效果：灵魂火焰
                for (int i = 0; i < 8; i++) {
                    level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                            hitPos.x,
                            hitPos.y,
                            hitPos.z,
                            (Math.random() - 0.5) * 0.1,
                            (Math.random() - 0.5) * 0.1,
                            (Math.random() - 0.5) * 0.1);
                }
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        // 空实现，因为这是一个简化的投射物
    }
}