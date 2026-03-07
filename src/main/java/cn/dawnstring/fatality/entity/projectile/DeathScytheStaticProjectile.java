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
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/**
 * 死亡镰刀静止弹幕 - 镰刀飞行过程中留下的静止弹幕
 * 特性：静止不动，对靠近的敌人造成持续伤害
 */
public class DeathScytheStaticProjectile extends AbstractArrow {
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(DeathScytheStaticProjectile.class, EntityDataSerializers.FLOAT);

    private int lifeTime = 0;
    private final int maxLifeTime = 200; // 10秒生命周期
    private ItemStack weaponStack;
    private static final float DAMAGE_RADIUS = 3.0f; // 伤害半径3格
    private static final int DAMAGE_INTERVAL = 20; // 每20tick造成一次伤害

    public DeathScytheStaticProjectile(EntityType<? extends DeathScytheStaticProjectile> type, Level level) {
        super(type, level);
        this.weaponStack = ItemStack.EMPTY;
        this.setNoGravity(true); // 静止弹幕无视重力
        this.setNoPhysics(true); // 无物理碰撞
        this.entityData.set(DATA_DAMAGE, 837.0f); // 默认伤害（2790 * 0.3）
    }

    public DeathScytheStaticProjectile(Level level, LivingEntity shooter, ItemStack weapon, float damage) {
        this(ModEntities.DEATH_SCYTHE_STATIC_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.weaponStack = weapon.copy();

        // 使用传入的伤害值
        this.setBaseDamage(damage);
        this.entityData.set(DATA_DAMAGE, damage);

        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    /**
     * 获取弹幕伤害（从同步数据中读取）
     */
    public float getStaticDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 837.0f);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 生成静止弹幕粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 2; i++) {
                // 使用灵魂火焰和烟幕粒子
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX() + (this.random.nextDouble() - 0.5) * 1.0,
                        this.getY() + (this.random.nextDouble() - 0.5) * 1.0,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 1.0,
                        0, 0.05, 0);

                this.level().addParticle(ParticleTypes.SMOKE,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.8,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.8,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.8,
                        0, 0.03, 0);
            }
        }

        // 对周围敌人造成伤害
        if (!this.level().isClientSide() && lifeTime % DAMAGE_INTERVAL == 0) {
            damageNearbyEntities();
        }

        // 检查生命周期结束
        if (lifeTime >= maxLifeTime) {
            if (!this.level().isClientSide()) {
                // 播放消失音效
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 0.5F, 1.2F);
            }
            this.discard();
        }
    }

    /**
     * 对周围敌人造成伤害
     */
    private void damageNearbyEntities() {
        // 获取周围所有实体
        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(DAMAGE_RADIUS))) {
            // 排除自己和发射者
            if (entity != this.getOwner()) {
                // 计算伤害
                float damage = getStaticDamage();
                
                // 应用伤害
                if (damage > 0) {
                    entity.hurt(entity.damageSources().indirectMagic(this, this.getOwner()), damage);
                    
                    // 播放伤害音效
                    this.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                            SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 0.3F, 1.0F);
                }
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        // 静止弹幕不处理碰撞
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        // 静止弹幕不处理实体碰撞
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
        if (compound.contains("StaticDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("StaticDamage"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putFloat("StaticDamage", getStaticDamage());
    }
}