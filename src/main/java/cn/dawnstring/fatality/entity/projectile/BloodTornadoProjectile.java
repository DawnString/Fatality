package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 血色龙卷风投射物类
 * 特性：直线前进，吸引4格内实体，造成持续伤害，红色粒子效果
 */
public class BloodTornadoProjectile extends AbstractArrow {
    private static final int MAX_LIFETIME = 200; // 最大生命周期10秒
    private static final float ATTRACTION_RADIUS = 4.0f; // 吸引半径4格（比普通龙卷风更大）
    private static final int DAMAGE_INTERVAL = 10; // 伤害间隔10tick（0.5秒）
    private static final float ATTRACTION_FORCE = 0.4f; // 吸引力大小（比普通龙卷风更强）
    
    private Player shooter;
    private float baseDamage;
    private int lifeTicks = 0;
    private int damageTickCounter = 0;

    public BloodTornadoProjectile(EntityType<? extends BloodTornadoProjectile> type, Level level) {
        super(type, level);
        this.shooter = null;
        this.baseDamage = 0.0f;
        this.setNoGravity(true); // 不受重力影响
        this.pickup = AbstractArrow.Pickup.DISALLOWED; // 不允许拾取
    }

    public BloodTornadoProjectile(Level level, Player shooter, float damage) {
        this(ModEntities.BLOOD_TORNADO_PROJECTILE.get(), level);
        this.shooter = shooter;
        this.baseDamage = damage;
        
        // 设置投射物属性
        this.setOwner(shooter);
        this.setBaseDamage(damage);
        
        // 设置初始位置和方向
        Vec3 lookVec = shooter.getLookAngle();
        Vec3 eyePos = shooter.getEyePosition();
        
        this.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
        this.shoot(lookVec.x, lookVec.y, lookVec.z, 1.5f, 0.0f); // 1.5速度，无散布
    }

