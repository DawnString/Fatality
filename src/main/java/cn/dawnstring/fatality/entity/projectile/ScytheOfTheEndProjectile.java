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

public class ScytheOfTheEndProjectile extends AbstractArrow {
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(ScytheOfTheEndProjectile.class, EntityDataSerializers.FLOAT);

    private int lifeTime = 0;
    private final int maxLifeTime = 100; // 5秒生命周期
    private ItemStack weaponStack;

    public ScytheOfTheEndProjectile(EntityType<? extends ScytheOfTheEndProjectile> type, Level level) {
        super(type, level);
        this.weaponStack = ItemStack.EMPTY;
        this.setNoGravity(true); // 镰刀无视重力，模拟魔法投掷
        this.entityData.set(DATA_DAMAGE, 3600.0f); // 默认伤害
    }

    public ScytheOfTheEndProjectile(Level level, LivingEntity shooter, ItemStack weapon, float damage) {
        this(ModEntities.SCYTHE_OF_THE_END_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.weaponStack = weapon.copy();

        // 使用传入的伤害值
        this.setBaseDamage(damage);
        this.entityData.set(DATA_DAMAGE, damage);

        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    /**
     * 获取镰刀伤害（从同步数据中读取）
     */
    public float getScytheDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 3600.0f);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 生成镰刀飞行轨迹粒子效果 - 使用暗影主题粒子
        if (this.level().isClientSide()) {
            for (int i = 0; i < 3; i++) {
                // 使用灵魂火焰和烟幕粒子，符合终焉镰刀主题
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                        0, 0, 0);

                this.level().addParticle(ParticleTypes.SMOKE,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
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
            // 播放命中音效 - 使用更符合终焉主题的音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1.0f, 0.8f);

            // 生成命中粒子效果
            if (this.level().isClientSide()) {
                for (int i = 0; i < 15; i++) {
                    this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
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
                    SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 0.7F);

            // 添加击退效果
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
        if (compound.contains("ScytheDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("ScytheDamage"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putFloat("ScytheDamage", getScytheDamage());
    }
}
