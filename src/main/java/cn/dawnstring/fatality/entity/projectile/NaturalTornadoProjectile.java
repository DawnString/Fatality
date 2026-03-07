package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.core.particles.DustParticleOptions;
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
import org.joml.Vector3f;

/**
 * 自然龙卷风投射物类
 * 特性：绿色龙卷风，直线前进，吸引3格内实体，造成持续伤害
 */
public class NaturalTornadoProjectile extends AbstractArrow {
    private static final int MAX_LIFETIME = 200; // 最大生命周期10秒
    private static final float ATTRACTION_RADIUS = 3.0f; // 吸引半径3格
    private static final int DAMAGE_INTERVAL = 10; // 伤害间隔10tick（0.5秒）
    private static final float ATTRACTION_FORCE = 0.3f; // 吸引力大小
    
    private Player shooter;
    private float baseDamage;
    private int lifeTicks = 0;
    private int damageTickCounter = 0;

    public NaturalTornadoProjectile(EntityType<? extends NaturalTornadoProjectile> type, Level level) {
        super(type, level);
        this.shooter = null;
        this.baseDamage = 0.0f;
        this.setNoGravity(true); // 不受重力影响
        this.pickup = AbstractArrow.Pickup.DISALLOWED; // 不允许拾取
    }

    public NaturalTornadoProjectile(Level level, Player shooter, float damage) {
        this(ModEntities.NATURAL_TORNADO_PROJECTILE.get(), level);
        this.shooter = shooter;
        this.baseDamage = damage;
        
        // 设置投射物属性
        this.setOwner(shooter);
        this.setBaseDamage(damage);
        
        // 设置初始位置和方向
        Vec3 lookVec = shooter.getLookAngle();
        Vec3 eyePos = shooter.getEyePosition();
        
        this.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
        this.shoot(lookVec.x, lookVec.y, lookVec.z, 1.2f, 0.0f); // 1.2速度，无散布
    }

    @Override
    public void tick() {
        super.tick();
        lifeTicks++;
        
        // 生成自然龙卷风粒子效果
        if (this.level().isClientSide()) {
            spawnNaturalTornadoParticles();
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
            // 碰撞时播放音效并消失
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.NEUTRAL,
                    0.5F, 1.0F);
            this.discard();
        }
    }
    
    /**
     * 生成自然龙卷风粒子效果（绿色主题）
     */
    private void spawnNaturalTornadoParticles() {
        // 生成自然龙卷风核心粒子（绿色粒子）
        for (int i = 0; i < 5; i++) {
            // 使用绿色粒子效果
            this.level().addParticle(ParticleTypes.HAPPY_VILLAGER,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                    this.getY() + (this.random.nextDouble() - 0.5) * 0.5,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                    0, 0.1, 0);
        }
        
        // 生成自然龙卷风外围粒子（绿色和树叶粒子）
        for (int i = 0; i < 8; i++) {
            double angle = (lifeTicks * 0.1 + i * 0.785) % (2 * Math.PI);
            double radius = 1.0 + this.random.nextDouble() * 0.5;
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;
            
            // 交替生成绿色粒子和树叶粒子
            if (i % 2 == 0) {
                // 绿色粒子
                this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        this.getX() + offsetX,
                        this.getY() + this.random.nextDouble() * 2.0,
                        this.getZ() + offsetZ,
                        0, 0.05, 0);
            } else {
                // 树叶粒子
                this.level().addParticle(ParticleTypes.CHERRY_LEAVES,
                        this.getX() + offsetX,
                        this.getY() + this.random.nextDouble() * 2.0,
                        this.getZ() + offsetZ,
                        0, 0.05, 0);
            }
        }
        
        // 生成自然龙卷风轨迹粒子（绿色光点）
        if (lifeTicks % 3 == 0) {
            this.level().addParticle(new DustParticleOptions(new Vector3f(0.2f, 0.8f, 0.2f), 1.0f),
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                    this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                    0, 0.05, 0);
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
                // 吸引实体向龙卷风中心
                attractEntity(livingEntity);
                
                // 对实体造成伤害
                damageEntity(livingEntity);
            }
        }
    }
    
    /**
     * 吸引实体向龙卷风中心
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
            // 生成自然伤害粒子（绿色粒子）
            if (this.level().isClientSide()) {
                for (int i = 0; i < 3; i++) {
                    this.level().addParticle(ParticleTypes.HAPPY_VILLAGER,
                            entity.getX() + (this.random.nextDouble() - 0.5) * entity.getBbWidth(),
                            entity.getY() + this.random.nextDouble() * entity.getBbHeight(),
                            entity.getZ() + (this.random.nextDouble() - 0.5) * entity.getBbWidth(),
                            0, 0.1, 0);
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
        // 生成自然碰撞粒子（绿色和树叶粒子）
        if (this.level().isClientSide()) {
            for (int i = 0; i < 10; i++) {
                if (i % 2 == 0) {
                    this.level().addParticle(ParticleTypes.HAPPY_VILLAGER,
                            target.getX() + (this.random.nextDouble() - 0.5) * target.getBbWidth(),
                            target.getY() + this.random.nextDouble() * target.getBbHeight(),
                            target.getZ() + (this.random.nextDouble() - 0.5) * target.getBbWidth(),
                            0, 0.1, 0);
                } else {
                    this.level().addParticle(ParticleTypes.CHERRY_LEAVES,
                            target.getX() + (this.random.nextDouble() - 0.5) * target.getBbWidth(),
                            target.getY() + this.random.nextDouble() * target.getBbHeight(),
                            target.getZ() + (this.random.nextDouble() - 0.5) * target.getBbWidth(),
                            0, 0.1, 0);
                }
            }
        }
        
        // 销毁投射物
        this.discard();
    }
}