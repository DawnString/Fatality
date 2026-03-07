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
 * 凤凰射线投射物 - 凤凰武器发射的射线
 * 特性：火焰粒子效果、灼烧效果、中等生命周期
 */
public class PhoenixRayProjectile extends AbstractArrow
{
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(PhoenixRayProjectile.class, EntityDataSerializers.FLOAT);

    private int lifeTime = 0;
    private final int maxLifeTime = 60; // 3秒生命周期
    private ItemStack weaponItem; // 存储发射的武器

    public PhoenixRayProjectile(EntityType<? extends PhoenixRayProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(false); // 射线有重力，模拟真实弹道
        this.entityData.set(DATA_DAMAGE, 452.0f); // 默认伤害
    }

    public PhoenixRayProjectile(Level level, LivingEntity shooter, ItemStack weapon, float damage) {
        this(ModEntities.PHOENIX_RAY_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.setBaseDamage(damage); // 使用传入的伤害值
        this.entityData.set(DATA_DAMAGE, damage); // 同步伤害数据
        this.weaponItem = weapon; // 存储武器信息
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    /**
     * 获取射线伤害（从同步数据中读取）
     */
    public float getRayDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 452.0f);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 生成凤凰射线粒子效果（火焰和火焰粒子）
        if (this.level().isClientSide()) {
            for (int i = 0; i < 3; i++) {
                // 火焰粒子
                this.level().addParticle(ParticleTypes.FLAME,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                        0, 0.05, 0);
                
                // 火焰粒子
                this.level().addParticle(ParticleTypes.FIREWORK,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
                        0, 0.03, 0);
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
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 1.2F);

            // 生成爆炸粒子效果
            if (this.level().isClientSide()) {
                for (int i = 0; i < 8; i++) {
                    this.level().addParticle(ParticleTypes.LAVA,
                            this.getX() + (this.random.nextDouble() - 0.5) * 1.0,
                            this.getY() + (this.random.nextDouble() - 0.5) * 1.0,
                            this.getZ() + (this.random.nextDouble() - 0.5) * 1.0,
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
                    SoundEvents.BLAZE_HURT, SoundSource.PLAYERS, 0.8F, 1.0F);
            
            // 应用灼烧效果
            applyBurnEffect(livingEntity);
        }
    }

    /**
     * 应用灼烧效果
     */
    private void applyBurnEffect(LivingEntity target) {
        // 设置目标着火，持续3秒
        target.setSecondsOnFire(3);
        
        // 播放灼烧音效
        this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.5F, 1.5F);
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
        if (compound.contains("RayDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("RayDamage"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putFloat("RayDamage", getRayDamage());
    }
}