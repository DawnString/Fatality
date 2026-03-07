package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 暗黑长矛投射物类
 * 实现长矛投掷功能，受重力影响，击中目标后爆炸，造成范围伤害
 */
public class SpearOfDarknessProjectile extends AbstractArrow {
    private static final int MAX_LIFETIME = 200; // 最大生命周期10秒
    private static final float EXPLOSION_RADIUS = 5.0f; // 爆炸半径5格
    private static final float DAMAGE_REDUCTION_PER_BLOCK = 0.05f; // 每格伤害衰减5%
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(SpearOfDarknessProjectile.class, EntityDataSerializers.FLOAT);
    
    private Player shooter;
    private ItemStack weaponStack;
    private float baseDamage;

    public SpearOfDarknessProjectile(EntityType<? extends SpearOfDarknessProjectile> type, Level level) {
        super(type, level);
        this.shooter = null;
        this.weaponStack = ItemStack.EMPTY;
        this.baseDamage = 0.0f;
        this.setNoGravity(false); // 受重力影响
        this.pickup = AbstractArrow.Pickup.DISALLOWED; // 不允许拾取
        this.entityData.set(DATA_DAMAGE, 0.0f); // 初始化同步数据
    }

    public SpearOfDarknessProjectile(Level level, Player shooter, ItemStack weaponStack, float damage) {
        this(ModEntities.SPEAR_OF_DARKNESS_PROJECTILE.get(), level);
        this.shooter = shooter;
        this.weaponStack = weaponStack;
        this.baseDamage = damage;

        // 设置投射物属性
        this.setOwner(shooter);
        this.setBaseDamage(damage);
        this.entityData.set(DATA_DAMAGE, damage); // 同步伤害数据

        // 设置位置和方向
        Vec3 shooterPos = shooter.getEyePosition();
        Vec3 lookVec = shooter.getLookAngle();

        this.setPos(shooterPos.x, shooterPos.y, shooterPos.z);
        this.shoot(lookVec.x, lookVec.y, lookVec.z, 2.0f, 1.0f);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 0.0f);
    }

    /**
     * 获取暗黑长矛伤害（从同步数据中读取）
     */
    public float getSpearDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    @Override
    public void tick() {
        super.tick();

        // 生成飞行粒子效果
        if (this.level().isClientSide()) {
            spawnFlightParticles();
        }

        // 检查是否超时（10秒后消失）
        if (this.tickCount > MAX_LIFETIME) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (!this.level().isClientSide()) {
            // 创建爆炸效果
            createExplosionEffect();

            // 播放爆炸音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 1.0F);

            // 生成暗黑爆炸粒子
            spawnDarkExplosionParticles();

            // 销毁投射物
            this.discard();
        }
    }

    /**
     * 创建爆炸效果，对半径5格内的实体造成伤害
     */
    private void createExplosionEffect() {
        Vec3 explosionCenter = this.position();
        
        // 获取爆炸范围内的所有实体
        AABB explosionArea = new AABB(
                explosionCenter.x - EXPLOSION_RADIUS,
                explosionCenter.y - EXPLOSION_RADIUS,
                explosionCenter.z - EXPLOSION_RADIUS,
                explosionCenter.x + EXPLOSION_RADIUS,
                explosionCenter.y + EXPLOSION_RADIUS,
                explosionCenter.z + EXPLOSION_RADIUS
        );
        
        List<Entity> entitiesInRange = this.level().getEntities(this, explosionArea);
        
        for (Entity entity : entitiesInRange) {
            if (entity instanceof LivingEntity && entity != this.shooter) {
                LivingEntity target = (LivingEntity) entity;
                
                // 计算距离衰减伤害
                double distance = target.distanceTo(this);
                float distanceMultiplier = Math.max(0.0f, 1.0f - (float)(distance / EXPLOSION_RADIUS));
                float finalDamage = this.baseDamage * distanceMultiplier;

                // 对光明生物造成额外伤害（村民、铁傀儡、羊等）
                if (isLightCreature(target)) {
                    finalDamage *= 1.3f; // 对光明生物造成30%额外伤害
                }

                // 造成伤害
                if (finalDamage > 0) {
                    DamageSource damageSource = this.damageSources().playerAttack(this.shooter);
                    target.hurt(damageSource, finalDamage);

                    // 播放暗黑打击音效
                    this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.8F, 0.8F);
                }
            }
        }
    }

    /**
     * 判断是否为光明生物
     */
    private boolean isLightCreature(LivingEntity entity) {
        String entityName = entity.getType().getDescription().getString().toLowerCase();
        return entityName.contains("villager") || 
               entityName.contains("iron_golem") ||
               entityName.contains("sheep") ||
               entityName.contains("cow") ||
               entityName.contains("pig") ||
               entityName.contains("chicken");
    }

    /**
     * 生成飞行粒子效果
     */
    private void spawnFlightParticles() {
        Vec3 pos = this.position();

        // 生成暗黑粒子
        for (int i = 0; i < 2; i++) {
            this.level().addParticle(ParticleTypes.SMOKE,
                    pos.x + (Math.random() - 0.5) * 0.3,
                    pos.y + (Math.random() - 0.5) * 0.3,
                    pos.z + (Math.random() - 0.5) * 0.3,
                    (Math.random() - 0.5) * 0.02,
                    (Math.random() - 0.5) * 0.02,
                    (Math.random() - 0.5) * 0.02);
        }

        // 生成暗影轨迹粒子
        if (this.tickCount % 3 == 0) {
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    pos.x, pos.y, pos.z,
                    this.getDeltaMovement().x * -0.1,
                    this.getDeltaMovement().y * -0.1,
                    this.getDeltaMovement().z * -0.1);
        }
    }

    /**
     * 生成暗黑爆炸粒子
     */
    private void spawnDarkExplosionParticles() {
        Vec3 center = this.position();

        // 生成暗黑爆炸粒子
        for (int i = 0; i < 30; i++) {
            double angle = Math.random() * Math.PI * 2;
            double radius = Math.random() * EXPLOSION_RADIUS;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + (Math.random() - 0.5) * EXPLOSION_RADIUS;

            // 生成烟雾粒子
            this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                    x, y, z,
                    (Math.random() - 0.5) * 0.2,
                    Math.random() * 0.1,
                    (Math.random() - 0.5) * 0.2);

            // 生成灵魂火焰粒子
            if (i % 3 == 0) {
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        x, y, z,
                        (Math.random() - 0.5) * 0.15,
                        Math.random() * 0.08,
                        (Math.random() - 0.5) * 0.15);
            }
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY; // 不允许拾取
    }
}