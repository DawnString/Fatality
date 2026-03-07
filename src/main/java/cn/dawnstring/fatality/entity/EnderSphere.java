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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;

/**
 * 末影球体 - 生成黑色球体并发射闪电攻击
 */
public class EnderSphere extends Entity {
    private static final EntityDataAccessor<Integer> DATA_LIFETIME = SynchedEntityData.defineId(EnderSphere.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(EnderSphere.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_OWNER_ID = SynchedEntityData.defineId(EnderSphere.class, EntityDataSerializers.INT);

    private int lifeTime = 0;
    private Player owner;
    private final float radius = 0.5f; // 球体半径0.5格
    private final int attackInterval = 20; // 每20tick（1秒）攻击一次
    private final float attackRange = 8.0f; // 攻击范围8格
    private int lastAttackTick = 0;

    public EnderSphere(EntityType<? extends EnderSphere> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.setInvulnerable(true);
        this.noPhysics = true;
    }

    public EnderSphere(Level level, Player owner, float damage, int maxLifeTime) {
        this(ModEntities.ENDER_SPHERE.get(), level);
        this.owner = owner;
        this.entityData.set(DATA_DAMAGE, damage);
        this.entityData.set(DATA_LIFETIME, maxLifeTime);
        this.entityData.set(DATA_OWNER_ID, owner.getId());
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_LIFETIME, 200);
        this.entityData.define(DATA_DAMAGE, 8.0f);
        this.entityData.define(DATA_OWNER_ID, -1);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 检查生命周期
        int maxLifeTime = this.entityData.get(DATA_LIFETIME);
        if (lifeTime >= maxLifeTime) {
            this.discard();
            return;
        }

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

        // 如果所有者不存在，则移除球体
        if (owner == null || owner.isRemoved()) {
            this.discard();
            return;
        }

        // 生成黑色球体粒子效果（在客户端生成）
        if (this.level().isClientSide()) {
            generateSphereParticles();
        }

        // 每1秒攻击一次（在服务器端执行）
        if (!this.level().isClientSide() && lifeTime - lastAttackTick >= attackInterval) {
            attackNearbyEntities();
            lastAttackTick = lifeTime;
        }
    }

    /**
     * 生成黑色球体粒子效果
     */
    private void generateSphereParticles() {
        Vec3 center = this.position();
        
        // 球体粒子效果
        for (int i = 0; i < 12; i++) {
            double angle1 = (lifeTime * 0.1 + i * Math.PI * 2 / 12) % (Math.PI * 2);
            double angle2 = (lifeTime * 0.15 + i * Math.PI / 6) % (Math.PI * 2);
            
            // 球体表面粒子分布
            double x = center.x + Math.sin(angle1) * Math.cos(angle2) * radius;
            double y = center.y + Math.sin(angle1) * Math.sin(angle2) * radius;
            double z = center.z + Math.cos(angle1) * radius;

            // 使用黑色粒子（灵魂火焰、烟雾、末影粒子）
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    x, y, z,
                    (Math.random() - 0.5) * 0.05, (Math.random() - 0.5) * 0.05, (Math.random() - 0.5) * 0.05);

            // 添加末影粒子增强黑暗效果
            if (i % 3 == 0) {
                this.level().addParticle(ParticleTypes.END_ROD,
                        x, y, z,
                        (Math.random() - 0.5) * 0.08, (Math.random() - 0.5) * 0.08, (Math.random() - 0.5) * 0.08);
            }

            // 添加烟雾粒子
            if (i % 4 == 0) {
                this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                        x, y, z,
                        (Math.random() - 0.5) * 0.06, (Math.random() - 0.5) * 0.06, (Math.random() - 0.5) * 0.06);
            }
        }

