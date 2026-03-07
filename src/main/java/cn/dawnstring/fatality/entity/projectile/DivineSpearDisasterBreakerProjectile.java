package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 神枪-破灾投射物
 * 特性：直线飞行，不受重力影响，命中后分裂并可能爆炸
 */
public class DivineSpearDisasterBreakerProjectile extends AbstractArrow {
    
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(DivineSpearDisasterBreakerProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> CHARGE_LEVEL = SynchedEntityData.defineId(DivineSpearDisasterBreakerProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SPLIT_DAMAGE = SynchedEntityData.defineId(DivineSpearDisasterBreakerProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_SPLIT_PROJECTILE = SynchedEntityData.defineId(DivineSpearDisasterBreakerProjectile.class, EntityDataSerializers.BOOLEAN);
    
    private int lifeTicks = 0;
    private static final int MAX_LIFE_TICKS = 200; // 10秒生命周期
    
    public DivineSpearDisasterBreakerProjectile(EntityType<? extends DivineSpearDisasterBreakerProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true); // 不受重力影响
    }
    
    public DivineSpearDisasterBreakerProjectile(Level level, LivingEntity shooter, float damage, int chargeLevel, int splitDamage) {
        this(ModEntities.DIVINE_SPEAR_DISASTER_BREAKER_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.getEntityData().set(DAMAGE, damage);
        this.getEntityData().set(CHARGE_LEVEL, chargeLevel);
        this.getEntityData().set(SPLIT_DAMAGE, splitDamage);
        this.getEntityData().set(IS_SPLIT_PROJECTILE, false);
        
        // 设置基础属性
        this.setBaseDamage(damage);
        this.pickup = AbstractArrow.Pickup.DISALLOWED; // 不能拾取
    }
    
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.getEntityData().define(DAMAGE, 0f);
        this.getEntityData().define(CHARGE_LEVEL, 0);
        this.getEntityData().define(SPLIT_DAMAGE, 0);
        this.getEntityData().define(IS_SPLIT_PROJECTILE, false);
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // 生命周期管理
        lifeTicks++;
        if (lifeTicks > MAX_LIFE_TICKS && !level().isClientSide()) {
            this.discard();
            return;
        }
        
        // 生成飞行粒子效果
        if (level().isClientSide()) {
            generateFlightParticles();
        }
    }
    
    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        
        Entity target = result.getEntity();
        Entity owner = this.getOwner();
        
        if (!level().isClientSide()) {
            // 对目标造成伤害
            if (target instanceof LivingEntity) {
                LivingEntity livingTarget = (LivingEntity) target;
                
                // 计算伤害
                float damage = this.getEntityData().get(DAMAGE);
                
                // 应用伤害
                if (owner instanceof LivingEntity) {
                    livingTarget.hurt(level().damageSources().mobProjectile(this, (LivingEntity) owner), damage);
                } else {
                    livingTarget.hurt(level().damageSources().magic(), damage);
                }
                
                // 播放命中音效
                level().playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.NEUTRAL, 1.0F, 1.0F);
                
                // 检查是否需要分裂（只有第二段和第三段蓄力，且不是分裂出的投射物）
                int chargeLevel = this.getEntityData().get(CHARGE_LEVEL);
                boolean isSplitProjectile = this.getEntityData().get(IS_SPLIT_PROJECTILE);
                
                if (chargeLevel >= 1 && !isSplitProjectile) {
                    splitProjectiles(chargeLevel);
                }
            }
            
            // 移除主投射物
            this.discard();
        }
    }
    
    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        
        if (!level().isClientSide()) {
            // 检查是否需要爆炸（只有第三段蓄力的分裂投射物）
            int chargeLevel = this.getEntityData().get(CHARGE_LEVEL);
            boolean isSplitProjectile = this.getEntityData().get(IS_SPLIT_PROJECTILE);
            
            if (chargeLevel == 2 && isSplitProjectile) {
                createExplosion();
            }
            
            // 播放命中音效
            level().playSound(null, result.getLocation().x, result.getLocation().y, result.getLocation().z,
                    SoundEvents.TRIDENT_HIT_GROUND, SoundSource.NEUTRAL, 1.0F, 1.0F);
            
            // 移除投射物
            this.discard();
        }
    }
    
    /**
     * 分裂投射物
     */
    private void splitProjectiles(int chargeLevel) {
        int splitCount = 3; // 分裂成3份
        int splitDamage = this.getEntityData().get(SPLIT_DAMAGE);
        
        // 当前投射物的位置和方向
        Vec3 currentPos = this.position();
        Vec3 currentDirection = this.getDeltaMovement().normalize();
        
        // 计算分裂方向（向后下方）
        Vec3 splitBaseDirection = currentDirection.scale(-0.5).add(0, -0.3, 0).normalize();
        
        for (int i = 0; i < splitCount; i++) {
            // 计算每个分裂投射物的方向（稍微分散）
            double angle = (2 * Math.PI * i) / splitCount;
            Vec3 offsetDirection = new Vec3(
                    Math.cos(angle) * 0.3,
                    0,
                    Math.sin(angle) * 0.3
            );
            
            Vec3 splitDirection = splitBaseDirection.add(offsetDirection).normalize();
            
            // 创建分裂投射物
            DivineSpearDisasterBreakerProjectile splitProjectile = new DivineSpearDisasterBreakerProjectile(
                    level(), (LivingEntity) this.getOwner(), splitDamage, chargeLevel, splitDamage);
            
            // 设置分裂投射物属性
            splitProjectile.getEntityData().set(IS_SPLIT_PROJECTILE, true);
            splitProjectile.setPos(currentPos);
            splitProjectile.setDeltaMovement(splitDirection.scale(2.0)); // 分裂速度较慢
            
            // 添加到世界
            level().addFreshEntity(splitProjectile);
        }
        
        // 播放分裂音效
        level().playSound(null, currentPos.x, currentPos.y, currentPos.z,
                SoundEvents.ENDER_DRAGON_FLAP, SoundSource.NEUTRAL, 0.8F, 1.2F);
    }
    
    /**
     * 创建爆炸效果
     */
    private void createExplosion() {
        // 爆炸位置
        Vec3 explosionPos = this.position();
        
        // 创建爆炸
        level().explode(this, explosionPos.x, explosionPos.y, explosionPos.z, 3.0f, false, Level.ExplosionInteraction.NONE);
        
        // 播放爆炸音效
        level().playSound(null, explosionPos.x, explosionPos.y, explosionPos.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 1.0F, 1.0F);
        
        // 生成爆炸粒子
        if (level().isClientSide()) {
            for (int i = 0; i < 20; i++) {
                double offsetX = (random.nextDouble() - 0.5) * 2.0;
                double offsetY = (random.nextDouble() - 0.5) * 2.0;
                double offsetZ = (random.nextDouble() - 0.5) * 2.0;
                
                level().addParticle(ParticleTypes.EXPLOSION,
                        explosionPos.x + offsetX, explosionPos.y + offsetY, explosionPos.z + offsetZ,
                        0, 0, 0);
            }
        }
    }
    
    /**
     * 生成飞行粒子效果
     */
    private void generateFlightParticles() {
        // 根据蓄力等级选择粒子颜色
        int chargeLevel = this.getEntityData().get(CHARGE_LEVEL);
        boolean isSplitProjectile = this.getEntityData().get(IS_SPLIT_PROJECTILE);
        
        // 粒子类型
        if (isSplitProjectile) {
            // 分裂投射物使用金色粒子
            level().addParticle(ParticleTypes.GLOW,
                    this.getX(), this.getY(), this.getZ(),
                    0, 0, 0);
        } else {
            // 主投射物根据蓄力等级选择粒子
            switch (chargeLevel) {
                case 0:
                    level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                            this.getX(), this.getY(), this.getZ(),
                            0, 0, 0);
                    break;
                case 1:
                    level().addParticle(ParticleTypes.FLAME,
                            this.getX(), this.getY(), this.getZ(),
                            0, 0, 0);
                    break;
                case 2:
                    level().addParticle(ParticleTypes.GLOW,
                            this.getX(), this.getY(), this.getZ(),
                            0, 0, 0);
                    break;
            }
        }
    }
    
    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY; // 不能拾取
    }
    
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("Damage", this.getEntityData().get(DAMAGE));
        compound.putInt("ChargeLevel", this.getEntityData().get(CHARGE_LEVEL));
        compound.putInt("SplitDamage", this.getEntityData().get(SPLIT_DAMAGE));
        compound.putBoolean("IsSplitProjectile", this.getEntityData().get(IS_SPLIT_PROJECTILE));
        compound.putInt("LifeTicks", lifeTicks);
    }
    
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Damage")) {
            this.getEntityData().set(DAMAGE, compound.getFloat("Damage"));
        }
        if (compound.contains("ChargeLevel")) {
            this.getEntityData().set(CHARGE_LEVEL, compound.getInt("ChargeLevel"));
        }
        if (compound.contains("SplitDamage")) {
            this.getEntityData().set(SPLIT_DAMAGE, compound.getInt("SplitDamage"));
        }
        if (compound.contains("IsSplitProjectile")) {
            this.getEntityData().set(IS_SPLIT_PROJECTILE, compound.getBoolean("IsSplitProjectile"));
        }
        if (compound.contains("LifeTicks")) {
            lifeTicks = compound.getInt("LifeTicks");
        }
    }
}