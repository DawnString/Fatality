package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.registry.ModEntities;
import cn.dawnstring.fatality.registry.ModItems;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;

public class WaveBreakerProjectile extends AbstractArrow {
    private int lifeTicks = 0;
    private final int maxLifeTicks = 100;
    private boolean hasExploded = false;
    private final ItemStack weaponStack;
    private static final EntityDataAccessor<Boolean> DATA_EXPLODED = SynchedEntityData.defineId(WaveBreakerProjectile.class, EntityDataSerializers.BOOLEAN);
    private int explosionStartTick = 0;

    public WaveBreakerProjectile(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
        this.pickup = Pickup.DISALLOWED;
        this.weaponStack = new ItemStack(ModItems.YOUS_WAVE_BREAKER.get());
    }

    public WaveBreakerProjectile(Level level, LivingEntity shooter, ItemStack weaponStack) {
        super(ModEntities.WAVE_BREAKER_PROJECTILE.get(), shooter, level);
        this.pickup = Pickup.DISALLOWED;
        this.weaponStack = weaponStack.copy();
        this.setBaseDamage(15.0);

        // 设置直线飞行
        this.setNoGravity(true);
        this.setPierceLevel((byte) 0);

        // 设置投掷物速度
        Vec3 lookVec = shooter.getLookAngle();
        this.shoot(lookVec.x, lookVec.y, lookVec.z, 1.5F, 1.0F);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_EXPLODED, false);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public boolean hasExploded() {
        return this.entityData.get(DATA_EXPLODED);
    }

