package cn.dawnstring.fatality.entity.projectile;

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
 * 泡泡投射物类 - 追踪目标的泡泡
 * 特性：缓慢飞行、自动追踪最近目标、击中后产生水花效果
 */
public class BubbleProjectile extends AbstractArrow {
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(BubbleProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_TRACKING = SynchedEntityData.defineId(BubbleProjectile.class, EntityDataSerializers.BOOLEAN);
    
    private static final int MAX_LIFE_TIME = 200; // 最大生命周期200tick（10秒）
    private static final double TRACKING_RANGE = 16.0; // 追踪范围16格
    private static final double TRACKING_STRENGTH = 0.05; // 追踪强度
    private static final float BUBBLE_SPEED = 1.0f; // 泡泡飞行速度
    
    private LivingEntity currentTarget;
    private int trackingInterval = 0;
    private int lifeTime = 0;

    public BubbleProjectile(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
        this.setNoGravity(true); // 泡泡不受重力影响
        this.setBaseDamage(0); // 基础伤害为0，通过数据同步设置实际伤害
    }

    public BubbleProjectile(Level level, Player shooter, ItemStack weaponStack, float damage, boolean tracking) {
        this(EntityType.ARROW, level);
        this.setOwner(shooter);
        this.setBaseDamage(damage);
        this.entityData.set(DATA_DAMAGE, damage);
        this.entityData.set(DATA_TRACKING, tracking);
        this.pickup = AbstractArrow.Pickup.DISALLOWED; // 不允许拾取
        
        // 设置初始位置和方向
        Vec3 lookVec = shooter.getLookAngle();
        Vec3 eyePos = shooter.getEyePosition();
        
        this.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
        this.shoot(lookVec.x, lookVec.y, lookVec.z, BUBBLE_SPEED, 0.0F); // 低速发射，无散布
    }

    /**
     * 获取泡泡伤害
     */
    public float getBubbleDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    /**
     * 获取是否处于追踪模式
     */
    public boolean isTrackingMode() {
        return this.entityData.get(DATA_TRACKING);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 0.0f);
        this.entityData.define(DATA_TRACKING, false);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 追踪逻辑
        if (isTrackingMode()) {
            handleTrackingLogic();
        }

        // 生成泡泡粒子效果
        if (this.level().isClientSide()) {
            spawnBubbleParticles();
        }

        // 检查生命周期结束
        if (lifeTime >= MAX_LIFE_TIME) {
            this.discard();
        }
    }

    /**
     * 处理追踪逻辑
     */
    private void handleTrackingLogic() {
        trackingInterval++;

        // 每5tick更新一次目标（缓慢追踪）
        if (trackingInterval >= 5) {
            trackingInterval = 0;
            updateTarget();
        }

        // 如果有目标，追踪目标
        if (currentTarget != null && currentTarget.isAlive()) {
            Vec3 targetPos = currentTarget.getEyePosition();
            Vec3 currentPos = this.position();
            Vec3 direction = targetPos.subtract(currentPos).normalize();

            // 缓慢转向追踪
            Vec3 currentMotion = this.getDeltaMovement();
            Vec3 newMotion = currentMotion.add(direction.scale(TRACKING_STRENGTH)).normalize().scale(BUBBLE_SPEED);
            this.setDeltaMovement(newMotion);
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
                this.getBoundingBox().inflate(TRACKING_RANGE));

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
     * 生成泡泡粒子效果
     */
    private void spawnBubbleParticles() {
        Vec3 position = this.position();

        // 泡泡核心粒子
        for (int i = 0; i < 3; i++) {
            double offsetX = (Math.random() - 0.5) * 0.3;
            double offsetY = (Math.random() - 0.5) * 0.3;
            double offsetZ = (Math.random() - 0.5) * 0.3;

            // 气泡粒子
            this.level().addParticle(ParticleTypes.BUBBLE,
                    position.x + offsetX,
                    position.y + offsetY,
                    position.z + offsetZ,
                    0, 0.01, 0);

            // 水花粒子
            if (i % 2 == 0) {
                this.level().addParticle(ParticleTypes.SPLASH,
                        position.x + offsetX * 0.6,
                        position.y + offsetY * 0.6,
                        position.z + offsetZ * 0.6,
                        0, 0.008, 0);
            }
        }

        // 泡泡尾缀效果
        for (int i = 0; i < 2; i++) {
            // 在泡泡后方生成粒子
            Vec3 motion = this.getDeltaMovement();
            double trailOffset = -0.3 * (i + 1);
            Vec3 trailPos = position.add(motion.normalize().scale(trailOffset));

            // 气泡尾缀
            this.level().addParticle(ParticleTypes.BUBBLE,
                    trailPos.x + (Math.random() - 0.5) * 0.2,
                    trailPos.y + (Math.random() - 0.5) * 0.2,
                    trailPos.z + (Math.random() - 0.5) * 0.2,
                    -motion.x * 0.02, -motion.y * 0.02, -motion.z * 0.02);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            // 播放泡泡破裂音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 0.6F, 0.8F);

            // 生成水花粒子效果
            if (this.level().isClientSide()) {
                for (int i = 0; i < 8; i++) {
                    double offsetX = (Math.random() - 0.5) * 0.5;
                    double offsetY = (Math.random() - 0.5) * 0.5;
                    double offsetZ = (Math.random() - 0.5) * 0.5;

                    this.level().addParticle(ParticleTypes.SPLASH,
                            this.getX() + offsetX,
                            this.getY() + offsetY,
                            this.getZ() + offsetZ,
                            0, 0.1, 0);
                }
            }

            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();

        if (entity instanceof LivingEntity livingEntity) {
            // 播放实体命中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 0.8F, 1.0F);
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
        this.lifeTime = compound.getInt("LifeTime");
        if (compound.contains("BubbleDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("BubbleDamage"));
        }
        if (compound.contains("TrackingMode")) {
            this.entityData.set(DATA_TRACKING, compound.getBoolean("TrackingMode"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", lifeTime);
        compound.putFloat("BubbleDamage", this.entityData.get(DATA_DAMAGE));
        compound.putBoolean("TrackingMode", this.entityData.get(DATA_TRACKING));
    }
}