package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.entity.boss.endofnightmare.EndOfNightmare;
import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.core.particles.DustParticleOptions;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.joml.Vector3f;

public class EnhancedElementalMissileProjectile extends AbstractArrow {
    // 同步数据定义
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(EnhancedElementalMissileProjectile.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Boolean> DATA_IS_SPIRAL = SynchedEntityData.defineId(EnhancedElementalMissileProjectile.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_EXPLODE_ON_WALL = SynchedEntityData.defineId(EnhancedElementalMissileProjectile.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> DATA_MISSILE_ID = SynchedEntityData.defineId(EnhancedElementalMissileProjectile.class, EntityDataSerializers.INT);
    
    // 私有属性
    private LivingEntity target;
    private int lifeTime = 0;
    private int maxLifeTime = 300; // 15秒生命周期
    private double spiralAngle = 0;
    private static final double SPIRAL_SPEED = 0.2;
    private Vec3 initialDirection;
    
    // 墙壁爆炸相关属性
    private float explodeDamage = 300f;
    private int explodeCount = 12;

    public EnhancedElementalMissileProjectile(EntityType<? extends EnhancedElementalMissileProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    public EnhancedElementalMissileProjectile(Level level, EndOfNightmare owner, float damage, LivingEntity target, int missileId) {
        this(ModEntities.ENHANCED_ELEMENTAL_MISSILE_PROJECTILE.get(), level);
        this.setOwner(owner);
        this.target = target;
        this.entityData.set(DATA_DAMAGE, damage);
        this.entityData.set(DATA_MISSILE_ID, missileId);
        
        // 设置飞弹生成位置和方向
        if (owner != null) {
            setupInitialPositionAndDirection(owner);
        } else {
            // 如果owner为null，确保initialDirection有默认值
            setupDefaultPositionAndDirection();
        }
    }

    private void setupInitialPositionAndDirection(EndOfNightmare owner) {
        // 设置飞弹生成位置为boss前方3格，避免与boss碰撞箱重叠
        Vec3 bossLookDirection = owner.getLookAngle().normalize();
        Vec3 spawnOffset = bossLookDirection.scale(3.0);
        Vec3 bossPos = owner.position().add(spawnOffset).add(0, 2, 0);
        this.setPos(bossPos.x, bossPos.y, bossPos.z);
        
        // 计算朝向目标的方向
        if (target != null && target.isAlive()) {
            Vec3 targetPos = target.position().add(0, 1, 0);
            this.initialDirection = targetPos.subtract(bossPos).normalize();
            
            // 立即设置初始速度和朝向
            this.setDeltaMovement(this.initialDirection.scale(2.0));
            this.setYRot((float) Math.toDegrees(Math.atan2(this.initialDirection.x, this.initialDirection.z)));
            this.setXRot((float) Math.toDegrees(Math.asin(this.initialDirection.y)));
        } else {
            // 如果没有目标，使用随机方向
            this.initialDirection = new Vec3(
                this.random.nextDouble() - 0.5,
                this.random.nextDouble() - 0.5,
                this.random.nextDouble() - 0.5
            ).normalize();
            this.setDeltaMovement(this.initialDirection.scale(1.5));
        }
    }

    private void setupDefaultPositionAndDirection() {
        // 当owner为null时，使用默认位置和方向
        this.setPos(this.getX(), this.getY(), this.getZ());
        
        // 计算朝向目标的方向
        if (target != null && target.isAlive()) {
            Vec3 targetPos = target.position().add(0, 1, 0);
            Vec3 currentPos = this.position();
            this.initialDirection = targetPos.subtract(currentPos).normalize();
            
            // 立即设置初始速度和朝向
            this.setDeltaMovement(this.initialDirection.scale(2.0));
            this.setYRot((float) Math.toDegrees(Math.atan2(this.initialDirection.x, this.initialDirection.z)));
            this.setXRot((float) Math.toDegrees(Math.asin(this.initialDirection.y)));
        } else {
            // 如果没有目标，使用随机方向
            this.initialDirection = new Vec3(
                this.random.nextDouble() - 0.5,
                this.random.nextDouble() - 0.5,
                this.random.nextDouble() - 0.5
            ).normalize();
            this.setDeltaMovement(this.initialDirection.scale(1.5));
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 0.0f);
        this.entityData.define(DATA_IS_SPIRAL, false);
        this.entityData.define(DATA_EXPLODE_ON_WALL, false);
        this.entityData.define(DATA_MISSILE_ID, 0);
    }

    // 设置最大生命周期
    public void setMaxAge(int maxAge) {
        this.maxLifeTime = maxAge;
    }

    // 设置螺旋运动
    public void setSpiralMovement(boolean spiral) {
        this.entityData.set(DATA_IS_SPIRAL, spiral);
    }

    // 设置墙壁爆炸属性
    public void setExplodeOnWall(boolean explodeOnWall) {
        this.entityData.set(DATA_EXPLODE_ON_WALL, explodeOnWall);
    }

    public void setExplodeDamage(float explodeDamage) {
        this.explodeDamage = explodeDamage;
    }

    public void setExplodeCount(int explodeCount) {
        this.explodeCount = explodeCount;
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 生成粒子效果
        if (this.level().isClientSide()) {
            spawnEnhancedParticles();
        }

        // 检查生命周期结束
        if (lifeTime >= maxLifeTime) {
            this.discard();
            return;
        }

        // 更新运动轨迹
        updateMovement();
    }

    private void updateMovement() {
        if (target == null || !target.isAlive()) {
            // 如果没有目标，保持当前运动方向
            Vec3 currentMotion = this.getDeltaMovement();
            if (currentMotion.length() < 0.5) {
                // 检查initialDirection是否为null，如果是则使用随机方向
                if (this.initialDirection != null) {
                    this.setDeltaMovement(this.initialDirection.scale(1.5));
                } else {
                    // 如果initialDirection为null，使用随机方向
                    Vec3 randomDirection = new Vec3(
                        this.random.nextDouble() - 0.5,
                        this.random.nextDouble() - 0.5,
                        this.random.nextDouble() - 0.5
                    ).normalize();
                    this.setDeltaMovement(randomDirection.scale(1.5));
                }
            }
            return;
        }

        // 前10个tick保持初始方向，确保飞弹从boss位置正确发射
        if (lifeTime < 10) {
            double currentSpeed = this.getDeltaMovement().length();
            if (currentSpeed < 0.5) {
                // 检查initialDirection是否为null，如果是则使用朝向目标的方向
                if (this.initialDirection != null) {
                    this.setDeltaMovement(this.initialDirection.scale(2.0));
                } else {
                    // 如果initialDirection为null，计算朝向目标的方向
                    Vec3 currentPos = this.position();
                    Vec3 targetPos = target.position().add(0, 1, 0);
                    Vec3 direction = targetPos.subtract(currentPos).normalize();
                    this.setDeltaMovement(direction.scale(2.0));
                }
            }
            return;
        }

        Vec3 currentPos = this.position();
        Vec3 targetPos = target.position().add(0, 1, 0);

        if (this.entityData.get(DATA_IS_SPIRAL)) {
            // 螺旋运动轨迹
            spiralAngle += SPIRAL_SPEED;
            
            Vec3 baseDirection = targetPos.subtract(currentPos).normalize();
            
            // 螺旋偏移
            double offsetX = Math.cos(spiralAngle) * 2.0;
            double offsetZ = Math.sin(spiralAngle) * 2.0;
            double offsetY = Math.sin(spiralAngle * 0.5) * 1.5;
            
            Vec3 spiralDirection = baseDirection.add(offsetX, offsetY, offsetZ).normalize();
            
            // 保持速度在1.5-2.0之间
            double currentSpeed = this.getDeltaMovement().length();
            double newSpeed = Math.max(1.5, Math.min(currentSpeed, 2.0));
            
            this.setDeltaMovement(spiralDirection.scale(newSpeed));
        } else {
            // 直接追踪运动
            Vec3 direction = targetPos.subtract(currentPos).normalize();
            
            // 直接设置朝向目标的方向，保持速度
            double currentSpeed = this.getDeltaMovement().length();
            double newSpeed = Math.max(1.5, Math.min(currentSpeed, 2.5));
            
            this.setDeltaMovement(direction.scale(newSpeed));
        }
    }

    private void spawnEnhancedParticles() {
        int missileId = this.entityData.get(DATA_MISSILE_ID);
        
        // 基础粒子效果
        for (int i = 0; i < 3; i++) {
            Vector3f[] colors = {
                new Vector3f(1.0f, 0.0f, 0.0f), // 红色
                new Vector3f(0.0f, 1.0f, 0.0f), // 绿色
                new Vector3f(0.0f, 0.0f, 1.0f), // 蓝色
                new Vector3f(1.0f, 1.0f, 0.0f), // 黄色
                new Vector3f(1.0f, 0.0f, 1.0f)  // 紫色
            };

            Vector3f color = colors[missileId % colors.length];
            DustParticleOptions particle = new DustParticleOptions(color, 1.5f);

            this.level().addParticle(particle,
                this.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                this.getY() + (this.random.nextDouble() - 0.5) * 0.5,
                this.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                (this.random.nextDouble() - 0.5) * 0.1,
                (this.random.nextDouble() - 0.5) * 0.1,
                (this.random.nextDouble() - 0.5) * 0.1);
        }

        // 光晕效果粒子
        if (lifeTime % 3 == 0) {
            this.level().addParticle(ParticleTypes.GLOW,
                this.getX(), this.getY(), this.getZ(),
                0, 0, 0);
        }

        // 轨迹粒子效果
        if (lifeTime % 2 == 0) {
            Vector3f trailColor = new Vector3f(
                (missileId * 0.2f) % 1.0f,
                (missileId * 0.3f) % 1.0f,
                (missileId * 0.4f) % 1.0f
            );
            DustParticleOptions trailParticle = new DustParticleOptions(trailColor, 1.0f);
            
            this.level().addParticle(trailParticle,
                this.getX() - this.getDeltaMovement().x * 0.5,
                this.getY() - this.getDeltaMovement().y * 0.5,
                this.getZ() - this.getDeltaMovement().z * 0.5,
                0, 0, 0);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity entity = result.getEntity();
        if (entity instanceof LivingEntity livingEntity && livingEntity != this.getOwner()) {
            float damage = this.entityData.get(DATA_DAMAGE);
            livingEntity.hurt(this.damageSources().indirectMagic(this, this.getOwner()), damage);
            
            // 爆炸效果和音效
            spawnExplosionParticles();
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE,
                net.minecraft.sounds.SoundSource.HOSTILE, 1.0f, 1.2f);
            
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        
        if (!this.level().isClientSide() && this.entityData.get(DATA_EXPLODE_ON_WALL)) {
            // 碰到墙壁后爆炸成小飞弹
            spawnSmallMissiles();
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (result.getType() == HitResult.Type.BLOCK && !this.entityData.get(DATA_EXPLODE_ON_WALL)) {
            this.discard(); // 普通碰撞方块后消失
        }
    }

    private void spawnExplosionParticles() {
        if (this.level().isClientSide()) {
            for (int i = 0; i < 15; i++) {
                Vector3f color = new Vector3f(
                    this.random.nextFloat(),
                    this.random.nextFloat(),
                    this.random.nextFloat()
                );
                DustParticleOptions particle = new DustParticleOptions(color, 2.0f);

                this.level().addParticle(particle,
                    this.getX() + (this.random.nextDouble() - 0.5) * 2.0,
                    this.getY() + (this.random.nextDouble() - 0.5) * 2.0,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 2.0,
                    (this.random.nextDouble() - 0.5) * 0.5,
                    (this.random.nextDouble() - 0.5) * 0.5,
                    (this.random.nextDouble() - 0.5) * 0.5);
            }
        }
    }

    private void spawnSmallMissiles() {
        Vec3 explosionPos = this.position();
        
        // 生成指定数量的小飞弹
        for (int i = 0; i < explodeCount; i++) {
            // 随机方向（球状散射）
            double phi = Math.acos(1 - 2 * this.random.nextDouble());
            double theta = 2 * Math.PI * this.random.nextDouble();
            
            double x = Math.sin(phi) * Math.cos(theta);
            double y = Math.sin(phi) * Math.sin(theta);
            double z = Math.cos(phi);
            
            Vec3 direction = new Vec3(x, y, z).normalize();
            
            // 创建小飞弹
            EnhancedElementalMissileProjectile missile = new EnhancedElementalMissileProjectile(
                    this.level(), (EndOfNightmare) this.getOwner(), explodeDamage, null, 0
            );
            missile.setPos(explosionPos.x, explosionPos.y, explosionPos.z);
            missile.setDeltaMovement(direction.scale(1.5));
            missile.setNoGravity(true);
            
            this.level().addFreshEntity(missile);
        }
        
        // 播放爆炸音效
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE,
                net.minecraft.sounds.SoundSource.HOSTILE, 1.0f, 1.0f);
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
        if (compound.contains("MissileDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("MissileDamage"));
        }
        if (compound.contains("IsSpiral")) {
            this.entityData.set(DATA_IS_SPIRAL, compound.getBoolean("IsSpiral"));
        }
        if (compound.contains("ExplodeOnWall")) {
            this.entityData.set(DATA_EXPLODE_ON_WALL, compound.getBoolean("ExplodeOnWall"));
        }
        if (compound.contains("MissileId")) {
            this.entityData.set(DATA_MISSILE_ID, compound.getInt("MissileId"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putFloat("MissileDamage", this.entityData.get(DATA_DAMAGE));
        compound.putBoolean("IsSpiral", this.entityData.get(DATA_IS_SPIRAL));
        compound.putBoolean("ExplodeOnWall", this.entityData.get(DATA_EXPLODE_ON_WALL));
        compound.putInt("MissileId", this.entityData.get(DATA_MISSILE_ID));
    }
}