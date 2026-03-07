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

public class StarSpearProjectile extends AbstractArrow
{
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(StarSpearProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_STAR_SPAWN_TIMER = SynchedEntityData.defineId(StarSpearProjectile.class, EntityDataSerializers.INT);

    private int lifeTime = 0;
    private final int maxLifeTime = 200; // 10秒生命周期（20tick/秒）
    private ItemStack weaponItem; // 存储发射的武器
    private int starSpawnTimer = 0; // 星星弹幕生成计时器
    private static final int STAR_SPAWN_INTERVAL = 10; // 每10tick（0.5秒）生成一次星星弹幕

    public StarSpearProjectile(EntityType<? extends StarSpearProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(false); // 星辰矛受重力影响
        this.entityData.set(DATA_DAMAGE, 250.0f); // 默认伤害
        this.entityData.set(DATA_STAR_SPAWN_TIMER, 0);
    }

    public StarSpearProjectile(Level level, LivingEntity shooter, ItemStack weapon, float damage) {
        this(ModEntities.STAR_SPEAR_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.setBaseDamage(damage); // 使用传入的伤害值
        this.entityData.set(DATA_DAMAGE, damage); // 同步伤害数据
        this.weaponItem = weapon; // 存储武器信息
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    /**
     * 获取星辰矛伤害（从同步数据中读取）
     */
    public float getSpearDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 250.0f);
        this.entityData.define(DATA_STAR_SPAWN_TIMER, 0);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;
        starSpawnTimer++;

        // 生成星辰矛轨迹粒子效果
        if (this.level().isClientSide()) {
            // 生成金色轨迹粒子
            this.level().addParticle(ParticleTypes.END_ROD,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                    this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                    0, 0, 0);
            
            // 生成闪烁星光粒子
            if (lifeTime % 3 == 0) {
                this.level().addParticle(ParticleTypes.GLOW,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.5,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                        0, 0.02, 0);
            }
        }

        // 生成星星弹幕（每0.5秒一次）
        if (starSpawnTimer >= STAR_SPAWN_INTERVAL) {
            spawnStarProjectile();
            starSpawnTimer = 0;
        }

        // 检查生命周期结束
        if (lifeTime >= maxLifeTime) {
            this.discard();
        }
    }

    /**
     * 生成星星弹幕
     */
    private void spawnStarProjectile() {
        if (this.level().isClientSide() || !isAlive()) return;

        // 计算星星弹幕伤害（星辰矛伤害的0.6倍）
        float starDamage = getSpearDamage() * 0.6f;

        // 创建星星弹幕
        StarProjectile star = new StarProjectile(level(), (LivingEntity) getOwner(), weaponItem, starDamage);
        
        // 在星辰矛后方生成星星弹幕
        Vec3 spearPos = this.position();
        Vec3 backwardDir = this.getDeltaMovement().normalize().scale(-0.5);
        Vec3 spawnPos = spearPos.add(backwardDir);
        
        star.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        
        // 设置星星弹幕向下掉落
        double randomX = (Math.random() - 0.5) * 0.2;
        double randomZ = (Math.random() - 0.5) * 0.2;
        star.setDeltaMovement(randomX, -0.5, randomZ);
        
        // 添加到世界
        level().addFreshEntity(star);

        // 播放星星生成音效
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.3F, 1.5F);
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
                    this.level().addParticle(ParticleTypes.GLOW_SQUID_INK,
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
                    SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.6F, 1.0F);
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
        this.starSpawnTimer = compound.getInt("StarSpawnTimer");
        if (compound.contains("SpearDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("SpearDamage"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putInt("StarSpawnTimer", this.starSpawnTimer);
        compound.putFloat("SpearDamage", getSpearDamage());
    }
}