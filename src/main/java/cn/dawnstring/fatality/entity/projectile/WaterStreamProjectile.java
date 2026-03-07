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
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/**
 * 水柱投射物类 - 用于高压水枪的持续水柱效果
 * 特性：持续发射蓝色粒子构成的水柱，具有穿透效果
 */
public class WaterStreamProjectile extends AbstractArrow
{
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(WaterStreamProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_STREAM_LENGTH = SynchedEntityData.defineId(WaterStreamProjectile.class, EntityDataSerializers.INT);

    private int lifeTime = 0;
    private final int maxLifeTime = 40; // 2秒生命周期（持续发射）
    private LivingEntity shooter;

    public WaterStreamProjectile(EntityType<? extends WaterStreamProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true); // 水柱不受重力影响
        this.entityData.set(DATA_DAMAGE, 10.0f); // 默认伤害
        this.entityData.set(DATA_STREAM_LENGTH, 10); // 默认水柱长度10格
    }

    public WaterStreamProjectile(Level level, LivingEntity shooter, ItemStack weapon, float damage, int streamLength) {
        this(ModEntities.WATER_STREAM_PROJECTILE.get(), level);
        this.shooter = shooter;
        this.setOwner(shooter);
        this.setBaseDamage(damage);
        this.entityData.set(DATA_DAMAGE, damage);
        this.entityData.set(DATA_STREAM_LENGTH, streamLength);
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    /**
     * 获取水柱伤害
     */
    public float getWaterStreamDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    /**
     * 获取水柱长度
     */
    public int getStreamLength() {
        return this.entityData.get(DATA_STREAM_LENGTH);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 10.0f);
        this.entityData.define(DATA_STREAM_LENGTH, 10);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 生成水柱粒子效果
        if (this.level().isClientSide()) {
            spawnWaterStreamParticles();
        }

        // 检查生命周期结束
        if (lifeTime >= maxLifeTime) {
            this.discard();
        }
    }

    /**
     * 生成水柱粒子效果
     */
    private void spawnWaterStreamParticles() {
        Vec3 startPos = this.position();
        Vec3 direction = this.getDeltaMovement().normalize();
        int streamLength = getStreamLength();

        // 沿着水柱方向生成蓝色粒子
        for (int i = 0; i < streamLength; i++) {
            double progress = (double) i / streamLength;
            Vec3 particlePos = startPos.add(direction.scale(progress * 2.0)); // 每格2倍距离

            // 生成蓝色水粒子
            this.level().addParticle(ParticleTypes.BUBBLE_COLUMN_UP,
                    particlePos.x + (this.random.nextDouble() - 0.5) * 0.3,
                    particlePos.y + (this.random.nextDouble() - 0.5) * 0.3,
                    particlePos.z + (this.random.nextDouble() - 0.5) * 0.3,
                    0, 0.1, 0);

            // 生成水花粒子增强效果
            if (i % 3 == 0) {
                this.level().addParticle(ParticleTypes.SPLASH,
                        particlePos.x + (this.random.nextDouble() - 0.5) * 0.2,
                        particlePos.y + (this.random.nextDouble() - 0.5) * 0.2,
                        particlePos.z + (this.random.nextDouble() - 0.5) * 0.2,
                        0, 0.15, 0);
            }

            // 生成气泡粒子
            if (i % 2 == 0) {
                this.level().addParticle(ParticleTypes.BUBBLE,
                        particlePos.x + (this.random.nextDouble() - 0.5) * 0.4,
                        particlePos.y + (this.random.nextDouble() - 0.5) * 0.4,
                        particlePos.z + (this.random.nextDouble() - 0.5) * 0.4,
                        0, 0.2, 0);
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            // 播放水花音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.6F, 1.2F);

            // 生成水花粒子效果
            if (this.level().isClientSide()) {
                for (int i = 0; i < 5; i++) {
                    this.level().addParticle(ParticleTypes.SPLASH,
                            this.getX() + (this.random.nextDouble() - 0.5),
                            this.getY(),
                            this.getZ() + (this.random.nextDouble() - 0.5),
                            0, 0.2, 0);
                }
            }

            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();

        if (entity instanceof LivingEntity livingEntity && livingEntity != this.getOwner()) {
            // 播放命中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 0.5F, 1.0F);

            // 水柱具有穿透效果，不立即消失
            // 继续飞行，可以击中多个目标
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
        if (compound.contains("WaterStreamDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("WaterStreamDamage"));
        }
        if (compound.contains("StreamLength")) {
            this.entityData.set(DATA_STREAM_LENGTH, compound.getInt("StreamLength"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putFloat("WaterStreamDamage", getWaterStreamDamage());
        compound.putInt("StreamLength", getStreamLength());
    }
}