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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/**
 * 天星投射物类
 * 特性：从天而降，击中目标时产生范围伤害和星辰粒子效果
 */
public class CelestialStarProjectile extends AbstractArrow
{
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(CelestialStarProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_AOE_RADIUS = SynchedEntityData.defineId(CelestialStarProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_HAS_IMPACTED = SynchedEntityData.defineId(CelestialStarProjectile.class, EntityDataSerializers.BOOLEAN);

    private int lifeTime = 0;
    private final int maxLifeTime = 100; // 5秒生命周期
    private Player shooter;
    private float fallSpeed = 1.5f; // 下落速度

    public CelestialStarProjectile(EntityType<? extends CelestialStarProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(false); // 受重力影响，实现下落效果
        this.entityData.set(DATA_DAMAGE, 100.0f); // 默认伤害
        this.entityData.set(DATA_AOE_RADIUS, 5.0f); // 默认范围半径
        this.entityData.set(DATA_HAS_IMPACTED, false);
    }

    public CelestialStarProjectile(Level level, Player shooter, float damage, float aoeRadius) {
        this(ModEntities.CELESTIAL_STAR_PROJECTILE.get(), level);
        this.shooter = shooter;
        this.setOwner(shooter);
        this.entityData.set(DATA_DAMAGE, damage); // 同步伤害数据
        this.entityData.set(DATA_AOE_RADIUS, aoeRadius); // 同步范围半径
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
        
        // 设置初始向下速度
        this.setDeltaMovement(0, -fallSpeed, 0);
    }

    /**
     * 获取天星伤害（从同步数据中读取）
     */
    public float getStarDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    /**
     * 获取范围伤害半径
     */
    public float getAoeRadius() {
        return this.entityData.get(DATA_AOE_RADIUS);
    }

    /**
     * 检查是否已经撞击
     */
    public boolean hasImpacted() {
        return this.entityData.get(DATA_HAS_IMPACTED);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 100.0f);
        this.entityData.define(DATA_AOE_RADIUS, 5.0f);
        this.entityData.define(DATA_HAS_IMPACTED, false);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 生成下落轨迹粒子效果
        if (this.level().isClientSide()) {
            spawnTrailParticles();
        }

        // 检查生命周期结束
        if (lifeTime >= maxLifeTime) {
            this.impact();
            return;
        }

        // 检查是否撞击地面或实体
        if (!this.level().isClientSide() && !hasImpacted()) {
            // 检查是否接近地面（Y坐标小于等于0或与方块碰撞）
            if (this.getY() <= 0 || this.isInWall()) {
                this.impact();
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide() && !hasImpacted()) {
            this.impact();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();

        if (entity instanceof LivingEntity livingEntity && !hasImpacted()) {
            // 对命中的实体造成直接伤害
            float directDamage = getStarDamage() * 1.5f; // 直接命中伤害更高
            livingEntity.hurt(this.damageSources().indirectMagic(this, shooter), directDamage);
            
            // 播放命中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.NEUTRAL, 1.0F, 1.2F);
        }
    }

    /**
     * 天星撞击效果
     */
    private void impact() {
        if (hasImpacted()) return;

        this.entityData.set(DATA_HAS_IMPACTED, true);

        // 播放撞击音效
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ENDER_DRAGON_GROWL, SoundSource.NEUTRAL, 1.0F, 0.8F);

        // 生成撞击粒子效果
        if (this.level().isClientSide()) {
            spawnImpactParticles();
        }

        // 造成范围伤害
        if (!this.level().isClientSide()) {
            applyAreaDamage();
        }

        // 延迟销毁投射物，让粒子效果有足够时间显示
        this.discard();
    }

    /**
     * 生成下落轨迹粒子
     */
    private void spawnTrailParticles() {
        // 生成金色轨迹粒子
        for (int i = 0; i < 3; i++) {
            this.level().addParticle(ParticleTypes.GLOW,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                    this.getY() + 0.5 + (this.random.nextDouble() - 0.5) * 0.5,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                    0, -0.1, 0);
            
            // 生成火焰粒子模拟星辰光芒
            this.level().addParticle(ParticleTypes.FLAME,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                    this.getY() + 0.3 + (this.random.nextDouble() - 0.5) * 0.3,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                    0, -0.05, 0);
        }
    }

    /**
     * 生成撞击粒子效果
     */
    private void spawnImpactParticles() {
        float radius = getAoeRadius();
        
        // 生成撞击中心粒子
        for (int i = 0; i < 15; i++) {
            this.level().addParticle(ParticleTypes.EXPLOSION,
                    this.getX() + (this.random.nextDouble() - 0.5) * 2.0,
                    this.getY() + (this.random.nextDouble() - 0.5) * 2.0,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 2.0,
                    0, 0.1, 0);
        }

        // 生成范围粒子效果
        for (int i = 0; i < 30; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2;
            double distance = this.random.nextDouble() * radius;
            double x = this.getX() + Math.cos(angle) * distance;
            double z = this.getZ() + Math.sin(angle) * distance;
            double y = this.getY() + this.random.nextDouble() * 2.0;

            // 金色光芒粒子
            this.level().addParticle(ParticleTypes.GLOW, x, y, z, 0, 0.1, 0);
            
            // 火焰粒子
            this.level().addParticle(ParticleTypes.FLAME, x, y, z, 
                    (this.random.nextDouble() - 0.5) * 0.2, 
                    0.2, 
                    (this.random.nextDouble() - 0.5) * 0.2);
        }
    }

    /**
     * 应用范围伤害
     */
    private void applyAreaDamage() {
        Vec3 center = this.position();
        float radius = getAoeRadius();

        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(radius))) {
            if (entity != this.getOwner()) {
                double distance = entity.distanceToSqr(center);
                if (distance <= radius * radius) {
                    // 距离越近伤害越高
                    double actualDistance = Math.sqrt(distance);
                    float distanceMultiplier = (float) Math.max(0.3, 1.0 - (actualDistance / radius));
                    float damage = getStarDamage() * distanceMultiplier;
                    
                    if (damage > 0) {
                        entity.hurt(this.damageSources().indirectMagic(this, shooter), damage);
                        
                        // 轻微击退效果
                        Vec3 knockbackVec = entity.position().subtract(center).normalize().scale(0.3);
                        entity.setDeltaMovement(entity.getDeltaMovement().add(knockbackVec));
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
        if (compound.contains("StarDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("StarDamage"));
        }
        if (compound.contains("AoeRadius")) {
            this.entityData.set(DATA_AOE_RADIUS, compound.getFloat("AoeRadius"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putFloat("StarDamage", getStarDamage());
        compound.putFloat("AoeRadius", getAoeRadius());
    }
}