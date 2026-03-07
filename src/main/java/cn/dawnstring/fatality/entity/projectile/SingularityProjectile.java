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
 * 奇点弹投射物
 * 特性：击中目标后产生大范围爆炸，造成10倍基础伤害
 */
public class SingularityProjectile extends AbstractArrow
{
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(SingularityProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_EXPLOSION_RADIUS = SynchedEntityData.defineId(SingularityProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_EXPLODED = SynchedEntityData.defineId(SingularityProjectile.class, EntityDataSerializers.BOOLEAN);

    private int lifeTime = 0;
    private final int maxLifeTime = 60; // 3秒生命周期
    private ItemStack weaponItem;
    private static final float EXPLOSION_RADIUS = 5.0f; // 5格爆炸半径

    public SingularityProjectile(EntityType<? extends SingularityProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(false); // 受重力影响
        this.entityData.set(DATA_DAMAGE, 1000.0f);
        this.entityData.set(DATA_EXPLOSION_RADIUS, EXPLOSION_RADIUS);
        this.entityData.set(DATA_EXPLODED, false);
    }

    public SingularityProjectile(Level level, LivingEntity shooter, ItemStack weapon, float baseDamage) {
        this(ModEntities.SINGULARITY_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.setBaseDamage(baseDamage * 10.0f); // 10倍基础伤害
        this.entityData.set(DATA_DAMAGE, baseDamage * 10.0f);
        this.weaponItem = weapon;
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
        this.entityData.set(DATA_EXPLODED, false);
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

    /**
     * 检查是否已爆炸
     */
    public boolean isExploded() {
        return this.entityData.get(DATA_EXPLODED);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 1000.0f);
        this.entityData.define(DATA_EXPLOSION_RADIUS, EXPLOSION_RADIUS);
        this.entityData.define(DATA_EXPLODED, false);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 生成紫色奇点轨迹粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 3; i++) {
                this.level().addParticle(ParticleTypes.PORTAL,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.5,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                        0, 0, 0);
                
                this.level().addParticle(ParticleTypes.ENCHANT,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                        0, -0.01, 0);
            }
        }

        // 检查生命周期结束
        if (lifeTime >= maxLifeTime && !isExploded()) {
            explode();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide() && !isExploded()) {
            explode();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();

        if (entity instanceof LivingEntity livingEntity && !this.level().isClientSide() && !isExploded()) {
            // 对主要目标造成伤害
            Player owner = this.getOwner() instanceof Player ? (Player) this.getOwner() : null;
            livingEntity.hurt(livingEntity.damageSources().magic(), getBulletDamage() * 0.5f);
            
            // 爆炸
            explode();
        }
    }

    /**
     * 爆炸效果
     */
    private void explode() {
        if (isExploded()) {
            return;
        }

        this.entityData.set(DATA_EXPLODED, true);
        
        // 播放爆炸音效
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 2.0F, 0.8F);

        // 生成爆炸粒子效果
        if (this.level().isClientSide()) {
            float radius = getExplosionRadius();
            for (int i = 0; i < 100; i++) {
                double angle = Math.random() * Math.PI * 2;
                double distance = Math.random() * radius;
                double x = this.getX() + Math.cos(angle) * distance;
                double z = this.getZ() + Math.sin(angle) * distance;
                double y = this.getY() + (Math.random() - 0.5) * radius;
                
                this.level().addParticle(ParticleTypes.EXPLOSION,
                        x, y, z,
                        0, 0.1, 0);
                
                this.level().addParticle(ParticleTypes.FLAME,
                        x, y, z,
                        (Math.random() - 0.5) * 0.2,
                        Math.random() * 0.3,
                        (Math.random() - 0.5) * 0.2);
            }
        }

        // 服务器端处理爆炸伤害
        if (!this.level().isClientSide()) {
            applyExplosionDamage();
        }

        // 延迟销毁投射物
        this.discard();
    }

    /**
     * 应用爆炸伤害
     */
    private void applyExplosionDamage() {
        float explosionRadius = getExplosionRadius();
        AABB explosionArea = new AABB(
                this.getX() - explosionRadius, this.getY() - explosionRadius, this.getZ() - explosionRadius,
                this.getX() + explosionRadius, this.getY() + explosionRadius, this.getZ() + explosionRadius
        );

        List<LivingEntity> affectedEntities = this.level().getEntitiesOfClass(LivingEntity.class, explosionArea);
        Player owner = this.getOwner() instanceof Player ? (Player) this.getOwner() : null;

        for (LivingEntity entity : affectedEntities) {
            // 跳过自己和玩家
            if (entity == owner || entity == this.getOwner()) {
                continue;
            }

            // 计算到爆炸中心的距离
            double distance = this.distanceTo(entity);
            
            // 如果实体在爆炸范围内
            if (distance <= explosionRadius) {
                // 计算伤害衰减（距离越远，伤害越低）
                float damageMultiplier = 1.0f - (float)(distance / explosionRadius);
                float finalDamage = getBulletDamage() * damageMultiplier;
                
                // 造成伤害
                entity.hurt(entity.damageSources().magic(), finalDamage);
                
                // 击退效果
                Vec3 knockbackDirection = entity.position().subtract(this.position()).normalize();
                double knockbackStrength = 2.0 * damageMultiplier;
                entity.setDeltaMovement(entity.getDeltaMovement().add(
                        knockbackDirection.x * knockbackStrength,
                        knockbackDirection.y * knockbackStrength + 0.5,
                        knockbackDirection.z * knockbackStrength
                ));
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
        if (compound.contains("ExplosionRadius")) {
            this.entityData.set(DATA_EXPLOSION_RADIUS, compound.getFloat("ExplosionRadius"));
        }
        if (compound.contains("Exploded")) {
            this.entityData.set(DATA_EXPLODED, compound.getBoolean("Exploded"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putFloat("BulletDamage", getBulletDamage());
        compound.putFloat("ExplosionRadius", getExplosionRadius());
        compound.putBoolean("Exploded", isExploded());
    }
}