package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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

public class GaussLaserProjectile extends AbstractArrow {
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(GaussLaserProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_IS_TRACKING = SynchedEntityData.defineId(GaussLaserProjectile.class, EntityDataSerializers.BOOLEAN);

    private int lifeTime = 0;
    private int maxLifeTime = 4800; // 240秒 * 20tick/秒
    private LivingEntity target;

    public GaussLaserProjectile(EntityType<? extends GaussLaserProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.entityData.set(DATA_DAMAGE, 280.0f); // 更新伤害值为280
    }

    public GaussLaserProjectile(Level level, LivingEntity shooter, float damage, boolean isTracking, LivingEntity target) {
        this(ModEntities.GAUSS_LASER_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.entityData.set(DATA_DAMAGE, damage);
        this.entityData.set(DATA_IS_TRACKING, isTracking);
        this.target = target;
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    // 设置最大生命周期
    public void setMaxAge(int maxAge) {
        this.maxLifeTime = maxAge;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 280.0f); // 更新默认伤害值为280
        this.entityData.define(DATA_IS_TRACKING, false);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 性能优化：超过寿命的弹幕及时销毁
        if (lifeTime >= maxLifeTime) {
            this.discard();
            return;
        }

        // 追踪逻辑
        if (this.entityData.get(DATA_IS_TRACKING) && target != null && target.isAlive()) {
            Vec3 currentPos = this.position();
            Vec3 targetPos = target.position().add(0, target.getEyeHeight() / 2, 0);
            Vec3 direction = targetPos.subtract(currentPos).normalize();

            // 缓慢转向追踪
            Vec3 currentMotion = this.getDeltaMovement();
            Vec3 newMotion = currentMotion.add(direction.scale(0.05)).normalize().scale(1.5);
            this.setDeltaMovement(newMotion);
        }

        // 激光粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 15; i++) {
                this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                        0, 0, 0);
            }
            
            // 高斯激光特有的粒子效果
            Vec3 direction = this.getDeltaMovement().normalize();
            for (int i = 0; i < 8; i++) {
                double distance = i * 0.5;
                Vec3 particlePos = this.position().add(direction.scale(distance));
                
                this.level().addParticle(ParticleTypes.GLOW_SQUID_INK,
                        particlePos.x + (this.random.nextDouble() - 0.5) * 0.2,
                        particlePos.y + (this.random.nextDouble() - 0.5) * 0.2,
                        particlePos.z + (this.random.nextDouble() - 0.5) * 0.2,
                        0, 0, 0);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity entity = result.getEntity();
        if (entity instanceof LivingEntity livingEntity && livingEntity != this.getOwner()) {
            float damage = this.entityData.get(DATA_DAMAGE);
            livingEntity.hurt(this.damageSources().indirectMagic(this, this.getOwner()), damage);
            this.discard(); // 碰撞后消失
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            this.discard(); // 碰撞任何物体后消失
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
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", lifeTime);
        compound.putInt("MaxLifeTime", maxLifeTime);
        compound.putBoolean("IsTracking", this.entityData.get(DATA_IS_TRACKING));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        lifeTime = compound.getInt("LifeTime");
        maxLifeTime = compound.getInt("MaxLifeTime");
        this.entityData.set(DATA_IS_TRACKING, compound.getBoolean("IsTracking"));
    }
}