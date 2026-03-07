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

public class MagneticBurstProjectile extends AbstractArrow {
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(MagneticBurstProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_BURST_ACTIVATED = SynchedEntityData.defineId(MagneticBurstProjectile.class, EntityDataSerializers.BOOLEAN);

    private int lifeTime = 0;
    private final int maxLifeTime = 4800; // 2秒生命周期
    private final int burstDelay = 20; // 1秒后触发磁暴

    public MagneticBurstProjectile(EntityType<? extends MagneticBurstProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.entityData.set(DATA_DAMAGE, 25.0f);
        this.entityData.set(DATA_BURST_ACTIVATED, false);
    }

    public MagneticBurstProjectile(Level level, LivingEntity shooter, float damage) {
        this(ModEntities.MAGNETIC_BURST_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.entityData.set(DATA_DAMAGE, damage);
        this.pickup = AbstractArrow.Pickup.DISALLOWED;

        // 设置中等速度
        Vec3 lookVec = shooter.getLookAngle();
        this.setDeltaMovement(lookVec.scale(1.0));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 25.0f);
        this.entityData.define(DATA_BURST_ACTIVATED, false);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 磁暴粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 3; i++) {
                this.level().addParticle(ParticleTypes.ENCHANTED_HIT,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.5,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                        0, 0, 0);
            }
        }

        // 触发磁暴
        if (lifeTime >= burstDelay && !this.entityData.get(DATA_BURST_ACTIVATED)) {
            this.activateMagneticBurst();
        }

        // 生命周期结束
        if (lifeTime >= maxLifeTime) {
            this.discard();
        }
    }

    private void activateMagneticBurst() {
        this.entityData.set(DATA_BURST_ACTIVATED, true);

        // 播放磁暴音效
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 0.5F, 1.0F);

        // 磁暴粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 20; i++) {
                double angle = 2 * Math.PI * i / 20;
                double x = this.getX() + Math.cos(angle) * 3.0;
                double z = this.getZ() + Math.sin(angle) * 3.0;

                this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        x, this.getY(), z,
                        0, 0.1, 0);
            }
        }

        // 磁暴效果 - 击退周围实体
        if (!this.level().isClientSide()) {
            Vec3 center = this.position();
            float burstRadius = 4.0f; // 4格磁暴半径
            float damage = this.entityData.get(DATA_DAMAGE);

            for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class,
                    this.getBoundingBox().inflate(burstRadius))) {
                if (entity != this.getOwner()) {
                    double distance = entity.distanceToSqr(center);
                    if (distance <= burstRadius * burstRadius) {
                        // 造成伤害
                        entity.hurt(this.damageSources().indirectMagic(this, this.getOwner()), damage);

                        // 强力击退效果
                        Vec3 knockbackVec = entity.position().subtract(center).normalize().scale(1.5);
                        entity.setDeltaMovement(entity.getDeltaMovement().add(knockbackVec));
                    }
                }
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide() && !this.entityData.get(DATA_BURST_ACTIVATED)) {
            this.activateMagneticBurst();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide() && !this.entityData.get(DATA_BURST_ACTIVATED)) {
            this.activateMagneticBurst();
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
        if (compound.contains("MagneticDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("MagneticDamage"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putFloat("MagneticDamage", this.entityData.get(DATA_DAMAGE));
    }
}