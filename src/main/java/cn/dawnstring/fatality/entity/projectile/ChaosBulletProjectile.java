package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

/**
 * 祸乱子弹投射物 - 击中目标后生成弹幕
 */
public class ChaosBulletProjectile extends AbstractArrow {
    
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(ChaosBulletProjectile.class, EntityDataSerializers.FLOAT);
    private static final Random random = new Random();
    
    public ChaosBulletProjectile(EntityType<? extends ChaosBulletProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(false); // 有重力，模拟真实子弹
        this.setPierceLevel((byte) 0); // 不穿透
    }
    
    public ChaosBulletProjectile(Level level, LivingEntity owner, ItemStack weaponStack, float damage) {
        this(ModEntities.CHAOS_BULLET_PROJECTILE.get(), level);
        this.setOwner(owner);
        this.setDamage(damage);
        this.setBaseDamage(damage);
        
        // 设置初始位置
        Vec3 lookVec = owner.getLookAngle();
        Vec3 eyePos = owner.getEyePosition();
        this.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
    }
    
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DAMAGE, 0.0f);
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // 生成子弹尾迹粒子
        if (this.level().isClientSide()) {
            for (int i = 0; i < 2; i++) {
                double offsetX = (random.nextDouble() - 0.5) * 0.1;
                double offsetY = (random.nextDouble() - 0.5) * 0.1;
                double offsetZ = (random.nextDouble() - 0.5) * 0.1;
                
                this.level().addParticle(ParticleTypes.SMOKE,
                        this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ,
                        0, 0, 0);
            }
        }
    }
    
    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        
        if (!this.level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            // 播放击中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE,
                    net.minecraft.sounds.SoundSource.NEUTRAL, 0.8F, 0.9F);
            
            // 生成击中粒子效果
            if (this.level().isClientSide()) {
                for (int i = 0; i < 15; i++) {
                    double offsetX = (random.nextDouble() - 0.5) * 1.5;
                    double offsetY = (random.nextDouble() - 0.5) * 1.5;
                    double offsetZ = (random.nextDouble() - 0.5) * 1.5;
                    
                    this.level().addParticle(ParticleTypes.FLAME,
                            target.getX() + offsetX, target.getY() + offsetY, target.getZ() + offsetZ,
                            0, 0.1, 0);
                }
            }
            
            // 生成弹幕（4-8个）
            int barrageCount = 4 + random.nextInt(5); // 4-8个弹幕
            float barrageDamage = this.getDamage() * 0.5f; // 弹幕伤害为基础伤害的0.5倍
            
            for (int i = 0; i < barrageCount; i++) {
                ChaosBarrageProjectile barrage = new ChaosBarrageProjectile(this.level(), 
                        (LivingEntity) this.getOwner(), barrageDamage, target);
                this.level().addFreshEntity(barrage);
            }
            
            // 播放弹幕生成音效
            this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    net.minecraft.sounds.SoundEvents.FIREWORK_ROCKET_LAUNCH,
                    net.minecraft.sounds.SoundSource.NEUTRAL, 0.6F, 1.5F);
        }
    }
    
    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY; // 不能被捡起
    }
    
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Damage")) {
            this.setDamage(compound.getFloat("Damage"));
        }
    }
    
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("Damage", this.getDamage());
    }
    
    // Getter和Setter方法
    public float getDamage() {
        return this.entityData.get(DAMAGE);
    }
    
    public void setDamage(float damage) {
        this.entityData.set(DAMAGE, damage);
    }
}