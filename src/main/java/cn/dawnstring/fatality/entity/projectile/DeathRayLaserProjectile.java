package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * DeathRay激光投射物类
 * 实现激光的穿透效果，能够穿透多个目标
 */
public class DeathRayLaserProjectile extends AbstractArrow {
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(DeathRayLaserProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_PENETRATION_COUNT = SynchedEntityData.defineId(DeathRayLaserProjectile.class, EntityDataSerializers.INT);
    
    private Player shooter;
    private ItemStack weaponStack;
    private float baseDamage;
    private int maxPenetration;
    private Set<UUID> hitEntities = new HashSet<>(); // 记录已击中的实体，避免重复伤害
    private int lifeTime = 0;
    private static final int MAX_LIFETIME = 40; // 最大生命周期2秒（40tick）
    private static final double LASER_LENGTH = 100.0; // 激光长度100格

    public DeathRayLaserProjectile(EntityType<? extends DeathRayLaserProjectile> type, Level level) {
        super(type, level);
        this.shooter = null;
        this.weaponStack = ItemStack.EMPTY;
        this.baseDamage = 0.0f;
        this.maxPenetration = 0;
        this.setNoGravity(true); // 不受重力影响
        this.pickup = AbstractArrow.Pickup.DISALLOWED; // 不允许拾取
        this.entityData.set(DATA_DAMAGE, 0.0f);
        this.entityData.set(DATA_PENETRATION_COUNT, 0);
    }

    public DeathRayLaserProjectile(Level level, Player shooter, ItemStack weaponStack, float damage, int maxPenetration) {
        this(ModEntities.DEATH_RAY_LASER_PROJECTILE.get(), level);
        this.shooter = shooter;
        this.weaponStack = weaponStack;
        this.baseDamage = damage;
        this.maxPenetration = maxPenetration;
        this.setOwner(shooter);
        this.setBaseDamage(damage);
        this.entityData.set(DATA_DAMAGE, damage);
        this.entityData.set(DATA_PENETRATION_COUNT, 0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 0.0f);
        this.entityData.define(DATA_PENETRATION_COUNT, 0);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 生成激光粒子效果
        if (this.level().isClientSide) {
            spawnLaserParticles();
        }

        // 检测激光路径上的实体
        if (!this.level().isClientSide && lifeTime % 2 == 0) { // 每2tick检测一次
            checkLaserPath();
        }

        // 生命周期结束
        if (lifeTime >= MAX_LIFETIME) {
            this.discard();
        }
    }

    /**
     * 生成激光粒子效果
     */
    private void spawnLaserParticles() {
        Vec3 startPos = this.position();
        Vec3 direction = this.getDeltaMovement().normalize();
        
        // 沿着激光路径生成粒子
        for (int i = 0; i < 20; i++) {
            double progress = (double) i / 20;
            Vec3 particlePos = startPos.add(direction.scale(LASER_LENGTH * progress));
            
            // 生成红色激光粒子
            this.level().addParticle(ParticleTypes.FLAME,
                    particlePos.x, particlePos.y, particlePos.z,
                    0, 0, 0);
            
            // 生成发光粒子
            this.level().addParticle(ParticleTypes.GLOW,
                    particlePos.x, particlePos.y, particlePos.z,
                    0, 0, 0);
        }
    }

    /**
     * 检测激光路径上的实体
     */
    private void checkLaserPath() {
        Vec3 startPos = this.position();
        Vec3 direction = this.getDeltaMovement().normalize();
        Vec3 endPos = startPos.add(direction.scale(LASER_LENGTH));

        // 创建激光路径的检测区域
        AABB laserBox = createLaserBoundingBox(startPos, endPos);
        
        // 获取路径上的所有实体
        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, laserBox)) {
            // 排除射击者和已击中的实体
            if (entity == shooter || hitEntities.contains(entity.getUUID())) {
                continue;
            }

            // 检查实体是否在激光路径上
            if (isEntityInLaserPath(entity, startPos, direction)) {
                // 对实体造成伤害
                applyLaserDamage(entity);
                
                // 记录已击中的实体
                hitEntities.add(entity.getUUID());
                
                // 更新穿透计数
                int currentPenetration = this.entityData.get(DATA_PENETRATION_COUNT);
                this.entityData.set(DATA_PENETRATION_COUNT, currentPenetration + 1);
                
                // 如果达到最大穿透数，销毁激光
                if (currentPenetration + 1 >= maxPenetration) {
                    this.discard();
                    return;
                }
            }
        }
    }

    /**
     * 创建激光路径的边界框
     */
    private AABB createLaserBoundingBox(Vec3 start, Vec3 end) {
        // 计算激光路径的最小和最大坐标
        double minX = Math.min(start.x, end.x) - 0.5;
        double minY = Math.min(start.y, end.y) - 0.5;
        double minZ = Math.min(start.z, end.z) - 0.5;
        double maxX = Math.max(start.x, end.x) + 0.5;
        double maxY = Math.max(start.y, end.y) + 0.5;
        double maxZ = Math.max(start.z, end.z) + 0.5;
        
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * 检查实体是否在激光路径上
     */
    private boolean isEntityInLaserPath(LivingEntity entity, Vec3 startPos, Vec3 direction) {
        Vec3 entityPos = entity.position().add(0, entity.getEyeHeight() / 2, 0);
        Vec3 toEntity = entityPos.subtract(startPos);
        
        // 计算实体到激光线的距离
        double distanceToLine = toEntity.cross(direction).length();
        
        // 如果距离小于实体半径，则认为在路径上
        return distanceToLine <= entity.getBbWidth() / 2 + 0.5;
    }

    /**
     * 对实体应用激光伤害
     */
    private void applyLaserDamage(LivingEntity target) {
        if (shooter == null || !target.isAlive()) return;

        // 使用正确的伤害源
        boolean damageApplied = target.hurt(target.damageSources().playerAttack(shooter), baseDamage);
        
        if (damageApplied) {
            // 播放击中音效
            this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH, 
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 1.2F);
            
            // 生成击中粒子效果
            spawnHitParticles(target);
        }
    }

    /**
     * 生成击中粒子效果
     */
    private void spawnHitParticles(LivingEntity target) {
        if (this.level().isClientSide) {
            Vec3 hitPos = target.position().add(0, target.getEyeHeight() / 2, 0);
            
            // 生成爆炸粒子效果
            for (int i = 0; i < 10; i++) {
                this.level().addParticle(ParticleTypes.FLASH,
                        hitPos.x, hitPos.y, hitPos.z,
                        (Math.random() - 0.5) * 0.1,
                        (Math.random() - 0.5) * 0.1,
                        (Math.random() - 0.5) * 0.1);
            }
            
            // 生成火焰粒子
            for (int i = 0; i < 5; i++) {
                this.level().addParticle(ParticleTypes.FLAME,
                        hitPos.x, hitPos.y, hitPos.z,
                        (Math.random() - 0.5) * 0.2,
                        (Math.random() - 0.5) * 0.2,
                        (Math.random() - 0.5) * 0.2);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        // 激光穿透实体，不因击中而消失
        // 伤害逻辑在checkLaserPath中处理
    }

    @Override
    protected void onHit(HitResult result) {
        // 激光击中方块时消失
        if (result.getType() == HitResult.Type.BLOCK) {
            this.discard();
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
        if (compound.contains("LaserDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("LaserDamage"));
        }
        if (compound.contains("MaxPenetration")) {
            this.maxPenetration = compound.getInt("MaxPenetration");
        }
        if (compound.contains("PenetrationCount")) {
            this.entityData.set(DATA_PENETRATION_COUNT, compound.getInt("PenetrationCount"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("LaserDamage", this.entityData.get(DATA_DAMAGE));
        compound.putInt("MaxPenetration", this.maxPenetration);
        compound.putInt("PenetrationCount", this.entityData.get(DATA_PENETRATION_COUNT));
    }
}