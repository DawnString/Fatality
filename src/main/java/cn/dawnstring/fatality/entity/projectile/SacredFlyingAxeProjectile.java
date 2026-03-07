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
 * 神圣飞斧投射物 - 具有飞行后返回玩家功能的特殊飞斧
 * 特性：飞行20格后自动返回，返回过程中仍然造成伤害，不受重力影响
 */
public class SacredFlyingAxeProjectile extends AbstractArrow {
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(SacredFlyingAxeProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_IS_RETURNING = SynchedEntityData.defineId(SacredFlyingAxeProjectile.class, EntityDataSerializers.BOOLEAN);

    private int lifeTime = 0;
    private final int maxLifeTime = 200; // 10秒生命周期（允许返回过程）
    private final int maxFlightDistance = 20; // 最大飞行距离20格
    private Vec3 startPosition;
    private boolean hasReturned = false;

    public SacredFlyingAxeProjectile(EntityType<? extends SacredFlyingAxeProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true); // 神圣飞斧不受重力影响
        this.entityData.set(DATA_DAMAGE, 165.0f); // 默认伤害
        this.entityData.set(DATA_IS_RETURNING, false);
    }

    public SacredFlyingAxeProjectile(Level level, LivingEntity shooter, ItemStack weapon, float damage) {
        this(ModEntities.SACRED_FLYING_AXE_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.setBaseDamage(damage);
        this.entityData.set(DATA_DAMAGE, damage);
        this.startPosition = shooter.position();
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    /**
     * 获取飞斧伤害
     */
    public float getFlyingAxeDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    /**
     * 检查是否正在返回
     */
    public boolean isReturning() {
        return this.entityData.get(DATA_IS_RETURNING);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 165.0f);
        this.entityData.define(DATA_IS_RETURNING, false);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 生成神圣飞斧飞行轨迹粒子效果 - 金色粒子
        if (this.level().isClientSide()) {
            spawnSacredParticles();
        }

        LivingEntity owner = (LivingEntity) this.getOwner();
        
        if (owner != null && !this.isReturning()) {
            // 检查是否达到最大飞行距离
            double currentDistance = this.distanceToSqr(startPosition);
            if (currentDistance >= maxFlightDistance * maxFlightDistance) {
                // 开始返回玩家
                startReturnToPlayer(owner);
            }
        }

        if (this.isReturning() && owner != null) {
            // 返回玩家逻辑
            returnToPlayer(owner);
            
            // 检查是否已经返回玩家（距离小于1.5格）
            if (this.distanceToSqr(owner) < 2.25) { // 1.5^2 = 2.25
                // 播放返回完成音效
                if (!this.level().isClientSide()) {
                    this.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(),
                            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 0.8F);
                }
                
                // 生成返回完成粒子效果
                if (this.level().isClientSide()) {
                    for (int i = 0; i < 15; i++) {
                        this.level().addParticle(ParticleTypes.GLOW,
                                owner.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                                owner.getY() + (this.random.nextDouble() - 0.5) * 0.5 + 1,
                                owner.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                                (this.random.nextDouble() - 0.5) * 0.1,
                                (this.random.nextDouble() - 0.5) * 0.1,
                                (this.random.nextDouble() - 0.5) * 0.1);
                    }
                }
                
                // 立即消失
                this.discard();
                return;
            }
        }

        // 检查生命周期结束（超时保护）
        if (lifeTime >= maxLifeTime) {
            this.discard();
        }
    }

    /**
     * 生成神圣飞斧粒子效果
     */
    private void spawnSacredParticles() {
        // 金色粒子效果
        for (int i = 0; i < 2; i++) {
            this.level().addParticle(ParticleTypes.GLOW,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.4,
                    this.getY() + (this.random.nextDouble() - 0.5) * 0.4,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.4,
                    0, 0.05, 0);
        }

        // 旋转粒子效果
        if (this.isReturning()) {
            // 返回时增加粒子密度
            for (int i = 0; i < 3; i++) {
                this.level().addParticle(ParticleTypes.ENCHANT,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.6,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.6,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.6,
                        (this.random.nextDouble() - 0.5) * 0.1,
                        (this.random.nextDouble() - 0.5) * 0.1,
                        (this.random.nextDouble() - 0.5) * 0.1);
            }
        }
    }

