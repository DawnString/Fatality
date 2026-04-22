package cn.dawnstring.fatality.entity.boss.commanderoftheundeadguard;

import cn.dawnstring.fatality.entity.BaseBoss;
import cn.dawnstring.fatality.entity.ai.BossPhaseAIGoal;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * 亡灵守卫指挥官Boss
 * AI行为：
 * 1. 游走WALK：攻击间隔时，倾向与玩家保存距离，并向左或向右移动，面向玩家
 * 2. 冲刺RUSH：摆出冲刺姿势，1s后向玩家方向直线冲刺，造成20点伤害
 * 3. 攻击ATTACK：快速靠近玩家，进行攻击，每次动画会攻击2次，伤害分别为10，15
 * 4. 跳劈JUMP：摆出起跳姿势，2s后起跳，砸向玩家，落地时对其半径3格的生物造成40点伤害，并产生粒子效果
 * 攻击方式随机选择，进行一次攻击后的攻击间隔为2s-5s
 */
public class CommanderOfTheUndeadGuard extends BaseBoss
{
    private static final int ATTACK_COOLDOWN_MIN = 40; // 2秒 (20 ticks = 1秒)
    private static final int ATTACK_COOLDOWN_MAX = 100; // 5秒
    private int attackCooldown = 0;
    private AttackType currentAttack = AttackType.WALK;
    private int attackTimer = 0;
    private Vec3 rushDirection = Vec3.ZERO;
    private boolean isRushing = false;
    public boolean   isJumping = false;
    private Vec3 jumpTarget = Vec3.ZERO;
    private int jumpTimer = 0;

    public final AnimationState standAnimationState = new AnimationState();
    public final AnimationState moveAnimationState = new AnimationState();
    public final AnimationState rushReadyAnimationState = new AnimationState();
    public final AnimationState rushAnimationState = new AnimationState();
    public final AnimationState rushEndAnimationState = new AnimationState();
    public final AnimationState jumpReadyAnimationState = new AnimationState();
    public final AnimationState jumpStartAnimationState = new AnimationState();
    public final AnimationState jumpAnimationState = new AnimationState();
    public final AnimationState jumpEndAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();

    private int rushTimer = 0;
    private static final int RUSH_READY_DURATION = 10;  // 准备阶段持续时间（tick）
    private static final int RUSH_END_DURATION = 15;    // 结束阶段持续时间（tick）
    private boolean wasOnGround = true;


    public CommanderOfTheUndeadGuard(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }
    
