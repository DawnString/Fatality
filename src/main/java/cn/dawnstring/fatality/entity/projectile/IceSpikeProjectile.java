package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.effects.FreezeEffect;
import cn.dawnstring.fatality.registry.ModEffects;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;

/**
 * 冰刺投射物 - IceSpikeSpear的专属投射物
 * 特性：直线飞行、冰系粒子效果、冻结效果叠加
 */
public class IceSpikeProjectile extends AbstractArrow
{
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(IceSpikeProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_FREEZE_LEVEL = SynchedEntityData.defineId(IceSpikeProjectile.class, EntityDataSerializers.INT);

    private int lifeTime = 0;
    private final int maxLifeTime = 100; // 5秒生命周期
    private boolean hasHit = false;

    public IceSpikeProjectile(EntityType<? extends IceSpikeProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true); // 冰刺无重力，直线飞行
        this.entityData.set(DATA_DAMAGE, 15.0f); // 默认伤害
        this.entityData.set(DATA_FREEZE_LEVEL, 1); // 默认冻结等级
    }

    public IceSpikeProjectile(Level level, Player shooter, float damage, int freezeLevel) {
        this(ModEntities.ICE_SPIKE_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.setBaseDamage(damage); // 使用传入的伤害值
        this.entityData.set(DATA_DAMAGE, damage); // 同步伤害数据
        this.entityData.set(DATA_FREEZE_LEVEL, freezeLevel); // 同步冻结等级
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    /**
     * 获取冰刺伤害（从同步数据中读取）
     */
    public float getIceSpikeDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    /**
     * 获取冻结等级（从同步数据中读取）
     */
    public int getFreezeLevel() {
        return this.entityData.get(DATA_FREEZE_LEVEL);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 15.0f);
        this.entityData.define(DATA_FREEZE_LEVEL, 1);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 生成冰刺飞行粒子效果
        if (this.level().isClientSide()) {
            // 冰晶粒子效果
            for (int i = 0; i < 3; i++) {
                this.level().addParticle(ParticleTypes.ITEM_SNOWBALL,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getDeltaMovement().x * -0.1,
                        this.getDeltaMovement().y * -0.1,
                        this.getDeltaMovement().z * -0.1);
            }

            // 雪花粒子效果
            if (this.random.nextFloat() < 0.4f) {
                this.level().addParticle(ParticleTypes.SNOWFLAKE,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                        (this.random.nextDouble() - 0.5) * 0.05,
                        (this.random.nextDouble() - 0.5) * 0.05,
                        (this.random.nextDouble() - 0.5) * 0.05);
            }

            // 冰雾拖尾效果
            if (this.tickCount % 2 == 0) {
                this.level().addParticle(ParticleTypes.CLOUD,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.1,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.1,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.1,
                        this.getDeltaMovement().x * -0.2,
                        this.getDeltaMovement().y * -0.2,
                        this.getDeltaMovement().z * -0.2);
            }
        }

        // 检查生命周期结束
        if (lifeTime >= maxLifeTime && !hasHit) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide() && !hasHit) {
            hasHit = true;

            // 播放冰刺破碎音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.0F, 1.2F);

            // 生成冰刺破碎粒子效果
            if (this.level().isClientSide()) {
                for (int i = 0; i < 20; i++) {
                    this.level().addParticle(ParticleTypes.ITEM_SNOWBALL,
                            this.getX() + (this.random.nextDouble() - 0.5) * 1.5,
                            this.getY() + (this.random.nextDouble() - 0.5) * 1.5,
                            this.getZ() + (this.random.nextDouble() - 0.5) * 1.5,
                            (this.random.nextDouble() - 0.5) * 0.3,
                            (this.random.nextDouble() - 0.5) * 0.3,
                            (this.random.nextDouble() - 0.5) * 0.3);
                }

                // 冰雾爆炸效果
                for (int i = 0; i < 15; i++) {
                    this.level().addParticle(ParticleTypes.CLOUD,
                            this.getX() + (this.random.nextDouble() - 0.5) * 1.0,
                            this.getY() + (this.random.nextDouble() - 0.5) * 1.0,
                            this.getZ() + (this.random.nextDouble() - 0.5) * 1.0,
                            (this.random.nextDouble() - 0.5) * 0.2,
                            0.1,
                            (this.random.nextDouble() - 0.5) * 0.2);
                }
            }

            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();

        if (entity instanceof LivingEntity livingEntity && !hasHit) {
            hasHit = true;

            // 对目标造成伤害
            float damage = getIceSpikeDamage();
            livingEntity.hurt(this.damageSources().indirectMagic(this, this.getOwner()), damage);

            // 播放实体命中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);

            // 应用冻结效果
            int freezeLevel = getFreezeLevel();
            if (freezeLevel > 0) {
                MobEffectInstance currentFreeze = livingEntity.getEffect(ModEffects.FREEZE.get());
                int newLevel = freezeLevel;
                
                // 叠加冻结效果（最多5级）
                if (currentFreeze != null) {
                    newLevel = Math.min(5, currentFreeze.getAmplifier() + 1 + freezeLevel);
                }
                
                // 应用冻结效果，持续5秒（100tick）
                livingEntity.addEffect(new MobEffectInstance(ModEffects.FREEZE.get(), 100, newLevel - 1));
            }

            // 生成命中特效
            if (this.level().isClientSide()) {
                for (int i = 0; i < 10; i++) {
                    this.level().addParticle(ParticleTypes.ITEM_SNOWBALL,
                            livingEntity.getX() + (this.random.nextDouble() - 0.5) * 1.0,
                            livingEntity.getY() + this.random.nextDouble() * 2.0,
                            livingEntity.getZ() + (this.random.nextDouble() - 0.5) * 1.0,
                            (this.random.nextDouble() - 0.5) * 0.1,
                            0.1,
                            (this.random.nextDouble() - 0.5) * 0.1);
                }
            }
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
        this.hasHit = compound.getBoolean("HasHit");
        if (compound.contains("IceSpikeDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("IceSpikeDamage"));
        }
        if (compound.contains("FreezeLevel")) {
            this.entityData.set(DATA_FREEZE_LEVEL, compound.getInt("FreezeLevel"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putBoolean("HasHit", hasHit);
        compound.putFloat("IceSpikeDamage", getIceSpikeDamage());
        compound.putInt("FreezeLevel", getFreezeLevel());
    }
}