    /**
     * 开始返回玩家
     */
    private void startReturnToPlayer(LivingEntity owner) {
        this.entityData.set(DATA_IS_RETURNING, true);
        
        // 播放返回音效
        if (!this.level().isClientSide()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.6F, 1.5F);
        }
    }

    /**
     * 返回玩家逻辑
     */
    private void returnToPlayer(LivingEntity owner) {
        Vec3 targetPos = owner.getEyePosition();
        Vec3 currentPos = this.position();
        
        // 计算朝向玩家的方向
        Vec3 direction = targetPos.subtract(currentPos).normalize();
        
        // 设置返回速度（比发射速度稍慢）
        double returnSpeed = 2.0;
        this.setDeltaMovement(direction.scale(returnSpeed));
        
        // 更新旋转角度
        this.setYRot((float)(Math.atan2(direction.x, direction.z) * (180 / Math.PI)));
        this.setXRot((float)(Math.atan2(direction.y, Math.sqrt(direction.x * direction.x + direction.z * direction.z)) * (180 / Math.PI)));
        
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    @Override
    protected void onHit(HitResult result) {
        if (this.isReturning()) {
            // 返回过程中不处理碰撞，直接穿透
            return;
        }
        
        super.onHit(result);
        if (!this.level().isClientSide()) {
            // 播放命中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8f, 1.2f);

            // 生成神圣命中粒子效果
            if (this.level().isClientSide()) {
                for (int i = 0; i < 10; i++) {
                    this.level().addParticle(ParticleTypes.GLOW,
                            this.getX() + (this.random.nextDouble() - 0.5) * 0.8,
                            this.getY() + (this.random.nextDouble() - 0.5) * 0.8,
                            this.getZ() + (this.random.nextDouble() - 0.5) * 0.8,
                            (this.random.nextDouble() - 0.5) * 0.2,
                            (this.random.nextDouble() - 0.5) * 0.2,
                            (this.random.nextDouble() - 0.5) * 0.2);
                }
            }

            // 命中后立即开始返回
            LivingEntity owner = (LivingEntity) this.getOwner();
            if (owner != null) {
                startReturnToPlayer(owner);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity entity = result.getEntity();

        if (entity instanceof LivingEntity livingEntity && entity != this.getOwner()) {
            // 播放实体命中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 1.1F);

            // 添加神圣击退效果
            Vec3 knockback = this.getDeltaMovement().normalize().scale(1.5);
            livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().add(knockback));

            // 返回过程中仍然造成伤害
            if (this.isReturning()) {
                // 返回时伤害减半
                float returnDamage = getFlyingAxeDamage() * 0.5f;
                livingEntity.hurt(livingEntity.damageSources().indirectMagic(this, this.getOwner()), returnDamage);
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
        if (compound.contains("FlyingAxeDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("FlyingAxeDamage"));
        }
        if (compound.contains("IsReturning")) {
            this.entityData.set(DATA_IS_RETURNING, compound.getBoolean("IsReturning"));
        }
        if (compound.contains("StartX") && compound.contains("StartY") && compound.contains("StartZ")) {
            this.startPosition = new Vec3(
                    compound.getDouble("StartX"),
                    compound.getDouble("StartY"),
                    compound.getDouble("StartZ")
            );
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putFloat("FlyingAxeDamage", getFlyingAxeDamage());
        compound.putBoolean("IsReturning", isReturning());
        if (this.startPosition != null) {
            compound.putDouble("StartX", this.startPosition.x);
            compound.putDouble("StartY", this.startPosition.y);
            compound.putDouble("StartZ", this.startPosition.z);
        }
    }
}