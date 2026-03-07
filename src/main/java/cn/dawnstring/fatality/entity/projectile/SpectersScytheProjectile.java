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

public class SpectersScytheProjectile extends AbstractArrow {
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(SpectersScytheProjectile.class, EntityDataSerializers.FLOAT);

    private int lifeTime = 0;
    private final int maxLifeTime = 80; // 4秒生命周期（比终焉镰刀稍短）
    private ItemStack weaponStack;

    public SpectersScytheProjectile(EntityType<? extends SpectersScytheProjectile> type, Level level) {
        super(type, level);
        this.weaponStack = ItemStack.EMPTY;
        this.setNoGravity(false); // 幽灵镰刀受重力影响，更符合幽灵主题
        this.entityData.set(DATA_DAMAGE, 144.0f); // 默认伤害
    }

    public SpectersScytheProjectile(Level level, LivingEntity shooter, ItemStack weapon, float damage) {
        this(ModEntities.SPECTERS_SCYTHE_PROJECTILE.get(), level);
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
        this.entityData.define(DATA_DAMAGE, 144.0f);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 生成幽灵镰刀飞行轨迹粒子效果 - 使用幽灵主题粒子
        if (this.level().isClientSide()) {
            for (int i = 0; i < 2; i++) {
                // 使用幽灵火焰和末影粒子，符合幽灵镰刀主题
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.4,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.4,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.4,
                        0, 0, 0);

                // 添加一些幽灵般的粒子效果
                this.level().addParticle(ParticleTypes.END_ROD,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                        (this.random.nextDouble() - 0.5) * 0.1,
                        (this.random.nextDouble() - 0.5) * 0.1,
                        (this.random.nextDouble() - 0.5) * 0.1);
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
            // 播放命中音效 - 使用幽灵主题音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1.0f, 1.0f);

            // 生成命中粒子效果
            if (this.level().isClientSide()) {
                for (int i = 0; i < 10; i++) {
                    this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                            this.getX() + (this.random.nextDouble() - 0.5) * 1.0,
                            this.getY() + (this.random.nextDouble() - 0.5) * 1.0,
                            this.getZ() + (this.random.nextDouble() - 0.5) * 1.0,
                            (this.random.nextDouble() - 0.5) * 0.2,
                            (this.random.nextDouble() - 0.5) * 0.2,
                            (this.random.nextDouble() - 0.5) * 0.2);

                    // 添加幽灵消散效果
                    this.level().addParticle(ParticleTypes.WITCH,
                            this.getX() + (this.random.nextDouble() - 0.5) * 0.8,
                            this.getY() + (this.random.nextDouble() - 0.5) * 0.8,
                            this.getZ() + (this.random.nextDouble() - 0.5) * 0.8,
                            (this.random.nextDouble() - 0.5) * 0.1,
                            (this.random.nextDouble() - 0.5) * 0.1,
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

        if (entity instanceof LivingEntity livingEntity && entity != this.getOwner()) {
            // 播放实体命中音效 - 使用幽灵主题
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GHAST_SCREAM, SoundSource.HOSTILE, 0.5F, 1.5F);

            // 添加幽灵击退效果（比终焉镰刀稍弱）
            Vec3 knockback = this.getDeltaMovement().normalize().scale(1.5);
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