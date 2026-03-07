package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 不完整神圣光束 - 散射光束效果实体
 * 特性：从玩家位置向准星方向发射光束，随使用时间增加光束会变得散射
 * 伤害：每tick造成伤害，散射程度随使用时间增加
 */
public class IncompleteHolyBeam extends Entity {
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(IncompleteHolyBeam.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_OWNER_ID = SynchedEntityData.defineId(IncompleteHolyBeam.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SCATTER_LEVEL = SynchedEntityData.defineId(IncompleteHolyBeam.class, EntityDataSerializers.INT);

    private Player owner;
    private final float beamLength = 40.0f; // 光束长度40格
    private final float baseBeamWidth = 1.0f; // 基础光束宽度1格
    private int scatterLevel = 0; // 散射等级（0-5级）

    public IncompleteHolyBeam(EntityType<? extends IncompleteHolyBeam> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.setInvulnerable(true);
        this.noPhysics = true;
    }

    public IncompleteHolyBeam(Level level, Player owner, float damage, int useDuration) {
        this(ModEntities.INCOMPLETE_HOLY_BEAM.get(), level);
        this.owner = owner;
        this.entityData.set(DATA_DAMAGE, damage);
        this.entityData.set(DATA_OWNER_ID, owner.getId());
        this.scatterLevel = calculateScatterLevel(useDuration);
        this.entityData.set(DATA_SCATTER_LEVEL, scatterLevel);

        // 设置位置到玩家位置
        this.setPos(owner.getX(), owner.getY() + 1.5, owner.getZ());
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_DAMAGE, 0.0f);
        this.entityData.define(DATA_OWNER_ID, -1);
        this.entityData.define(DATA_SCATTER_LEVEL, 0);
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

        // 生成神圣光束粒子效果（在客户端生成）
        if (this.level().isClientSide()) {
            generateHolyBeamParticles();
        }

        // 每5tick（0.25秒）造成一次伤害（在服务器端执行）
        if (!this.level().isClientSide() && tickCount % 5 == 0) {
            applyDamageToTargets();
        }
    }

    /**
     * 更新伤害值
     */
    public void updateDamage(float newDamage) {
        this.entityData.set(DATA_DAMAGE, newDamage);
    }

    /**
     * 更新散射程度
     */
    public void updateScatter(int useDuration) {
        int newScatterLevel = calculateScatterLevel(useDuration);
        if (newScatterLevel != this.scatterLevel) {
            this.scatterLevel = newScatterLevel;
            this.entityData.set(DATA_SCATTER_LEVEL, scatterLevel);
        }
    }

    /**
     * 根据使用时间计算散射等级
     */
    private int calculateScatterLevel(int useDuration) {
        // 散射等级随使用时间增加：
        // 0-2秒：等级0（不散射）
        // 2-4秒：等级1（轻微散射）
        // 4-6秒：等级2（中等散射）
        // 6-8秒：等级3（较强散射）
        // 8-10秒：等级4（强散射）
        // 10秒以上：等级5（最大散射）
        int seconds = useDuration / 20; // 转换为秒
        return Math.min(5, seconds / 2);
    }

    /**
     * 生成神圣光束粒子效果
     */
    private void generateHolyBeamParticles() {
        if (owner == null) return;

        Vec3 startPos = this.position();
        Vec3 lookDirection = owner.getLookAngle();
        
        // 根据散射等级计算当前光束宽度
        float currentBeamWidth = baseBeamWidth + (scatterLevel * 0.5f);
        
        // 生成主光束粒子
        for (int i = 0; i < 6 + scatterLevel * 2; i++) { // 散射等级越高，粒子越多
            double progress = (tickCount * 0.1 + i * 0.3) % beamLength;
            Vec3 particlePos = startPos.add(lookDirection.scale(progress));
            
            // 主光束粒子（金色和白色）
            this.level().addParticle(ParticleTypes.GLOW,
                    particlePos.x, particlePos.y, particlePos.z,
                    0, 0, 0);
                    
            // 散射光束粒子（根据散射等级生成额外的光束）
            if (scatterLevel > 0) {
                for (int j = 0; j < scatterLevel; j++) {
                    double scatterAngle = (j * Math.PI * 2 / scatterLevel + tickCount * 0.05) % (Math.PI * 2);
                    double scatterStrength = scatterLevel * 0.1;
                    
                    Vec3 scatterDirection = lookDirection
                            .yRot((float) (Math.sin(scatterAngle) * scatterStrength))
                            .xRot((float) (Math.cos(scatterAngle) * scatterStrength * 0.5));
                    
                    Vec3 scatterPos = startPos.add(scatterDirection.scale(progress));
                    
                    this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                            scatterPos.x, scatterPos.y, scatterPos.z,
                            (random.nextDouble() - 0.5) * 0.02, 
                            (random.nextDouble() - 0.5) * 0.02, 
                            (random.nextDouble() - 0.5) * 0.02);
                }
            }
        }

        // 生成光束起点特效
        for (int i = 0; i < 8 + scatterLevel * 2; i++) {
            double offsetX = (random.nextDouble() - 0.5) * currentBeamWidth * 1.5;
            double offsetY = (random.nextDouble() - 0.5) * currentBeamWidth * 1.5;
            double offsetZ = (random.nextDouble() - 0.5) * currentBeamWidth * 1.5;
            
            this.level().addParticle(ParticleTypes.END_ROD,
                    startPos.x + offsetX, startPos.y + offsetY, startPos.z + offsetZ,
                    (random.nextDouble() - 0.5) * 0.03, 
                    (random.nextDouble() - 0.5) * 0.03, 
                    (random.nextDouble() - 0.5) * 0.03);
                    
            this.level().addParticle(ParticleTypes.GLOW,
                    startPos.x + offsetX, startPos.y + offsetY, startPos.z + offsetZ,
                    0, 0, 0);
        }

        // 生成光束终点特效
        Vec3 endPos = startPos.add(lookDirection.scale(beamLength));
        for (int i = 0; i < 10 + scatterLevel * 3; i++) {
            double offsetX = (random.nextDouble() - 0.5) * currentBeamWidth * 2;
            double offsetY = (random.nextDouble() - 0.5) * currentBeamWidth * 2;
            double offsetZ = (random.nextDouble() - 0.5) * currentBeamWidth * 2;
            
            this.level().addParticle(ParticleTypes.GLOW,
                    endPos.x + offsetX, endPos.y + offsetY, endPos.z + offsetZ,
                    0, 0, 0);
                    
            this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    endPos.x + offsetX, endPos.y + offsetY, endPos.z + offsetZ,
                    (random.nextDouble() - 0.5) * 0.04, 
                    (random.nextDouble() - 0.5) * 0.04, 
                    (random.nextDouble() - 0.5) * 0.04);
        }
    }

    /**
     * 对光束路径上的目标造成伤害
     */
    private void applyDamageToTargets() {
        if (owner == null) return;

        Vec3 startPos = this.position();
        Vec3 lookDirection = owner.getLookAngle();
        float damage = this.entityData.get(DATA_DAMAGE);
        
        // 根据散射等级计算检测范围
        float detectionWidth = baseBeamWidth + (scatterLevel * 0.8f);
        
        // 检测光束路径上的实体
        for (int i = 1; i <= beamLength; i += 2) {
            Vec3 checkPos = startPos.add(lookDirection.scale(i));
            
            // 创建检测区域
            AABB detectionBox = new AABB(
                    checkPos.x - detectionWidth, checkPos.y - detectionWidth, checkPos.z - detectionWidth,
                    checkPos.x + detectionWidth, checkPos.y + detectionWidth, checkPos.z + detectionWidth
            );
            
            // 获取区域内的所有实体
            for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, detectionBox)) {
                // 排除玩家自己和友方实体
                if (target == owner || target.isAlliedTo(owner)) continue;
                
                // 计算伤害（散射等级越高，单次伤害越低，但攻击范围更大）
                float finalDamage = damage * (1.0f - scatterLevel * 0.1f);
                
                // 造成伤害
                target.hurt(this.damageSources().indirectMagic(this, owner), finalDamage);
                
                // 播放命中音效
                this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.6f, 1.0f + random.nextFloat() * 0.2f);
                
                // 生成命中粒子
                if (this.level().isClientSide()) {
                    for (int j = 0; j < 3; j++) {
                        this.level().addParticle(ParticleTypes.CRIT,
                                target.getX(), target.getY() + target.getEyeHeight() * 0.5, target.getZ(),
                                (random.nextDouble() - 0.5) * 0.2, 
                                (random.nextDouble() - 0.5) * 0.2, 
                                (random.nextDouble() - 0.5) * 0.2);
                    }
                }
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        // 读取保存的数据
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        // 保存数据
    }
}