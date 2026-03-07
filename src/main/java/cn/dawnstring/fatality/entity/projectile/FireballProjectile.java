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


public class FireballProjectile extends AbstractArrow
{
    private static final EntityDataAccessor<Boolean> DATA_EXPLODED = SynchedEntityData.defineId(FireballProjectile.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(FireballProjectile.class, EntityDataSerializers.FLOAT);

    private int lifeTime = 0;
    private final int maxLifeTime = 60; // 3秒生命周期

    public FireballProjectile(EntityType<? extends FireballProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.entityData.set(DATA_DAMAGE, 25.0f); // 默认伤害
    }

    public FireballProjectile(Level level, LivingEntity shooter, ItemStack weapon, float damage) {
        this(ModEntities.FIREBALL_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.setBaseDamage(damage); // 使用传入的伤害值
        this.entityData.set(DATA_DAMAGE, damage); // 同步伤害数据
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    /**
     * 获取火球伤害（从同步数据中读取）
     */
    public float getFireballDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_EXPLODED, false);
        this.entityData.define(DATA_DAMAGE, 25.0f);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 生成火焰粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 2; i++) {
                this.level().addParticle(ParticleTypes.FLAME,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
                        0, 0, 0);
            }
        }

        // 检查生命周期结束或碰撞
        if (lifeTime >= maxLifeTime || this.isInWater()) {
            this.explode();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide() && !this.entityData.get(DATA_EXPLODED)) {
            this.explode();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();

        if (entity instanceof LivingEntity livingEntity) {
            // 点燃目标3秒
            livingEntity.setSecondsOnFire(3);
        }
    }

    private void explode() {
        if (this.entityData.get(DATA_EXPLODED)) return;

        this.entityData.set(DATA_EXPLODED, true);

        // 播放爆炸音效
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1.0F, 1.0F);

        // 生成爆炸粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 20; i++) {
                this.level().addParticle(ParticleTypes.EXPLOSION,
                        this.getX() + (this.random.nextDouble() - 0.5) * 2.0,
                        this.getY() + (this.random.nextDouble() - 0.5) * 2.0,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 2.0,
                        0, 0, 0);
                this.level().addParticle(ParticleTypes.FLAME,
                        this.getX() + (this.random.nextDouble() - 0.5) * 2.0,
                        this.getY() + (this.random.nextDouble() - 0.5) * 2.0,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 2.0,
                        0, 0, 0);
            }
        }

        // 造成范围伤害
        if (!this.level().isClientSide()) {
            Vec3 center = this.position();
            float explosionRadius = 3.0f; // 3格爆炸半径

            for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class,
                    this.getBoundingBox().inflate(explosionRadius))) {
                if (entity != this.getOwner()) {
                    double distance = entity.distanceToSqr(center);
                    if (distance <= explosionRadius * explosionRadius) {
                        // 距离越近伤害越高，使用计算好的伤害值
                        float damage = (float) (getFireballDamage() * (1.0 - distance / (explosionRadius * explosionRadius)));
                        entity.hurt(this.damageSources().indirectMagic(this, this.getOwner()), damage);

                        // 击退效果
                        Vec3 knockbackVec = entity.position().subtract(center).normalize().scale(0.5);
                        entity.setDeltaMovement(entity.getDeltaMovement().add(knockbackVec));
                    }
                }
            }
        }

        this.discard();
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
        if (compound.contains("FireballDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("FireballDamage"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putFloat("FireballDamage", getFireballDamage());
    }
}