    public static AttributeSupplier.Builder createAttributes() {
        return BaseBoss.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 5000.0) // 5000血量
                .add(Attributes.ATTACK_DAMAGE, 20.0) // 基础攻击伤害
                .add(Attributes.MOVEMENT_SPEED, 0.3) // 移动速度
                .add(Attributes.ARMOR, 15.0) // 护甲
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0); // 击退抗性
    }
    
    @Override
    protected void registerGoals() {
        // 目标选择
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        
        // AI行为
        this.goalSelector.addGoal(2, new CommanderAIGoal(this));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this));
    }
    
    @Override
    protected void initializePhases() {
        // 单阶段配置，5000血量
        phases.add(new BossPhase("亡灵守卫指挥官", 0.0f, 5000.0, 20.0, 0.3, 15.0));
    }
    
    @Override
    protected void initializeValidBiomes() {
        // 可以在任何群系生成
        // 如果需要限制群系，可以在这里添加
    }

    @Override
    public void tick() {
        super.tick();

        // 攻击冷却计时
        if (attackCooldown > 0) {
            attackCooldown--;
        }

        // 处理当前攻击行为
        handleCurrentAttack();

        if (level().isClientSide()) {
            // 攻击动画 - 根据当前攻击类型触发
            boolean shouldPlayAttackAnimation = false;
            if (currentAttack == AttackType.ATTACK && attackTimer > 0 && attackTimer <= 25) {
                shouldPlayAttackAnimation = true;
            } else if (currentAttack == AttackType.JUMP && attackTimer >= 40 && attackTimer <= 60) {
                shouldPlayAttackAnimation = true;
            } else if (currentAttack == AttackType.RUSH && attackTimer >= 20 && attackTimer <= 40) {
                shouldPlayAttackAnimation = true;
            }
            
            if (shouldPlayAttackAnimation) {
                attackAnimationState.startIfStopped(tickCount);
                stopAllOtherAnimations(attackAnimationState);
            } else {
                attackAnimationState.stop();
            }

            // 跳跃动画逻辑
            boolean isOnGround = onGround();
            
            // 跳跃准备动画（跳劈攻击的准备阶段）
            if (currentAttack == AttackType.JUMP && attackTimer > 0 && attackTimer < 40) {
                jumpStartAnimationState.startIfStopped(tickCount);
                jumpAnimationState.stop();
                jumpEndAnimationState.stop();
                stopAllOtherAnimations(jumpStartAnimationState);
            }
            // 跳跃上升动画
            else if (!isOnGround && wasOnGround) {
                // 刚离开地面（跳跃开始）
                jumpStartAnimationState.stop();
                jumpAnimationState.start(tickCount);
                jumpEndAnimationState.stop();
                stopAllOtherAnimations(jumpAnimationState);
            } else if (!isOnGround && !wasOnGround) {
                // 在空中继续跳跃动画
                if (!jumpAnimationState.isStarted()) {
                    jumpAnimationState.start(tickCount);
                }
            } else if (isOnGround && !wasOnGround) {
                // 刚落地
                jumpAnimationState.stop();
                jumpEndAnimationState.start(tickCount);
            }

            // 冲刺动画逻辑
            if (currentAttack == AttackType.RUSH) {
                if (attackTimer > 0 && attackTimer < 20) {
                    // 冲刺准备阶段
                    rushReadyAnimationState.startIfStopped(tickCount);
                    rushAnimationState.stop();
                    rushEndAnimationState.stop();
                    stopAllOtherAnimations(rushReadyAnimationState);
                } else if (attackTimer >= 20 && attackTimer < 40) {
                    // 冲刺中
                    rushReadyAnimationState.stop();
                    rushAnimationState.startIfStopped(tickCount);
                    rushEndAnimationState.stop();
                    stopAllOtherAnimations(rushAnimationState);
                } else if (attackTimer >= 40 && attackTimer < 55) {
                    // 冲刺结束阶段
                    rushReadyAnimationState.stop();
                    rushAnimationState.stop();
                    rushEndAnimationState.startIfStopped(tickCount);
                    stopAllOtherAnimations(rushEndAnimationState);
                }
            } else {
                // 如果不在冲刺攻击中，停止所有冲刺动画
                if (rushReadyAnimationState.isStarted() || 
                    rushAnimationState.isStarted() || 
                    rushEndAnimationState.isStarted()) {
                    rushReadyAnimationState.stop();
                    rushAnimationState.stop();
                    rushEndAnimationState.stop();
                }
            }

            // 移动动画（当不处于任何特殊状态时）
            boolean isSpecialAnimationActive = attackAnimationState.isStarted() ||
                    jumpStartAnimationState.isStarted() ||
                    jumpAnimationState.isStarted() ||
                    jumpEndAnimationState.isStarted() ||
                    rushReadyAnimationState.isStarted() ||
                    rushAnimationState.isStarted() ||
                    rushEndAnimationState.isStarted();
            
            if (!isSpecialAnimationActive) {
                if (getDeltaMovement().horizontalDistanceSqr() > 0.001) {
                    moveAnimationState.startIfStopped(tickCount);
                    standAnimationState.stop();
                } else {
                    standAnimationState.startIfStopped(tickCount);
                    moveAnimationState.stop();
                }
            } else {
                // 如果有特殊动画，停止移动和站立动画
                moveAnimationState.stop();
                standAnimationState.stop();
            }

            wasOnGround = isOnGround;
        }
    }

    // ===== 状态判断方法（用于Renderer） =====
    public boolean isAttacking() {
        return attackAnimationState.isStarted();
    }

    public boolean isRushReady() {
        return rushReadyAnimationState.isStarted();
    }

    public boolean isRushing() {
        return rushAnimationState.isStarted();
    }

    public boolean isRushEnding() {
        return rushEndAnimationState.isStarted();
    }

    /**
     * 停止除当前动画外的所有其他动画
     */
    private void stopAllOtherAnimations(AnimationState currentAnimation) {
        AnimationState[] allAnimations = {
                standAnimationState, moveAnimationState,
                rushReadyAnimationState, rushAnimationState, rushEndAnimationState,
                jumpReadyAnimationState, jumpStartAnimationState,
                jumpAnimationState, jumpEndAnimationState,
                attackAnimationState
        };

        for (AnimationState animation : allAnimations) {
            if (animation != currentAnimation) {
                animation.stop();
            }
        }
    }
    
    // 获取当前攻击类型
    public AttackType getCurrentAttackType() {
        return currentAttack;
    }
    
    // 获取攻击计时器（用于动画同步）
    public int getAttackTimer() {
        return attackTimer;
    }
    
    private void handleCurrentAttack() {
        if (attackCooldown > 0) return;
        
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) {
            currentAttack = AttackType.WALK;
            return;
        }
        
        attackTimer++;
        
        switch (currentAttack) {
            case WALK:
                handleWalkBehavior(target);
                break;
            case RUSH:
                handleRushAttack(target);
                break;
            case ATTACK:
                handleMeleeAttack(target);
                break;
            case JUMP:
                handleJumpAttack(target);
                break;
        }
    }
    
    private void handleWalkBehavior(LivingEntity target) {
        // 保持距离，左右移动
        double distance = distanceToSqr(target);
        
        if (distance < 16.0) { // 4格以内太近
            // 后退
            Vec3 awayDirection = position().subtract(target.position()).normalize();
            setDeltaMovement(awayDirection.scale(0.1));
        } else if (distance > 64.0) { // 8格以外太远
            // 靠近
            Vec3 towardDirection = target.position().subtract(position()).normalize();
            setDeltaMovement(towardDirection.scale(0.15));
        } else {
            // 左右移动
            Vec3 rightDirection = target.position().subtract(position()).cross(new Vec3(0, 1, 0)).normalize();
            if (random.nextBoolean()) {
                rightDirection = rightDirection.scale(-1); // 随机左右
            }
            setDeltaMovement(rightDirection.scale(0.1));
        }
        
        // 面向玩家
        getLookControl().setLookAt(target, 30.0F, 30.0F);
        
        // 检查是否应该开始攻击
        if (attackTimer > 20) { // 1秒后考虑攻击
            selectNextAttack();
            attackTimer = 0;
        }
    }
    
    private void handleRushAttack(LivingEntity target) {
        if (attackTimer == 1) {
            // 准备冲刺姿势
            isRushing = false;
            rushDirection = target.position().subtract(position()).normalize();
        } else if (attackTimer == 20) { // 1秒后开始冲刺
            isRushing = true;
            // 播放冲刺音效
            playSound(net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 0.8F);
        } else if (attackTimer > 20 && isRushing) {
            // 冲刺阶段
            setDeltaMovement(rushDirection.scale(1.5));
            
            // 检查碰撞伤害
            if (attackTimer % 5 == 0) { // 每5tick检查一次
                checkRushCollision(target);
            }
            
            // 冲刺结束
            if (attackTimer > 40) { // 冲刺持续1秒
                completeAttack();
            }
        }
    }
    
    private void handleMeleeAttack(LivingEntity target) {
        // 快速靠近目标
        if (distanceToSqr(target) > 9.0) { // 3格以外
            Vec3 direction = target.position().subtract(position()).normalize();
            setDeltaMovement(direction.scale(0.3));
        }
        
        // 攻击逻辑
        if (attackTimer == 10) { // 第一次攻击
            if (distanceToSqr(target) <= 16.0) { // 4格以内
                target.hurt(damageSources().mobAttack(this), 10.0F);
                playSound(net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_STRONG, 1.0F, 1.0F);
            }
        } else if (attackTimer == 20) { // 第二次攻击
            if (distanceToSqr(target) <= 16.0) { // 4格以内
                target.hurt(damageSources().mobAttack(this), 15.0F);
                playSound(net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_STRONG, 1.0F, 0.9F);
            }
            completeAttack();
        }
    }
    
    private void handleJumpAttack(LivingEntity target) {
        if (attackTimer == 1) {
            // 准备跳跃
            isJumping = false;
            jumpTarget = target.position();
            jumpTimer = 0;
        } else if (attackTimer == 40) { // 2秒后起跳
            isJumping = true;
            jumpTimer = 0;
            // 播放起跳音效
            playSound(net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.0F, 0.7F);
        } else if (isJumping) {
            jumpTimer++;
            
            // 跳跃轨迹
            double progress = (double) jumpTimer / 20.0; // 跳跃持续1秒
            double height = Math.sin(progress * Math.PI) * 3.0; // 抛物线轨迹
            
            Vec3 currentPos = position();
            Vec3 targetPos = jumpTarget.add(0, 1, 0); // 目标位置稍微抬高
            Vec3 direction = targetPos.subtract(currentPos).normalize();
            
            // 设置位置
            setPos(currentPos.x + direction.x * 2.0, currentPos.y + height, currentPos.z + direction.z * 2.0);
            
            // 落地检测
            if (jumpTimer >= 20) {
                // 落地伤害
                performLandingDamage();
                completeAttack();
            }
        }
    }
    
    private void checkRushCollision(LivingEntity target) {
        if (distanceToSqr(target) <= 9.0) { // 3格以内
            target.hurt(damageSources().mobAttack(this), 20.0F);
            // 击退效果
            Vec3 knockback = target.position().subtract(position()).normalize().scale(1.5);
            target.setDeltaMovement(knockback.x, 0.5, knockback.z);
        }
    }
    
    private void performLandingDamage() {
        // 对半径3格内的所有生物造成伤害
        level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(3.0))
                .stream()
                .filter(entity -> entity != this && distanceToSqr(entity) <= 9.0)
                .forEach(entity -> {
                    entity.hurt(damageSources().mobAttack(this), 40.0F);
                    // 击退效果
                    Vec3 knockback = entity.position().subtract(position()).normalize().scale(2.0);
                    entity.setDeltaMovement(knockback.x, 0.8, knockback.z);
                });
        
        // 播放落地音效
        playSound(net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE, 1.0F, 0.8F);
        
        // 添加地面方块粒子效果
        spawnLandingParticles();
    }
    
    /**
     * 生成跳劈落地时的粒子效果
     */
    private void spawnLandingParticles() {
        if (level().isClientSide()) return;
        
        // 在半径3格内的所有位置生成粒子效果
        int radius = 3;
        int particleCount = 50;
        
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                // 检查是否在圆形半径内
                double distance = Math.sqrt(x * x + z * z);
                if (distance <= radius) {
                    // 获取该位置的方块状态
                    BlockParticleOption particleOption = new BlockParticleOption(ParticleTypes.BLOCK, 
                            level().getBlockState(blockPosition().offset(x, -1, z)));
                    
                    // 在该位置上方生成多个粒子
                    int particlesPerBlock = particleCount / (radius * radius * 4); // 根据面积分配粒子数量
                    
                    for (int i = 0; i < particlesPerBlock; i++) {
                        double blockX = getX() + x + random.nextDouble();
                        double blockZ = getZ() + z + random.nextDouble();
                        double blockY = getY(); // 在当前位置的高度生成
                        
                        // 添加随机偏移和速度
                        double velocityX = (random.nextDouble() - 0.5) * 0.2;
                        double velocityY = random.nextDouble() * 0.5 + 0.2;
                        double velocityZ = (random.nextDouble() - 0.5) * 0.2;
                        
                        level().addParticle(particleOption, 
                                blockX, 
                                blockY, 
                                blockZ, 
                                velocityX, velocityY, velocityZ);
                    }
                }
            }
        }
        
        // 添加中心区域的额外粒子效果（冲击波）
        for (int i = 0; i < 20; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double distance = random.nextDouble() * radius;
            double offsetX = Math.cos(angle) * distance;
            double offsetZ = Math.sin(angle) * distance;
            
            // 爆炸粒子向上飞散
            level().addParticle(ParticleTypes.EXPLOSION,
                    getX() + offsetX,
                    getY() + 0.5,
                    getZ() + offsetZ,
                    0.0, random.nextDouble() * 0.5 + 0.3, 0.0);
        }
        
        // 添加地面震动效果（烟雾粒子）
        for (int i = 0; i < 15; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double distance = random.nextDouble() * radius;
            double offsetX = Math.cos(angle) * distance;
            double offsetZ = Math.sin(angle) * distance;
            
            level().addParticle(ParticleTypes.CLOUD,
                    getX() + offsetX,
                    getY() + 0.1,
                    getZ() + offsetZ,
                    (random.nextDouble() - 0.5) * 0.1, 0.1, (random.nextDouble() - 0.5) * 0.1);
        }
    }
    
    private void selectNextAttack() {
        AttackType[] attacks = AttackType.values();
        currentAttack = attacks[random.nextInt(attacks.length)];
        attackTimer = 0;
        isRushing = false;
        isJumping = false;
    }
    
    private void completeAttack() {
        // 设置攻击冷却
        attackCooldown = ATTACK_COOLDOWN_MIN + random.nextInt(ATTACK_COOLDOWN_MAX - ATTACK_COOLDOWN_MIN);
        
        // 重置攻击状态
        currentAttack = AttackType.WALK;
        attackTimer = 0;
        isRushing = false;
        isJumping = false;
    }
    
    public AttackType getCurrentAttack() {
        return currentAttack;
    }
    
    /**
     * 免疫摔落伤害
     */
    @Override
    public boolean causeFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        // Boss免疫所有摔落伤害
        return false;
    }
    
    /**
     * 重写此方法以确保boss不会受到摔落伤害
     */
    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        // 免疫摔落伤害
        if (damageSource.getEntity() != null && damageSource.getEntity().fallDistance > 0.0F) {
            return true;
        }
        return super.isInvulnerableTo(damageSource);
    }

    // 攻击类型枚举
    public enum AttackType {
        WALK, RUSH, ATTACK, JUMP
    }
    
    // 自定义AI目标
    private static class CommanderAIGoal extends BossPhaseAIGoal {
        
        public CommanderAIGoal(CommanderOfTheUndeadGuard boss) {
            super(boss, 0); // 只在阶段0有效
        }
        
        @Override
        public boolean canUse() {
            return super.canUse() && ((CommanderOfTheUndeadGuard) boss).isAttacking();
        }
        
        @Override
        public void tick() {
            // AI行为由boss自身的tick方法处理
        }
    }
    
    // 看向玩家目标
    private static class LookAtPlayerGoal extends Goal {
        private final CommanderOfTheUndeadGuard boss;
        
        public LookAtPlayerGoal(CommanderOfTheUndeadGuard boss) {
            this.boss = boss;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }
        
        @Override
        public boolean canUse() {
            return boss.getTarget() != null;
        }
        
        @Override
        public void tick() {
            if (boss.getTarget() != null) {
                boss.getLookControl().setLookAt(boss.getTarget(), 30.0F, 30.0F);
            }
        }
    }
}