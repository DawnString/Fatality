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
 * 死亡镰刀投射物 - 右键蓄力投掷的镰刀
 * 特性：飞行过程中留下静止弹幕，命中敌人后造成高额伤害
 */
public class DeathScytheProjectile extends AbstractArrow {
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(DeathScytheProjectile.class, EntityDataSerializers.FLOAT);

    private int lifeTime = 0;
    private final int maxLifeTime = 100; // 5秒生命周期
    private ItemStack weaponStack;
    private boolean hasLeftStaticProjectile = false; // 是否已留下静止弹幕

    public DeathScytheProjectile(EntityType<? extends DeathScytheProjectile> type, Level level) {
        super(type, level);
        this.weaponStack = ItemStack.EMPTY;
        this.setNoGravity(true); // 镰刀无视重力，模拟魔法投掷
        this.entityData.set(DATA_DAMAGE, 2790.0f); // 默认伤害
    }

    public DeathScytheProjectile(Level level, LivingEntity shooter, ItemStack weapon, float damage) {
        this(ModEntities.DEATH_SCYTHE_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.weaponStack = weapon.copy();

        // 使用传入的伤害值
        this.setBaseDamage(damage);
        this.entityData.set(DATA_DAMAGE, damage);

        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    /**
     * 获取镰刀伤害（从同步数据中读取）
     */
    public float getScytheDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 2790.0f);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 生成镰刀飞行轨迹粒子效果 - 使用死亡主题粒子
        if (this.level().isClientSide()) {
            for (int i = 0; i < 3; i++) {
                // 使用灵魂火焰和烟幕粒子，符合死亡镰刀主题
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                        0, 0, 0);

                this.level().addParticle(ParticleTypes.SMOKE,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
                        0, 0, 0);
            }
        }

        // 每10tick留下一个静止弹幕
        if (!this.level().isClientSide() && lifeTime % 10 == 0 && !hasLeftStaticProjectile) {
            createStaticProjectile();
        }

        // 检查生命周期结束
        if (lifeTime >= maxLifeTime) {
            this.discard();
        }
    }

    /**
     * 创建静止弹幕
     */
    private void createStaticProjectile() {
        // 检查所有者是否为LivingEntity
        Entity owner = this.getOwner();
        if (!(owner instanceof LivingEntity livingOwner)) {
            return; // 如果不是LivingEntity，不创建弹幕
        }
        
        // 创建静止的死亡弹幕
        DeathScytheStaticProjectile staticProjectile = new DeathScytheStaticProjectile(this.level(), livingOwner, this.weaponStack, getScytheDamage() * 0.3f);
        
        // 设置弹幕位置
        staticProjectile.setPos(this.getX(), this.getY(), this.getZ());
        
        // 添加到世界
        this.level().addFreshEntity(staticProjectile);
        
        // 播放弹幕生成音效
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 0.3F, 1.0F);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            // 播放命中音效 - 使用死亡主题音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.0f, 0.8f);

            // 生成命中粒子效果
            if (this.level().isClientSide()) {
                for (int i = 0; i < 15; i++) {
                    this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                            this.getX() + (this.random.nextDouble() - 0.5) * 0.8,
                            this.getY() + (this.random.nextDouble() - 0.5) * 0.8,
                            this.getZ() + (this.random.nextDouble() - 0.5) * 0.8,
                            (this.random.nextDouble() - 0.5) * 0.3,
                            (this.random.nextDouble() - 0.5) * 0.3,
                            (this.random.nextDouble() - 0.5) * 0.3);
                }
            }

            // 在命中位置留下最后一个静止弹幕
            if (!hasLeftStaticProjectile) {
                createStaticProjectile();
                hasLeftStaticProjectile = true;
            }

            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();

        if (entity instanceof LivingEntity livingEntity && entity != this.getOwner()) {
            // 播放实体命中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 0.7F);

            // 添加击退效果
            Vec3 knockback = this.getDeltaMovement().normalize().scale(3.0);
            livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().add(knockback));
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
        if (compound.contains("ScytheDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("ScytheDamage"));
        }
        this.hasLeftStaticProjectile = compound.getBoolean("HasLeftStaticProjectile");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putFloat("ScytheDamage", getScytheDamage());
        compound.putBoolean("HasLeftStaticProjectile", hasLeftStaticProjectile);
    }
}