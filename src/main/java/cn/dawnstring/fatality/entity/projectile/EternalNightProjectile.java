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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;

/**
 * 永恒之夜投射物 - 用于EternalNight武器的黑暗法球
 * 特性：黑色法球飞行，命中目标后产生黑雾，1秒后包裹目标造成二次伤害
 */
public class EternalNightProjectile extends AbstractArrow
{
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(EternalNightProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_FOG_DELAY = SynchedEntityData.defineId(EternalNightProjectile.class, EntityDataSerializers.INT);

    private int lifeTime = 0;
    private final int maxLifeTime = 100; // 5秒生命周期
    private boolean hasHit = false;
    private int fogTimer = 0;
    private Vec3 hitPosition;
    private LivingEntity hitTarget;
    private Player shooter;
    private ItemStack weaponStack;

    public EternalNightProjectile(EntityType<? extends EternalNightProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true); // 永恒之夜投射物无重力
        this.entityData.set(DATA_DAMAGE, 225.0f); // 默认伤害
        this.entityData.set(DATA_FOG_DELAY, 20); // 默认黑雾延迟20tick（1秒）
        this.shooter = null;
        this.weaponStack = ItemStack.EMPTY;
    }

    public EternalNightProjectile(Level level, Player shooter, ItemStack weaponStack, float damage, int fogDelay) {
        this(ModEntities.ETERNAL_NIGHT_PROJECTILE.get(), level);
        this.shooter = shooter;
        this.weaponStack = weaponStack;
        this.setOwner(shooter);
        this.setBaseDamage(damage); // 使用传入的伤害值
        this.entityData.set(DATA_DAMAGE, damage); // 同步伤害数据
        this.entityData.set(DATA_FOG_DELAY, fogDelay); // 同步黑雾延迟
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    /**
     * 获取永恒之夜投射物伤害（从同步数据中读取）
     */
    public float getEternalNightDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    /**
     * 获取黑雾延迟时间
     */
    public int getFogDelay() {
        return this.entityData.get(DATA_FOG_DELAY);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 225.0f);
        this.entityData.define(DATA_FOG_DELAY, 20);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        if (hasHit) {
            // 黑雾阶段
            fogTimer++;
            handleFogPhase();
        } else {
            // 飞行阶段
            handleFlightPhase();
        }

        // 检查生命周期结束
        if (lifeTime >= maxLifeTime) {
            this.discard();
        }
    }

    /**
     * 处理飞行阶段
     */
    private void handleFlightPhase() {
        // 生成黑暗法球粒子效果
        if (this.level().isClientSide()) {
            spawnFlightParticles();
        }
    }

    /**
     * 处理黑雾阶段
     */
    private void handleFogPhase() {
        if (hitPosition != null) {
            // 生成黑雾粒子效果
            if (this.level().isClientSide()) {
                spawnFogParticles();
            }

            // 黑雾包裹目标造成伤害
            if (fogTimer >= getFogDelay() && !this.level().isClientSide()) {
                dealFogDamage();
                this.discard(); // 伤害完成后消失
            }
        }
    }

