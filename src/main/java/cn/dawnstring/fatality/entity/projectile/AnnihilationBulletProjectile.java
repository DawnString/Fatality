package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.items.weapon.ranged.AnnihilationJudgment;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.UUID;

public class AnnihilationBulletProjectile extends AbstractArrow
{
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(AnnihilationBulletProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_IS_MARKED_TARGET = SynchedEntityData.defineId(AnnihilationBulletProjectile.class, EntityDataSerializers.BOOLEAN);

    private int lifeTime = 0;
    private final int maxLifeTime = 200; // 10秒生命周期，适合狙击枪
    private ItemStack weaponItem; // 存储发射的武器
    private UUID targetUUID; // 存储目标UUID，用于标记判断

    public AnnihilationBulletProjectile(EntityType<? extends AnnihilationBulletProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(false); // 子弹有重力，模拟真实弹道
        this.entityData.set(DATA_DAMAGE, 3750.0f); // 默认伤害
        this.entityData.set(DATA_IS_MARKED_TARGET, false); // 默认不是标记目标
    }

    public AnnihilationBulletProjectile(Level level, LivingEntity shooter, ItemStack weapon, float damage, UUID targetUUID, boolean isMarkedTarget) {
        this(ModEntities.ANNIHILATION_BULLET_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.setBaseDamage(damage);
        this.entityData.set(DATA_DAMAGE, damage);
        this.weaponItem = weapon;
        this.targetUUID = targetUUID;
        this.entityData.set(DATA_IS_MARKED_TARGET, isMarkedTarget);
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
        
        // 设置更高的速度和更远的射程
        Vec3 lookVec = shooter.getLookAngle();
        this.shoot(lookVec.x, lookVec.y, lookVec.z, 5.0f, 0.0f); // 高速、无散射
    }

    /**
     * 获取子弹伤害（从同步数据中读取）
     */
    public float getBulletDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    /**
     * 判断是否为标记目标
     */
    public boolean isMarkedTarget() {
        return this.entityData.get(DATA_IS_MARKED_TARGET);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 3750.0f);
        this.entityData.define(DATA_IS_MARKED_TARGET, false);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 生成狙击子弹轨迹粒子效果（星光效果）
        if (this.level().isClientSide()) {
            for (int i = 0; i < 2; i++) {
                this.level().addParticle(ParticleTypes.END_ROD,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
                        0, 0, 0);
            }
            
            // 如果是标记目标，生成特殊粒子效果
            if (isMarkedTarget()) {
                this.level().addParticle(ParticleTypes.GLOW,
                        this.getX(), this.getY(), this.getZ(),
                        0, 0.1, 0);
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
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.2F);

            // 生成命中粒子效果
            if (this.level().isClientSide()) {
                for (int i = 0; i < 8; i++) {
                    this.level().addParticle(ParticleTypes.GLOW,
                            this.getX() + (this.random.nextDouble() - 0.5) * 1.0,
                            this.getY() + (this.random.nextDouble() - 0.5) * 1.0,
                            this.getZ() + (this.random.nextDouble() - 0.5) * 1.0,
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

        if (entity instanceof LivingEntity livingEntity) {
            // 播放实体命中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 0.8F);
            
            // 检查武器类型并触发特殊效果
            if (this.getOwner() instanceof Player player) {
                if (weaponItem != null && !weaponItem.isEmpty()) {
                    // 检查是否是寂灭裁决
                    if (weaponItem.getItem() instanceof AnnihilationJudgment) {
                        AnnihilationJudgment.onBulletHit(livingEntity, level(), player, targetUUID, isMarkedTarget(), getBulletDamage());
                    }
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
        if (compound.contains("BulletDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("BulletDamage"));
        }
        if (compound.contains("IsMarkedTarget")) {
            this.entityData.set(DATA_IS_MARKED_TARGET, compound.getBoolean("IsMarkedTarget"));
        }
        if (compound.contains("TargetUUID")) {
            this.targetUUID = compound.getUUID("TargetUUID");
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putFloat("BulletDamage", getBulletDamage());
        compound.putBoolean("IsMarkedTarget", isMarkedTarget());
        if (targetUUID != null) {
            compound.putUUID("TargetUUID", targetUUID);
        }
    }
}