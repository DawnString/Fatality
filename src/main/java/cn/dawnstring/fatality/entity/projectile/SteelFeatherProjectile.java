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
 * 钢羽投射物类 - 继承BulletProjectile并添加分裂功能
 */
public class SteelFeatherProjectile extends AbstractArrow {
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(SteelFeatherProjectile.class, EntityDataSerializers.FLOAT);

    private int distanceTraveled = 0;
    private boolean hasSplit = false;
    private ItemStack weaponStack;
    
    // 钢羽投射物参数
    private static final int SPLIT_DISTANCE = 10; // 分裂距离（格）
    private static final int SPLIT_COUNT = 5; // 分裂数量
    private static final float PROJECTILE_SPEED = 3.0f; // 投射物速度

    public SteelFeatherProjectile(EntityType<? extends SteelFeatherProjectile> type, Level level) {
        super(type, level);
        this.weaponStack = ItemStack.EMPTY;
        this.setNoGravity(true); // 钢羽投射物无视重力
        this.entityData.set(DATA_DAMAGE, 47.0f); // 默认伤害
    }

    public SteelFeatherProjectile(Level level, LivingEntity shooter, ItemStack weapon, float damage) {
        this(ModEntities.STEEL_FEATHER_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.weaponStack = weapon.copy();

        // 使用传入的伤害值
        this.setBaseDamage(damage);
        this.entityData.set(DATA_DAMAGE, damage);

        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    /**
     * 获取钢羽伤害（从同步数据中读取）
     */
    public float getFeatherDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 47.0f);
    }

    @Override
    public void tick() {
        super.tick();
        
        // 更新飞行距离
        distanceTraveled++;
        
        // 检查是否达到分裂距离
        if (!hasSplit && distanceTraveled >= SPLIT_DISTANCE * 20) { // 20tick/格
            splitIntoMultipleFeathers();
            hasSplit = true;
            this.discard(); // 移除原始羽毛
        }
        
        // 生成钢羽飞行轨迹粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 2; i++) {
                this.level().addParticle(ParticleTypes.CLOUD,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
                        0, 0, 0);

                if (i % 2 == 0) {
                    this.level().addParticle(ParticleTypes.GLOW,
                            this.getX() + (this.random.nextDouble() - 0.5) * 0.1,
                            this.getY() + (this.random.nextDouble() - 0.5) * 0.1,
                            this.getZ() + (this.random.nextDouble() - 0.5) * 0.1,
                            0, 0, 0);
                }
            }
        }
        
        // 检查生命周期结束（10秒后消失）
        if (this.tickCount > 200) {
            this.discard();
        }
    }

    /**
     * 分裂成多个羽毛
     */
    private void splitIntoMultipleFeathers() {
        Level level = this.level();
        LivingEntity owner = (LivingEntity) this.getOwner();
        
        if (owner == null) return;
        
        for (int i = 0; i < SPLIT_COUNT; i++) {
            // 计算分裂羽毛的伤害（原伤害的70%）
            float splitDamage = this.getFeatherDamage() * 0.7f;
            
            SteelFeatherProjectile splitFeather = new SteelFeatherProjectile(level, owner, this.weaponStack, splitDamage);
            splitFeather.hasSplit = true; // 标记为已分裂，避免再次分裂
            
            // 设置位置
            splitFeather.setPos(this.getX(), this.getY(), this.getZ());
            
            // 计算随机方向
            double randomX = (Math.random() - 0.5) * 1.5;
            double randomY = (Math.random() - 0.5) * 0.8;
            double randomZ = (Math.random() - 0.5) * 1.5;
            
            // 确保分裂羽毛有一定的向前速度
            Vec3 baseDirection = this.getDeltaMovement().normalize();
            double finalX = baseDirection.x * 0.5 + randomX;
            double finalY = baseDirection.y * 0.3 + randomY;
            double finalZ = baseDirection.z * 0.5 + randomZ;
            
            splitFeather.shoot(finalX, finalY, finalZ, PROJECTILE_SPEED * 0.6f, 0.3f);
            
            // 添加到世界
            level.addFreshEntity(splitFeather);
        }
        
        // 播放分裂音效
        level.playSound(null, this.getX(), this.getY(), this.getZ(), 
            SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL, 0.5F, 1.5F);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            // 播放命中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ARROW_HIT, SoundSource.BLOCKS, 0.5F, 1.5F);

            // 生成命中粒子效果
            if (this.level().isClientSide()) {
                spawnHitFeatherParticles();
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
                    SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 0.8F, 1.0F);
        }
    }

    /**
     * 生成击中粒子效果
     */
    private void spawnHitFeatherParticles() {
        Vec3 position = this.position();
        
        // 在客户端生成粒子效果
        for (int i = 0; i < 8; i++) {
            double offsetX = (Math.random() - 0.5) * 0.6;
            double offsetY = (Math.random() - 0.5) * 0.6;
            double offsetZ = (Math.random() - 0.5) * 0.6;
            
            // 生成钢羽碎片粒子效果
            this.level().addParticle(ParticleTypes.CLOUD,
                position.x + offsetX,
                position.y + offsetY,
                position.z + offsetZ,
                offsetX * 0.15,
                offsetY * 0.15,
                offsetZ * 0.15);

            // 生成金属光泽粒子
            if (i % 3 == 0) {
                this.level().addParticle(ParticleTypes.GLOW,
                    position.x + offsetX * 0.7,
                    position.y + offsetY * 0.7,
                    position.z + offsetZ * 0.7,
                    offsetX * 0.1,
                    offsetY * 0.1,
                    offsetZ * 0.1);
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
        if (compound.contains("FeatherDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("FeatherDamage"));
        }
        this.distanceTraveled = compound.getInt("DistanceTraveled");
        this.hasSplit = compound.getBoolean("HasSplit");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("FeatherDamage", getFeatherDamage());
        compound.putInt("DistanceTraveled", distanceTraveled);
        compound.putBoolean("HasSplit", hasSplit);
    }
}