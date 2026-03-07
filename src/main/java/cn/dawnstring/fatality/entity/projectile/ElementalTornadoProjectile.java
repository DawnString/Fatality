package cn.dawnstring.fatality.entity.projectile;

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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class ElementalTornadoProjectile extends AbstractArrow {
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(ElementalTornadoProjectile.class, EntityDataSerializers.FLOAT);

    private int lifeTime = 0;
    private int maxLifeTime = 300; // 15秒
    private Vec3 movementDirection;
    private float damagePerTick = 6.0f; // 每秒120伤害（每tick 6伤害）
    private boolean colorfulParticles = true; // 彩色粒子效果
    private boolean randomMovement = true; // 随机方向行进

    public ElementalTornadoProjectile(EntityType<? extends ElementalTornadoProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.entityData.set(DATA_DAMAGE, 120.0f);

        // 修复：在默认构造函数中也初始化movementDirection
        this.movementDirection = new Vec3(
                this.random.nextDouble() - 0.5,
                0,
                this.random.nextDouble() - 0.5
        ).normalize();
    }

    public ElementalTornadoProjectile(Level level, LivingEntity owner, float damage) {
        super(ModEntities.ELEMENTAL_TORNADO_PROJECTILE.get(), owner, level);
        this.entityData.set(DATA_DAMAGE, damage);
        this.setNoGravity(true);

        // 随机初始方向
        this.movementDirection = new Vec3(
                this.random.nextDouble() - 0.5,
                0,
                this.random.nextDouble() - 0.5
        ).normalize();
    }

    // 设置每tick伤害（每秒120伤害）
    public void setDamagePerTick(float damagePerTick) {
        this.damagePerTick = damagePerTick;
    }

    // 设置持续时间（300 tick = 15秒）
    public void setDuration(int duration) {
        this.maxLifeTime = duration;
    }

    // 设置彩色粒子效果
    public void setColorfulParticles(boolean colorfulParticles) {
        this.colorfulParticles = colorfulParticles;
    }

    // 设置随机方向行进
    public void setRandomMovement(boolean randomMovement) {
        this.randomMovement = randomMovement;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 120.0f);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 性能优化：超过寿命的弹幕及时销毁
        if (lifeTime >= maxLifeTime) {
            this.discard();
            return;
        }

        // 随机改变方向
        if (this.tickCount % 40 == 0 && randomMovement) { // 每2秒可能改变方向
            if (this.random.nextDouble() < 0.3) {
                this.movementDirection = new Vec3(
                        this.random.nextDouble() - 0.5,
                        0,
                        this.random.nextDouble() - 0.5
                ).normalize();
            }
        }

        // 移动
        this.setDeltaMovement(this.movementDirection.scale(0.3));

        // 彩色粒子效果
        if (this.level().isClientSide && colorfulParticles) {
            spawnColorfulParticles();
        }

        // 持续伤害 - 每秒120伤害
        if (!this.level().isClientSide && this.tickCount % 20 == 0) { // 每秒伤害
            applyContinuousDamage();
        }
    }

    private void spawnColorfulParticles() {
        Vec3 center = this.position();
        double radius = 3.0; // 增大半径
        int particleCount = 40; // 增加粒子数量

        // 基础粒子效果 - 龙卷风主体
        for (int i = 0; i < particleCount; i++) {
            double angle = 2 * Math.PI * i / particleCount;
            double height = this.random.nextDouble() * 6; // 增加高度范围
            double currentRadius = radius * (0.8 + 0.4 * Math.sin(this.tickCount * 0.1 + angle)); // 动态半径

            double x = center.x + Math.cos(angle) * currentRadius;
            double y = center.y + height;
            double z = center.z + Math.sin(angle) * currentRadius;

            // 根据高度选择粒子类型
            if (height < 2) {
                // 底部 - 火焰和烟雾效果
                this.level().addParticle(ParticleTypes.FLAME, x, y, z,
                        (this.random.nextDouble() - 0.5) * 0.2, 0.2, (this.random.nextDouble() - 0.5) * 0.2);
                this.level().addParticle(ParticleTypes.SMOKE, x, y, z,
                        (this.random.nextDouble() - 0.5) * 0.1, 0.1, (this.random.nextDouble() - 0.5) * 0.1);
            } else if (height < 4) {
                // 中部 - 电光和灵魂火焰
                this.level().addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z,
                        (this.random.nextDouble() - 0.5) * 0.3, 0.3, (this.random.nextDouble() - 0.5) * 0.3);
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z,
                        (this.random.nextDouble() - 0.5) * 0.2, 0.2, (this.random.nextDouble() - 0.5) * 0.2);
            } else {
                // 顶部 - 发光粒子和魔法效果
                this.level().addParticle(ParticleTypes.GLOW, x, y, z,
                        (this.random.nextDouble() - 0.5) * 0.4, 0.4, (this.random.nextDouble() - 0.5) * 0.4);
                this.level().addParticle(ParticleTypes.WITCH, x, y, z,
                        (this.random.nextDouble() - 0.5) * 0.2, 0.2, (this.random.nextDouble() - 0.5) * 0.2);
            }
        }

        // 额外效果 - 随机爆发粒子
        if (this.tickCount % 10 == 0) {
            for (int i = 0; i < 10; i++) {
                double burstAngle = this.random.nextDouble() * 2 * Math.PI;
                double burstRadius = this.random.nextDouble() * radius;
                double burstHeight = this.random.nextDouble() * 4;

                double burstX = center.x + Math.cos(burstAngle) * burstRadius;
                double burstY = center.y + burstHeight;
                double burstZ = center.z + Math.sin(burstAngle) * burstRadius;

                // 爆发粒子效果
                this.level().addParticle(ParticleTypes.CRIT, burstX, burstY, burstZ,
                        (this.random.nextDouble() - 0.5) * 0.5, 0.5, (this.random.nextDouble() - 0.5) * 0.5);
                this.level().addParticle(ParticleTypes.ENCHANT, burstX, burstY, burstZ,
                        (this.random.nextDouble() - 0.5) * 0.3, 0.3, (this.random.nextDouble() - 0.5) * 0.3);
            }
        }

        // 地面效果 - 烟雾和灰尘
        if (this.tickCount % 5 == 0) {
            for (int i = 0; i < 8; i++) {
                double groundAngle = this.random.nextDouble() * 2 * Math.PI;
                double groundRadius = this.random.nextDouble() * radius * 0.8;

                double groundX = center.x + Math.cos(groundAngle) * groundRadius;
                double groundY = center.y;
                double groundZ = center.z + Math.sin(groundAngle) * groundRadius;

                this.level().addParticle(ParticleTypes.CLOUD, groundX, groundY, groundZ,
                        (this.random.nextDouble() - 0.5) * 0.1, 0.05, (this.random.nextDouble() - 0.5) * 0.1);
                this.level().addParticle(ParticleTypes.ASH, groundX, groundY, groundZ,
                        (this.random.nextDouble() - 0.5) * 0.08, 0.03, (this.random.nextDouble() - 0.5) * 0.08);
            }
        }

        // 旋转效果 - 螺旋粒子流
        if (this.tickCount % 3 == 0) {
            for (int i = 0; i < 6; i++) {
                double spiralAngle = this.tickCount * 0.2 + i * Math.PI / 3;
                double spiralRadius = radius * 0.6;
                double spiralHeight = (this.tickCount % 20) * 0.3;

                double spiralX = center.x + Math.cos(spiralAngle) * spiralRadius;
                double spiralY = center.y + spiralHeight;
                double spiralZ = center.z + Math.sin(spiralAngle) * spiralRadius;

                this.level().addParticle(ParticleTypes.END_ROD, spiralX, spiralY, spiralZ,
                        (this.random.nextDouble() - 0.5) * 0.1, 0.1, (this.random.nextDouble() - 0.5) * 0.1);
            }
        }
    }

    private void applyContinuousDamage() {
        Vec3 center = this.position();
        double radius = 3.0; // 伤害半径

        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(radius))) {
            if (entity != this.getOwner() && entity.distanceToSqr(center) <= radius * radius) {
                // 每秒120伤害
                entity.hurt(this.damageSources().indirectMagic(this, this.getOwner()), 120.0f);
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        // 龙卷风不因碰撞而消失，继续存在
        if (result instanceof EntityHitResult entityResult) {
            Entity entity = entityResult.getEntity();
            if (entity instanceof LivingEntity livingEntity && livingEntity != this.getOwner()) {
                // 碰到实体造成伤害
                livingEntity.hurt(this.damageSources().indirectMagic(this, this.getOwner()), 120.0f);
            }
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
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", lifeTime);
        compound.putInt("MaxLifeTime", maxLifeTime);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        lifeTime = compound.getInt("LifeTime");
        maxLifeTime = compound.getInt("MaxLifeTime");
    }
}