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
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 光之长矛投射物类
 * 实现长矛投掷功能，受重力影响，击中目标时产生神圣光芒
 */
public class SpearOfLightProjectile extends AbstractArrow {
    private static final int MAX_LIFETIME = 200; // 最大生命周期10秒
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(SpearOfLightProjectile.class, EntityDataSerializers.FLOAT);
    
    private Player shooter;
    private ItemStack weaponStack;
    private float baseDamage;

    public SpearOfLightProjectile(EntityType<? extends SpearOfLightProjectile> type, Level level) {
        super(type, level);
        this.shooter = null;
        this.weaponStack = ItemStack.EMPTY;
        this.baseDamage = 0.0f;
        this.setNoGravity(false); // 受重力影响
        this.pickup = AbstractArrow.Pickup.DISALLOWED; // 不允许拾取
        this.entityData.set(DATA_DAMAGE, 0.0f); // 初始化同步数据
    }

    public SpearOfLightProjectile(Level level, Player shooter, ItemStack weaponStack, float damage) {
        this(ModEntities.SPEAR_OF_LIGHT_PROJECTILE.get(), level);
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
     * 获取光之长矛伤害（从同步数据中读取）
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
            // 处理击中逻辑
            if (result.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityResult = (EntityHitResult) result;
                Entity target = entityResult.getEntity();

                if (target instanceof LivingEntity) {
                    // 造成伤害并播放神圣效果
                    createHolyEffect((LivingEntity) target);
                }
            }

            // 播放击中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, 1.0F, 1.0F);

            // 生成神圣光芒粒子
            spawnHolyParticles();

            // 销毁投射物
            this.discard();
        }
    }

    /**
     * 创建神圣效果，对目标造成伤害并生成神圣粒子
     */
    private void createHolyEffect(LivingEntity target) {
        // 计算最终伤害（考虑距离衰减）
        double distance = target.distanceTo(this.shooter);
        float distanceMultiplier = Math.max(0.5f, 1.0f - (float)(distance / 20.0f)); // 20格内伤害不衰减
        float finalDamage = this.baseDamage * distanceMultiplier;

        // 对邪恶生物造成额外伤害（僵尸、骷髅、凋零骷髅等）
        if (isEvilCreature(target)) {
            finalDamage *= 1.5f; // 对邪恶生物造成50%额外伤害
        }

        // 造成伤害
        if (finalDamage > 0) {
            DamageSource damageSource = this.damageSources().playerAttack(this.shooter);
            target.hurt(damageSource, finalDamage);

            // 播放神圣打击音效
            this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 0.8F, 1.2F);
        }
    }

    /**
     * 判断是否为邪恶生物
     */
    private boolean isEvilCreature(LivingEntity entity) {
        String entityName = entity.getType().getDescription().getString().toLowerCase();
        return entityName.contains("zombie") || 
               entityName.contains("skeleton") || 
               entityName.contains("wither") || 
               entityName.contains("phantom") ||
               entityName.contains("ghast") ||
               entityName.contains("enderman") ||
               entityName.contains("spider") ||
               entityName.contains("creeper");
    }

    /**
     * 生成飞行粒子效果
     */
    private void spawnFlightParticles() {
        Vec3 pos = this.position();

        // 生成金色光芒粒子
        for (int i = 0; i < 2; i++) {
            this.level().addParticle(ParticleTypes.GLOW,
                    pos.x + (Math.random() - 0.5) * 0.3,
                    pos.y + (Math.random() - 0.5) * 0.3,
                    pos.z + (Math.random() - 0.5) * 0.3,
                    (Math.random() - 0.5) * 0.02,
                    (Math.random() - 0.5) * 0.02,
                    (Math.random() - 0.5) * 0.02);
        }

        // 生成光之轨迹粒子
        if (this.tickCount % 3 == 0) {
            this.level().addParticle(ParticleTypes.END_ROD,
                    pos.x, pos.y, pos.z,
                    this.getDeltaMovement().x * -0.1,
                    this.getDeltaMovement().y * -0.1,
                    this.getDeltaMovement().z * -0.1);
        }
    }

    /**
     * 生成神圣光芒粒子
     */
    private void spawnHolyParticles() {
        Vec3 center = this.position();

        // 生成神圣光芒爆炸粒子
        for (int i = 0; i < 20; i++) {
            double angle = Math.random() * Math.PI * 2;
            double radius = Math.random() * 2.0;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + (Math.random() - 0.5) * 2.0;

            // 生成金色光芒粒子
            this.level().addParticle(ParticleTypes.GLOW,
                    x, y, z,
                    (Math.random() - 0.5) * 0.1,
                    Math.random() * 0.05,
                    (Math.random() - 0.5) * 0.1);

            // 生成光之粒子
            this.level().addParticle(ParticleTypes.END_ROD,
                    x, y, z,
                    (Math.random() - 0.5) * 0.08,
                    Math.random() * 0.03,
                    (Math.random() - 0.5) * 0.08);
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY; // 不允许拾取
    }
}