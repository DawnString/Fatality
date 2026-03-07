package cn.dawnstring.fatality.entity;

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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.ArrayList;
import java.util.List;

/**
 * 邪恶黑暗效果 - 对范围内的生物造成持续伤害
 */
public class VileDarknessEffect extends Entity {
    private static final EntityDataAccessor<Integer> DATA_DURATION = SynchedEntityData.defineId(VileDarknessEffect.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(VileDarknessEffect.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_OWNER_ID = SynchedEntityData.defineId(VileDarknessEffect.class, EntityDataSerializers.INT);

    private int lifeTime = 0;
    private final int maxLifeTime = 100; // 5秒持续时间
    private Player owner;
    private List<LivingEntity> affectedEntities = new ArrayList<>();
    private final float radius = 8.0f; // 8格半径

    public VileDarknessEffect(EntityType<? extends VileDarknessEffect> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.setInvulnerable(true);
        this.noPhysics = true;
    }

    public VileDarknessEffect(Level level, Player owner, float damage) {
        this(ModEntities.VILE_DARKNESS_EFFECT.get(), level);
        this.owner = owner;
        this.entityData.set(DATA_DAMAGE, damage);
        this.entityData.set(DATA_DURATION, maxLifeTime);
        this.entityData.set(DATA_OWNER_ID, owner.getId());

        // 设置位置到玩家位置
        this.setPos(owner.getX(), owner.getY() + 1.0, owner.getZ());
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_DURATION, maxLifeTime);
        this.entityData.define(DATA_DAMAGE, 57.0f);
        this.entityData.define(DATA_OWNER_ID, -1);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 在客户端，从实体数据中获取所有者ID并查找所有者
        if (this.level().isClientSide()) {
            int ownerId = this.entityData.get(DATA_OWNER_ID);
            if (ownerId != -1 && (owner == null || owner.isRemoved())) {
                Entity entity = this.level().getEntity(ownerId);
                if (entity instanceof Player) {
                    owner = (Player) entity;
                }
            }
        }

        // 如果所有者不存在，尝试从实体数据中重新获取
        if (owner == null) {
            int ownerId = this.entityData.get(DATA_OWNER_ID);
            if (ownerId != -1) {
                Entity entity = this.level().getEntity(ownerId);
                if (entity instanceof Player) {
                    owner = (Player) entity;
                }
            }
        }

        // 如果所有者不存在，则移除效果
        if (owner == null || owner.isRemoved()) {
            this.discard();
            return;
        }

        // 更新位置到所有者位置
        this.setPos(owner.getX(), owner.getY() + 1.0, owner.getZ());

        // 生成邪恶黑暗粒子效果（在客户端生成）
        if (this.level().isClientSide()) {
            generateVileDarknessParticles();
        }

        // 每20tick（1秒）造成一次伤害（在服务器端执行）
        if (!this.level().isClientSide() && lifeTime % 20 == 0) {
            applyDamageToTargets();
        }

        // 检查魔法值消耗（每秒1点魔法值）
        if (!this.level().isClientSide() && lifeTime % 20 == 0) {
            if (!cn.dawnstring.fatality.system.ManaSystem.safeConsumeMana(owner, 1.0f)) {
                // 魔法值不足，移除效果
                this.discard();
                return;
            }
        }
    }

    /**
     * 更新伤害值
     */
    public void updateDamage(float newDamage) {
        this.entityData.set(DATA_DAMAGE, newDamage);
    }

    /**
     * 生成邪恶黑暗粒子效果
     */
    private void generateVileDarknessParticles() {
        Vec3 center = this.position();
        double radius = 8.0; // 8格半径
        double height = 4.0; // 圆柱高度

        // 1. 在玩家脚底下生成法阵效果
        if (owner != null) {
            Vec3 ownerPos = owner.position();

            // 生成圆形法阵粒子（在玩家脚下）
            for (int i = 0; i < 24; i++) {
                double angle = (lifeTime * 0.1 + i * Math.PI * 2 / 24) % (Math.PI * 2);
                double circleRadius = 1.5 + Math.sin(lifeTime * 0.05 + i * 0.2) * 0.3; // 动态变化的半径

                double x = ownerPos.x + Math.cos(angle) * circleRadius;
                double y = ownerPos.y + 0.1; // 紧贴地面
                double z = ownerPos.z + Math.sin(angle) * circleRadius;

                // 法阵粒子（使用灵魂火焰和末影粒子）
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        x, y, z,
                        0, 0.02, 0);

                // 添加旋转的法阵粒子
                if (i % 4 == 0) {
                    this.level().addParticle(ParticleTypes.END_ROD,
                            x, y + 0.1, z,
                            Math.cos(angle) * 0.05, 0.03, Math.sin(angle) * 0.05);
                }
            }
        }

        // 获取范围内的所有生物
        List<LivingEntity> entitiesInRange = this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(radius, height, radius),
                entity -> entity != owner && !(entity instanceof Player) && entity.isAlive()
        );

        // 2. 为每个目标生成围绕目标的黑色圆柱效果
        for (LivingEntity target : entitiesInRange) {
            Vec3 targetPos = target.position().add(0, 1, 0);

            // 生成围绕目标的圆柱形粒子效果
            for (int i = 0; i < 16; i++) {
                double angle = (lifeTime * 0.3 + i * Math.PI * 2 / 16) % (Math.PI * 2);
                double verticalPos = (lifeTime * 0.1 + i * 0.25) % height; // 垂直位置在圆柱高度内循环

                // 圆柱形粒子分布（围绕目标）
                double x = targetPos.x + Math.cos(angle) * 1.2;
                double y = targetPos.y + verticalPos;
                double z = targetPos.z + Math.sin(angle) * 1.2;

                // 使用黑色粒子（灵魂火焰、烟雾、末影等）
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        x, y, z,
                        Math.cos(angle + Math.PI/2) * 0.25, 0.12, Math.sin(angle + Math.PI/2) * 0.25);

                this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                        x, y + 0.1, z,
                        Math.cos(angle) * 0.18, 0.06, Math.sin(angle) * 0.18);

                // 添加末影粒子增强黑暗效果
                if (i % 2 == 0) {
                    this.level().addParticle(ParticleTypes.END_ROD,
                            x, y + 0.2, z,
                            Math.cos(angle) * 0.18, 0.1, Math.sin(angle) * 0.18);
                }
            }
        }
    }

    /**
     * 对范围内的所有目标造成伤害
     */
    private void applyDamageToTargets() {
        if (owner == null || owner.isRemoved()) return;

        float damage = this.entityData.get(DATA_DAMAGE);

        // 获取8格半径内的所有生物（排除玩家）
        List<LivingEntity> targets = this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(radius),
                entity -> entity != owner && !(entity instanceof Player) && entity.isAlive()
        );

        // 对每个目标造成伤害
        for (LivingEntity target : targets) {
            target.hurt(this.damageSources().indirectMagic(this, owner), damage);

            // 播放黑暗伤害音效
            this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.SOUL_ESCAPE, SoundSource.HOSTILE, 0.6f, 0.8f);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("LifeTime")) {
            this.lifeTime = compound.getInt("LifeTime");
        }
        if (compound.contains("OwnerId")) {
            int ownerId = compound.getInt("OwnerId");
            Entity entity = this.level().getEntity(ownerId);
            if (entity instanceof Player) {
                this.owner = (Player) entity;
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("LifeTime", lifeTime);
        if (owner != null) {
            compound.putInt("OwnerId", owner.getId());
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}