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

/**
 * 曳光子弹投射物 - 用于TracerLight的曳光子弹攻击
 * 特性：明亮的曳光轨迹、自动追踪最近目标、高伤害
 */
public class TracerLightProjectile extends Projectile
{
    private LivingEntity target;
    private float damage;
    private Player shooter;
    private int ticksLived = 0;
    private static final int MAX_LIFETIME = 100; // 最大生存时间（5秒）
    private static final float TRACKING_STRENGTH = 0.12f; // 追踪强度
    private static final double TRACKING_RANGE = 20.0; // 追踪范围20格

    public TracerLightProjectile(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
        this.shooter = null;
        this.target = null;
        this.damage = 0;
        this.setNoGravity(true); // 曳光子弹无视重力
    }

    public TracerLightProjectile(Level level, Player shooter, float damage) {
        this(ModEntities.TRACER_LIGHT_PROJECTILE.get(), level);
        this.shooter = shooter;
        this.damage = damage;
        this.target = null; // 初始无目标，需要手动设置
    }

    /**
     * 设置追踪目标
     */
    public void setTarget(LivingEntity target) {
        this.target = target;
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
        if (ticksLived > 3) { // 前3个tick不追踪，让子弹先飞出去
            Vec3 targetPos = target.getEyePosition();
            Vec3 bulletPos = position();
            Vec3 direction = targetPos.subtract(bulletPos).normalize();

            // 应用追踪
            setDeltaMovement(getDeltaMovement().add(direction.scale(TRACKING_STRENGTH)).scale(0.96));
        }

        // 检查是否击中目标
        if (target != null && distanceTo(target) < 1.5) {
            onHitTarget();
            discard();
            return;
        }

        // 生成曳光粒子效果
        spawnTracerParticles();
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
        target.hurt(target.damageSources().playerAttack(shooter), damage);

        // 生成击中粒子效果
        spawnHitParticles();

        // 播放击中音效
        level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.FIREWORK_ROCKET_BLAST_FAR, SoundSource.PLAYERS, 0.8F, 0.6F);
    }

    /**
     * 生成曳光粒子效果
     */
    private void spawnTracerParticles() {
        if (level().isClientSide()) {
            Vec3 pos = position();

            // 生成曳光主体粒子（明亮的橙色）
            level().addParticle(ParticleTypes.FLAME,
                    pos.x, pos.y, pos.z,
                    (Math.random() - 0.5) * 0.01,
                    (Math.random() - 0.5) * 0.01,
                    (Math.random() - 0.5) * 0.01);

            // 生成曳光尾迹（黄色光晕）
            if (ticksLived % 2 == 0) {
                level().addParticle(ParticleTypes.GLOW,
                        pos.x - getDeltaMovement().x * 0.1,
                        pos.y - getDeltaMovement().y * 0.1,
                        pos.z - getDeltaMovement().z * 0.1,
                        0, 0, 0);
            }

            // 生成曳光闪烁效果（随机闪烁）
            if (ticksLived % 5 == 0) {
                level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        pos.x + (Math.random() - 0.5) * 0.2,
                        pos.y + (Math.random() - 0.5) * 0.2,
                        pos.z + (Math.random() - 0.5) * 0.2,
                        0, 0.05, 0);
            }
        }
    }

    /**
     * 生成击中粒子效果
     */
    private void spawnHitParticles() {
        if (level().isClientSide() && target != null) {
            Vec3 hitPos = target.getEyePosition();

            // 生成曳光爆炸粒子
            for (int i = 0; i < 20; i++) {
                level().addParticle(ParticleTypes.FLAME,
                        hitPos.x,
                        hitPos.y,
                        hitPos.z,
                        (Math.random() - 0.5) * 0.3,
                        (Math.random() - 0.5) * 0.3,
                        (Math.random() - 0.5) * 0.3);
            }

            // 生成爆炸光晕
            for (int i = 0; i < 10; i++) {
                level().addParticle(ParticleTypes.GLOW,
                        hitPos.x,
                        hitPos.y,
                        hitPos.z,
                        (Math.random() - 0.5) * 0.2,
                        (Math.random() - 0.5) * 0.2,
                        (Math.random() - 0.5) * 0.2);
            }

            // 生成电火花效果
            for (int i = 0; i < 8; i++) {
                level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        hitPos.x,
                        hitPos.y,
                        hitPos.z,
                        (Math.random() - 0.5) * 0.1,
                        (Math.random() - 0.5) * 0.1,
                        (Math.random() - 0.5) * 0.1);
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        // 空实现，因为这是一个简化的投射物
    }
}