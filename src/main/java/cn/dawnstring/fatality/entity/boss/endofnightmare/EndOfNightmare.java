package cn.dawnstring.fatality.entity.boss.endofnightmare;

import cn.dawnstring.fatality.Fatality;
import cn.dawnstring.fatality.entity.BaseBoss;
import cn.dawnstring.fatality.entity.ai.FlyingMovementGoal;
import cn.dawnstring.fatality.entity.projectile.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;

/**
 * 噩梦终结者 - 飞行Boss实体，泰拉瑞亚风格弹幕战斗
 * 血量：1,100,000
 * 拥有飞行能力和弹幕攻击系统
 * 使用状态机系统替代原版Goal系统
 */
public class EndOfNightmare extends BaseBoss
{
    private boolean isSpawning = false; // 是否正在出场动画中
    private int spawnAnimationTicks = 0; // 出场动画计时器
    private static final int SPAWN_ANIMATION_DURATION = 100; // 5秒出场动画（100 ticks）

    // 战斗场地相关
    private BlockPos arenaCenter = null; // 战斗场地中心位置
    private static final int ARENA_SIZE = 129; // 战斗场地大小（129格）
    private static final int ARENA_HEIGHT = 100; // 战斗场地高度（100格）

    // 飞行相关
    private float flySpeed = 0.5f;
    private float flyHeight = 10.0f; // 默认飞行高度

    // 状态机系统
    private AttackState currentAttackState = AttackState.IDLE;
    private int currentAttackIndex = 0;
    private int attackCooldown = 0;
    private static final int ATTACK_COOLDOWN_TICKS = 40; // 攻击冷却时间（2秒）

    // 攻击执行器 - 替代原版Goal系统
    private final AttackExecutor attackExecutor;

    // AI状态标志
    private boolean isCharging = false;
    private boolean isCasting = false;
    private boolean isLaserCharging = false;
    private boolean isImmobile = false;
    private float damageReduction = 0.0f;

    // 攻击状态枚举
    private enum AttackState {
        IDLE,           // 空闲状态，等待执行攻击
        PREPARING,      // 准备攻击（施法动画等）
        EXECUTING,      // 执行攻击
        COOLDOWN        // 攻击冷却
    }

    public EndOfNightmare(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.attackExecutor = new AttackExecutor(this);
        this.setRequiresArena(true); // 这个Boss需要战斗场地

        // 设置飞行移动控制
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.setNoGravity(true); // 禁用重力

        // 初始化时设置为出场动画状态
        this.isSpawning = true;
    }

    /**
     * 设置战斗场地中心
     */
    public void setArenaCenter(BlockPos center) {
        this.arenaCenter = center;
        if (this.requiresArena() && !this.getArenaManager().isArenaActive()) {
            this.activateArena(center, ARENA_SIZE, ARENA_HEIGHT);
        }
    }

