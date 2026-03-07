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

public class TornadoEffect extends Entity {
    private static final EntityDataAccessor<Integer> DATA_DURATION = SynchedEntityData.defineId(TornadoEffect.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(TornadoEffect.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_TARGET_ID = SynchedEntityData.defineId(TornadoEffect.class, EntityDataSerializers.INT);

    private int lifeTime = 0;
    private final int maxLifeTime = 100; // 5秒持续时间
    private LivingEntity target;
    private Player owner;

    public TornadoEffect(EntityType<? extends TornadoEffect> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.setInvulnerable(true);
        this.noPhysics = true;
    }

    public TornadoEffect(Level level, Player owner, LivingEntity target, float damage) {
        this(ModEntities.TORNADO_EFFECT.get(), level); // 使用注册的实体类型
        this.owner = owner;
        this.target = target;
        this.entityData.set(DATA_DAMAGE, damage);
        this.entityData.set(DATA_DURATION, maxLifeTime);
        this.entityData.set(DATA_TARGET_ID, target.getId());

        // 设置位置到目标位置
        this.setPos(target.getX(), target.getY() + 1.0, target.getZ()); // 提高位置，让龙卷风从目标上方开始
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_DURATION, maxLifeTime);
        this.entityData.define(DATA_DAMAGE, 5.0f);
        this.entityData.define(DATA_TARGET_ID, -1);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 在客户端，从实体数据中获取目标ID并查找目标实体
        if (this.level().isClientSide()) {
            int targetId = this.entityData.get(DATA_TARGET_ID);
            if (targetId != -1 && (target == null || target.isRemoved())) {
                Entity entity = this.level().getEntity(targetId);
                if (entity instanceof LivingEntity) {
                    target = (LivingEntity) entity;
                }
            }
        }

        // 如果目标不存在，尝试从实体数据中重新获取
        if (target == null) {
            int targetId = this.entityData.get(DATA_TARGET_ID);
            if (targetId != -1) {
                Entity entity = this.level().getEntity(targetId);
                if (entity instanceof LivingEntity) {
                    target = (LivingEntity) entity;
                }
            }
        }

        // 如果仍然没有目标，则移除龙卷风效果
        if (target == null || target.isRemoved()) {
            this.discard();
            return;
        }

        // 更新目标位置
        this.setPos(target.getX(), target.getY() + 1.5, target.getZ());

        // 生成龙卷风粒子效果（在客户端生成）
        if (this.level().isClientSide()) {
            generateTornadoParticles();
        }

        // 每20tick（1秒）造成一次伤害（在服务器端执行）
        if (!this.level().isClientSide() && lifeTime % 20 == 0) {
            applyDamage();
        }

        // 检查生命周期结束
        if (lifeTime >= maxLifeTime) {
            this.discard();
        }
    }

    private void generateTornadoParticles() {
        // 即使目标暂时为null，也尝试生成粒子
        Vec3 center;
        if (target != null && !target.isRemoved()) {
            center = target.position().add(0, 1, 0);
        } else {
            // 如果目标不存在，使用龙卷风自身位置
            center = this.position();
        }

        // 减小粒子范围，使其更集中于目标
        double radius = 1.2; // 从2.5减小到1.2
        int particleCount = 15; // 从20减少到15，更密集

        for (int i = 0; i < particleCount; i++) {
            double angle = (lifeTime * 0.4 + i * Math.PI * 2 / particleCount) % (Math.PI * 2);
            double height = (lifeTime * 0.15 + i * 0.1) % 2.5; // 减小高度范围

            double x = center.x + Math.cos(angle) * radius;
            double y = center.y + height;
            double z = center.z + Math.sin(angle) * radius;

            // 生成粒子效果 - 增加粒子速度，使其更活跃
            this.level().addParticle(ParticleTypes.FLAME,
                    x, y, z,
                    Math.cos(angle + Math.PI/2) * 0.5, 0.6, Math.sin(angle + Math.PI/2) * 0.5);

            this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                    x, y + 0.3, z,
                    Math.cos(angle) * 0.25, 0.3, Math.sin(angle) * 0.25);

            // 添加更多粒子类型增强效果，但减少频率
            if (i % 3 == 0) {
                this.level().addParticle(ParticleTypes.FLASH,
                        x, y + 0.5, z, 0, 0.2, 0);
            }

            if (i % 4 == 0) {
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        x, y + 0.4, z,
                        Math.cos(angle) * 0.15, 0.35, Math.sin(angle) * 0.15);
            }
        }
    }

    private void applyDamage() {
        if (target == null || target.isRemoved() || owner == null) return;

        float damage = this.entityData.get(DATA_DAMAGE);
        target.hurt(this.damageSources().indirectMagic(this, owner), damage);

        // 播放伤害音效
        this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.5f, 1.0f);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("LifeTime")) {
            this.lifeTime = compound.getInt("LifeTime");
        }
        if (compound.contains("TargetId")) {
            int targetId = compound.getInt("TargetId");
            Entity entity = this.level().getEntity(targetId);
            if (entity instanceof LivingEntity) {
                this.target = (LivingEntity) entity;
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("LifeTime", lifeTime);
        if (target != null) {
            compound.putInt("TargetId", target.getId());
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}