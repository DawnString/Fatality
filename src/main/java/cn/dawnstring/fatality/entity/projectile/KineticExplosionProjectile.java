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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import java.util.List;

/**
 * 动能爆炸弹投射物
 * 特性：击中目标后爆炸，对周围造成范围伤害
 */
public class KineticExplosionProjectile extends AbstractArrow
{
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(KineticExplosionProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_EXPLOSION_RADIUS = SynchedEntityData.defineId(KineticExplosionProjectile.class, EntityDataSerializers.FLOAT);

    private int lifeTime = 0;
    private final int maxLifeTime = 60; // 3秒生命周期
    private ItemStack weaponItem;

    public KineticExplosionProjectile(EntityType<? extends KineticExplosionProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true); // 不受重力影响
        this.entityData.set(DATA_DAMAGE, 100.0f);
        this.entityData.set(DATA_EXPLOSION_RADIUS, 3.0f);
    }

    public KineticExplosionProjectile(Level level, LivingEntity shooter, ItemStack weapon, float damage) {
        this(ModEntities.KINETIC_EXPLOSION_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.setBaseDamage(damage);
        this.entityData.set(DATA_DAMAGE, damage);
        this.weaponItem = weapon;
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
        
        // 设置爆炸半径（基于伤害值）
        float explosionRadius = Math.min(5.0f, damage / 200.0f + 2.0f);
        this.entityData.set(DATA_EXPLOSION_RADIUS, explosionRadius);
    }

    /**
     * 获取子弹伤害
     */
    public float getBulletDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    /**
     * 获取爆炸半径
     */
    public float getExplosionRadius() {
        return this.entityData.get(DATA_EXPLOSION_RADIUS);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 100.0f);
        this.entityData.define(DATA_EXPLOSION_RADIUS, 3.0f);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 生成红色轨迹粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 2; i++) {
                this.level().addParticle(ParticleTypes.FLAME,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
                        0, 0, 0);
                
                this.level().addParticle(ParticleTypes.LAVA,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                        0, 0.05, 0);
            }
        }

        // 检查生命周期结束
        if (lifeTime >= maxLifeTime) {
            explode();
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            explode();
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide()) {
            explode();
            this.discard();
        }
    }

    /**
     * 爆炸方法
     */
    private void explode() {
        if (this.level().isClientSide()) {
            return;
        }

        float explosionRadius = getExplosionRadius();
        
        // 播放爆炸音效
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 1.0F, 0.8F);

        // 生成爆炸粒子效果
        for (int i = 0; i < 30; i++) {
            double offsetX = (this.random.nextDouble() - 0.5) * explosionRadius * 2;
            double offsetY = (this.random.nextDouble() - 0.5) * explosionRadius * 2;
            double offsetZ = (this.random.nextDouble() - 0.5) * explosionRadius * 2;
            
            this.level().addParticle(ParticleTypes.EXPLOSION,
                    this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ,
                    0, 0, 0);
            
            this.level().addParticle(ParticleTypes.FLAME,
                    this.getX() + offsetX * 0.5, this.getY() + offsetY * 0.5, this.getZ() + offsetZ * 0.5,
                    0, 0.1, 0);
        }

        // 对爆炸范围内的实体造成伤害
        AABB explosionArea = new AABB(
                this.getX() - explosionRadius, this.getY() - explosionRadius, this.getZ() - explosionRadius,
                this.getX() + explosionRadius, this.getY() + explosionRadius, this.getZ() + explosionRadius
        );

        List<LivingEntity> affectedEntities = this.level().getEntitiesOfClass(LivingEntity.class, explosionArea);
        Player owner = this.getOwner() instanceof Player ? (Player) this.getOwner() : null;

        for (LivingEntity entity : affectedEntities) {
            // 不伤害自己
            if (entity == owner) {
                continue;
            }
            
            // 计算距离伤害衰减
            double distance = entity.distanceTo(this);
            float distanceMultiplier = Math.max(0.1f, (float)(1.0 - (distance / explosionRadius)));
            float finalDamage = getBulletDamage() * distanceMultiplier * 0.6f; // 爆炸伤害为子弹伤害的60%
            
            // 应用伤害
            entity.hurt(entity.damageSources().explosion(this, owner), finalDamage);
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
        if (compound.contains("ExplosionRadius")) {
            this.entityData.set(DATA_EXPLOSION_RADIUS, compound.getFloat("ExplosionRadius"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putFloat("BulletDamage", getBulletDamage());
        compound.putFloat("ExplosionRadius", getExplosionRadius());
    }
}