        // 球体内部的旋转粒子效果
        for (int i = 0; i < 6; i++) {
            double innerRadius = radius * 0.3;
            double innerAngle = (lifeTime * 0.2 + i * Math.PI * 2 / 6) % (Math.PI * 2);
            
            double innerX = center.x + Math.cos(innerAngle) * innerRadius;
            double innerY = center.y + Math.sin(innerAngle) * innerRadius;
            double innerZ = center.z + Math.sin(innerAngle * 2) * innerRadius;

            // 内部旋转粒子（使用紫色粒子）
            this.level().addParticle(ParticleTypes.REVERSE_PORTAL,
                    innerX, innerY, innerZ,
                    Math.cos(innerAngle) * 0.1, Math.sin(innerAngle) * 0.1, Math.sin(innerAngle * 2) * 0.1);
        }
    }

    /**
     * 攻击附近的敌对实体
     */
    private void attackNearbyEntities() {
        Vec3 center = this.position();
        float damage = this.entityData.get(DATA_DAMAGE);

        // 查找攻击范围内的所有生物
        AABB attackBox = new AABB(
                center.x - attackRange, center.y - attackRange, center.z - attackRange,
                center.x + attackRange, center.y + attackRange, center.z + attackRange
        );

        List<LivingEntity> entitiesInRange = this.level().getEntitiesOfClass(
                LivingEntity.class, attackBox,
                entity -> entity != owner && entity.isAlive() && !(entity instanceof Player)
        );

        // 对每个实体发射闪电攻击
        for (LivingEntity target : entitiesInRange) {
            if (canSeeTarget(target)) {
                // 发射闪电
                shootLightningAtTarget(target, damage);
                
                // 每个球体每次只攻击一个目标，避免过于强大
                break;
            }
        }
    }

    /**
     * 检查是否可以看见目标
     */
    private boolean canSeeTarget(LivingEntity target) {
        Vec3 start = this.position();
        Vec3 end = target.getEyePosition();
        
        // 简单的视线检查（不考虑方块遮挡）
        double distance = start.distanceTo(end);
        return distance <= attackRange;
    }

    /**
     * 向目标发射闪电
     */
    private void shootLightningAtTarget(LivingEntity target, float damage) {
        // 造成伤害
        target.hurt(target.damageSources().indirectMagic(this, owner), damage);

        // 播放闪电音效
        this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 0.5F, 1.0F);

        // 生成闪电粒子效果（在客户端生成）
        if (this.level().isClientSide()) {
            generateLightningParticles(target);
        }
    }

    /**
     * 生成闪电粒子效果 - 使用白色粒子折线模仿真实闪电
     */
    private void generateLightningParticles(LivingEntity target) {
        Vec3 start = this.position();
        Vec3 end = target.getEyePosition();
        
        // 计算闪电方向
        Vec3 direction = end.subtract(start).normalize();
        double distance = start.distanceTo(end);
        
        // 生成更逼真的闪电折线效果
        generateRealisticLightningBolt(start, end, distance);
        
        // 在目标位置生成闪电击中效果
        generateLightningImpactEffect(end);
    }

    /**
     * 生成逼真的闪电折线
     */
    private void generateRealisticLightningBolt(Vec3 start, Vec3 end, double distance) {
        // 闪电主链的段数（根据距离调整）
        int mainSegments = Math.max(5, (int) (distance * 2));
        
        // 生成闪电主链
        Vec3[] mainChain = generateLightningChain(start, end, mainSegments, 0.2);
        
        // 渲染闪电主链
        for (int i = 0; i < mainChain.length - 1; i++) {
            renderLightningSegment(mainChain[i], mainChain[i + 1], 0.8);
        }
        
        // 生成闪电分支（随机数量）
        int branchCount = (int) (Math.random() * 3) + 1; // 1-3个分支
        for (int branch = 0; branch < branchCount; branch++) {
            generateLightningBranch(mainChain, distance);
        }
    }

    /**
     * 生成闪电链的节点位置
     */
    private Vec3[] generateLightningChain(Vec3 start, Vec3 end, int segments, double jitter) {
        Vec3[] chain = new Vec3[segments + 1];
        chain[0] = start;
        chain[segments] = end;
        
        Vec3 direction = end.subtract(start);
        double totalDistance = start.distanceTo(end);
        double segmentLength = totalDistance / segments;
        
        // 生成中间节点（带随机抖动）
        for (int i = 1; i < segments; i++) {
            double progress = (double) i / segments;
            Vec3 basePos = start.add(direction.scale(progress));
            
            // 添加随机抖动
            double offsetX = (Math.random() - 0.5) * jitter;
            double offsetY = (Math.random() - 0.5) * jitter;
            double offsetZ = (Math.random() - 0.5) * jitter;
            
            chain[i] = new Vec3(basePos.x + offsetX, basePos.y + offsetY, basePos.z + offsetZ);
        }
        
        return chain;
    }

    /**
     * 渲染闪电段
     */
    private void renderLightningSegment(Vec3 start, Vec3 end, double intensity) {
        Vec3 segmentDir = end.subtract(start);
        double segmentLength = segmentDir.length();
        segmentDir = segmentDir.normalize();
        
        // 每段生成多个粒子点
        int particlesPerSegment = Math.max(2, (int) (segmentLength * 8));
        
        for (int i = 0; i <= particlesPerSegment; i++) {
            double progress = (double) i / particlesPerSegment;
            Vec3 pos = start.add(segmentDir.scale(segmentLength * progress));
            
            // 添加轻微随机偏移
            double offsetX = (Math.random() - 0.5) * 0.05;
            double offsetY = (Math.random() - 0.5) * 0.05;
            double offsetZ = (Math.random() - 0.5) * 0.05;
            
            // 使用白色闪电粒子（ELECTRIC_SPARK是白色闪电粒子）
            this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                    offsetX * 0.5, offsetY * 0.5, offsetZ * 0.5);
            
            // 添加闪光效果粒子
            if (Math.random() < 0.3) {
                this.level().addParticle(ParticleTypes.FLASH,
                        pos.x, pos.y, pos.z,
                        0, 0, 0);
            }
        }
    }

    /**
     * 生成闪电分支
     */
    private void generateLightningBranch(Vec3[] mainChain, double totalDistance) {
        // 随机选择一个主链节点作为分支起点
        int startIndex = (int) (Math.random() * (mainChain.length - 1)) + 1;
        Vec3 branchStart = mainChain[startIndex];
        
        // 随机分支方向
        double branchAngleX = (Math.random() - 0.5) * Math.PI * 0.5;
        double branchAngleY = (Math.random() - 0.5) * Math.PI * 0.5;
        
        // 分支长度（随机，不超过总距离的1/3）
        double branchLength = Math.random() * totalDistance * 0.3;
        
        // 计算分支终点
        double branchX = branchLength * Math.sin(branchAngleX) * Math.cos(branchAngleY);
        double branchY = branchLength * Math.sin(branchAngleY);
        double branchZ = branchLength * Math.cos(branchAngleX) * Math.cos(branchAngleY);
        
        Vec3 branchEnd = branchStart.add(branchX, branchY, branchZ);
        
        // 生成分支闪电链
        int branchSegments = Math.max(2, (int) (branchLength * 3));
        Vec3[] branchChain = generateLightningChain(branchStart, branchEnd, branchSegments, 0.15);
        
        // 渲染分支闪电
        for (int i = 0; i < branchChain.length - 1; i++) {
            renderLightningSegment(branchChain[i], branchChain[i + 1], 0.5);
        }
    }

    /**
     * 生成闪电击中效果
     */
    private void generateLightningImpactEffect(Vec3 impactPos) {
        // 在击中点生成爆炸式粒子效果
        for (int i = 0; i < 15; i++) {
            double angle = Math.random() * Math.PI * 2;
            double radius = Math.random() * 0.5;
            double speed = Math.random() * 0.2 + 0.1;
            
            double offsetX = Math.cos(angle) * radius;
            double offsetY = Math.sin(angle) * radius;
            double offsetZ = Math.sin(angle * 2) * radius;
            
            // 白色闪电粒子
            this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    impactPos.x + offsetX, impactPos.y + offsetY, impactPos.z + offsetZ,
                    offsetX * speed, offsetY * speed, offsetZ * speed);
            
            // 添加闪光效果
            if (i % 3 == 0) {
                this.level().addParticle(ParticleTypes.FLASH,
                        impactPos.x + offsetX * 0.5, impactPos.y + offsetY * 0.5, impactPos.z + offsetZ * 0.5,
                        0, 0, 0);
            }
        }
        
        // 在击中点生成黑色烟雾效果
        for (int i = 0; i < 8; i++) {
            double offsetX = (Math.random() - 0.5) * 0.3;
            double offsetY = Math.random() * 0.2;
            double offsetZ = (Math.random() - 0.5) * 0.3;
            
            this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                    impactPos.x + offsetX, impactPos.y + offsetY, impactPos.z + offsetZ,
                    offsetX * 0.1, offsetY * 0.1 + 0.05, offsetZ * 0.1);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        // Entity类的readAdditionalSaveData是抽象方法，不需要调用super
        this.lifeTime = compound.getInt("LifeTime");
        if (compound.contains("SphereDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("SphereDamage"));
        }
        if (compound.contains("MaxLifeTime")) {
            this.entityData.set(DATA_LIFETIME, compound.getInt("MaxLifeTime"));
        }
        // 从NBT数据中恢复所有者ID
        if (compound.contains("OwnerId")) {
            int ownerId = compound.getInt("OwnerId");
            Entity entity = this.level().getEntity(ownerId);
            if (entity instanceof Player) {
                this.owner = (Player) entity;
                this.entityData.set(DATA_OWNER_ID, ownerId);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        // Entity类的addAdditionalSaveData是抽象方法，不需要调用super
        compound.putInt("LifeTime", this.lifeTime);
        compound.putFloat("SphereDamage", this.entityData.get(DATA_DAMAGE));
        compound.putInt("MaxLifeTime", this.entityData.get(DATA_LIFETIME));
        // 保存所有者ID
        if (owner != null) {
            compound.putInt("OwnerId", owner.getId());
        }
    }
}