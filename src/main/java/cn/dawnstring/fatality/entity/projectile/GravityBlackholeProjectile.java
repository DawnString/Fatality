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
 * 重力黑洞弹投射物
 * 特性：击中目标后产生重力场，吸附周围实体并持续造成伤害
 */
public class GravityBlackholeProjectile extends AbstractArrow
{
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(GravityBlackholeProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_GRAVITY_RADIUS = SynchedEntityData.defineId(GravityBlackholeProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_ACTIVATED = SynchedEntityData.defineId(GravityBlackholeProjectile.class, EntityDataSerializers.BOOLEAN);

    private int lifeTime = 0;
    private final int maxLifeTime = 120; // 6秒生命周期
    private final int gravityDuration = 60; // 重力场持续时间3秒
    private ItemStack weaponItem;
    private static final float GRAVITY_STRENGTH = 0.3f; // 重力强度

    public GravityBlackholeProjectile(EntityType<? extends GravityBlackholeProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true); // 不受重力影响
        this.entityData.set(DATA_DAMAGE, 100.0f);
        this.entityData.set(DATA_GRAVITY_RADIUS, 4.0f);
        this.entityData.set(DATA_ACTIVATED, false);
    }

    public GravityBlackholeProjectile(Level level, LivingEntity shooter, ItemStack weapon, float damage) {
        this(ModEntities.GRAVITY_BLACKHOLE_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.setBaseDamage(damage);
        this.entityData.set(DATA_DAMAGE, damage);
        this.weaponItem = weapon;
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
        
        // 设置重力场半径（基于伤害值）
        float gravityRadius = Math.min(6.0f, damage / 150.0f + 3.0f);
        this.entityData.set(DATA_GRAVITY_RADIUS, gravityRadius);
        this.entityData.set(DATA_ACTIVATED, false);
    }

    /**
     * 获取子弹伤害
     */
    public float getBulletDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    /**
     * 获取重力场半径
     */
    public float getGravityRadius() {
        return this.entityData.get(DATA_GRAVITY_RADIUS);
    }

    /**
     * 检查是否已激活重力场
     */
    public boolean isActivated() {
        return this.entityData.get(DATA_ACTIVATED);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 100.0f);
        this.entityData.define(DATA_GRAVITY_RADIUS, 4.0f);
        this.entityData.define(DATA_ACTIVATED, false);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 生成黄色重力轨迹粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 2; i++) {
                this.level().addParticle(ParticleTypes.GLOW,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                        0, 0, 0);
                
                this.level().addParticle(ParticleTypes.AMBIENT_ENTITY_EFFECT,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
                        0, -0.02, 0);
            }
        }

        // 如果已激活重力场，处理重力效果
        if (isActivated() && lifeTime < gravityDuration) {
            applyGravityEffect();
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
            activateGravityField();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();

        if (entity instanceof LivingEntity livingEntity && !this.level().isClientSide()) {
            // 对主要目标造成伤害
            Player owner = this.getOwner() instanceof Player ? (Player) this.getOwner() : null;
            livingEntity.hurt(livingEntity.damageSources().magic(), getBulletDamage() * 0.8f);
            
            // 激活重力场
            activateGravityField();
        }
    }

    /**
     * 激活重力场
     */
    private void activateGravityField() {
        if (isActivated()) {
            return;
        }

        this.entityData.set(DATA_ACTIVATED, true);
        
        // 播放重力场激活音效
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.BEACON_DEACTIVATE, SoundSource.NEUTRAL, 0.8F, 0.5F);

        // 生成重力场激活粒子效果
        if (this.level().isClientSide()) {
            float radius = getGravityRadius();
            for (int i = 0; i < 50; i++) {
                double angle = Math.random() * Math.PI * 2;
                double distance = Math.random() * radius;
                double x = this.getX() + Math.cos(angle) * distance;
                double z = this.getZ() + Math.sin(angle) * distance;
                double y = this.getY() + (Math.random() - 0.5) * radius;
                
                this.level().addParticle(ParticleTypes.PORTAL,
                        x, y, z,
                        0, -0.1, 0);
            }
        }
    }

    /**
     * 应用重力效果
     */
    private void applyGravityEffect() {
        if (this.level().isClientSide()) {
            return;
        }

        float gravityRadius = getGravityRadius();
        AABB gravityArea = new AABB(
                this.getX() - gravityRadius, this.getY() - gravityRadius, this.getZ() - gravityRadius,
                this.getX() + gravityRadius, this.getY() + gravityRadius, this.getZ() + gravityRadius
        );

        List<LivingEntity> affectedEntities = this.level().getEntitiesOfClass(LivingEntity.class, gravityArea);
        Player owner = this.getOwner() instanceof Player ? (Player) this.getOwner() : null;

        for (LivingEntity entity : affectedEntities) {
            // 跳过自己和玩家
            if (entity == owner || entity == this.getOwner()) {
                continue;
            }

            // 计算到重力中心的向量
            Vec3 toCenter = this.position().subtract(entity.position());
            double distance = toCenter.length();

            // 如果实体在重力范围内
            if (distance <= gravityRadius && distance > 0.5) {
                // 计算重力强度（距离越近，引力越强）
                float gravityStrength = GRAVITY_STRENGTH * (float)(1.0 - (distance / gravityRadius));
                
                // 应用引力
                Vec3 pullDirection = toCenter.normalize().scale(gravityStrength);
                entity.setDeltaMovement(entity.getDeltaMovement().add(pullDirection));
                
                // 每10tick造成一次伤害
                if (lifeTime % 10 == 0) {
                    float damage = getBulletDamage() * 0.1f; // 持续伤害为子弹伤害的10%
                    entity.hurt(entity.damageSources().magic(), damage);
                }

                // 生成重力粒子效果
                if (this.level().isClientSide() && lifeTime % 5 == 0) {
                    Vec3 entityPos = entity.position();
                    this.level().addParticle(ParticleTypes.REVERSE_PORTAL,
                            entityPos.x, entityPos.y + 1, entityPos.z,
                            0, 0.1, 0);
                }
            }
        }

        // 生成重力场中心粒子效果
        if (this.level().isClientSide() && lifeTime % 3 == 0) {
            this.level().addParticle(ParticleTypes.PORTAL,
                    this.getX(), this.getY(), this.getZ(),
                    (this.random.nextDouble() - 0.5) * 0.1,
                    (this.random.nextDouble() - 0.5) * 0.1,
                    (this.random.nextDouble() - 0.5) * 0.1);
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
        if (compound.contains("GravityRadius")) {
            this.entityData.set(DATA_GRAVITY_RADIUS, compound.getFloat("GravityRadius"));
        }
        if (compound.contains("Activated")) {
            this.entityData.set(DATA_ACTIVATED, compound.getBoolean("Activated"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putFloat("BulletDamage", getBulletDamage());
        compound.putFloat("GravityRadius", getGravityRadius());
        compound.putBoolean("Activated", isActivated());
    }
}