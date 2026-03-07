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

public class HighEnergyElementBallProjectile extends AbstractArrow {
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(HighEnergyElementBallProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_EXPLODED = SynchedEntityData.defineId(HighEnergyElementBallProjectile.class, EntityDataSerializers.BOOLEAN);

    private int lifeTime = 0;
    private final int maxLifeTime = 1800; // 6秒生命周期
    private LivingEntity target;

    public HighEnergyElementBallProjectile(EntityType<? extends HighEnergyElementBallProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.entityData.set(DATA_DAMAGE, 320.0f);
        this.entityData.set(DATA_EXPLODED, false);
    }

    public HighEnergyElementBallProjectile(Level level, LivingEntity shooter, float damage, LivingEntity target) {
        this(ModEntities.HIGH_ENERGY_ELEMENT_BALL_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.entityData.set(DATA_DAMAGE, damage);
        this.target = target;
        this.pickup = AbstractArrow.Pickup.DISALLOWED;

        // 设置缓慢飞行速度
        if (target != null) {
            Vec3 direction = target.position().subtract(this.position()).normalize();
            this.setDeltaMovement(direction.scale(0.4));
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 320.0f);
        this.entityData.define(DATA_EXPLODED, false);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 性能优化：超过寿命的弹幕及时销毁
        if (lifeTime >= maxLifeTime || this.entityData.get(DATA_EXPLODED)) {
            this.discard();
            return;
        }

        // 追踪目标
        if (target != null && target.isAlive() && !this.entityData.get(DATA_EXPLODED)) {
            Vec3 currentPos = this.position();
            Vec3 targetPos = target.position().add(0, target.getEyeHeight() / 2, 0);
            Vec3 direction = targetPos.subtract(currentPos).normalize();

            // 缓慢转向
            Vec3 currentMotion = this.getDeltaMovement();
            Vec3 newMotion = currentMotion.add(direction.scale(0.05)).normalize().scale(0.4);
            this.setDeltaMovement(newMotion);

            // 高能粒子效果
            if (this.level().isClientSide()) {
                for (int i = 0; i < 5; i++) {
                    this.level().addParticle(ParticleTypes.FIREWORK,
                            this.getX() + (this.random.nextDouble() - 0.5) * 0.8,
                            this.getY() + (this.random.nextDouble() - 0.5) * 0.8,
                            this.getZ() + (this.random.nextDouble() - 0.5) * 0.8,
                            0, 0, 0);
                }
                
                // 高能元素球特有的粒子效果
                Vec3 pos = this.position();
                for (int i = 0; i < 8; i++) {
                    double angle = 2 * Math.PI * i / 8;
                    double radius = 0.6;
                    double x = pos.x + Math.cos(angle) * radius;
                    double z = pos.z + Math.sin(angle) * radius;
                    
                    this.level().addParticle(ParticleTypes.GLOW,
                            x, pos.y, z, 0, 0.05, 0);
                }
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide() && !this.entityData.get(DATA_EXPLODED)) {
            this.explode();
        }
    }

    private void explode() {
        this.entityData.set(DATA_EXPLODED, true);
        
        // 爆炸伤害
        Vec3 center = this.position();
        double radius = 3.0;
        
        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(radius))) {
            if (entity != this.getOwner() && entity.distanceToSqr(center) <= radius * radius) {
                float damage = this.entityData.get(DATA_DAMAGE);
                entity.hurt(this.damageSources().indirectMagic(this, this.getOwner()), damage);
            }
        }
        
        // 爆炸粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 20; i++) {
                double angle = 2 * Math.PI * i / 20;
                double distance = this.random.nextDouble() * 3;
                double x = center.x + Math.cos(angle) * distance;
                double z = center.z + Math.sin(angle) * distance;
                
                this.level().addParticle(ParticleTypes.EXPLOSION,
                        x, center.y, z, 0, 0, 0);
            }
        }
        
        // 播放爆炸音效
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0f, 1.0f);
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
        compound.putBoolean("Exploded", this.entityData.get(DATA_EXPLODED));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        lifeTime = compound.getInt("LifeTime");
        this.entityData.set(DATA_EXPLODED, compound.getBoolean("Exploded"));
    }
}