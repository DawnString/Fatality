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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;

public class WaterArrowProjectile extends AbstractArrow
{
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(WaterArrowProjectile.class, EntityDataSerializers.FLOAT);

    private int lifeTime = 0;
    private final int maxLifeTime = 80; // 4秒生命周期（比火球长）

    public WaterArrowProjectile(EntityType<? extends WaterArrowProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.entityData.set(DATA_DAMAGE, 8.0f); // 默认伤害
    }

    public WaterArrowProjectile(Level level, LivingEntity shooter, ItemStack weapon, float damage) {
        this(ModEntities.WATER_ARROW_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.setBaseDamage(damage); // 使用传入的伤害值
        this.entityData.set(DATA_DAMAGE, damage); // 同步伤害数据
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    /**
     * 获取水箭伤害（从同步数据中读取）
     */
    public float getWaterArrowDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 8.0f);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 生成水粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 2; i++) {
                this.level().addParticle(ParticleTypes.BUBBLE,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
                        0, 0.1, 0);
            }

            // 添加水花粒子
            if (this.isInWater()) {
                for (int i = 0; i < 3; i++) {
                    this.level().addParticle(ParticleTypes.SPLASH,
                            this.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                            this.getY(),
                            this.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                            0, 0.2, 0);
                }
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
            // 播放水花音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.8F, 1.0F);

            // 生成水花粒子效果
            if (this.level().isClientSide()) {
                for (int i = 0; i < 8; i++) {
                    this.level().addParticle(ParticleTypes.SPLASH,
                            this.getX() + (this.random.nextDouble() - 0.5),
                            this.getY(),
                            this.getZ() + (this.random.nextDouble() - 0.5),
                            0, 0.3, 0);
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
            // 施加减速效果3秒
            livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1)); // 3秒减速II

            // 如果是燃烧的实体，熄灭火焰
            if (livingEntity.isOnFire()) {
                livingEntity.clearFire();
            }

            // 播放命中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 0.6F, 1.2F);
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
        if (compound.contains("WaterArrowDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("WaterArrowDamage"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putFloat("WaterArrowDamage", getWaterArrowDamage());
    }
}