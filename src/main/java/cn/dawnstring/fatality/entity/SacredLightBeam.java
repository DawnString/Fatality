package cn.dawnstring.fatality.entity;

import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 神圣光芒光束 - 持续发射的光束效果实体
 * 特性：从玩家位置向准星方向发射光束，对路径上的敌人造成伤害
 */
public class SacredLightBeam extends Entity {
    private static final EntityDataAccessor<Integer> DATA_DURATION = SynchedEntityData.defineId(SacredLightBeam.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(SacredLightBeam.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_OWNER_ID = SynchedEntityData.defineId(SacredLightBeam.class, EntityDataSerializers.INT);

    private int lifeTime = 0;
    private Player owner;
    private final float beamLength = 50.0f; // 光束长度50格
    private final float beamWidth = 1.5f; // 光束宽度1.5格

    public SacredLightBeam(EntityType<? extends SacredLightBeam> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.setInvulnerable(true);
        this.noPhysics = true;
    }

    public SacredLightBeam(Level level, Player owner, float damage) {
        this(ModEntities.SACRED_LIGHT_BEAM.get(), level);
        this.owner = owner;
        this.entityData.set(DATA_DAMAGE, damage);
        this.entityData.set(DATA_OWNER_ID, owner.getId());

        // 设置位置到玩家位置
        this.setPos(owner.getX(), owner.getY() + 1.5, owner.getZ());
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_DAMAGE, 0.0f);
        this.entityData.define(DATA_OWNER_ID, -1);
    }

    @Override
    public void tick() {
        super.tick();
        
        if (owner == null || owner.isRemoved()) {
            this.discard();
            return;
        }

        // 更新位置到玩家位置
        this.setPos(owner.getX(), owner.getY() + 1.5, owner.getZ());

        lifeTime++;

        // 生成神圣光芒粒子效果（在客户端生成）
        if (this.level().isClientSide()) {
            // 每帧都生成粒子，确保效果连续可见
            generateSacredLightParticles();
        }

        // 每10tick（0.5秒）造成一次伤害（在服务器端执行）
        if (!this.level().isClientSide() && lifeTime % 10 == 0) {
            applyDamageToTargets();
        }

        // 检查魔法值消耗（每秒2点魔法值）
        if (!this.level().isClientSide() && lifeTime % 20 == 0) {
            if (!cn.dawnstring.fatality.system.ManaSystem.safeConsumeMana(owner, 2.0f)) {
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
     * 生成神圣光芒粒子效果
     */
    private void generateSacredLightParticles() {
        if (owner == null) return;

        Vec3 startPos = this.position();
        Vec3 lookDirection = owner.getLookAngle();
        
        // 生成光束主体粒子效果 - 增加粒子数量和密度
        for (int i = 0; i < 8; i++) {
            double progress = (lifeTime * 0.1 + i * 0.2) % beamLength;
            Vec3 particlePos = startPos.add(lookDirection.scale(progress));
            
            // 主光束粒子（白色和金色）
            this.level().addParticle(ParticleTypes.END_ROD,
                    particlePos.x, particlePos.y, particlePos.z,
                    (random.nextDouble() - 0.5) * 0.02, 
                    (random.nextDouble() - 0.5) * 0.02, 
                    (random.nextDouble() - 0.5) * 0.02);
                    
            // 使用更可见的粒子替代FLASH
            this.level().addParticle(ParticleTypes.GLOW,
                    particlePos.x, particlePos.y, particlePos.z,
                    0, 0, 0);
                    
            // 光束边缘粒子（金色）
            if (i % 2 == 0) {
                double angle = (lifeTime * 0.05 + i * Math.PI * 2 / 6) % (Math.PI * 2);
                double offsetX = Math.cos(angle) * beamWidth * 0.6;
                double offsetZ = Math.sin(angle) * beamWidth * 0.6;
                
                this.level().addParticle(ParticleTypes.FLAME,
                        particlePos.x + offsetX, particlePos.y, particlePos.z + offsetZ,
                        0, 0.01, 0);
                        
                // 添加额外的金色粒子
                this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        particlePos.x + offsetX * 0.5, particlePos.y + 0.1, particlePos.z + offsetZ * 0.5,
                        0, 0.005, 0);
            }
        }

        // 生成光束起点特效（在玩家面前）
        for (int i = 0; i < 12; i++) {
            double offsetX = (random.nextDouble() - 0.5) * beamWidth * 1.2;
            double offsetY = (random.nextDouble() - 0.5) * beamWidth * 1.2;
            double offsetZ = (random.nextDouble() - 0.5) * beamWidth * 1.2;
            
            this.level().addParticle(ParticleTypes.END_ROD,
                    startPos.x + offsetX, startPos.y + offsetY, startPos.z + offsetZ,
                    (random.nextDouble() - 0.5) * 0.05, 
                    (random.nextDouble() - 0.5) * 0.05, 
                    (random.nextDouble() - 0.5) * 0.05);
                    
            this.level().addParticle(ParticleTypes.GLOW,
                    startPos.x + offsetX, startPos.y + offsetY, startPos.z + offsetZ,
                    0, 0, 0);
                    
            // 添加起点火花效果
            this.level().addParticle(ParticleTypes.SCRAPE,
                    startPos.x + offsetX * 0.5, startPos.y + offsetY * 0.5, startPos.z + offsetZ * 0.5,
                    (random.nextDouble() - 0.5) * 0.02, 
                    (random.nextDouble() - 0.5) * 0.02, 
                    (random.nextDouble() - 0.5) * 0.02);
        }

        // 生成光束终点特效
        Vec3 endPos = startPos.add(lookDirection.scale(beamLength));
        for (int i = 0; i < 15; i++) {
            double offsetX = (random.nextDouble() - 0.5) * beamWidth * 3;
            double offsetY = (random.nextDouble() - 0.5) * beamWidth * 3;
            double offsetZ = (random.nextDouble() - 0.5) * beamWidth * 3;
            
            this.level().addParticle(ParticleTypes.GLOW,
                    endPos.x + offsetX, endPos.y + offsetY, endPos.z + offsetZ,
                    0, 0, 0);
                    
            this.level().addParticle(ParticleTypes.FLAME,
                    endPos.x + offsetX, endPos.y + offsetY, endPos.z + offsetZ,
                    (random.nextDouble() - 0.5) * 0.03, 
                    (random.nextDouble() - 0.5) * 0.03, 
                    (random.nextDouble() - 0.5) * 0.03);
                    
            // 添加一些火花效果
            this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    endPos.x + offsetX, endPos.y + offsetY, endPos.z + offsetZ,
                    (random.nextDouble() - 0.5) * 0.05, 
                    (random.nextDouble() - 0.5) * 0.05, 
                    (random.nextDouble() - 0.5) * 0.05);
                    
            // 添加终点爆炸效果
            if (i % 3 == 0) {
                this.level().addParticle(ParticleTypes.WAX_OFF,
                        endPos.x + offsetX * 0.3, endPos.y + offsetY * 0.3, endPos.z + offsetZ * 0.3,
                        (random.nextDouble() - 0.5) * 0.1, 
                        (random.nextDouble() - 0.5) * 0.1, 
                        (random.nextDouble() - 0.5) * 0.1);
            }
        }
    }

    /**
     * 对光束路径上的所有目标造成伤害
     */
    private void applyDamageToTargets() {
        if (owner == null || owner.isRemoved()) return;

        float damage = this.entityData.get(DATA_DAMAGE);
        Vec3 startPos = this.position();
        Vec3 lookDirection = owner.getLookAngle();
        Vec3 endPos = startPos.add(lookDirection.scale(beamLength));

        // 创建光束的碰撞盒（从起点到终点的长方体）
        AABB beamBox = new AABB(
                Math.min(startPos.x, endPos.x) - beamWidth, 
                Math.min(startPos.y, endPos.y) - beamWidth, 
                Math.min(startPos.z, endPos.z) - beamWidth,
                Math.max(startPos.x, endPos.x) + beamWidth, 
                Math.max(startPos.y, endPos.y) + beamWidth, 
                Math.max(startPos.z, endPos.z) + beamWidth
        );

        // 获取光束范围内的所有生物（排除玩家）
        java.util.List<LivingEntity> targets = this.level().getEntitiesOfClass(
                LivingEntity.class,
                beamBox,
                entity -> entity != owner && !(entity instanceof Player) && entity.isAlive()
        );

        // 对每个目标造成伤害
        for (LivingEntity target : targets) {
            // 检查目标是否在光束路径上（使用点到直线的距离）
            if (isTargetInBeamPath(target, startPos, endPos)) {
                target.hurt(this.damageSources().indirectMagic(this, owner), damage);

                // 播放神圣伤害音效
                this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.BEACON_AMBIENT, SoundSource.HOSTILE, 0.4f, 1.2f);

                // 添加发光效果（2秒）
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.GLOWING, 40, 0));
            }
        }
    }

    /**
     * 检查目标是否在光束路径上
     */
    private boolean isTargetInBeamPath(LivingEntity target, Vec3 start, Vec3 end) {
        Vec3 targetPos = target.position().add(0, target.getBbHeight() / 2, 0);
        
        // 计算点到直线的距离
        Vec3 lineVec = end.subtract(start);
        Vec3 pointVec = targetPos.subtract(start);
        
        double lineLength = lineVec.length();
        if (lineLength == 0) return false;
        
        Vec3 lineDir = lineVec.normalize();
        double projection = pointVec.dot(lineDir);
        
        // 检查投影是否在线段范围内
        if (projection < 0 || projection > lineLength) {
            return false;
        }
        
        // 计算垂直距离
        Vec3 closestPoint = start.add(lineDir.scale(projection));
        double distance = targetPos.distanceTo(closestPoint);
        
        // 检查距离是否在光束宽度范围内
        return distance <= beamWidth;
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
}