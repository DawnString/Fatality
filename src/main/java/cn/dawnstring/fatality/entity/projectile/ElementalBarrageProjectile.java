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

import java.util.List;

/**
 * 元素弹幕投射物 - 用于ElementalStaff的元素弹幕攻击
 * 特性：元素粒子效果、追踪最近目标、元素伤害
 */
public class ElementalBarrageProjectile extends AbstractArrow
{
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(ElementalBarrageProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_TRACKING_MODE = SynchedEntityData.defineId(ElementalBarrageProjectile.class, EntityDataSerializers.BOOLEAN);

    private int ticksLived = 0;
    private final int maxLifeTime = 100; // 5秒生命周期（100tick）
    private LivingEntity currentTarget = null;
    private int trackingInterval = 0;
    private static final float TRACKING_SPEED = 1.5f; // 追踪速度
    private static final double SEARCH_RADIUS = 15.0; // 15格搜索半径

    public ElementalBarrageProjectile(EntityType<? extends ElementalBarrageProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true); // 元素弹幕无视重力
        this.entityData.set(DATA_DAMAGE, 0.0f); // 默认伤害
        this.entityData.set(DATA_TRACKING_MODE, false); // 默认非追踪模式
    }

    public ElementalBarrageProjectile(Level level, LivingEntity shooter, float damage) {
        this(ModEntities.ELEMENTAL_BARRAGE_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.setBaseDamage(damage);
        this.entityData.set(DATA_DAMAGE, damage);
        this.pickup = AbstractArrow.Pickup.DISALLOWED;

        // 设置初始位置和方向
        Vec3 lookVec = shooter.getLookAngle();
        Vec3 eyePos = shooter.getEyePosition();
        
        this.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
        this.shoot(lookVec.x, lookVec.y, lookVec.z, 1.2F, 0.0F); // 中等速度发射
    }

    /**
     * 设置追踪模式
     */
    public void setTrackingMode(boolean tracking) {
        this.entityData.set(DATA_TRACKING_MODE, tracking);
    }

    /**
     * 获取是否处于追踪模式
     */
    public boolean isTrackingMode() {
        return this.entityData.get(DATA_TRACKING_MODE);
    }

    /**
     * 获取元素弹幕伤害
     */
    public float getElementalDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 0.0f);
        this.entityData.define(DATA_TRACKING_MODE, false);
    }

    @Override
    public void tick() {
        super.tick();
        ticksLived++;

        // 检查生命周期结束
        if (ticksLived >= maxLifeTime) {
            this.discard();
            return;
        }

        // 延迟进入追踪模式
        if (ticksLived > 10) { // 前10个tick不追踪，让弹幕先飞出去
            setTrackingMode(true);
        }

        // 追踪逻辑
        if (isTrackingMode()) {
            handleTrackingLogic();
        }

        // 生成元素粒子效果
        if (this.level().isClientSide()) {
            spawnElementalParticles();
        }
    }

    /**
     * 处理追踪逻辑
     */
    private void handleTrackingLogic() {
        trackingInterval++;

        // 每5tick更新一次目标
        if (trackingInterval >= 5) {
            trackingInterval = 0;
            updateTarget();
        }

        // 如果有目标，追踪目标
        if (currentTarget != null && currentTarget.isAlive()) {
            Vec3 targetPos = currentTarget.getEyePosition();
            Vec3 currentPos = this.position();
            Vec3 direction = targetPos.subtract(currentPos).normalize();

            // 平滑转向追踪
            Vec3 currentMotion = this.getDeltaMovement();
            Vec3 newMotion = currentMotion.add(direction.scale(0.2)).scale(0.95);
            this.setDeltaMovement(newMotion);

            // 如果距离目标很近，直接命中
            if (currentPos.distanceTo(targetPos) <= 1.5) {
                this.explode();
            }
        }
    }

    /**
     * 更新追踪目标
     */
    private void updateTarget() {
        if (this.getOwner() == null || !(this.getOwner() instanceof LivingEntity owner)) return;

        Vec3 currentPos = this.position();

        // 获取范围内的所有生物
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(SEARCH_RADIUS));

        LivingEntity closestTarget = null;
        double closestDistance = Double.MAX_VALUE;

        for (LivingEntity entity : entities) {
            // 排除玩家和已死亡的实体
            if (entity == owner || entity instanceof Player || !entity.isAlive()) continue;

            double distance = currentPos.distanceTo(entity.position());
            if (distance < closestDistance) {
                closestDistance = distance;
                closestTarget = entity;
            }
        }

        currentTarget = closestTarget;
    }

    /**
     * 生成元素粒子效果
     */
    private void spawnElementalParticles() {
        Vec3 position = this.position();
        Vec3 motion = this.getDeltaMovement();

        // 元素粒子核心
        for (int i = 0; i < 6; i++) {
            double offsetX = (Math.random() - 0.5) * 0.3;
            double offsetY = (Math.random() - 0.5) * 0.3;
            double offsetZ = (Math.random() - 0.5) * 0.3;

            // 根据弹幕ID选择不同的元素粒子
            int elementType = this.getId() % 4;
            net.minecraft.core.particles.ParticleOptions particleType;
            
            switch (elementType) {
                case 0:
                    particleType = ParticleTypes.FLAME; // 火元素
                    break;
                case 1:
                    particleType = ParticleTypes.DRIPPING_WATER; // 水元素
                    break;
                case 2:
                    particleType = ParticleTypes.ELECTRIC_SPARK; // 雷元素
                    break;
                case 3:
                    particleType = ParticleTypes.ENCHANT; // 风元素
                    break;
                default:
                    particleType = ParticleTypes.FLAME;
            }

            this.level().addParticle(particleType,
                    position.x + offsetX,
                    position.y + offsetY,
                    position.z + offsetZ,
                    0, 0.01, 0);
        }

        // 元素尾缀效果
        if (ticksLived % 3 == 0) {
            Vec3 trailPos = position.subtract(motion.normalize().scale(0.5));
            
            for (int i = 0; i < 3; i++) {
                double trailOffset = -0.3 * (i + 1);
                Vec3 particlePos = position.add(motion.normalize().scale(trailOffset));

                this.level().addParticle(ParticleTypes.GLOW,
                        particlePos.x + (Math.random() - 0.5) * 0.2,
                        particlePos.y + (Math.random() - 0.5) * 0.2,
                        particlePos.z + (Math.random() - 0.5) * 0.2,
                        -motion.x * 0.02, -motion.y * 0.02, -motion.z * 0.02);
            }
        }
    }

    /**
     * 爆炸效果（命中目标）
     */
    private void explode() {
        // 播放元素弹幕命中音效
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6F, 1.2F);

        // 生成爆炸粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 8; i++) {
                double offsetX = (Math.random() - 0.5) * 0.8;
                double offsetY = (Math.random() - 0.5) * 0.8;
                double offsetZ = (Math.random() - 0.5) * 0.8;

                this.level().addParticle(ParticleTypes.GLOW,
                        this.getX() + offsetX,
                        this.getY() + offsetY,
                        this.getZ() + offsetZ,
                        0, 0.06, 0);
            }
        }

        this.discard();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        
        if (result.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityResult = (EntityHitResult) result;
            Entity entity = entityResult.getEntity();
            
            // 确保不会伤害发射者
            if (entity == this.getOwner()) return;
            
            // 对实体造成伤害
            if (entity instanceof LivingEntity livingEntity) {
                float damage = getElementalDamage();
                if (damage > 0) {
                    livingEntity.hurt(this.damageSources().magic(), damage);
                }
            }
        }
        
        this.explode();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        // 已经在onHit中处理，这里不需要重复处理
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("ElementalDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("ElementalDamage"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("ElementalDamage", this.entityData.get(DATA_DAMAGE));
    }
}