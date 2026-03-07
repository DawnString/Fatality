package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.entity.SpaceCollapseEntity;
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
 * 破碎领域箭矢 - 用于BowOfShatteredRealm的空间坍缩箭矢攻击
 * 特性：无视重力、命中后创造空间坍缩吸附实体、喷发黑色粒子造成伤害
 */
public class ShatteredRealmArrow extends AbstractArrow
{
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(ShatteredRealmArrow.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_COLLAPSE_RADIUS = SynchedEntityData.defineId(ShatteredRealmArrow.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_COLLAPSE_DURATION = SynchedEntityData.defineId(ShatteredRealmArrow.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_DAMAGE_MULTIPLIER = SynchedEntityData.defineId(ShatteredRealmArrow.class, EntityDataSerializers.FLOAT);

    private int ticksLived = 0;
    private final int maxLifeTime = 200; // 10秒生命周期（200tick）

    public ShatteredRealmArrow(EntityType<? extends ShatteredRealmArrow> type, Level level) {
        super(type, level);
        this.setNoGravity(true); // 箭矢无视重力
        this.entityData.set(DATA_DAMAGE, 0.0f); // 默认伤害
        this.entityData.set(DATA_COLLAPSE_RADIUS, 5.0f); // 默认坍缩半径5格
        this.entityData.set(DATA_COLLAPSE_DURATION, 60); // 默认坍缩持续时间60tick
        this.entityData.set(DATA_DAMAGE_MULTIPLIER, 0.3f); // 默认坍缩伤害倍率30%
    }

    public ShatteredRealmArrow(Level level, LivingEntity shooter, float damage, float collapseRadius, int collapseDuration, float damageMultiplier) {
        this(ModEntities.SHATTERED_REALM_ARROW.get(), level);
        this.setOwner(shooter);
        this.setBaseDamage(damage);
        this.entityData.set(DATA_DAMAGE, damage);
        this.entityData.set(DATA_COLLAPSE_RADIUS, collapseRadius);
        this.entityData.set(DATA_COLLAPSE_DURATION, collapseDuration);
        this.entityData.set(DATA_DAMAGE_MULTIPLIER, damageMultiplier);
        this.pickup = AbstractArrow.Pickup.DISALLOWED;

        // 设置初始位置和方向
        Vec3 lookVec = shooter.getLookAngle();
        Vec3 eyePos = shooter.getEyePosition();
        
        this.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
        this.shoot(lookVec.x, lookVec.y, lookVec.z, 3.0F, 0.0F); // 高速发射
    }

    /**
     * 获取箭矢伤害
     */
    public float getArrowDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    /**
     * 获取坍缩半径
     */
    public float getCollapseRadius() {
        return this.entityData.get(DATA_COLLAPSE_RADIUS);
    }

    /**
     * 获取坍缩持续时间
     */
    public int getCollapseDuration() {
        return this.entityData.get(DATA_COLLAPSE_DURATION);
    }

    /**
     * 获取坍缩伤害倍率
     */
    public float getDamageMultiplier() {
        return this.entityData.get(DATA_DAMAGE_MULTIPLIER);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 0.0f);
        this.entityData.define(DATA_COLLAPSE_RADIUS, 5.0f);
        this.entityData.define(DATA_COLLAPSE_DURATION, 60);
        this.entityData.define(DATA_DAMAGE_MULTIPLIER, 0.3f);
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

        // 生成黑色粒子效果
        if (this.level().isClientSide()) {
            spawnDarkParticles();
        }
    }

    /**
     * 生成黑色粒子效果
     */
    private void spawnDarkParticles() {
        Vec3 position = this.position();
        Vec3 motion = this.getDeltaMovement();

        // 黑色粒子核心
        for (int i = 0; i < 6; i++) {
            double offsetX = (Math.random() - 0.5) * 0.5;
            double offsetY = (Math.random() - 0.5) * 0.5;
            double offsetZ = (Math.random() - 0.5) * 0.5;

            // 黑色烟雾粒子
            this.level().addParticle(ParticleTypes.SMOKE,
                    position.x + offsetX,
                    position.y + offsetY,
                    position.z + offsetZ,
                    0, 0.01, 0);

            // 黑色火焰粒子
            if (i % 2 == 0) {
                this.level().addParticle(ParticleTypes.FLAME,
                        position.x + offsetX * 0.8,
                        position.y + offsetY * 0.8,
                        position.z + offsetZ * 0.8,
                        0, 0.008, 0);
            }
        }

        // 黑色尾缀效果
        for (int i = 0; i < 3; i++) {
            // 在箭矢后方生成黑色粒子
            double trailOffset = -0.3 * (i + 1);
            Vec3 trailPos = position.add(motion.normalize().scale(trailOffset));

            // 黑色烟雾尾缀
            this.level().addParticle(ParticleTypes.SMOKE,
                    trailPos.x + (Math.random() - 0.5) * 0.3,
                    trailPos.y + (Math.random() - 0.5) * 0.3,
                    trailPos.z + (Math.random() - 0.5) * 0.3,
                    -motion.x * 0.03, -motion.y * 0.03, -motion.z * 0.03);
        }
    }

    /**
     * 创建空间坍缩效果
     */
    private void createSpaceCollapse(Vec3 position) {
        if (!this.level().isClientSide()) {
            // 创建空间坍缩实体
            SpaceCollapseEntity collapseEntity = new SpaceCollapseEntity(
                    this.level(), 
                    (Player) this.getOwner(), 
                    getArrowDamage() * getDamageMultiplier(), 
                    getCollapseRadius(), 
                    getCollapseDuration()
            );
            collapseEntity.setPos(position.x, position.y, position.z);
            this.level().addFreshEntity(collapseEntity);
            
            // 播放空间坍缩音效
            this.level().playSound(null, position.x, position.y, position.z,
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 0.3F);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            // 创建空间坍缩效果
            createSpaceCollapse(result.getLocation());
            
            // 播放空间撕裂音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 0.5F);
            
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
        this.ticksLived = compound.getInt("TicksLived");
        if (compound.contains("ArrowDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("ArrowDamage"));
        }
        if (compound.contains("CollapseRadius")) {
            this.entityData.set(DATA_COLLAPSE_RADIUS, compound.getFloat("CollapseRadius"));
        }
        if (compound.contains("CollapseDuration")) {
            this.entityData.set(DATA_COLLAPSE_DURATION, compound.getInt("CollapseDuration"));
        }
        if (compound.contains("DamageMultiplier")) {
            this.entityData.set(DATA_DAMAGE_MULTIPLIER, compound.getFloat("DamageMultiplier"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("TicksLived", this.ticksLived);
        compound.putFloat("ArrowDamage", getArrowDamage());
        compound.putFloat("CollapseRadius", getCollapseRadius());
        compound.putInt("CollapseDuration", getCollapseDuration());
        compound.putFloat("DamageMultiplier", getDamageMultiplier());
    }
}