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
 * 灾厄飞斧投射物 - 具有快速飞行速度和旋转效果的飞斧投射物
 */
public class DisasterFlyingAxeProjectile extends AbstractArrow {
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(DisasterFlyingAxeProjectile.class, EntityDataSerializers.FLOAT);

    private int lifeTime = 0;
    private final int maxLifeTime = 60; // 3秒生命周期（更短，适合快速攻击）
    private ItemStack weaponStack;

    public DisasterFlyingAxeProjectile(EntityType<? extends DisasterFlyingAxeProjectile> type, Level level) {
        super(type, level);
        this.weaponStack = ItemStack.EMPTY;
        this.setNoGravity(false); // 飞斧有重力，但飞行速度更快
        this.entityData.set(DATA_DAMAGE, 14.0f); // 默认伤害
    }

    public DisasterFlyingAxeProjectile(Level level, LivingEntity shooter, ItemStack weapon, float damage) {
        this(ModEntities.DISASTER_FLYING_AXE_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.weaponStack = weapon.copy();

        // 使用传入的伤害值
        this.setBaseDamage(damage);
        this.entityData.set(DATA_DAMAGE, damage);

        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    /**
     * 获取飞斧伤害（从同步数据中读取）
     */
    public float getFlyingAxeDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 14.0f);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 生成飞斧飞行轨迹粒子效果 - 更密集的粒子，体现快速特性
        if (this.level().isClientSide()) {
            for (int i = 0; i < 3; i++) { // 更多粒子
                this.level().addParticle(ParticleTypes.SWEEP_ATTACK, // 使用横扫攻击粒子
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                        0, 0, 0);
            }
        }

        // 检查生命周期结束
        if (lifeTime >= maxLifeTime) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            // 播放命中音效 - 更尖锐的音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8f, 1.5f);

            // 生成命中粒子效果 - 更多的粒子
            if (this.level().isClientSide()) {
                for (int i = 0; i < 15; i++) {
                    this.level().addParticle(ParticleTypes.SWEEP_ATTACK,
                            this.getX() + (this.random.nextDouble() - 0.5) * 0.8,
                            this.getY() + (this.random.nextDouble() - 0.5) * 0.8,
                            this.getZ() + (this.random.nextDouble() - 0.5) * 0.8,
                            (this.random.nextDouble() - 0.5) * 0.3,
                            (this.random.nextDouble() - 0.5) * 0.3,
                            (this.random.nextDouble() - 0.5) * 0.3);
                }
            }

            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();

        if (entity instanceof LivingEntity livingEntity && entity != this.getOwner()) {
            // 播放实体命中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 1.1F);

            // 添加击退效果 - 更强的击退，体现飞斧的冲击力
            Vec3 knockback = this.getDeltaMovement().normalize().scale(2.0);
            livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().add(knockback));
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
        if (compound.contains("FlyingAxeDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("FlyingAxeDamage"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putFloat("FlyingAxeDamage", getFlyingAxeDamage());
    }
}