    /**
     * 攻击完成回调方法
     */
    public void onAttackComplete() {
        // 重置攻击冷却，允许执行下一个攻击
        this.attackCooldown = 0;
        this.currentAttackState = AttackState.IDLE;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, level);
        flyingpathnavigation.setCanOpenDoors(false);
        flyingpathnavigation.setCanFloat(true);
        flyingpathnavigation.setCanPassDoors(true);
        return flyingpathnavigation;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1100000.0) // 110万血量
                .add(Attributes.ATTACK_DAMAGE, 50.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.FLYING_SPEED, 0.6) // 飞行速度
                .add(Attributes.ARMOR, 30.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    public boolean isPushable() {
        return false; // Boss不受推动
    }

    @Override
    public boolean isNoGravity() {
        return true; // Boss不受重力影响
    }

    @Override
    protected void registerGoals() {
        // 只保留必要的移动和目标选择AI
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.goalSelector.addGoal(1, new FlyingMovementGoal(this));
        // 移除所有攻击相关的Goal，使用状态机系统替代
    }

    @Override
    protected void onPhaseTransitionComplete(int newPhase) {
        super.onPhaseTransitionComplete(newPhase);

        // 重置攻击顺序和状态
        currentAttackIndex = 0;
        attackCooldown = 0;
        currentAttackState = AttackState.IDLE;

        // 根据阶段调整飞行高度、速度和弹幕强度
        switch (newPhase) {
            case 0 -> {
                flyHeight = 8.0f;
                flySpeed = 0.4f;
                // 阶段1：基础弹幕
            }
            case 1 -> {
                flyHeight = 10.0f;
                flySpeed = 0.45f;
            }
            case 2 -> {
                flyHeight = 12.0f;
                flySpeed = 0.5f;
                // 阶段3：开始增加弹幕密度
            }
            case 3 -> {
                flyHeight = 14.0f;
                flySpeed = 0.55f;
            }
            case 4 -> {
                flyHeight = 16.0f;
                flySpeed = 0.6f;
                // 阶段5：密集弹幕阶段
            }
            case 5 -> {
                flyHeight = 18.0f;
                flySpeed = 0.65f;
            }
            case 6 -> {
                flyHeight = 20.0f;
                flySpeed = 0.7f;
                // 阶段7：终极弹幕阶段
            }
            case 7 -> {
                flyHeight = 25.0f;
                flySpeed = 0.8f;
                // 最终阶段：最密集弹幕
            }
        }
    }

    @Override
    protected void initializePhases() {
        // 定义8个战斗阶段
        phases.add(new BossPhase("噩梦觉醒", 0.8f, 1100000.0, 50.0, 0.35, 30.0));
        phases.add(new BossPhase("元素盛宴", 0.75f, 1100000.0, 55.0, 0.38, 32.0));
        phases.add(new BossPhase("噩梦狂暴", 0.5f, 1100000.0, 60.0, 0.4, 35.0));
        phases.add(new BossPhase("绝望降临", 0.45f, 1100000.0, 65.0, 0.42, 37.0));
        phases.add(new BossPhase("终结噩梦", 0.2f, 1100000.0, 70.0, 0.45, 40.0));
        phases.add(new BossPhase("元素盛宴II", 0.15f, 1100000.0, 75.0, 0.48, 42.0));
        phases.add(new BossPhase("最终决战", 0.05f, 1100000.0, 80.0, 0.5, 45.0));
        phases.add(new BossPhase("终结时刻", 0.0f, 1100000.0, 85.0, 0.55, 50.0));
    }

    @Override
    public void tick()
    {
        super.tick();

        // 处理出场动画
        if (isSpawning) {
            handleSpawnAnimation();
            return; // 出场动画期间不执行其他逻辑
        }

        // 更新攻击冷却
        if (attackCooldown > 0) {
            attackCooldown--;
        }

        // 保持飞行高度（除非无法移动）
        if (!isImmobile) {
            maintainFlightHeight();
        }

        // 状态机系统：处理攻击状态转换（服务器端）
        if (!this.level().isClientSide()) {
            // 检查锁血机制
            attackExecutor.checkHealthLockMechanism();

            // 如果处于锁血状态，只处理元素盛宴
            if (attackExecutor.isHealthLocked) {
                handleLockedHealthState();
            } else {
                handleAttackStateMachine();
            }
        }

        // 保持飞行高度
        maintainFlightHeight();
    }

    /**
     * 攻击状态机处理（非锁血状态）
     */
    private void handleAttackStateMachine() {
        // 根据当前攻击状态调用相应的处理方法
        switch (currentAttackState) {
            case IDLE -> handleIdleState();
            case PREPARING -> handlePreparingState();
            case EXECUTING -> handleExecutingState();
            case COOLDOWN -> handleCooldownState();
        }
    }

    /**
     * 锁血状态处理
     */
    private void handleLockedHealthState() {
        // 在锁血状态下，只执行元素盛宴攻击
        if (currentAttackState == AttackState.IDLE) {
            // 开始元素盛宴攻击
            currentAttackState = AttackState.EXECUTING;
            attackExecutor.executeAttack(AttackExecutor.ELEMENTAL_FEAST_ATTACK); // 使用常量
            
            // 设置Boss为不可移动状态
            this.setImmobile(true);
        } else if (currentAttackState == AttackState.EXECUTING) {
            // 更新元素盛宴攻击
            attackExecutor.updateAttack();
        
            // 检查攻击是否完成（元素盛宴完成后结束锁血）
            if (attackExecutor.isAttackComplete()) {
                currentAttackState = AttackState.IDLE;
                attackExecutor.stopAttack();
                
                // 恢复Boss移动能力
                this.setImmobile(false);
        
                // 确保锁血状态正确结束
                if (attackExecutor.isHealthLocked) {
                    attackExecutor.endHealthLock();
                }
            }
        }
    }

    /**
     * 空闲状态处理
     */
    private void handleIdleState() {
        // 检查是否可以开始新的攻击
        if (attackCooldown <= 0 && this.getTarget() != null && this.getTarget().isAlive()) {
            // 检查是否处于锁血状态
            if (attackExecutor.isHealthLocked) {
                // 锁血状态下直接进入执行状态，开始元素盛宴
                currentAttackState = AttackState.EXECUTING;
                attackExecutor.executeAttack(AttackExecutor.ELEMENTAL_FEAST_ATTACK);
                this.setImmobile(true);
            } else {
                // 正常状态下使用新的攻击序列系统
                currentAttackState = AttackState.PREPARING;
                attackExecutor.executeNextAttackInSequence();
            }
        
            // 设置攻击冷却
            attackCooldown = ATTACK_COOLDOWN_TICKS;
        }
    }

    /**
     * 准备状态处理
     */
    private void handlePreparingState() {
        // 检查准备是否完成
        if (attackExecutor.isPreparationComplete()) {
            currentAttackState = AttackState.EXECUTING;
            attackExecutor.executeAttack(attackExecutor.getCurrentAttackId());
            
            // 根据攻击类型设置Boss状态
            int attackId = attackExecutor.getCurrentAttackId();
            if (attackId == AttackExecutor.ELEMENTAL_FEAST_ATTACK) {
                this.setImmobile(true); // 元素盛宴时Boss不能移动
            }
        }
    }

    /**
     * 执行状态处理
     */
    private void handleExecutingState() {
        // 更新攻击执行
        attackExecutor.updateAttack();
        
        // 检查攻击是否完成
        if (attackExecutor.isAttackComplete()) {
            currentAttackState = AttackState.COOLDOWN;
            attackCooldown = ATTACK_COOLDOWN_TICKS;
            attackExecutor.stopAttack();
            
            // 恢复Boss移动能力（如果被限制）
            this.setImmobile(false);
        }
    }

    /**
     * 冷却状态处理
     */
    private void handleCooldownState() {
        // 冷却结束后回到空闲状态
        if (attackCooldown <= 0) {
            currentAttackState = AttackState.IDLE;
        }
    }

    /**
     * 保持飞行高度和水平移动
     */
    private void maintainFlightHeight() {
        if (arenaCenter != null && this.getTarget() != null) {
            LivingEntity target = this.getTarget();
            
            // 计算理想位置：目标上方保持一定距离，水平方向保持一定距离
            double targetY = arenaCenter.getY() + flyHeight;
            double currentY = this.getY();
            
            // 计算水平方向理想位置：与目标保持10格水平距离
            Vec3 targetPos = target.position();
            Vec3 currentPos = this.position();
            
            // 计算水平方向向量
            Vec3 horizontalDirection = new Vec3(targetPos.x - currentPos.x, 0, targetPos.z - currentPos.z).normalize();
            double horizontalDistance = Math.sqrt(Math.pow(targetPos.x - currentPos.x, 2) + Math.pow(targetPos.z - currentPos.z, 2));
            
            // 理想水平位置：与目标保持10格距离
            double idealDistance = 10.0;
            Vec3 idealHorizontalPos = targetPos.subtract(horizontalDirection.scale(idealDistance));
            idealHorizontalPos = new Vec3(idealHorizontalPos.x, currentY, idealHorizontalPos.z);
            
            // 计算移动向量
            Vec3 moveDirection = idealHorizontalPos.subtract(currentPos);
            
            // 调整高度
            double heightDifference = targetY - currentY;
            if (Math.abs(heightDifference) > 1.0) {
                moveDirection = moveDirection.add(0, heightDifference * 0.1, 0);
            }
            
            // 设置移动速度
            double moveSpeed = flySpeed;
            
            // 如果距离理想位置较远，增加速度
            double distanceToIdeal = moveDirection.length();
            if (distanceToIdeal > 5.0) {
                moveSpeed *= 1.5;
            }
            
            // 归一化并应用速度
            if (moveDirection.lengthSqr() > 0.1) {
                moveDirection = moveDirection.normalize().scale(moveSpeed);
                this.setDeltaMovement(moveDirection);
            }
            
            // 始终面向目标
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
    }

    /**
     * 处理出场动画
     */
    private void handleSpawnAnimation() {
        spawnAnimationTicks++;

        if (!this.level().isClientSide()) {
            spawnAnimationEffects();

            if (spawnAnimationTicks % 20 == 0) {
                this.playSound(net.minecraft.sounds.SoundEvents.ENDER_DRAGON_GROWL,
                        1.0f, 0.5f + spawnAnimationTicks * 0.01f);
            }
        }

        if (spawnAnimationTicks >= SPAWN_ANIMATION_DURATION) {
            endSpawnAnimation();
        }
    }

    /**
     * 出场动画特效
     */
    private void spawnAnimationEffects() {
        if (this.level().isClientSide()) return;

        ServerLevel serverLevel = (ServerLevel) this.level();
        for (int i = 0; i < 8; i++) {
            double angle = (spawnAnimationTicks * 0.1) + (i * Math.PI / 4);
            double radius = spawnAnimationTicks * 0.2;
            double x = this.getX() + Math.cos(angle) * radius;
            double z = this.getZ() + Math.sin(angle) * radius;
            double y = this.getY() + spawnAnimationTicks * 0.1;

            Vector3f particleColor = new Vector3f(0.5f, 0.0f, 0.8f); // 紫色粒子
            DustParticleOptions particle = new DustParticleOptions(particleColor, 1.0f);
            serverLevel.sendParticles(particle, x, y, z, 1, 0.1, 0.1, 0.1, 0);
        }
    }

    /**
     * 结束出场动画
     */
    private void endSpawnAnimation() {
        this.isSpawning = false;
        this.spawnAnimationTicks = 0;

        // 激活战斗场地
        if (this.arenaCenter != null) {
            this.activateArena(this.arenaCenter, ARENA_SIZE, ARENA_HEIGHT);
        } else {
            // 如果没有设置场地中心，使用Boss当前位置
            this.activateArena(this.blockPosition(), ARENA_SIZE, ARENA_HEIGHT);
        }

        // 播放Boss战斗音乐
        this.playBackgroundMusic();
    }

    /**
     * 获取战斗场地中心
     */
    public BlockPos getArenaCenter() {
        return this.arenaCenter;
    }

    // 添加动画状态标志的getter方法
    public boolean isCharging() {
        return isCharging;
    }

    public boolean isCasting() {
        return isCasting;
    }

    public boolean isLaserCharging() {
        return isLaserCharging;
    }

    public boolean isImmobile() {
        return isImmobile;
    }

    // 设置方法
    public void setCharging(boolean charging) {
        this.isCharging = charging;
    }

    public void setCasting(boolean casting) {
        this.isCasting = casting;
    }

    public void setLaserCharging(boolean laserCharging) {
        this.isLaserCharging = laserCharging;
    }

    public void setImmobile(boolean immobile) {
        this.isImmobile = immobile;
    }

}