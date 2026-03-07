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
 * 偏光棱镜激光投射物类
 * 实现持续激光伤害效果，对激光路径上的实体造成持续伤害
 */
public class PolarizingPrismLaserProjectile extends AbstractArrow {
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(PolarizingPrismLaserProjectile.class, EntityDataSerializers.FLOAT);
    
    private Player shooter;
    private ItemStack weaponStack;
    private float baseDamage;
    private Set<UUID> hitEntities = new HashSet<>(); // 记录已击中的实体，避免重复伤害
    private int lifeTime = 0;
    private static final int MAX_LIFETIME = 5; // 最大生命周期5tick（激光持续存在时间）
    private static final double LASER_LENGTH = 50.0; // 激光长度50格
    private static final int DAMAGE_INTERVAL = 5; // 每5tick造成一次伤害

    public PolarizingPrismLaserProjectile(EntityType<? extends PolarizingPrismLaserProjectile> type, Level level) {
        super(type, level);
        this.shooter = null;
        this.weaponStack = ItemStack.EMPTY;
        this.baseDamage = 0.0f;
        this.setNoGravity(true); // 不受重力影响
        this.pickup = AbstractArrow.Pickup.DISALLOWED; // 不允许拾取
        this.entityData.set(DATA_DAMAGE, 0.0f);
    }

    public PolarizingPrismLaserProjectile(Level level, Player shooter, ItemStack weaponStack, float damage) {
        this(ModEntities.POLARIZING_PRISM_LASER_PROJECTILE.get(), level);
        this.shooter = shooter;
        this.weaponStack = weaponStack;
        this.baseDamage = damage;
        this.setOwner(shooter);
        this.setBaseDamage(damage);
        this.entityData.set(DATA_DAMAGE, damage);
    }

    /**
     * 设置射击者信息（用于从实体类型创建时设置）
     */
    public void setShooter(Player shooter, ItemStack weaponStack, float damage) {
        this.shooter = shooter;
        this.weaponStack = weaponStack;
        this.baseDamage = damage;
        this.setOwner(shooter);
        this.setBaseDamage(damage);
        this.entityData.set(DATA_DAMAGE, damage);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 0.0f);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 生成激光粒子效果
        if (this.level().isClientSide) {
            spawnLaserParticles();
        }

        // 检测激光路径上的实体并造成伤害（服务器端）
        if (!this.level().isClientSide) {
            // 每DAMAGE_INTERVAL tick检测一次
            if (lifeTime % DAMAGE_INTERVAL == 0) {
                checkLaserPath();
            }
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
        for (int i = 0; i < 15; i++) {
            double progress = (double) i / 15;
            Vec3 particlePos = startPos.add(direction.scale(LASER_LENGTH * progress));
            
            // 生成白色激光粒子（偏光棱镜效果）
            this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    particlePos.x, particlePos.y, particlePos.z,
                    0, 0, 0);
            
            // 生成发光粒子
            this.level().addParticle(ParticleTypes.GLOW,
                    particlePos.x, particlePos.y, particlePos.z,
                    0, 0, 0);
            
            // 生成彩虹色粒子（偏光效果）
            if (i % 3 == 0) {
                this.level().addParticle(ParticleTypes.END_ROD,
                        particlePos.x, particlePos.y, particlePos.z,
                        0, 0, 0);
            }
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
            // 排除射击者
            if (entity == shooter) {
                continue;
            }

            // 检查实体是否在激光路径上
            if (isEntityInLaserPath(entity, startPos, direction)) {
                // 对实体造成持续伤害
                applyLaserDamage(entity);
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
        return distanceToLine <= entity.getBbWidth() / 2 + 0.3;
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
                    net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH, net.minecraft.sounds.SoundSource.NEUTRAL, 0.3F, 1.0F);
            
            // 生成击中粒子效果
            if (this.level().isClientSide) {
                for (int i = 0; i < 3; i++) {
                    this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                            target.getX(), target.getY() + target.getEyeHeight() / 2, target.getZ(),
                            (this.random.nextFloat() - 0.5) * 0.2, 
                            (this.random.nextFloat() - 0.5) * 0.2, 
                            (this.random.nextFloat() - 0.5) * 0.2);
                }
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        // 激光不因碰撞而消失，持续存在
        if (result.getType() == HitResult.Type.BLOCK) {
            // 碰到方块时生成粒子效果
            if (this.level().isClientSide) {
                Vec3 hitPos = result.getLocation();
                for (int i = 0; i < 5; i++) {
                    this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                            hitPos.x, hitPos.y, hitPos.z,
                            (this.random.nextFloat() - 0.5) * 0.3, 
                            (this.random.nextFloat() - 0.5) * 0.3, 
                            (this.random.nextFloat() - 0.5) * 0.3);
                }
            }
        }
        
        // 不调用父类的onHit，避免激光消失
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        // 实体碰撞处理 - 激光持续存在，不因碰撞而消失
        if (result.getEntity() instanceof LivingEntity livingEntity) {
            // 应用伤害
            applyLaserDamage(livingEntity);
        }
        
        // 不调用父类的onHitEntity，避免激光消失
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("BaseDamage")) {
            this.baseDamage = compound.getFloat("BaseDamage");
            this.entityData.set(DATA_DAMAGE, this.baseDamage);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("BaseDamage", this.baseDamage);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY; // 不允许拾取
    }
}