    @Override
    public void tick() {
        super.tick();
        lifeTicks++;
        
        // 生成血色龙卷风粒子效果
        if (this.level().isClientSide()) {
            spawnBloodTornadoParticles();
        }
        
        // 检查生命周期结束
        if (lifeTicks >= MAX_LIFETIME) {
            this.discard();
            return;
        }
        
        // 每10tick执行一次吸引和伤害逻辑
        if (lifeTicks % DAMAGE_INTERVAL == 0) {
            performAttractionAndDamage();
        }
        
        // 检查是否击中方块或超出世界边界
        if (this.horizontalCollision || this.verticalCollision || this.isInWall()) {
            // 碰撞时播放血色音效并消失
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), 
                    SoundEvents.PLAYER_HURT_SWEET_BERRY_BUSH, SoundSource.HOSTILE, 1.0F, 0.5F);
            this.discard();
        }
    }
    
    /**
     * 生成血色龙卷风粒子效果
     */
    private void spawnBloodTornadoParticles() {
        // 生成血色龙卷风核心粒子（红色粒子）
        for (int i = 0; i < 6; i++) {
            this.level().addParticle(ParticleTypes.DAMAGE_INDICATOR,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                    this.getY() + (this.random.nextDouble() - 0.5) * 0.5,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                    0, 0.1, 0);
        }
        
        // 生成血色龙卷风外围粒子（深红色粒子）
        for (int i = 0; i < 10; i++) {
            double angle = (lifeTicks * 0.1 + i * 0.628) % (2 * Math.PI);
            double radius = 1.2 + this.random.nextDouble() * 0.6;
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;
            
            this.level().addParticle(ParticleTypes.LAVA,
                    this.getX() + offsetX,
                    this.getY() + this.random.nextDouble() * 2.5,
                    this.getZ() + offsetZ,
                    0, 0.08, 0);
        }
        
        // 生成血色烟雾粒子
        for (int i = 0; i < 4; i++) {
            this.level().addParticle(ParticleTypes.CRIMSON_SPORE,
                    this.getX() + (this.random.nextDouble() - 0.5) * 1.0,
                    this.getY() + this.random.nextDouble() * 2.0,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 1.0,
                    (this.random.nextDouble() - 0.5) * 0.1,
                    0.05,
                    (this.random.nextDouble() - 0.5) * 0.1);
        }
    }
    
    /**
     * 执行吸引和伤害逻辑
     */
    private void performAttractionAndDamage() {
        if (this.level().isClientSide()) {
            return;
        }
        
        // 计算吸引区域
        AABB attractionBox = new AABB(
                this.getX() - ATTRACTION_RADIUS, this.getY() - ATTRACTION_RADIUS, this.getZ() - ATTRACTION_RADIUS,
                this.getX() + ATTRACTION_RADIUS, this.getY() + ATTRACTION_RADIUS, this.getZ() + ATTRACTION_RADIUS
        );
        
        // 获取区域内的所有实体
        for (Entity entity : this.level().getEntities(this, attractionBox)) {
            if (entity instanceof LivingEntity livingEntity && entity != this.shooter && !entity.isAlliedTo(this.shooter)) {
                // 吸引实体向血色龙卷风中心
                attractEntity(livingEntity);
                
                // 对实体造成伤害
                damageEntity(livingEntity);
            }
        }
    }
    
    /**
     * 吸引实体向血色龙卷风中心
     */
    private void attractEntity(LivingEntity entity) {
        Vec3 tornadoPos = this.position();
        Vec3 entityPos = entity.position();
        
        // 计算吸引方向
        Vec3 attractionDirection = tornadoPos.subtract(entityPos).normalize();
        
        // 计算距离，距离越近吸引力越小
        double distance = tornadoPos.distanceTo(entityPos);
        double forceMultiplier = Math.min(1.0, distance / ATTRACTION_RADIUS);
        
        // 应用吸引力
        Vec3 currentMotion = entity.getDeltaMovement();
        Vec3 attractionMotion = attractionDirection.scale(ATTRACTION_FORCE * forceMultiplier);
        
        entity.setDeltaMovement(currentMotion.add(attractionMotion));
    }
    
    /**
     * 对实体造成伤害
     */
    private void damageEntity(LivingEntity entity) {
        // 计算伤害（基础伤害的1/4，因为每0.5秒触发一次）
        float damage = this.baseDamage * 0.25f;
        
        // 创建伤害源
        DamageSource damageSource = this.damageSources().indirectMagic(this, this.shooter);
        
        // 造成伤害
        if (entity.hurt(damageSource, damage)) {
            // 生成血色伤害粒子
            if (this.level().isClientSide()) {
                for (int i = 0; i < 4; i++) {
                    this.level().addParticle(ParticleTypes.DAMAGE_INDICATOR,
                            entity.getX() + (this.random.nextDouble() - 0.5) * entity.getBbWidth(),
                            entity.getY() + this.random.nextDouble() * entity.getBbHeight(),
                            entity.getZ() + (this.random.nextDouble() - 0.5) * entity.getBbWidth(),
                            0, 0.15, 0);
                }
                
                // 生成血色烟雾粒子
                for (int i = 0; i < 2; i++) {
                    this.level().addParticle(ParticleTypes.CRIMSON_SPORE,
                            entity.getX() + (this.random.nextDouble() - 0.5) * entity.getBbWidth(),
                            entity.getY() + this.random.nextDouble() * entity.getBbHeight(),
                            entity.getZ() + (this.random.nextDouble() - 0.5) * entity.getBbWidth(),
                            (this.random.nextDouble() - 0.5) * 0.1,
                            0.1,
                            (this.random.nextDouble() - 0.5) * 0.1);
                }
            }
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY; // 不能拾取
    }

    @Override
    public boolean isNoGravity() {
        return true; // 不受重力影响
    }
    
    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        // 生成血色碰撞粒子
        if (this.level().isClientSide()) {
            for (int i = 0; i < 12; i++) {
                this.level().addParticle(ParticleTypes.LAVA,
                        target.getX() + (this.random.nextDouble() - 0.5) * target.getBbWidth(),
                        target.getY() + this.random.nextDouble() * target.getBbHeight(),
                        target.getZ() + (this.random.nextDouble() - 0.5) * target.getBbWidth(),
                        0, 0.12, 0);
            }
            
            // 播放血色音效
            this.level().playSound(null, target.getX(), target.getY(), target.getZ(), 
                    SoundEvents.PLAYER_HURT_SWEET_BERRY_BUSH, SoundSource.HOSTILE, 0.8F, 0.6F);
        }
        
        // 销毁投射物
        this.discard();
    }
}