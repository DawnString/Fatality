package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.effects.ArmorBreakEffect;
import cn.dawnstring.fatality.effects.ArmorErosionEffect;
import cn.dawnstring.fatality.registry.ModEntities;
import cn.dawnstring.fatality.registry.ModEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;

public class RevengeProjectile extends AbstractArrow
{
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(RevengeProjectile.class, EntityDataSerializers.FLOAT);

    private int lifeTime = 0;
    private final int maxLifeTime = 200; // 10秒生命周期

    public RevengeProjectile(EntityType<? extends RevengeProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(false); // 狙击子弹有重力，模拟真实弹道
        this.entityData.set(DATA_DAMAGE, 370.0f); // 默认伤害
    }

    public RevengeProjectile(Level level, LivingEntity shooter, ItemStack weapon, float damage) {
        this(ModEntities.REVENGE_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.setBaseDamage(damage);
        this.entityData.set(DATA_DAMAGE, damage); // 同步伤害数据
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    /**
     * 获取子弹伤害（从同步数据中读取）
     */
    public float getBulletDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 370.0f);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 生成狙击子弹轨迹粒子效果（更少但更大的粒子）
        if (this.level().isClientSide()) {
            if (lifeTime % 3 == 0) { // 每3tick生成一次粒子，减少粒子数量
                this.level().addParticle(ParticleTypes.SMOKE,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
                        0, 0, 0);
            }
        }

        // 检查生命周期结束
        if (lifeTime >= maxLifeTime) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            // 播放狙击枪命中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ARROW_HIT, SoundSource.BLOCKS, 1.0F, 0.5F); // 更低的音调

            // 生成狙击命中粒子效果
            if (this.level().isClientSide()) {
                for (int i = 0; i < 8; i++) { // 更多的命中粒子
                    this.level().addParticle(ParticleTypes.CRIT,
                            this.getX() + (this.random.nextDouble() - 0.5) * 0.8,
                            this.getY() + (this.random.nextDouble() - 0.5) * 0.8,
                            this.getZ() + (this.random.nextDouble() - 0.5) * 0.8,
                            0, 0.2, 0);
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
                    SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 0.8F, 0.6F);

            // 应用护甲粉碎效果（使用已注册的ArmorBreakEffect）
            livingEntity.addEffect(new MobEffectInstance(ModEffects.ARMOR_BREAK.get(), 200, 0)); // 10秒护甲粉碎效果
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
        if (compound.contains("BulletDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("BulletDamage"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putFloat("BulletDamage", getBulletDamage());
    }
}