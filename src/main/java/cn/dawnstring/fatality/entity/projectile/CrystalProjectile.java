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

public class CrystalProjectile extends AbstractArrow {
    private static final EntityDataAccessor<Boolean> DATA_SHATTERED = SynchedEntityData.defineId(CrystalProjectile.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(CrystalProjectile.class, EntityDataSerializers.FLOAT);

    private int lifeTime = 0;
    private final int maxLifeTime = 100; // 5秒生命周期

    public CrystalProjectile(EntityType<? extends CrystalProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.entityData.set(DATA_DAMAGE, 15.0f); // 默认伤害
    }

    public CrystalProjectile(Level level, LivingEntity shooter, ItemStack weapon, float damage) {
        this(ModEntities.CRYSTAL_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.setBaseDamage(damage);
        this.entityData.set(DATA_DAMAGE, damage);
        this.pickup = AbstractArrow.Pickup.DISALLOWED;

        // 设置水晶的初始速度
        Vec3 lookVec = shooter.getLookAngle();
        this.setDeltaMovement(lookVec.scale(1.5));
    }

    /**
     * 获取水晶伤害
     */
    public float getCrystalDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SHATTERED, false);
        this.entityData.define(DATA_DAMAGE, 15.0f);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 生成水晶粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 3; i++) {
                this.level().addParticle(ParticleTypes.END_ROD,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                        0, 0.1, 0);
                this.level().addParticle(ParticleTypes.GLOW,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
                        0, 0.05, 0);
            }
        }

        // 检查生命周期结束
        if (lifeTime >= maxLifeTime && !this.entityData.get(DATA_SHATTERED)) {
            this.shatter();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide() && !this.entityData.get(DATA_SHATTERED)) {
            this.shatter();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();

        if (entity instanceof LivingEntity livingEntity && !this.entityData.get(DATA_SHATTERED)) {
            // 对目标造成伤害
            float damage = getCrystalDamage();
            livingEntity.hurt(this.damageSources().indirectMagic(this, this.getOwner()), damage);

            // 短暂减速效果
            livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().scale(0.8));
        }
    }

    private void shatter() {
        if (this.entityData.get(DATA_SHATTERED)) return;

        this.entityData.set(DATA_SHATTERED, true);

        // 播放水晶破碎音效
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.0F, 1.2F);

        // 生成破碎粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 15; i++) {
                this.level().addParticle(ParticleTypes.ITEM_SNOWBALL,
                        this.getX() + (this.random.nextDouble() - 0.5) * 1.0,
                        this.getY() + (this.random.nextDouble() - 0.5) * 1.0,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 1.0,
                        (this.random.nextDouble() - 0.5) * 0.2,
                        (this.random.nextDouble() - 0.5) * 0.2,
                        (this.random.nextDouble() - 0.5) * 0.2);
                this.level().addParticle(ParticleTypes.GLOW_SQUID_INK,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.8,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.8,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.8,
                        0, 0.1, 0);
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
        if (compound.contains("CrystalDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("CrystalDamage"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putFloat("CrystalDamage", getCrystalDamage());
    }
}
