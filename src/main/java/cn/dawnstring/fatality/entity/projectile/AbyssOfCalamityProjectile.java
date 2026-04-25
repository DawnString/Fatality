package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.registry.ModEntities;
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
 * 深渊之灾投射物类
 * 实现深渊长矛投掷功能，命中目标附加祸渊效果和苦难印记
 */
public class AbyssOfCalamityProjectile extends AbstractArrow {
    private static final int MAX_LIFETIME = 200; // 最大生命周期10秒
    private static final int CALAMITY_DURATION = 120; // 祸渊效果持续时间（6秒）
    private static final int CALAMITY_DAMAGE_INTERVAL = 20; // 祸渊伤害间隔（1秒）
    private static final float CALAMITY_DAMAGE = 50.0f; // 祸渊每秒伤害
    private static final int SUFFERING_MARK_MAX_STACKS = 5; // 苦难印记最大层数
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(AbyssOfCalamityProjectile.class, EntityDataSerializers.FLOAT);
    
    private Player shooter;
    private ItemStack weaponStack;
    private float baseDamage;

    public AbyssOfCalamityProjectile(EntityType<? extends AbyssOfCalamityProjectile> type, Level level) {
        super(type, level);
        this.shooter = null;
        this.weaponStack = ItemStack.EMPTY;
        this.baseDamage = 0.0f;
        this.setNoGravity(false); // 受重力影响
        this.pickup = AbstractArrow.Pickup.DISALLOWED; // 不允许拾取
        this.entityData.set(DATA_DAMAGE, 0.0f); // 初始化同步数据
    }

    public AbyssOfCalamityProjectile(Level level, Player shooter, ItemStack weaponStack, float baseDamage) {
        this(ModEntities.ABYSS_OF_CALAMITY_PROJECTILE.get(), level);
        this.shooter = shooter;
        this.weaponStack = weaponStack;
        this.baseDamage = baseDamage;

        // 设置投射物属性
        this.setOwner(shooter);
        this.setBaseDamage(this.baseDamage);
        this.entityData.set(DATA_DAMAGE, this.baseDamage); // 同步伤害数据

        // 设置位置和方向
        Vec3 shooterPos = shooter.getEyePosition();
        Vec3 lookVec = shooter.getLookAngle();

        this.setPos(shooterPos.x, shooterPos.y, shooterPos.z);
        this.shoot(lookVec.x, lookVec.y, lookVec.z, 2.5f, 1.0f); // 2.5速度，标准散布
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 0.0f);
    }

    /**
     * 获取深渊之灾伤害（从同步数据中读取）
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
                    // 造成伤害并附加祸渊效果
                    createCalamityEffect((LivingEntity) target);
                }
            }

            // 播放击中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, 1.0F, 0.8F);

            // 生成深渊粒子效果
            spawnAbyssParticles();

            // 销毁投射物
            this.discard();
        }
    }

    /**
     * 创建祸渊效果，对目标造成伤害并附加持续伤害效果
     */
    private void createCalamityEffect(LivingEntity target) {
        // 计算最终伤害
        double distance = target.distanceTo(this.shooter);
        float distanceMultiplier = Math.max(0.5f, 1.0f - (float)(distance / 20.0f)); // 20格内伤害不衰减
        float finalDamage = this.baseDamage * distanceMultiplier;

        // 对Boss造成额外伤害
        if (isBoss(target)) {
            finalDamage *= 1.2f; // 对Boss造成20%额外伤害
        }

        // 造成伤害
        if (finalDamage > 0) {
            target.hurt(this.damageSources().playerAttack(this.shooter), finalDamage);

            // 附加祸渊效果（凋零和减速）
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, CALAMITY_DURATION, 1));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, CALAMITY_DURATION, 1));

            // 启动祸渊持续伤害计时器
            startCalamityDamageTimer(target);

            // 累积苦难印记
            incrementSufferingMark(target);

            // 播放深渊打击音效
            this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 0.5F, 0.8F);
        }
    }

    /**
     * 判断是否为Boss
     */
    //TODO
    private boolean isBoss(LivingEntity entity) {
        return false;
    }

    /**
     * 启动祸渊持续伤害计时器
     */
    private void startCalamityDamageTimer(LivingEntity target) {
        // 使用持久化数据存储伤害次数
        target.getPersistentData().putInt("CalamityDamageTicks", 0);
        target.getPersistentData().putInt("CalamityMaxTicks", CALAMITY_DURATION);
    }

    /**
     * 累积苦难印记
     */
    private void incrementSufferingMark(LivingEntity target) {
        // 获取当前印记层数
        int currentStacks = target.getPersistentData().getInt("SufferingMarkStacks");
        
        // 不超过最大层数
        if (currentStacks < SUFFERING_MARK_MAX_STACKS) {
            target.getPersistentData().putInt("SufferingMarkStacks", currentStacks + 1);
            
            // 播放印记累积音效
            this.level().playSound(null, target.getX(), target.getY(), target.getZ(), 
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.5F, 1.2F);
        }
    }

    /**
     * 生成飞行粒子效果
     */
    private void spawnFlightParticles() {
        Vec3 pos = this.position();

        // 生成深渊烟雾粒子
        for (int i = 0; i < 3; i++) {
            this.level().addParticle(ParticleTypes.SMOKE,
                    pos.x + (Math.random() - 0.5) * 0.3,
                    pos.y + (Math.random() - 0.5) * 0.3,
                    pos.z + (Math.random() - 0.5) * 0.3,
                    (Math.random() - 0.5) * 0.02,
                    (Math.random() - 0.5) * 0.02,
                    (Math.random() - 0.5) * 0.02);
        }

        // 生成灵魂火焰粒子
        if (this.tickCount % 2 == 0) {
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    pos.x, pos.y, pos.z,
                    this.getDeltaMovement().x * -0.1,
                    this.getDeltaMovement().y * -0.1,
                    this.getDeltaMovement().z * -0.1);
        }
    }

    /**
     * 生成深渊粒子效果
     */
    private void spawnAbyssParticles() {
        Vec3 center = this.position();

        // 生成深渊爆炸粒子
        for (int i = 0; i < 25; i++) {
            double angle = Math.random() * Math.PI * 2;
            double radius = Math.random() * 2.5;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + (Math.random() - 0.5) * 2.5;

            // 生成灵魂火焰粒子
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    x, y, z,
                    (Math.random() - 0.5) * 0.15,
                    Math.random() * 0.08,
                    (Math.random() - 0.5) * 0.15);

            // 生成烟雾粒子
            this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                    x, y, z,
                    (Math.random() - 0.5) * 0.12,
                    Math.random() * 0.05,
                    (Math.random() - 0.5) * 0.12);
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY; // 不允许拾取
    }
}