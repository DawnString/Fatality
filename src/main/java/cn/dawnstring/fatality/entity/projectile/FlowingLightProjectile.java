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

public class FlowingLightProjectile extends AbstractArrow
{
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(FlowingLightProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_TRACKING = SynchedEntityData.defineId(FlowingLightProjectile.class, EntityDataSerializers.BOOLEAN);

    private int lifeTime = 0;
    private final int maxLifeTime = 200; // 10秒生命周期
    private LivingEntity target = null;
    private static final float TRACKING_STRENGTH = 0.05f; // 追踪强度
    private static final double MAX_TRACKING_RANGE = 64.0; // 最大追踪范围64格

    public FlowingLightProjectile(EntityType<? extends FlowingLightProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true); // 流动之光子弹无视重力
        this.entityData.set(DATA_DAMAGE, 400.0f); // 默认伤害
        this.entityData.set(DATA_TRACKING, false); // 默认非追踪模式
    }

    public FlowingLightProjectile(Level level, LivingEntity shooter, ItemStack weapon, float damage, boolean tracking) {
        this(ModEntities.FLOWING_LIGHT_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.setBaseDamage(damage);
        this.entityData.set(DATA_DAMAGE, damage); // 同步伤害数据
        this.entityData.set(DATA_TRACKING, tracking); // 设置追踪模式
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
        
        // 如果是追踪模式，寻找最近的目标
        if (tracking && shooter instanceof Player player) {
            this.target = findNearestTarget(level, player);
        }
    }

    /**
     * 获取子弹伤害（从同步数据中读取）
     */
    public float getBulletDamage() {
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
        this.entityData.define(DATA_DAMAGE, 400.0f);
        this.entityData.define(DATA_TRACKING, false);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 追踪逻辑
        if (isTrackingMode() && target != null && target.isAlive()) {
            // 检查目标是否在追踪范围内
            if (distanceTo(target) > MAX_TRACKING_RANGE) {
                target = null; // 超出范围，停止追踪
            } else {
                // 追踪目标
                if (lifeTime > 5) { // 前5个tick不追踪，让子弹先飞出去
                    Vec3 targetPos = target.getEyePosition();
                    Vec3 bulletPos = position();
                    Vec3 direction = targetPos.subtract(bulletPos).normalize();

                    // 应用追踪
                    setDeltaMovement(getDeltaMovement().add(direction.scale(TRACKING_STRENGTH)).scale(0.98));
                }
            }
        }

        // 生成流动之光粒子效果
        if (this.level().isClientSide()) {
            spawnFlowingLightParticles();
        }

        // 检查生命周期结束
        if (lifeTime >= maxLifeTime) {
            this.discard();
        }
    }

    /**
     * 生成流动之光粒子效果
     */
    private void spawnFlowingLightParticles() {
        Vec3 pos = position();
        
        // 生成蓝色流动粒子
        level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                pos.x, pos.y, pos.z,
                (Math.random() - 0.5) * 0.05,
                (Math.random() - 0.5) * 0.05,
                (Math.random() - 0.5) * 0.05);

        // 生成白色光点粒子
        if (lifeTime % 2 == 0) {
            level().addParticle(ParticleTypes.END_ROD,
                    pos.x, pos.y, pos.z,
                    getDeltaMovement().x * 0.1,
                    getDeltaMovement().y * 0.1,
                    getDeltaMovement().z * 0.1);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            // 播放流动之光命中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.8F, 1.2F);

            // 生成流动之光命中粒子效果
            if (this.level().isClientSide()) {
                for (int i = 0; i < 12; i++) {
                    this.level().addParticle(ParticleTypes.GLOW,
                            this.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                            this.getY() + (this.random.nextDouble() - 0.5) * 0.5,
                            this.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
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
                    SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.6F, 0.8F);
        }
    }

    /**
     * 寻找最近的追踪目标
     */
    private LivingEntity findNearestTarget(Level level, Player player) {
        LivingEntity nearestTarget = null;
        double nearestDistance = MAX_TRACKING_RANGE;
        
        // 获取玩家周围的所有实体
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, 
                player.getBoundingBox().inflate(MAX_TRACKING_RANGE))) {
            if (entity != player && entity.isAlive() && !entity.isInvulnerable()) {
                double distance = player.distanceTo(entity);
                if (distance < nearestDistance) {
                    nearestTarget = entity;
                    nearestDistance = distance;
                }
            }
        }
        
        return nearestTarget;
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
        if (compound.contains("BulletDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("BulletDamage"));
        }
        if (compound.contains("TrackingMode")) {
            this.entityData.set(DATA_TRACKING, compound.getBoolean("TrackingMode"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putFloat("BulletDamage", getBulletDamage());
        compound.putBoolean("TrackingMode", isTrackingMode());
    }
}