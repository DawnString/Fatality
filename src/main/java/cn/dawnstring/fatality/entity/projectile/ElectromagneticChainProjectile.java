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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import java.util.ArrayList;
import java.util.List;

/**
 * 电磁闪电链投射物
 * 特性：击中目标后产生闪电链，最多连锁5个目标
 */
public class ElectromagneticChainProjectile extends AbstractArrow
{
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(ElectromagneticChainProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_CHAIN_COUNT = SynchedEntityData.defineId(ElectromagneticChainProjectile.class, EntityDataSerializers.INT);

    private int lifeTime = 0;
    private final int maxLifeTime = 80; // 4秒生命周期
    private ItemStack weaponItem;
    private static final int MAX_CHAIN_TARGETS = 5; // 最大连锁目标数
    private static final double CHAIN_RANGE = 8.0; // 连锁范围8格

    public ElectromagneticChainProjectile(EntityType<? extends ElectromagneticChainProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true); // 不受重力影响
        this.entityData.set(DATA_DAMAGE, 100.0f);
        this.entityData.set(DATA_CHAIN_COUNT, 0);
    }

    public ElectromagneticChainProjectile(Level level, LivingEntity shooter, ItemStack weapon, float damage) {
        this(ModEntities.ELECTROMAGNETIC_CHAIN_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.setBaseDamage(damage);
        this.entityData.set(DATA_DAMAGE, damage);
        this.weaponItem = weapon;
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
        this.entityData.set(DATA_CHAIN_COUNT, 0);
    }

    /**
     * 获取子弹伤害
     */
    public float getBulletDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    /**
     * 获取当前连锁次数
     */
    public int getChainCount() {
        return this.entityData.get(DATA_CHAIN_COUNT);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 100.0f);
        this.entityData.define(DATA_CHAIN_COUNT, 0);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 生成蓝色电磁轨迹粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 3; i++) {
                this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                        0, 0, 0);
                
                this.level().addParticle(ParticleTypes.END_ROD,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
                        0, 0.02, 0);
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
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();

        if (entity instanceof LivingEntity livingEntity && !this.level().isClientSide()) {
            // 播放闪电音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.NEUTRAL, 0.5F, 1.2F);

            // 对主要目标造成伤害
            applyChainDamage(livingEntity, new ArrayList<>());
            
            this.discard();
        }
    }

    /**
     * 应用闪电链伤害
     */
    private void applyChainDamage(LivingEntity primaryTarget, List<LivingEntity> alreadyHit) {
        if (getChainCount() >= MAX_CHAIN_TARGETS) {
            return;
        }

        Player owner = this.getOwner() instanceof Player ? (Player) this.getOwner() : null;
        
        // 对当前目标造成伤害
        float chainDamage = getBulletDamage() * (1.0f - getChainCount() * 0.15f); // 每次连锁伤害递减15%
        primaryTarget.hurt(primaryTarget.damageSources().lightningBolt(), chainDamage);
        
        // 添加当前目标到已命中列表
        alreadyHit.add(primaryTarget);
        
        // 生成闪电命中特效
        spawnLightningEffect(primaryTarget);
        
        // 寻找下一个连锁目标
        LivingEntity nextTarget = findNextChainTarget(primaryTarget, alreadyHit);
        
        if (nextTarget != null) {
            // 增加连锁计数
            this.entityData.set(DATA_CHAIN_COUNT, getChainCount() + 1);
            
            // 生成闪电链粒子效果
            spawnChainLightningEffect(primaryTarget, nextTarget);
            
            // 递归应用连锁伤害
            applyChainDamage(nextTarget, alreadyHit);
        }
    }

    /**
     * 寻找下一个连锁目标
     */
    private LivingEntity findNextChainTarget(LivingEntity currentTarget, List<LivingEntity> alreadyHit) {
        AABB searchArea = new AABB(
                currentTarget.getX() - CHAIN_RANGE, currentTarget.getY() - CHAIN_RANGE, currentTarget.getZ() - CHAIN_RANGE,
                currentTarget.getX() + CHAIN_RANGE, currentTarget.getY() + CHAIN_RANGE, currentTarget.getZ() + CHAIN_RANGE
        );

        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(LivingEntity.class, searchArea);
        Player owner = this.getOwner() instanceof Player ? (Player) this.getOwner() : null;

        LivingEntity closestTarget = null;
        double closestDistance = Double.MAX_VALUE;

        for (LivingEntity entity : nearbyEntities) {
            // 跳过已命中的目标、自己和玩家
            if (alreadyHit.contains(entity) || entity == owner || entity == this.getOwner()) {
                continue;
            }

            double distance = currentTarget.distanceTo(entity);
            if (distance < closestDistance && distance <= CHAIN_RANGE) {
                closestTarget = entity;
                closestDistance = distance;
            }
        }

        return closestTarget;
    }

    /**
     * 生成闪电命中特效
     */
    private void spawnLightningEffect(LivingEntity target) {
        if (this.level().isClientSide()) {
            Vec3 pos = target.position();
            
            // 生成闪电粒子效果
            for (int i = 0; i < 10; i++) {
                this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        pos.x + (this.random.nextDouble() - 0.5) * target.getBbWidth(),
                        pos.y + this.random.nextDouble() * target.getBbHeight(),
                        pos.z + (this.random.nextDouble() - 0.5) * target.getBbWidth(),
                        0, 0.1, 0);
            }
        }
    }

    /**
     * 生成闪电链粒子效果
     */
    private void spawnChainLightningEffect(LivingEntity from, LivingEntity to) {
        if (this.level().isClientSide()) {
            Vec3 fromPos = from.position();
            Vec3 toPos = to.position();
            Vec3 direction = toPos.subtract(fromPos);
            double distance = direction.length();
            
            // 沿着两个实体之间生成闪电链粒子
            int particleCount = (int) (distance * 3);
            for (int i = 0; i < particleCount; i++) {
                double progress = (double) i / particleCount;
                Vec3 particlePos = fromPos.add(direction.scale(progress));
                
                this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        particlePos.x + (this.random.nextDouble() - 0.5) * 0.3,
                        particlePos.y + (this.random.nextDouble() - 0.5) * 0.3,
                        particlePos.z + (this.random.nextDouble() - 0.5) * 0.3,
                        0, 0.05, 0);
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
        if (compound.contains("BulletDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("BulletDamage"));
        }
        if (compound.contains("ChainCount")) {
            this.entityData.set(DATA_CHAIN_COUNT, compound.getInt("ChainCount"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putFloat("BulletDamage", getBulletDamage());
        compound.putInt("ChainCount", getChainCount());
    }
}