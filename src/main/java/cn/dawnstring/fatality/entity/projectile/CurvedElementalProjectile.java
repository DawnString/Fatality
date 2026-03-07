package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.entity.boss.endofnightmare.EndOfNightmare;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * 曲线弹幕投射物 - 实现华丽的曲线运动轨迹
 */
public class CurvedElementalProjectile extends Projectile {
    private final LivingEntity target;
    private final float damage;
    private final int projectileId;
    private final int layer;
    private double curveStrength;
    private double speed;
    private int lifeTime = 0;
    private static final int MAX_LIFE_TIME = 180;
    private double curvePhase = 0;

    public CurvedElementalProjectile(Level level, EndOfNightmare owner, float damage,
                                     LivingEntity target, int projectileId, int layer) {
        super(EntityType.ARROW, level);
        this.setOwner(owner);
        this.target = target;
        this.damage = damage;
        this.projectileId = projectileId;
        this.layer = layer;
        this.setNoGravity(true);
    }

    public void setCurveParameters(double curveStrength, double speed) {
        this.curveStrength = curveStrength;
        this.speed = speed;
        this.curvePhase = projectileId * 0.5; // 基于ID的相位偏移
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 生成华丽的粒子轨迹
        if (this.level().isClientSide()) {
            spawnCurvedTrailParticles();
        }

        // 更新曲线运动
        updateCurvedMovement();

        // 检查生命周期
        if (lifeTime >= MAX_LIFE_TIME) {
            explodeWithStyle();
            return;
        }

        // 检查命中
        if (target != null && target.isAlive() && this.distanceTo(target) < 2.5) {
            onHitTarget();
        }
    }

    private void updateCurvedMovement() {
        if (target == null || !target.isAlive()) {
            // 直线飞行作为备用
            return;
        }

        Vec3 currentPos = this.position();
        Vec3 targetPos = target.position().add(0, 1.5, 0);

        // 基础方向
        Vec3 baseDirection = targetPos.subtract(currentPos).normalize();

        // 曲线运动计算
        curvePhase += 0.1;
        double curveValue = Math.sin(curvePhase) * curveStrength;

        // 计算垂直和水平曲线分量
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = baseDirection.cross(up).normalize();

        // 正弦曲线运动
        double verticalCurve = Math.sin(curvePhase * 1.5) * curveStrength * 0.5;
        double horizontalCurve = Math.cos(curvePhase * 1.2) * curveStrength;

        // 合成最终方向
        Vec3 curvedDirection = baseDirection
                .add(up.scale(verticalCurve))
                .add(right.scale(horizontalCurve))
                .normalize();

        this.setDeltaMovement(curvedDirection.scale(speed));

        // 添加旋转效果
        this.setYRot((float) (curvePhase * 30) % 360);
    }

    private void spawnCurvedTrailParticles() {
        // 根据层数选择颜色
        Vector3f[] layerColors = {
                new Vector3f(1.0f, 0.2f, 0.2f), // 红色 - 底层
                new Vector3f(0.2f, 1.0f, 0.2f), // 绿色 - 中层
                new Vector3f(0.2f, 0.2f, 1.0f)  // 蓝色 - 顶层
        };

        Vector3f color = layerColors[layer % layerColors.length];

        // 主轨迹粒子
        for (int i = 0; i < 4; i++) {
            DustParticleOptions particle = new DustParticleOptions(color, 2.0f);

            this.level().addParticle(particle,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                    this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                    0, 0, 0);
        }

        // 光晕效果粒子
        if (lifeTime % 2 == 0) {
            this.level().addParticle(ParticleTypes.GLOW,
                    this.getX(), this.getY(), this.getZ(),
                    (this.random.nextDouble() - 0.5) * 0.05,
                    (this.random.nextDouble() - 0.5) * 0.05,
                    (this.random.nextDouble() - 0.5) * 0.05);
        }

        // 尾迹粒子（延迟效果）
        if (lifeTime > 5 && lifeTime % 3 == 0) {
            Vector3f trailColor = new Vector3f(
                    color.x() * 0.7f,
                    color.y() * 0.7f,
                    color.z() * 0.7f
            );
            DustParticleOptions trailParticle = new DustParticleOptions(trailColor, 1.0f);

            Vec3 trailPos = this.position().subtract(this.getDeltaMovement().scale(2));
            this.level().addParticle(trailParticle,
                    trailPos.x, trailPos.y, trailPos.z,
                    0, 0, 0);
        }
    }

    private void onHitTarget() {
        if (!this.level().isClientSide() && target != null && target.isAlive()) {
            target.hurt(this.damageSources().indirectMagic(this, this.getOwner()), damage);
            createImpactEffect();
        }
        this.discard();
    }

    private void explodeWithStyle() {
        if (!this.level().isClientSide()) {
            createImpactEffect();
        }
        this.discard();
    }

    private void createImpactEffect() {
        if (this.level().isClientSide()) {
            // 华丽的爆炸粒子效果
            for (int i = 0; i < 25; i++) {
                Vector3f color = new Vector3f(
                        this.random.nextFloat() * 0.8f + 0.2f,
                        this.random.nextFloat() * 0.8f + 0.2f,
                        this.random.nextFloat() * 0.8f + 0.2f
                );
                DustParticleOptions particle = new DustParticleOptions(color, 2.5f);

                double spread = 3.0;
                this.level().addParticle(particle,
                        this.getX() + (this.random.nextDouble() - 0.5) * spread,
                        this.getY() + (this.random.nextDouble() - 0.5) * spread,
                        this.getZ() + (this.random.nextDouble() - 0.5) * spread,
                        (this.random.nextDouble() - 0.5) * 0.8,
                        (this.random.nextDouble() - 0.5) * 0.8,
                        (this.random.nextDouble() - 0.5) * 0.8);
            }
        }

        // 播放音效
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE,
                net.minecraft.sounds.SoundSource.HOSTILE, 0.8f, 1.5f);
    }

    @Override
    protected void defineSynchedData() {
        // 不需要同步数据
    }
}