package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class ElementalSpearProjectile extends AbstractArrow {

    private float damage;
    private boolean hasLanded = false;
    private int pillarDuration = 400; // 20秒 * 20tick/秒
    private int pillarTick = 0;
    private boolean createLightPillar = true; // 生成光柱
    private float lightPillarDamage = 200.0f; // 光柱伤害每秒200
    private double lightPillarRadius = 2.0; // 光柱半径
    private int lightPillarDuration = 400; // 光柱持续时间20秒
    private int maxLifeTime = 400;

    public ElementalSpearProjectile(Level level, LivingEntity owner, float damage) {
        super(ModEntities.ELEMENTAL_SPEAR_PROJECTILE.get(), owner, level);
        this.damage = damage;
        this.setNoGravity(true);
        this.setBaseDamage(damage);
    }

    public ElementalSpearProjectile(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    // 设置是否生成光柱
    public void setCreateLightPillar(boolean createLightPillar) {
        this.createLightPillar = createLightPillar;
    }

    // 设置光柱伤害
    public void setLightPillarDamage(float lightPillarDamage) {
        this.lightPillarDamage = lightPillarDamage;
    }

    // 设置光柱半径
    public void setLightPillarRadius(double lightPillarRadius) {
        this.lightPillarRadius = lightPillarRadius;
    }

    // 设置光柱持续时间
    public void setLightPillarDuration(int lightPillarDuration) {
        this.lightPillarDuration = lightPillarDuration;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!this.level().isClientSide) {
            // 碰到玩家造成320伤害
            if (result.getEntity() instanceof LivingEntity living && living != this.getOwner()) {
                living.hurt(this.damageSources().indirectMagic(this, this.getOwner()), 320.0f);
            }
            // 长枪不消失，继续存在
            this.hasLanded = true;
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            // 插入地面后不消失，生成光柱
            this.hasLanded = true;
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public void tick() {
        super.tick();

        // 性能优化：超过寿命的弹幕及时销毁
        if (this.tickCount >= maxLifeTime) {
            this.discard();
            return;
        }

        if (this.hasLanded && createLightPillar) {
            // 光柱阶段
            pillarTick++;

            // 生成光柱粒子效果
            if (this.level().isClientSide) {
                spawnPillarParticles();
            }

            // 光柱伤害逻辑 - 每秒200伤害
            if (!this.level().isClientSide && pillarTick % 20 == 0) { // 每秒一次伤害
                applyPillarDamage();
            }

            // 持续时间结束
            if (pillarTick >= lightPillarDuration) {
                this.discard();
            }
        } else if (this.hasLanded && !createLightPillar) {
            // 如果不生成光柱，则直接消失
            this.discard();
        } else {
            // 飞行阶段
            if (this.level().isClientSide) {
                // 生成飞行粒子效果
                Vec3 pos = this.position();
                for (int i = 0; i < 5; i++) {
                    this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                            pos.x + (this.random.nextDouble() - 0.5) * 0.5,
                            pos.y + (this.random.nextDouble() - 0.5) * 0.5,
                            pos.z + (this.random.nextDouble() - 0.5) * 0.5,
                            0, 0, 0);
                }
            }
        }
    }

    private void spawnPillarParticles() {
        Vec3 center = this.position();
        double radius = lightPillarRadius;
        int height = 10;

        // 光柱主体粒子
        for (int y = 0; y < height; y++) {
            for (int i = 0; i < 8; i++) {
                double angle = 2 * Math.PI * i / 8;
                double x = center.x + Math.cos(angle) * radius;
                double z = center.z + Math.sin(angle) * radius;
                double particleY = center.y + y;

                this.level().addParticle(ParticleTypes.GLOW,
                        x, particleY, z, 0, 0.1, 0);
            }
        }
    }

    private void applyPillarDamage() {
        Vec3 center = this.position();
        double radius = lightPillarRadius;

        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(radius))) {
            if (entity != this.getOwner() && entity.distanceToSqr(center) <= radius * radius) {
                entity.hurt(this.damageSources().indirectMagic(this, this.getOwner()), lightPillarDamage);
            }
        }
    }
}