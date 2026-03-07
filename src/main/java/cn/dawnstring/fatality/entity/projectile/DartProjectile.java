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
import net.minecraftforge.network.NetworkHooks;

public class DartProjectile extends AbstractArrow
{
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(DartProjectile.class, EntityDataSerializers.FLOAT);

    private int lifeTime = 0;
    private final int maxLifeTime = 40; // 2秒生命周期，比箭矢短

    public DartProjectile(EntityType<? extends DartProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(false); // 飞镖有重力，更真实
        this.entityData.set(DATA_DAMAGE, 8.0f); // 默认伤害
    }

    public DartProjectile(Level level, LivingEntity shooter, ItemStack weapon, float damage) {
        this(ModEntities.DART_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.setBaseDamage(damage); // 使用传入的伤害值
        this.entityData.set(DATA_DAMAGE, damage); // 同步伤害数据
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    /**
     * 获取飞镖伤害（从同步数据中读取）
     */
    public float getDartDamage() {
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

        // 生成飞镖轨迹粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 1; i++) { // 少量粒子，保持性能
                this.level().addParticle(ParticleTypes.CRIT,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.1,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.1,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.1,
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
            // 播放命中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ARROW_HIT, SoundSource.BLOCKS, 0.6F, 1.8F); // 更高音调

            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();

        if (entity instanceof LivingEntity livingEntity) {
            // 播放命中实体音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 0.7F, 1.6F);
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
        if (compound.contains("DartDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("DartDamage"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putFloat("DartDamage", getDartDamage());
    }
}