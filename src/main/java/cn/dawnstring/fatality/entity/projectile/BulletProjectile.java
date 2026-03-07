package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.items.weapon.ranged.BloodSpiritBlaster;
import cn.dawnstring.fatality.items.weapon.ranged.DragonFlameBlaster;
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

public class BulletProjectile extends AbstractArrow
{
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(BulletProjectile.class, EntityDataSerializers.FLOAT);

    private int lifeTime = 0;
    private final int maxLifeTime = 100; // 2秒生命周期
    private ItemStack weaponItem; // 存储发射的武器

    public BulletProjectile(EntityType<? extends BulletProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true); // 子弹有重力，模拟真实弹道
        this.entityData.set(DATA_DAMAGE, 15.0f); // 默认伤害
    }

    public BulletProjectile(Level level, LivingEntity shooter, ItemStack weapon, float damage) {
        this(ModEntities.BULLET_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.setBaseDamage(damage); // 使用传入的伤害值
        this.entityData.set(DATA_DAMAGE, damage); // 同步伤害数据
        this.weaponItem = weapon; // 存储武器信息
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    /**
     * 获取子弹伤害（从同步数据中读取）
     */
    public float getBulletDamage() {
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

        // 生成子弹轨迹粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 1; i++) {
                this.level().addParticle(ParticleTypes.SMOKE,
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
                    SoundEvents.ARROW_HIT, SoundSource.BLOCKS, 0.5F, 1.5F);

            // 生成命中粒子效果
            if (this.level().isClientSide()) {
                for (int i = 0; i < 3; i++) {
                    this.level().addParticle(ParticleTypes.CRIT,
                            this.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                            this.getY() + (this.random.nextDouble() - 0.5) * 0.5,
                            this.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                            0, 0.1, 0);
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
                    SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 0.8F, 1.0F);
            
            // 检查武器类型并触发特殊效果
            if (this.getOwner() instanceof Player player) {
                if (weaponItem != null && !weaponItem.isEmpty()) {
                    // 检查是否是血灵爆破者
                    if (weaponItem.getItem() instanceof BloodSpiritBlaster) {
                        BloodSpiritBlaster.onBulletHit(livingEntity, level(), player);
                    }
                    // 检查是否是龙焰喷射器
                    else if (weaponItem.getItem() instanceof DragonFlameBlaster) {
                        DragonFlameBlaster.onBulletHit(livingEntity, level(), player);
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
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putFloat("BulletDamage", getBulletDamage());
    }
}