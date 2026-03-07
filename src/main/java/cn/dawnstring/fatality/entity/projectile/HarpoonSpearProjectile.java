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
 * 鱼叉投射物类
 * 特性：受重力影响，飞行中每0.5秒在身后生成一条竖直落下的鱼
 */
public class HarpoonSpearProjectile extends AbstractArrow
{
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(HarpoonSpearProjectile.class, EntityDataSerializers.FLOAT);

    private int lifeTime = 0;
    private final int maxLifeTime = 200; // 10秒生命周期（20tick/秒）
    private ItemStack weaponItem; // 存储发射的武器
    private int fishSpawnTimer = 0; // 鱼生成计时器
    private static final int FISH_SPAWN_INTERVAL = 10; // 每10tick（0.5秒）生成一次鱼

    public HarpoonSpearProjectile(EntityType<? extends HarpoonSpearProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(false); // 鱼叉受重力影响
        this.entityData.set(DATA_DAMAGE, 250.0f); // 默认伤害
    }

    public HarpoonSpearProjectile(Level level, LivingEntity shooter, ItemStack weapon, float damage) {
        this(ModEntities.HARPOON_SPEAR_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.setBaseDamage(damage); // 使用传入的伤害值
        this.entityData.set(DATA_DAMAGE, damage); // 同步伤害数据
        this.weaponItem = weapon; // 存储武器信息
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    /**
     * 获取鱼叉伤害（从同步数据中读取）
     */
    public float getSpearDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 250.0f);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;
        fishSpawnTimer++;

        // 生成鱼叉轨迹粒子效果
        if (this.level().isClientSide()) {
            // 生成水花粒子效果
            this.level().addParticle(ParticleTypes.BUBBLE,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                    this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                    0, 0, 0);
            
            // 生成水滴粒子效果
            if (lifeTime % 3 == 0) {
                this.level().addParticle(ParticleTypes.DRIPPING_WATER,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.5,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                        0, 0.02, 0);
            }
        }

        // 生成鱼（每0.5秒一次）
        if (fishSpawnTimer >= FISH_SPAWN_INTERVAL) {
            spawnFishProjectile();
            fishSpawnTimer = 0;
        }

        // 检查生命周期结束
        if (lifeTime >= maxLifeTime) {
            this.discard();
        }
    }

    /**
     * 生成鱼投射物
     */
    private void spawnFishProjectile() {
        if (this.level().isClientSide() || !isAlive()) return;

        // 计算鱼伤害（鱼叉伤害的0.5倍）
        float fishDamage = getSpearDamage() * 0.5f;

        // 创建鱼投射物
        FishProjectile fish = new FishProjectile(level(), (LivingEntity) getOwner(), weaponItem, fishDamage);
        
        // 在鱼叉后方生成鱼投射物
        Vec3 spearPos = this.position();
        Vec3 backwardDir = this.getDeltaMovement().normalize().scale(-0.5);
        Vec3 spawnPos = spearPos.add(backwardDir);
        
        fish.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        
        // 设置鱼投射物向下掉落
        double randomX = (Math.random() - 0.5) * 0.2;
        double randomZ = (Math.random() - 0.5) * 0.2;
        fish.setDeltaMovement(randomX, -0.5, randomZ);
        
        // 添加到世界
        level().addFreshEntity(fish);

        // 播放鱼生成音效
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.SALMON_FLOP, SoundSource.BLOCKS, 0.3F, 1.0F);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            // 播放命中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.TRIDENT_HIT, SoundSource.BLOCKS, 0.8F, 1.0F);

            // 生成命中粒子效果
            if (this.level().isClientSide()) {
                for (int i = 0; i < 12; i++) {
                    this.level().addParticle(ParticleTypes.BUBBLE_POP,
                            this.getX() + (this.random.nextDouble() - 0.5) * 1.0,
                            this.getY() + (this.random.nextDouble() - 0.5) * 1.0,
                            this.getZ() + (this.random.nextDouble() - 0.5) * 1.0,
                            0, 0.15, 0);
                }
            }

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
                    SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 0.6F, 1.0F);
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
        this.fishSpawnTimer = compound.getInt("FishSpawnTimer");
        if (compound.contains("SpearDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("SpearDamage"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putInt("FishSpawnTimer", this.fishSpawnTimer);
        compound.putFloat("SpearDamage", getSpearDamage());
    }
}