package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.effects.ArmorBreakEffect;
import cn.dawnstring.fatality.effects.SpiritualFireBurnEffect;
import cn.dawnstring.fatality.registry.ModEffects;
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
import net.minecraft.world.effect.MobEffectInstance;
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

/**
 * 灵火审判投射物 - SpiritFireJudgment的专属投射物
 * 特性：击中后散射子弹、锥形区域伤害、护甲粉碎、灵火灼烧效果
 */
public class SpiritFireJudgmentProjectile extends AbstractArrow {
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(SpiritFireJudgmentProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_HAS_SCATTERED = SynchedEntityData.defineId(SpiritFireJudgmentProjectile.class, EntityDataSerializers.BOOLEAN);
    
    private Player shooter;
    private ItemStack weaponItem;
    private int lifeTime = 0;
    private final int maxLifeTime = 100; // 5秒生命周期
    
    public SpiritFireJudgmentProjectile(EntityType<? extends SpiritFireJudgmentProjectile> type, Level level) {
        super(type, level);
        this.shooter = null;
        this.weaponItem = ItemStack.EMPTY;
        this.setNoGravity(true);
        this.entityData.set(DATA_DAMAGE, 798.0f);
        this.entityData.set(DATA_HAS_SCATTERED, false);
    }
    
    public SpiritFireJudgmentProjectile(Level level, Player shooter, ItemStack weaponItem, float damage) {
        this(ModEntities.SPIRIT_FIRE_JUDGMENT_PROJECTILE.get(), level);
        this.shooter = shooter;
        this.weaponItem = weaponItem;
        
        // 设置投射物属性
        this.setOwner(shooter);
        this.setBaseDamage(damage);
        this.entityData.set(DATA_DAMAGE, damage);
        this.entityData.set(DATA_HAS_SCATTERED, false);
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
        
        // 设置位置和方向
        Vec3 shooterPos = shooter.getEyePosition();
        Vec3 lookVec = shooter.getLookAngle();
        
        this.setPos(shooterPos.x, shooterPos.y, shooterPos.z);
        this.shoot(lookVec.x, lookVec.y, lookVec.z, 3.0f, 0.0f); // 高速度，无散射
    }
    
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 798.0f);
        this.entityData.define(DATA_HAS_SCATTERED, false);
    }
    
    @Override
    public void tick() {
        super.tick();
        lifeTime++;
        
        // 生成灵火飞行粒子效果
        if (this.level().isClientSide()) {
            spawnFlightParticles();
        }
        
        // 检查生命周期结束
        if (lifeTime >= maxLifeTime) {
            this.discard();
        }
    }
    
    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        
        if (!this.level().isClientSide() && !this.entityData.get(DATA_HAS_SCATTERED)) {
            // 标记为已散射，避免重复触发
            this.entityData.set(DATA_HAS_SCATTERED, true);
            
            // 处理击中逻辑
            if (result.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityResult = (EntityHitResult) result;
                Entity target = entityResult.getEntity();
                
                if (target instanceof LivingEntity) {
                    // 对主要目标造成伤害并应用效果
                    applyPrimaryTargetEffects((LivingEntity) target);
                    
                    // 散射子弹，对锥形区域造成伤害
                    scatterBullets((LivingEntity) target);
                }
            } else {
                // 击中方块或其他物体，也散射子弹
                scatterBullets(null);
            }
            
            // 播放灵火爆炸音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.5F, 0.7F);
            
            // 销毁投射物
            this.discard();
        }
    }
    
    /**
     * 对主要目标应用伤害和效果
     */
    private void applyPrimaryTargetEffects(LivingEntity primaryTarget) {
        // 对主要目标造成全额伤害
        float damage = this.entityData.get(DATA_DAMAGE);
        if (damage > 0) {
            primaryTarget.hurt(primaryTarget.damageSources().arrow(this, shooter), damage);
            
            // 应用护甲粉碎效果（5秒）
            applyArmorBreakEffect(primaryTarget);
            
            // 应用灵火灼烧效果（5秒）
            applySpiritualFireBurnEffect(primaryTarget);
            
            // 播放命中音效
            this.level().playSound(null, primaryTarget.getX(), primaryTarget.getY(), primaryTarget.getZ(),
                    SoundEvents.BLAZE_HURT, SoundSource.PLAYERS, 1.0F, 0.9F);
        }
    }
    
    /**
     * 散射子弹，对锥形区域造成伤害
     */
    private void scatterBullets(LivingEntity primaryTarget) {
        Vec3 hitPos = this.position();
        Vec3 hitDirection = this.getDeltaMovement().normalize();
        
        // 散射6-8发子弹
        int bulletCount = 6 + this.random.nextInt(3); // 6-8发
        
        for (int i = 0; i < bulletCount; i++) {
            // 计算散射方向
            Vec3 scatterDirection = calculateScatterDirection(hitDirection, i, bulletCount);
            
            // 创建散射子弹
            createScatterBullet(hitPos, scatterDirection, primaryTarget);
        }
        
        // 生成散射粒子效果
        spawnScatterParticles(hitPos);
    }
    
    /**
     * 计算散射方向
     */
    private Vec3 calculateScatterDirection(Vec3 baseDirection, int index, int totalBullets) {
        // 计算锥形散射角度（15-30度）
        float maxAngle = 30.0f;
        float angle = (float) index / (totalBullets - 1) * maxAngle - maxAngle / 2.0f;
        
        // 转换为弧度
        double angleRad = Math.toRadians(angle);
        
        // 计算旋转后的方向
        double x = baseDirection.x * Math.cos(angleRad) - baseDirection.z * Math.sin(angleRad);
        double z = baseDirection.x * Math.sin(angleRad) + baseDirection.z * Math.cos(angleRad);
        
        return new Vec3(x, baseDirection.y, z).normalize();
    }
    
    /**
     * 创建散射子弹
     */
    private void createScatterBullet(Vec3 startPos, Vec3 direction, LivingEntity primaryTarget) {
        // 散射子弹造成30%的伤害
        float scatterDamage = this.entityData.get(DATA_DAMAGE) * 0.3f;
        
        // 创建散射子弹投射物
        SpiritFireScatterProjectile scatterProjectile = new SpiritFireScatterProjectile(
                this.level(), this.shooter, this.weaponItem, scatterDamage, direction
        );
        
        // 设置位置和速度
        scatterProjectile.setPos(startPos.x, startPos.y, startPos.z);
        scatterProjectile.shoot(direction.x, direction.y, direction.z, 2.0f, 0.0f);
        
        // 添加到世界
        this.level().addFreshEntity(scatterProjectile);
    }
    
    /**
     * 应用护甲粉碎效果
     */
    private void applyArmorBreakEffect(LivingEntity target) {
        if (ModEffects.ARMOR_BREAK != null) {
            // 护甲粉碎效果：持续5秒
            MobEffectInstance armorBreakEffect = new MobEffectInstance(ModEffects.ARMOR_BREAK.get(), 100, 0);
            target.addEffect(armorBreakEffect);
        }
    }
    
    /**
     * 应用灵火灼烧效果
     */
    private void applySpiritualFireBurnEffect(LivingEntity target) {
        if (ModEffects.SPIRITUAL_FIRE_BURN != null) {
            // 灵火灼烧效果：持续5秒
            MobEffectInstance spiritualFireEffect = new MobEffectInstance(ModEffects.SPIRITUAL_FIRE_BURN.get(), 100, 0);
            target.addEffect(spiritualFireEffect);
        }
    }
    
    /**
     * 生成飞行粒子效果
     */
    private void spawnFlightParticles() {
        Vec3 pos = this.position();
        
        // 生成灵火粒子（橙色火焰）
        if (this.tickCount % 2 == 0) {
            this.level().addParticle(ParticleTypes.FLAME,
                    pos.x, pos.y, pos.z,
                    this.getDeltaMovement().x * -0.1,
                    this.getDeltaMovement().y * -0.1,
                    this.getDeltaMovement().z * -0.1);
        }
        
        // 生成灵魂火焰粒子
        if (this.tickCount % 3 == 0) {
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    pos.x, pos.y, pos.z,
                    this.getDeltaMovement().x * -0.05,
                    this.getDeltaMovement().y * -0.05,
                    this.getDeltaMovement().z * -0.05);
        }
    }
    
    /**
     * 生成散射粒子效果
     */
    private void spawnScatterParticles(Vec3 center) {
        // 生成灵火爆炸粒子
        for (int i = 0; i < 20; i++) {
            double angle = Math.random() * Math.PI * 2;
            double radius = Math.random() * 1.5;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + (Math.random() - 0.5) * 1.5;
            
            // 生成火焰粒子
            this.level().addParticle(ParticleTypes.FLAME,
                    x, y, z,
                    (Math.random() - 0.5) * 0.3,
                    Math.random() * 0.2,
                    (Math.random() - 0.5) * 0.3);
            
            // 生成灵魂火焰粒子
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    x, y, z,
                    (Math.random() - 0.5) * 0.2,
                    Math.random() * 0.15,
                    (Math.random() - 0.5) * 0.2);
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
        if (compound.contains("ProjectileDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("ProjectileDamage"));
        }
        if (compound.contains("HasScattered")) {
            this.entityData.set(DATA_HAS_SCATTERED, compound.getBoolean("HasScattered"));
        }
    }
    
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putFloat("ProjectileDamage", this.entityData.get(DATA_DAMAGE));
        compound.putBoolean("HasScattered", this.entityData.get(DATA_HAS_SCATTERED));
    }
}