    private void setExploded(boolean exploded) {
        this.entityData.set(DATA_EXPLODED, exploded);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTicks++;

        // 确保直线飞行
        if (!this.isNoGravity()) {
            this.setNoGravity(true);
        }

        // 检查是否超时
        if (lifeTicks >= maxLifeTicks && !hasExploded()) {
            triggerExplosion();
            return;
        }

        // 处理爆炸后的逻辑
        if (hasExploded()) {
            // 客户端处理粒子效果
            if (this.level().isClientSide()) {
                handleExplosionEffects();
            }

            // 服务器端处理伤害和移除逻辑
            if (!this.level().isClientSide()) {
                int phaseTicks = lifeTicks - explosionStartTick;
                // 10tick后移除实体
                if (phaseTicks >= 10) {
                    performExplosionDamage();
                    this.discard();
                }
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (!hasExploded() && !this.level().isClientSide()) {
            triggerExplosion();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!hasExploded() && !this.level().isClientSide()) {
            triggerExplosion();
        }
    }

    @Override
    protected void onHitBlock(net.minecraft.world.phys.BlockHitResult result) {
        if (!hasExploded() && !this.level().isClientSide()) {
            triggerExplosion();
        }
    }

    private void triggerExplosion() {
        if (hasExploded()) return;

        setExploded(true);
        explosionStartTick = lifeTicks;

        // 立即停止移动
        this.setDeltaMovement(0, 0, 0);

        // 服务器端播放水花音效
        if (!this.level().isClientSide()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.5f, 0.8f + this.random.nextFloat() * 0.4f);
        }
    }

    private void handleExplosionEffects() {
        // 只在客户端生成粒子效果
        if (this.level().isClientSide()) {
            Vec3 center = this.position();
            int phaseTicks = lifeTicks - explosionStartTick;

            if (phaseTicks < 10) {
                // 简单直接的粒子生成逻辑
                generateExplosionParticles(center, phaseTicks);
            }
        }
    }

    private void generateExplosionParticles(Vec3 center, int phaseTicks) {
        // 修改为纯水花粒子效果，去除TNT爆炸效果
        int particleCount;

        // 第一阶段：气泡向上扩散 (0-3tick)
        if (phaseTicks < 3) {
            particleCount = 30; // 增加气泡粒子数量
            for (int i = 0; i < particleCount; i++) {
                // 球形分布的气泡
                double theta = Math.PI * this.random.nextDouble(); // 垂直角度
                double phi = Math.PI * 2 * this.random.nextDouble(); // 水平角度
                double radius = phaseTicks * 1.5; // 随时间扩散

                double x = center.x + Math.sin(theta) * Math.cos(phi) * radius;
                double y = center.y + Math.cos(theta) * radius;
                double z = center.z + Math.sin(theta) * Math.sin(phi) * radius;

                this.level().addParticle(ParticleTypes.BUBBLE,
                        x, y, z,
                        (this.random.nextDouble() - 0.5) * 0.2,
                        0.1 + this.random.nextDouble() * 0.2, // 向上浮动
                        (this.random.nextDouble() - 0.5) * 0.2);
            }
        }

        // 第二阶段：水花球形扩散 (3-6tick)
        else if (phaseTicks < 6) {
            particleCount = 40; // 大幅增加水花粒子数量
            double progress = (phaseTicks - 3) / 3.0;
            double radius = 4.0 + progress * 2.0; // 从4到6的球形半径

            for (int i = 0; i < particleCount; i++) {
                // 球形分布的水花
                double theta = Math.PI * this.random.nextDouble();
                double phi = Math.PI * 2 * this.random.nextDouble();

                double x = center.x + Math.sin(theta) * Math.cos(phi) * radius;
                double y = center.y + Math.cos(theta) * radius;
                double z = center.z + Math.sin(theta) * Math.sin(phi) * radius;

                // 水花向外扩散
                double dx = (x - center.x) * 0.1;
                double dy = (y - center.y) * 0.1;
                double dz = (z - center.z) * 0.1;

                this.level().addParticle(ParticleTypes.SPLASH,
                        x, y, z,
                        dx, dy, dz);
            }
        }

        // 第三阶段：水花下落效果 (6-10tick)
        else {
            particleCount = 35; // 水花下落粒子
            double progress = (phaseTicks - 6) / 4.0;
            double radius = 6.0 * (1 - progress * 0.5); // 半径逐渐收缩

            for (int i = 0; i < particleCount; i++) {
                // 球形分布的下落水花
                double theta = Math.PI * this.random.nextDouble();
                double phi = Math.PI * 2 * this.random.nextDouble();

                double x = center.x + Math.sin(theta) * Math.cos(phi) * radius;
                double y = center.y + Math.cos(theta) * radius;
                double z = center.z + Math.sin(theta) * Math.sin(phi) * radius;

                // 水花向下落
                this.level().addParticle(ParticleTypes.SPLASH,
                        x, y, z,
                        0, -0.1 - this.random.nextDouble() * 0.2, 0);
            }

            // 添加水花飞溅效果
            for (int i = 0; i < 15; i++) {
                double x = center.x + (this.random.nextDouble() - 0.5) * 3.0;
                double y = center.y + this.random.nextDouble() * 2.0;
                double z = center.z + (this.random.nextDouble() - 0.5) * 3.0;

                this.level().addParticle(ParticleTypes.SPLASH,
                        x, y, z,
                        (this.random.nextDouble() - 0.5) * 0.3,
                        0.2,
                        (this.random.nextDouble() - 0.5) * 0.3);
            }
        }

        // 中心发光水珠效果
        for (int i = 0; i < 10; i++) {
            double x = center.x + (this.random.nextDouble() - 0.5) * 1.0;
            double y = center.y + (this.random.nextDouble() - 0.5) * 1.0;
            double z = center.z + (this.random.nextDouble() - 0.5) * 1.0;

            this.level().addParticle(ParticleTypes.GLOW,
                    x, y, z,
                    0, 0.05, 0);
        }

        // 添加水滴下落效果
        for (int i = 0; i < 8; i++) {
            double x = center.x + (this.random.nextDouble() - 0.5) * 4.0;
            double y = center.y + this.random.nextDouble() * 3.0;
            double z = center.z + (this.random.nextDouble() - 0.5) * 4.0;

            this.level().addParticle(ParticleTypes.FALLING_WATER,
                    x, y, z,
                    0, -0.3, 0);
        }
    }

    private void performExplosionDamage() {
        if (this.level().isClientSide()) return;

        Vec3 center = this.position();

        // 造成球体范围伤害
        List<LivingEntity> entities = this.level().getEntitiesOfClass(
                LivingEntity.class, this.getBoundingBox().inflate(5.0));

        for (LivingEntity entity : entities) {
            if (entity != this.getOwner() && entity.isAlive()) {
                double distance = entity.distanceToSqr(center);
                if (distance <= 25.0) {
                    float damage = calculateWeaponDamage(entity);
                    entity.hurt(this.damageSources().arrow(this, (LivingEntity) this.getOwner()), damage);

                    // 添加击退效果
                    Vec3 knockback = entity.position().subtract(center).normalize().scale(1.0);
                    entity.setDeltaMovement(entity.getDeltaMovement().add(knockback));
                }
            }
        }

        // 水花爆炸音效
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 2.0f, 0.6f + this.random.nextFloat() * 0.4f);
    }

    private float calculateWeaponDamage(LivingEntity target) {
        // 简化伤害计算
        float baseDamage = 15.0f;

        // 考虑玩家攻击力
        if (this.getOwner() instanceof net.minecraft.world.entity.player.Player player) {
            var attackDamageAttr = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
            if (attackDamageAttr != null) {
                baseDamage += (float) attackDamageAttr.getValue();
            }
        }

        // 伤害浮动
        float fluctuation = 0.8f + this.random.nextFloat() * 0.4f;
        baseDamage *= fluctuation;

        // 暴击判断
        if (this.random.nextFloat() < 0.25f) {
            baseDamage *= 1.5f;
        }

        return Math.max(1.0f, baseDamage);
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }
}