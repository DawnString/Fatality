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
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;

/**
 * 幽灵投射物 - 具有幽灵特性的魔法投射物
 */
public class GhostProjectile extends AbstractArrow
{
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(GhostProjectile.class, EntityDataSerializers.FLOAT);

    private int lifeTime = 0;
    private final int maxLifeTime = 80; // 4秒生命周期

    public GhostProjectile(EntityType<? extends GhostProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true); // 幽灵投射物无重力
        this.entityData.set(DATA_DAMAGE, 36.0f); // 默认伤害
    }

    public GhostProjectile(Level level, LivingEntity shooter, ItemStack weapon, float damage) {
        this(ModEntities.GHOST_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.setBaseDamage(damage); // 使用传入的伤害值
        this.entityData.set(DATA_DAMAGE, damage); // 同步伤害数据
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    /**
     * 获取幽灵投射物伤害（从同步数据中读取）
     */
    public float getGhostDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 36.0f);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 生成幽灵粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 3; i++) {
                // 幽灵粒子效果 - 使用灵魂火焰和末影粒子
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                        0, 0.05, 0);

                this.level().addParticle(ParticleTypes.END_ROD,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
                        0, 0.02, 0);
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

    /**
     * 播放击中效果
     */
    private void playHitEffect() {
        // 播放幽灵击中音效
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.SOUL_ESCAPE, SoundSource.BLOCKS, 0.8F, 1.2F);

        // 生成击中粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 15; i++) {
                this.level().addParticle(ParticleTypes.SOUL,
                        this.getX() + (this.random.nextDouble() - 0.5) * 1.5,
                        this.getY() + (this.random.nextDouble() - 0.5) * 1.5,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 1.5,
                        0, 0.1, 0);
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
        if (compound.contains("GhostDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("GhostDamage"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putFloat("GhostDamage", getGhostDamage());
    }
}