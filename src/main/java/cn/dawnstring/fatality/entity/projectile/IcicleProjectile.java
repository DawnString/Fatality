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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;

/**
 * 冰锥投射物 - 从空中垂直下落造成伤害的冰系投射物
 * 特性：垂直下落、冰系粒子效果、冰冻效果
 */
public class IcicleProjectile extends AbstractArrow
{
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(IcicleProjectile.class, EntityDataSerializers.FLOAT);

    private int lifeTime = 0;
    private final int maxLifeTime = 60; // 3秒生命周期（垂直下落需要更长时间）
    private boolean hasHit = false;

    public IcicleProjectile(EntityType<? extends IcicleProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(false); // 冰锥有重力，模拟真实下落
        this.entityData.set(DATA_DAMAGE, 15.0f); // 默认伤害
    }

    public IcicleProjectile(Level level, Player shooter, float damage) {
        this(ModEntities.ICICLE_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.setBaseDamage(damage); // 使用传入的伤害值
        this.entityData.set(DATA_DAMAGE, damage); // 同步伤害数据
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    /**
     * 获取冰锥伤害（从同步数据中读取）
     */
    public float getIcicleDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 15.0f);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 生成冰锥下落粒子效果
        if (this.level().isClientSide()) {
            // 冰晶粒子效果
            for (int i = 0; i < 2; i++) {
                this.level().addParticle(ParticleTypes.ITEM_SNOWBALL,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                        0, -0.05, 0);
            }

            // 雪花粒子效果
            if (this.random.nextFloat() < 0.3f) {
                this.level().addParticle(ParticleTypes.SNOWFLAKE,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.5,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                        (this.random.nextDouble() - 0.5) * 0.1,
                        -0.1,
                        (this.random.nextDouble() - 0.5) * 0.1);
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

            // 播放冰锥破碎音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.0F, 1.2F);

            // 生成冰锥破碎粒子效果
            if (this.level().isClientSide()) {
                for (int i = 0; i < 15; i++) {
                    this.level().addParticle(ParticleTypes.ITEM_SNOWBALL,
                            this.getX() + (this.random.nextDouble() - 0.5) * 1.0,
                            this.getY() + (this.random.nextDouble() - 0.5) * 1.0,
                            this.getZ() + (this.random.nextDouble() - 0.5) * 1.0,
                            (this.random.nextDouble() - 0.5) * 0.2,
                            (this.random.nextDouble() - 0.5) * 0.2,
                            (this.random.nextDouble() - 0.5) * 0.2);
                }

                // 冰雾效果
                for (int i = 0; i < 8; i++) {
                    this.level().addParticle(ParticleTypes.CLOUD,
                            this.getX() + (this.random.nextDouble() - 0.5) * 0.8,
                            this.getY() + (this.random.nextDouble() - 0.5) * 0.8,
                            this.getZ() + (this.random.nextDouble() - 0.5) * 0.8,
                            (this.random.nextDouble() - 0.5) * 0.1,
                            0.05,
                            (this.random.nextDouble() - 0.5) * 0.1);
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
            float damage = getIcicleDamage();
            livingEntity.hurt(this.damageSources().indirectMagic(this, this.getOwner()), damage);

            // 播放实体命中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);

            // 添加短暂减速效果（冰冻效果）
            livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().scale(0.7));
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
        if (compound.contains("IcicleDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("IcicleDamage"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putBoolean("HasHit", hasHit);
        compound.putFloat("IcicleDamage", getIcicleDamage());
    }
}