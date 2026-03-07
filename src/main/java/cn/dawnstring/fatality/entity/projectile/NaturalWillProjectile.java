package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.registry.ModEntities;
import cn.dawnstring.fatality.system.LifeRingEffectManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
 * 自然意志投射物类
 * 实现长矛投掷功能，受重力影响，击中目标后生成生命之环
 * 生命之环：使用粒子云实现，持续治疗范围内的友方单位，伤害敌方单位
 */
public class NaturalWillProjectile extends AbstractArrow {
    private static final int MAX_LIFETIME = 200; // 最大生命周期10秒
    private static final float LIFE_RING_RADIUS = 5.0f; // 生命之环半径5格
    private static final int LIFE_RING_DURATION = 100; // 生命之环持续时间5秒
    private static final float HEAL_AMOUNT = 10.0f; // 每次治疗量
    private static final float DAMAGE_AMOUNT = 5.0f; // 每次伤害量
    private static final int EFFECT_INTERVAL = 20; // 效果间隔1秒
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(NaturalWillProjectile.class, EntityDataSerializers.FLOAT);
    
    private Player shooter;
    private ItemStack weaponStack;
    private float baseDamage;

    public NaturalWillProjectile(EntityType<? extends NaturalWillProjectile> type, Level level) {
        super(type, level);
        this.shooter = null;
        this.weaponStack = ItemStack.EMPTY;
        this.baseDamage = 0.0f;
        this.setNoGravity(false); // 受重力影响
        this.pickup = AbstractArrow.Pickup.DISALLOWED; // 不允许拾取
        this.entityData.set(DATA_DAMAGE, 0.0f); // 初始化同步数据
    }

    public NaturalWillProjectile(Level level, Player shooter, ItemStack weaponStack, float damage) {
        this(ModEntities.NATURAL_WILL_PROJECTILE.get(), level);
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
        this.shoot(lookVec.x, lookVec.y, lookVec.z, 2.5f, 1.0f);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 0.0f);
    }

    /**
     * 获取自然意志伤害（从同步数据中读取）
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
                    // 造成初始伤害并生成生命之环
                    createLifeRingEffect((LivingEntity) target);
                }
            } else {
                // 击中地面或其他物体，也生成生命之环
                createLifeRingEffect(null);
            }

            // 播放击中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, 1.0F, 0.8F);

            // 生成自然粒子效果
            spawnNatureParticles();

            // 销毁投射物
            this.discard();
        }
    }

    /**
     * 创建生命之环效果 - 使用粒子云实现
     */
    private void createLifeRingEffect(LivingEntity initialTarget) {
        Vec3 center = this.position();
        
        // 对初始目标造成伤害
        if (initialTarget != null) {
            // 计算最终伤害（考虑距离衰减）
            double distance = initialTarget.distanceTo(this.shooter);
            float distanceMultiplier = Math.max(0.5f, 1.0f - (float)(distance / 20.0f)); // 20格内伤害不衰减
            float finalDamage = this.baseDamage * distanceMultiplier;

            // 造成伤害
            if (finalDamage > 0) {
                initialTarget.hurt(this.damageSources().playerAttack(this.shooter), finalDamage);
            }
        }

        // 启动生命之环粒子云效果
        startLifeRingParticleCloud(center);

        // 播放生命之环激活音效
        this.level().playSound(null, center.x, center.y, center.z,
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8F, 1.0F);
    }

    /**
     * 启动生命之环粒子云效果
     */
    private void startLifeRingParticleCloud(Vec3 center) {
        // 在服务器端启动生命之环效果管理器
        if (!this.level().isClientSide()) {
            // 使用新的生命之环效果管理器
            LifeRingEffectManager.startLifeRingEffect(this.level(), center, LIFE_RING_DURATION, LIFE_RING_RADIUS, this.shooter);
        }
    }

    /**
     * 生成飞行粒子效果
     */
    private void spawnFlightParticles() {
        Vec3 pos = this.position();

        // 生成自然粒子（绿色和金色）
        for (int i = 0; i < 3; i++) {
            this.level().addParticle(ParticleTypes.HAPPY_VILLAGER,
                    pos.x + (Math.random() - 0.5) * 0.3,
                    pos.y + (Math.random() - 0.5) * 0.3,
                    pos.z + (Math.random() - 0.5) * 0.3,
                    (Math.random() - 0.5) * 0.02,
                    (Math.random() - 0.5) * 0.02,
                    (Math.random() - 0.5) * 0.02);
        }

        // 生成光之轨迹粒子
        if (this.tickCount % 2 == 0) {
            this.level().addParticle(ParticleTypes.GLOW,
                    pos.x, pos.y, pos.z,
                    this.getDeltaMovement().x * -0.1,
                    this.getDeltaMovement().y * -0.1,
                    this.getDeltaMovement().z * -0.1);
        }
    }

    /**
     * 生成自然粒子效果
     */
    private void spawnNatureParticles() {
        Vec3 center = this.position();

        // 生成生命之环粒子
        for (int i = 0; i < 30; i++) {
            double angle = Math.random() * Math.PI * 2;
            double radius = Math.random() * LIFE_RING_RADIUS;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + (Math.random() - 0.5) * 2.0;

            // 生成绿色治愈粒子
            this.level().addParticle(ParticleTypes.HAPPY_VILLAGER,
                    x, y, z,
                    (Math.random() - 0.5) * 0.1,
                    Math.random() * 0.05,
                    (Math.random() - 0.5) * 0.1);

            // 生成金色光芒粒子
            if (i % 3 == 0) {
                this.level().addParticle(ParticleTypes.GLOW,
                        x, y, z,
                        (Math.random() - 0.5) * 0.08,
                        Math.random() * 0.03,
                        (Math.random() - 0.5) * 0.08);
            }
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY; // 不允许拾取
    }
}