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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
 * 天火矛投射物 - 从天而降的神圣火焰长矛
 * 特性：垂直下落、火焰粒子效果、灼烧效果
 */
public class SkyfireSpearProjectile extends AbstractArrow
{
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(SkyfireSpearProjectile.class, EntityDataSerializers.FLOAT);

    private int lifeTime = 0;
    private final int maxLifeTime = 100; // 5秒生命周期（垂直下落需要时间）
    private boolean hasHit = false;
    private Player shooter;

    public SkyfireSpearProjectile(EntityType<? extends SkyfireSpearProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(false); // 天火矛有重力，模拟真实下落
        this.entityData.set(DATA_DAMAGE, 1790.0f); // 默认伤害
        this.shooter = null;
    }

    public SkyfireSpearProjectile(Level level, Player shooter, float damage) {
        this(ModEntities.SKYFIRE_SPEAR_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.shooter = shooter;
        this.setBaseDamage(damage); // 使用传入的伤害值
        this.entityData.set(DATA_DAMAGE, damage); // 同步伤害数据
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    /**
     * 获取天火矛伤害（从同步数据中读取）
     */
    public float getSkyfireSpearDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 1790.0f);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 生成天火矛下落粒子效果
        if (this.level().isClientSide()) {
            // 火焰粒子效果
            for (int i = 0; i < 3; i++) {
                this.level().addParticle(ParticleTypes.FLAME,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.5,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                        0, -0.1, 0);
            }

            // 火花粒子效果
            if (this.random.nextFloat() < 0.4f) {
                this.level().addParticle(ParticleTypes.LAVA,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.8,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.8,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.8,
                        (this.random.nextDouble() - 0.5) * 0.05,
                        -0.05,
                        (this.random.nextDouble() - 0.5) * 0.05);
            }

            // 神圣光芒粒子效果
            if (this.tickCount % 3 == 0) {
                this.level().addParticle(ParticleTypes.GLOW,
                        this.getX(), this.getY(), this.getZ(),
                        0, 0, 0);
            }
        }

        // 检查生命周期结束
        if (lifeTime >= maxLifeTime && !hasHit) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide() && !hasHit) {
            hasHit = true;

            // 播放天火矛爆炸音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1.0F, 0.8F);

            // 生成天火矛爆炸粒子效果
            if (this.level().isClientSide()) {
                for (int i = 0; i < 20; i++) {
                    this.level().addParticle(ParticleTypes.FLAME,
                            this.getX() + (this.random.nextDouble() - 0.5) * 2.0,
                            this.getY() + (this.random.nextDouble() - 0.5) * 2.0,
                            this.getZ() + (this.random.nextDouble() - 0.5) * 2.0,
                            (this.random.nextDouble() - 0.5) * 0.3,
                            (this.random.nextDouble() - 0.5) * 0.3,
                            (this.random.nextDouble() - 0.5) * 0.3);
                }

                // 爆炸冲击波粒子
                for (int i = 0; i < 15; i++) {
                    this.level().addParticle(ParticleTypes.CRIT,
                            this.getX() + (this.random.nextDouble() - 0.5) * 1.5,
                            this.getY() + (this.random.nextDouble() - 0.5) * 1.5,
                            this.getZ() + (this.random.nextDouble() - 0.5) * 1.5,
                            (this.random.nextDouble() - 0.5) * 0.2,
                            0.2,
                            (this.random.nextDouble() - 0.5) * 0.2);
                }
            }

            // 对周围敌人造成范围伤害
            createExplosionDamage();

            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();

        if (entity instanceof LivingEntity livingEntity && !hasHit) {
            hasHit = true;

            // 对目标造成直接伤害
            float damage = getSkyfireSpearDamage();
            livingEntity.hurt(this.damageSources().indirectMagic(this, this.getOwner()), damage);

            // 播放实体命中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BLAZE_HURT, SoundSource.PLAYERS, 1.0F, 1.0F);

            // 添加灼烧效果
            applyBurnEffect(livingEntity);
        }
    }

    /**
     * 创建爆炸范围伤害
     */
    private void createExplosionDamage() {
        Vec3 center = this.position();
        float explosionRadius = 3.0f; // 爆炸半径3格
        
        // 对周围敌人造成范围伤害
        for (Entity entity : this.level().getEntities(this, this.getBoundingBox().inflate(explosionRadius))) {
            if (entity instanceof LivingEntity livingEntity && entity != shooter) {
                // 计算距离衰减伤害
                double distance = center.distanceTo(entity.position());
                float distanceMultiplier = (float) Math.max(0.1, 1.0 - (distance / explosionRadius));
                float explosionDamage = getSkyfireSpearDamage() * 0.4f * distanceMultiplier; // 爆炸伤害为40%
                
                if (explosionDamage > 0) {
                    livingEntity.hurt(this.damageSources().explosion(null, shooter), explosionDamage);
                    
                    // 对爆炸范围内的敌人应用灼烧效果
                    applyBurnEffect(livingEntity);
                }
            }
        }
    }

    /**
     * 应用灼烧效果
     */
    private void applyBurnEffect(LivingEntity target) {
        // 灼烧效果：持续5秒，每秒造成伤害
        MobEffectInstance burnEffect = new MobEffectInstance(MobEffects.WITHER, 100, 0); // 使用凋零效果模拟灼烧
        target.addEffect(burnEffect);
        
        // 生成火焰粒子效果
        for (int i = 0; i < 5; i++) {
            double offsetX = (Math.random() - 0.5) * 1.0;
            double offsetY = Math.random() * 1.5;
            double offsetZ = (Math.random() - 0.5) * 1.0;
            
            this.level().addParticle(ParticleTypes.FLAME,
                    target.getX() + offsetX,
                    target.getY() + offsetY,
                    target.getZ() + offsetZ,
                    0, 0.1, 0);
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
        if (compound.contains("SkyfireSpearDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("SkyfireSpearDamage"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putBoolean("HasHit", hasHit);
        compound.putFloat("SkyfireSpearDamage", getSkyfireSpearDamage());
    }
}