    /**
     * 生成飞行阶段粒子效果
     */
    private void spawnFlightParticles() {
        // 黑暗法球核心粒子
        for (int i = 0; i < 5; i++) {
            // 黑色烟幕粒子
            this.level().addParticle(ParticleTypes.SMOKE,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.4,
                    this.getY() + (this.random.nextDouble() - 0.5) * 0.4,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.4,
                    0, 0.05, 0);

            // 灵魂火焰粒子（黑色特效）
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                    this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                    0, 0.03, 0);

            // 末影粒子（魔法特效）
            if (i % 2 == 0) {
                this.level().addParticle(ParticleTypes.PORTAL,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
                        0, 0.02, 0);
            }
        }
    }

    /**
     * 生成黑雾粒子效果
     */
    private void spawnFogParticles() {
        // 黑雾扩散效果
        for (int i = 0; i < 8; i++) {
            double radius = 1.5 + (fogTimer / (double)getFogDelay()) * 2.0; // 黑雾逐渐扩大
            double angle = (i * 45 + fogTimer * 5) * Math.PI / 180.0;
            
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;
            double offsetY = (this.random.nextDouble() - 0.5) * 1.0;

            // 浓密黑雾粒子
            this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                    hitPosition.x + offsetX,
                    hitPosition.y + offsetY,
                    hitPosition.z + offsetZ,
                    0, 0.02, 0);

            // 灵魂粒子（黑雾特效）
            if (i % 3 == 0) {
                this.level().addParticle(ParticleTypes.SOUL,
                        hitPosition.x + offsetX * 0.7,
                        hitPosition.y + offsetY * 0.7,
                        hitPosition.z + offsetZ * 0.7,
                        0, 0.01, 0);
            }
        }

        // 如果命中目标，在目标周围生成包裹粒子
        if (hitTarget != null) {
            for (int i = 0; i < 6; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * hitTarget.getBbWidth();
                double offsetY = (this.random.nextDouble() - 0.5) * hitTarget.getBbHeight();
                double offsetZ = (this.random.nextDouble() - 0.5) * hitTarget.getBbWidth();

                this.level().addParticle(ParticleTypes.SMOKE,
                        hitTarget.getX() + offsetX,
                        hitTarget.getY() + offsetY,
                        hitTarget.getZ() + offsetZ,
                        0, 0.05, 0);
            }
        }
    }

    /**
     * 黑雾包裹目标造成伤害
     */
    private void dealFogDamage() {
        if (hitTarget != null && hitTarget.isAlive()) {
            // 计算黑雾伤害（基础伤害的0.5倍）
            float fogDamage = getEternalNightDamage() * 0.5f;
            
            // 对目标造成伤害
            hitTarget.hurt(this.damageSources().indirectMagic(this, shooter), fogDamage);
            
            // 播放黑雾伤害音效
            this.level().playSound(null, hitPosition.x, hitPosition.y, hitPosition.z,
                    SoundEvents.WITHER_HURT, SoundSource.HOSTILE, 0.8F, 0.6F);
        }

        // 对周围敌人造成范围伤害
        AABB area = new AABB(hitPosition.x - 2.0, hitPosition.y - 2.0, hitPosition.z - 2.0,
                            hitPosition.x + 2.0, hitPosition.y + 2.0, hitPosition.z + 2.0);
        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(LivingEntity.class, area);
        
        for (LivingEntity entity : nearbyEntities) {
            if (entity != hitTarget && entity != shooter && entity.isAlive()) {
                // 对周围敌人造成较小伤害
                float areaDamage = getEternalNightDamage() * 0.3f;
                entity.hurt(this.damageSources().indirectMagic(this, shooter), areaDamage);
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        
        if (!this.level().isClientSide() && !hasHit) {
            hasHit = true;
            hitPosition = new Vec3(this.getX(), this.getY(), this.getZ());
            
            // 停止移动
            this.setDeltaMovement(Vec3.ZERO);
            
            // 播放击中音效
            this.level().playSound(null, hitPosition.x, hitPosition.y, hitPosition.z,
                    SoundEvents.SOUL_ESCAPE, SoundSource.HOSTILE, 1.0F, 0.8F);
            
            // 检查是否命中实体
            if (result.getType() == HitResult.Type.ENTITY) {
                Entity hitEntity = ((net.minecraft.world.phys.EntityHitResult) result).getEntity();
                if (hitEntity instanceof LivingEntity) {
                    hitTarget = (LivingEntity) hitEntity;
                    
                    // 立即造成首次伤害
                    hitTarget.hurt(this.damageSources().indirectMagic(this, shooter), getEternalNightDamage());
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
        this.hasHit = compound.getBoolean("HasHit");
        this.fogTimer = compound.getInt("FogTimer");
        if (compound.contains("EternalNightDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("EternalNightDamage"));
        }
        if (compound.contains("FogDelay")) {
            this.entityData.set(DATA_FOG_DELAY, compound.getInt("FogDelay"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putBoolean("HasHit", this.hasHit);
        compound.putInt("FogTimer", this.fogTimer);
        compound.putFloat("EternalNightDamage", getEternalNightDamage());
        compound.putInt("FogDelay", getFogDelay());
    }
}