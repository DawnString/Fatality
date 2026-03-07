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
import net.minecraftforge.network.NetworkHooks;

public class StarProjectile extends AbstractArrow
{
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(StarProjectile.class, EntityDataSerializers.FLOAT);

    private int lifeTime = 0;
    private final int maxLifeTime = 80; // 4秒生命周期（20tick/秒）
    private ItemStack weaponItem; // 存储发射的武器

    public StarProjectile(EntityType<? extends StarProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true); // 星星弹幕无重力
        this.entityData.set(DATA_DAMAGE, 250.0f); // 默认伤害
    }

    public StarProjectile(Level level, LivingEntity shooter, ItemStack weapon, float damage) {
        this(ModEntities.STAR_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.setBaseDamage(damage); // 使用传入的伤害值
        this.entityData.set(DATA_DAMAGE, damage); // 同步伤害数据
        this.weaponItem = weapon; // 存储武器信息
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    /**
     * 获取星星弹幕伤害（从同步数据中读取）
     */
    public float getStarDamage() {
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

        // 生成星星轨迹粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 2; i++) {
                // 生成星星粒子效果
                this.level().addParticle(ParticleTypes.END_ROD,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
                        0, 0, 0);
                
                // 生成闪烁粒子效果
                if (lifeTime % 5 == 0) {
                    this.level().addParticle(ParticleTypes.GLOW,
                            this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                            this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                            this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                            0, 0.05, 0);
                }
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
            // 播放命中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.8F, 1.2F);

            // 生成命中粒子效果
            if (this.level().isClientSide()) {
                for (int i = 0; i < 8; i++) {
                    this.level().addParticle(ParticleTypes.GLOW_SQUID_INK,
                            this.getX() + (this.random.nextDouble() - 0.5) * 0.8,
                            this.getY() + (this.random.nextDouble() - 0.5) * 0.8,
                            this.getZ() + (this.random.nextDouble() - 0.5) * 0.8,
                            0, 0.1, 0);
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
        if (compound.contains("StarDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("StarDamage"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putFloat("StarDamage", getStarDamage());
    }
}