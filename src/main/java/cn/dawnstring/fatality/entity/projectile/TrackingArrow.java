package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraftforge.network.NetworkHooks;

import java.util.List;

/**
 * 追踪箭矢 - 用于BowOfLight的追踪箭矢攻击
 * 特性：金色粒子效果、极快速度追踪最近的目标、高伤害
 */
public class TrackingArrow extends AbstractArrow
{
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(TrackingArrow.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_TRACKING_MODE = SynchedEntityData.defineId(TrackingArrow.class, EntityDataSerializers.BOOLEAN);

    private int ticksLived = 0;
    private final int maxLifeTime = 100; // 5秒生命周期（100tick）
    private LivingEntity currentTarget = null;
    private int trackingInterval = 0;
    private static final float TRACKING_SPEED = 3.0f; // 追踪速度
    private static final double SEARCH_RADIUS = 20.0; // 20格搜索半径

    public TrackingArrow(EntityType<? extends TrackingArrow> type, Level level) {
        super(type, level);
        this.setNoGravity(true); // 追踪箭矢无视重力
        this.entityData.set(DATA_DAMAGE, 0.0f); // 默认伤害
        this.entityData.set(DATA_TRACKING_MODE, false); // 默认非追踪模式
    }

    public TrackingArrow(Level level, LivingEntity shooter, float damage) {
        this(ModEntities.TRACKING_ARROW.get(), level);
        this.setOwner(shooter);
        this.setBaseDamage(damage);
        this.entityData.set(DATA_DAMAGE, damage);
        this.pickup = AbstractArrow.Pickup.DISALLOWED;

        // 设置初始位置和方向
        Vec3 lookVec = shooter.getLookAngle();
        Vec3 eyePos = shooter.getEyePosition();
        
        this.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
        this.shoot(lookVec.x, lookVec.y, lookVec.z, 4.0F, 0.0F); // 高速发射
    }

    /**
     * 设置追踪模式
     */
    public void setTrackingMode(boolean tracking) {
        this.entityData.set(DATA_TRACKING_MODE, tracking);
    }

    /**
     * 获取是否处于追踪模式
     */
    public boolean isTrackingMode() {
        return this.entityData.get(DATA_TRACKING_MODE);
    }

    /**
     * 获取追踪箭矢伤害
     */
    public float getTrackingDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 0.0f);
        this.entityData.define(DATA_TRACKING_MODE, false);
    }

    @Override
    public void tick() {
        super.tick();
        ticksLived++;

        // 检查生命周期结束
        if (ticksLived >= maxLifeTime) {
            this.discard();
            return;
        }

        // 立即进入追踪模式（极快速度追踪）
        if (ticksLived > 2) { // 前2个tick不追踪，让箭矢先飞出去
            setTrackingMode(true);
        }

        // 追踪逻辑
        if (isTrackingMode()) {
            handleTrackingLogic();
        }

        // 生成金色粒子效果
        if (this.level().isClientSide()) {
            spawnLightParticles();
        }
    }

    /**
     * 处理追踪逻辑
     */
    private void handleTrackingLogic() {
        trackingInterval++;

        // 每2tick更新一次目标（极快速度）
        if (trackingInterval >= 2) {
            trackingInterval = 0;
            updateTarget();
        }

        // 如果有目标，追踪目标
        if (currentTarget != null && currentTarget.isAlive()) {
            Vec3 targetPos = currentTarget.getEyePosition();
            Vec3 currentPos = this.position();
            Vec3 direction = targetPos.subtract(currentPos).normalize();

            // 瞬间转向追踪（直接设置为目标方向，极快速度）
            this.setDeltaMovement(direction.scale(TRACKING_SPEED));

            // 如果距离目标很近，直接命中
            if (currentPos.distanceTo(targetPos) <= 1.0) {
                this.explode();
            }
        }
    }

    /**
     * 更新追踪目标
     */
    private void updateTarget() {
        if (this.getOwner() == null || !(this.getOwner() instanceof LivingEntity owner)) return;

        Vec3 currentPos = this.position();

        // 获取范围内的所有生物
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(SEARCH_RADIUS));

        LivingEntity closestTarget = null;
        double closestDistance = Double.MAX_VALUE;

        for (LivingEntity entity : entities) {
            // 排除玩家和已死亡的实体
            if (entity == owner || !entity.isAlive()) continue;

            double distance = currentPos.distanceTo(entity.position());
            if (distance < closestDistance) {
                closestDistance = distance;
                closestTarget = entity;
            }
        }

        currentTarget = closestTarget;
    }

    /**
     * 生成金色粒子效果
     */
    private void spawnLightParticles() {
        Vec3 position = this.position();
        Vec3 motion = this.getDeltaMovement();

        // 金色粒子核心
        for (int i = 0; i < 8; i++) {
            double offsetX = (Math.random() - 0.5) * 0.4;
            double offsetY = (Math.random() - 0.5) * 0.4;
            double offsetZ = (Math.random() - 0.5) * 0.4;

            // 金色火焰粒子
            this.level().addParticle(ParticleTypes.FLAME,
                    position.x + offsetX,
                    position.y + offsetY,
                    position.z + offsetZ,
                    0, 0.01, 0);

            // 金色火花粒子
            if (i % 3 == 0) {
                this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        position.x + offsetX * 0.6,
                        position.y + offsetY * 0.6,
                        position.z + offsetZ * 0.6,
                        0, 0.008, 0);
            }
        }

        // 金色尾缀效果
        for (int i = 0; i < 4; i++) {
            // 在箭矢后方生成金色粒子
            double trailOffset = -0.2 * (i + 1);
            Vec3 trailPos = position.add(motion.normalize().scale(trailOffset));

            // 金色火焰尾缀
            this.level().addParticle(ParticleTypes.FLAME,
                    trailPos.x + (Math.random() - 0.5) * 0.2,
                    trailPos.y + (Math.random() - 0.5) * 0.2,
                    trailPos.z + (Math.random() - 0.5) * 0.2,
                    -motion.x * 0.05, -motion.y * 0.05, -motion.z * 0.05);
        }
    }

    /**
     * 爆炸效果（命中目标）
     */
    private void explode() {
        // 播放神圣箭矢命中音效
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 0.8F, 0.6F);

        // 生成爆炸粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 12; i++) {
                double offsetX = (Math.random() - 0.5) * 0.8;
                double offsetY = (Math.random() - 0.5) * 0.8;
                double offsetZ = (Math.random() - 0.5) * 0.8;

                this.level().addParticle(ParticleTypes.FLAME,
                        this.getX() + offsetX,
                        this.getY() + offsetY,
                        this.getZ() + offsetZ,
                        0, 0.06, 0);

                if (i % 2 == 0) {
                    this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                            this.getX() + offsetX * 0.6,
                            this.getY() + offsetY * 0.6,
                            this.getZ() + offsetZ * 0.6,
                            0, 0.04, 0);
                }
            }
        }

        this.discard();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            this.explode();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();

        if (entity instanceof LivingEntity livingEntity) {
            // 播放实体命中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 0.8F, 1.0F);
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.ticksLived = compound.getInt("TicksLived");
        if (compound.contains("TrackingDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("TrackingDamage"));
        }
        if (compound.contains("TrackingMode")) {
            this.entityData.set(DATA_TRACKING_MODE, compound.getBoolean("TrackingMode"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("TicksLived", this.ticksLived);
        compound.putFloat("TrackingDamage", getTrackingDamage());
        compound.putBoolean("TrackingMode", isTrackingMode());
    }
}