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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/**
 * 死灵飞弹投射物 - 具有追踪功能的魔法投射物
 * 特性：自动追踪目标、死灵特效、高伤害
 */
public class PurgatoryMissileProjectile extends AbstractArrow
{
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(PurgatoryMissileProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_TARGET_ID = SynchedEntityData.defineId(PurgatoryMissileProjectile.class, EntityDataSerializers.INT);

    private int lifeTime = 0;
    private final int maxLifeTime = 200; // 10秒生命周期
    private LivingEntity target;
    private final double trackingSpeed = 0.15; // 追踪速度

    public PurgatoryMissileProjectile(EntityType<? extends PurgatoryMissileProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true); // 死灵飞弹无重力
        this.entityData.set(DATA_DAMAGE, 710.0f); // 默认伤害
        this.entityData.set(DATA_TARGET_ID, -1); // 默认无目标
    }

    public PurgatoryMissileProjectile(Level level, LivingEntity shooter, LivingEntity target, float damage) {
        this(ModEntities.PURGATORY_MISSILE_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.target = target;
        this.setBaseDamage(damage);
        this.entityData.set(DATA_DAMAGE, damage);
        this.entityData.set(DATA_TARGET_ID, target != null ? target.getId() : -1);
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    /**
     * 获取死灵飞弹伤害
     */
    public float getMissileDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    /**
     * 获取目标实体ID
     */
    public int getTargetId() {
        return this.entityData.get(DATA_TARGET_ID);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 710.0f);
        this.entityData.define(DATA_TARGET_ID, -1);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 在服务器端处理追踪逻辑
        if (!this.level().isClientSide()) {
            // 如果目标丢失，尝试重新获取
            if (target == null || !target.isAlive()) {
                int targetId = getTargetId();
                if (targetId != -1) {
                    net.minecraft.world.entity.Entity entity = this.level().getEntity(targetId);
                    if (entity instanceof LivingEntity livingEntity) {
                        target = livingEntity;
                    } else {
                        target = null;
                    }
                }
            }

            // 追踪目标
            if (target != null && target.isAlive()) {
                Vec3 targetPos = target.position().add(0, target.getEyeHeight() * 0.5, 0);
                Vec3 currentPos = this.position();
                Vec3 direction = targetPos.subtract(currentPos).normalize();
                
                // 设置速度朝向目标
                this.setDeltaMovement(this.getDeltaMovement().add(direction.scale(trackingSpeed)).scale(0.9));
            }
        }

        // 生成死灵粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 4; i++) {
                // 死灵粒子效果 - 使用灵魂火焰和紫色粒子
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.4,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.4,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.4,
                        0, 0.06, 0);

                this.level().addParticle(ParticleTypes.WITCH,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                        0, 0.04, 0);
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
            this.playHitEffect();
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide()) {
            // 播放击中实体音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.SOUL_ESCAPE, SoundSource.HOSTILE, 1.0F, 0.8F);
        }
    }

    /**
     * 播放击中效果
     */
    private void playHitEffect() {
        // 播放死灵爆炸音效
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 0.6F, 1.2F);

        // 生成爆炸粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 20; i++) {
                this.level().addParticle(ParticleTypes.SOUL,
                        this.getX() + (this.random.nextDouble() - 0.5) * 2.0,
                        this.getY() + (this.random.nextDouble() - 0.5) * 2.0,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 2.0,
                        (this.random.nextDouble() - 0.5) * 0.2,
                        (this.random.nextDouble() - 0.5) * 0.2,
                        (this.random.nextDouble() - 0.5) * 0.2);
            }
        }
    }

    @Override
    protected net.minecraft.world.item.ItemStack getPickupItem() {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.lifeTime = compound.getInt("LifeTime");
        if (compound.contains("MissileDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("MissileDamage"));
        }
        if (compound.contains("TargetId")) {
            this.entityData.set(DATA_TARGET_ID, compound.getInt("TargetId"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putFloat("MissileDamage", getMissileDamage());
        compound.putInt("TargetId", getTargetId());
    }
}