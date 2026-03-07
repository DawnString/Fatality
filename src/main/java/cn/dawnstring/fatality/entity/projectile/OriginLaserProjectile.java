package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class OriginLaserProjectile extends AbstractArrow {
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(OriginLaserProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_IS_ACTIVE = SynchedEntityData.defineId(OriginLaserProjectile.class, EntityDataSerializers.BOOLEAN);

    private int lifeTime = 0;
    private int maxLifeTime = 2000; // 100秒
    private LivingEntity target;
    private Vec3 startPos;
    private Vec3 endPos;
    private boolean hasInitialized = false;
    private boolean slowTurning = true; // 缓慢转向
    private double maxDistance = 100.0; // 最大距离100格
    private boolean bossImmobile = true; // boss不能移动

    public OriginLaserProjectile(EntityType<? extends OriginLaserProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.setInvulnerable(true);
        this.entityData.set(DATA_DAMAGE, 350.0f);
        this.entityData.set(DATA_IS_ACTIVE, false);
    }

    public OriginLaserProjectile(Level level, LivingEntity owner, float damage, LivingEntity target) {
        super(ModEntities.ORIGIN_LASER_PROJECTILE.get(), owner, level);
        this.entityData.set(DATA_DAMAGE, damage);
        this.entityData.set(DATA_IS_ACTIVE, false);
        this.target = target;
        this.setNoGravity(true);
    }

    // 设置最大生命周期（4800 tick = 240秒）
    public void setMaxAge(int maxAge) {
        this.maxLifeTime = maxAge;
    }

    // 设置缓慢转向
    public void setSlowTurning(boolean slowTurning) {
        this.slowTurning = slowTurning;
    }

    // 设置最大距离
    public void setMaxDistance(double maxDistance) {
        this.maxDistance = maxDistance;
    }

    // 设置boss不能移动
    public void setBossImmobile(boolean bossImmobile) {
        this.bossImmobile = bossImmobile;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 350.0f);
        this.entityData.define(DATA_IS_ACTIVE, false);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 性能优化：超过寿命的弹幕及时销毁
        if (lifeTime >= maxLifeTime) {
            this.discard();
            return;
        }

        if (!hasInitialized) {
            initializeLaser();
            hasInitialized = true;
        }

        // 缓慢转向追踪目标
        if (target != null && target.isAlive() && slowTurning) {
            updateLaserDirection();
        }

        // 激光效果
        if (this.level().isClientSide) {
            spawnLaserParticles();
        }

        // 持续伤害
        if (!this.level().isClientSide && this.tickCount % 10 == 0) { // 每0.5秒伤害一次
            applyLaserDamage();
        }
    }

    private void initializeLaser() {
        this.startPos = this.position();
        if (target != null) {
            this.endPos = target.position().add(0, target.getEyeHeight() / 2, 0);
        } else {
            this.endPos = this.startPos.add(this.getDeltaMovement().normalize().scale(maxDistance));
        }
        this.entityData.set(DATA_IS_ACTIVE, true);
    }

    private void updateLaserDirection() {
        Vec3 currentPos = this.position();
        Vec3 targetPos = target.position().add(0, target.getEyeHeight() / 2, 0);
        Vec3 direction = targetPos.subtract(currentPos).normalize();

        // 缓慢转向
        Vec3 currentDirection = this.getDeltaMovement().normalize();
        Vec3 newDirection = currentDirection.add(direction.scale(0.02)).normalize();
        this.setDeltaMovement(newDirection.scale(2.0));

        // 更新终点位置
        this.endPos = currentPos.add(newDirection.scale(maxDistance));
    }

    private void spawnLaserParticles() {
        if (!this.entityData.get(DATA_IS_ACTIVE)) return;

        Vec3 currentPos = this.position();
        Vec3 direction = this.getDeltaMovement().normalize();

        // 激光主体粒子
        for (int i = 0; i < 20; i++) {
            double distance = i * 0.5;
            Vec3 particlePos = currentPos.add(direction.scale(distance));

            // 起源激光特有的粒子效果
            this.level().addParticle(ParticleTypes.END_ROD,
                    particlePos.x + (this.random.nextDouble() - 0.5) * 0.1,
                    particlePos.y + (this.random.nextDouble() - 0.5) * 0.1,
                    particlePos.z + (this.random.nextDouble() - 0.5) * 0.1,
                    0, 0, 0);

            // 发光粒子
            this.level().addParticle(ParticleTypes.GLOW,
                    particlePos.x + (this.random.nextDouble() - 0.5) * 0.2,
                    particlePos.y + (this.random.nextDouble() - 0.5) * 0.2,
                    particlePos.z + (this.random.nextDouble() - 0.5) * 0.2,
                    0, 0.05, 0);
        }

        // 激光端点效果
        this.level().addParticle(ParticleTypes.FLASH,
                currentPos.x, currentPos.y, currentPos.z, 0, 0, 0);
        this.level().addParticle(ParticleTypes.FLASH,
                endPos.x, endPos.y, endPos.z, 0, 0, 0);
    }

    private void applyLaserDamage() {
        if (!this.entityData.get(DATA_IS_ACTIVE)) return;

        Vec3 currentPos = this.position();
        Vec3 direction = this.getDeltaMovement().normalize();

        // 检查激光路径上的所有实体
        for (int i = 0; i < 20; i++) {
            double distance = i * 0.5;
            Vec3 checkPos = currentPos.add(direction.scale(distance));

            AABB checkBox = new AABB(checkPos.x - 0.5, checkPos.y - 0.5, checkPos.z - 0.5,
                                     checkPos.x + 0.5, checkPos.y + 0.5, checkPos.z + 0.5);

            for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, checkBox)) {
                if (entity != this.getOwner()) {
                    float damage = this.entityData.get(DATA_DAMAGE);
                    entity.hurt(this.damageSources().indirectMagic(this, this.getOwner()), damage);
                }
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        // 起源激光不因碰撞而消失，继续存在
        if (result instanceof EntityHitResult entityResult) {
            Entity entity = entityResult.getEntity();
            if (entity instanceof LivingEntity livingEntity && livingEntity != this.getOwner()) {
                float damage = this.entityData.get(DATA_DAMAGE);
                livingEntity.hurt(this.damageSources().indirectMagic(this, this.getOwner()), damage);
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
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", lifeTime);
        compound.putBoolean("IsActive", this.entityData.get(DATA_IS_ACTIVE));
        compound.putBoolean("SlowTurning", slowTurning);
        compound.putDouble("MaxDistance", maxDistance);
        compound.putBoolean("BossImmobile", bossImmobile);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        lifeTime = compound.getInt("LifeTime");
        this.entityData.set(DATA_IS_ACTIVE, compound.getBoolean("IsActive"));
        slowTurning = compound.getBoolean("SlowTurning");
        maxDistance = compound.getDouble("MaxDistance");
        bossImmobile = compound.getBoolean("BossImmobile");
    }
}