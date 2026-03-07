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

/**
 * 绿叶飞刀投射物 - 具有追踪功能的飞刀投射物
 * 特性：部分飞刀具有追踪功能，绿色粒子效果，自然主题
 */
public class GreenLeafFlyingKnifeProjectile extends AbstractArrow {
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(GreenLeafFlyingKnifeProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_TRACKING = SynchedEntityData.defineId(GreenLeafFlyingKnifeProjectile.class, EntityDataSerializers.BOOLEAN);

    private int lifeTime = 0;
    private final int maxLifeTime = 100; // 5秒生命周期
    private LivingEntity target = null;
    private static final float TRACKING_STRENGTH = 0.08f; // 追踪强度
    private static final double MAX_TRACKING_RANGE = 32.0; // 最大追踪范围32格
    private int trackingInterval = 0;

    public GreenLeafFlyingKnifeProjectile(EntityType<? extends GreenLeafFlyingKnifeProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true); // 绿叶飞刀不受重力影响
        this.entityData.set(DATA_DAMAGE, 480.0f); // 默认伤害
        this.entityData.set(DATA_TRACKING, false); // 默认非追踪模式
    }

    public GreenLeafFlyingKnifeProjectile(Level level, LivingEntity shooter, float damage, boolean tracking) {
        this(ModEntities.GREEN_LEAF_FLYING_KNIFE_PROJECTILE.get(), level);
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
     * 获取飞刀伤害（从同步数据中读取）
     */
    public float getKnifeDamage() {
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
        this.entityData.define(DATA_DAMAGE, 480.0f);
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
                if (lifeTime > 3) { // 前3个tick不追踪，让飞刀先飞出去
                    Vec3 targetPos = target.getEyePosition();
                    Vec3 knifePos = position();
                    Vec3 direction = targetPos.subtract(knifePos).normalize();

                    // 应用追踪
                    setDeltaMovement(getDeltaMovement().add(direction.scale(TRACKING_STRENGTH)).scale(0.98));
                }
            }
        }

        // 生成绿叶飞刀粒子效果
        if (this.level().isClientSide()) {
            spawnGreenLeafParticles();
        }

        // 检查生命周期结束
        if (lifeTime >= maxLifeTime) {
            this.discard();
        }
    }

    /**
     * 生成绿叶飞刀粒子效果
     */
    private void spawnGreenLeafParticles() {
        Vec3 pos = position();
        
        // 生成绿色粒子核心
        level().addParticle(ParticleTypes.HAPPY_VILLAGER,
                pos.x, pos.y, pos.z,
                (Math.random() - 0.5) * 0.05,
                (Math.random() - 0.5) * 0.05,
                (Math.random() - 0.5) * 0.05);

        // 生成树叶粒子效果
        if (lifeTime % 3 == 0) {
            level().addParticle(ParticleTypes.CHERRY_LEAVES,
                    pos.x, pos.y, pos.z,
                    getDeltaMovement().x * 0.1,
                    getDeltaMovement().y * 0.1,
                    getDeltaMovement().z * 0.1);
        }

        // 追踪飞刀的特殊粒子效果
        if (isTrackingMode() && lifeTime % 2 == 0) {
            level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    pos.x, pos.y, pos.z,
                    (Math.random() - 0.5) * 0.02,
                    (Math.random() - 0.5) * 0.02,
                    (Math.random() - 0.5) * 0.02);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            // 播放绿叶飞刀命中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8F, 1.2F);

            // 生成绿叶飞刀命中粒子效果
            if (this.level().isClientSide()) {
                for (int i = 0; i < 8; i++) {
                    this.level().addParticle(ParticleTypes.HAPPY_VILLAGER,
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
                    SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 0.6F, 0.8F);
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
        if (compound.contains("KnifeDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("KnifeDamage"));
        }
        if (compound.contains("TrackingMode")) {
            this.entityData.set(DATA_TRACKING, compound.getBoolean("TrackingMode"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putFloat("KnifeDamage", getKnifeDamage());
        compound.putBoolean("TrackingMode", isTrackingMode());
    }
}