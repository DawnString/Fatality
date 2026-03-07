package cn.dawnstring.fatality.entity.boss.endofnightmare;

import cn.dawnstring.fatality.entity.projectile.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

/**
 * 攻击执行器 - 重构版战斗AI系统
 * 按照用户指定的技能顺序和效果实现
 */
public class AttackExecutor {
    private final EndOfNightmare boss;
    private int currentAttackId = -1;
    private int attackExecutionTime = 0;
    private boolean isAttacking = false;
    public boolean isHealthLocked = false; // 锁血状态
    private int healthLockDuration = 0; // 锁血持续时间
    private int sequenceIndex = 0; // 攻击序列索引

    // 攻击ID定义（按照用户指定的顺序）
    public static final int PHASE_1_ATTACK = 1;    // 阶段1：保持距离，发射飞弹，冲刺
    public static final int PHASE_2_ATTACK = 2;    // 阶段2：元素长枪、元素龙卷、高斯激光
    public static final int PHASE_3_ATTACK = 3;    // 阶段3：随机传送，发射磁暴球
    public static final int PHASE_4_ATTACK = 4;    // 阶段4：高能元素球、元素长枪、起源激光
    public static final int PHASE_5_ATTACK = 5;    // 阶段5：飞到玩家上方，散射飞弹
    public static final int ELEMENTAL_FEAST_ATTACK = 6; // 元素盛宴（锁血时触发）

    // 冲刺状态变量
    private boolean isRushing = false;
    private Vec3 rushStartPos;
    private int rushDuration = 0;
    private static final int RUSH_DURATION = 20; // 冲刺持续1秒

    // 攻击持续时间配置 (tick)
    private static final int[] ATTACK_DURATIONS = {
            600,  // PHASE_1_ATTACK: 30秒 (600 tick)
            300,  // PHASE_2_ATTACK: 15秒 (300 tick)
            150,  // PHASE_3_ATTACK: 15秒 (150 tick)
            180,  // PHASE_4_ATTACK: 9秒 (180 tick)
            120,  // PHASE_5_ATTACK: 6秒 (120 tick)
            400   // ELEMENTAL_FEAST_ATTACK: 20秒 (400 tick)
    };

    // 攻击序列 (用户指定的固定顺序)
    private static final int[] ATTACK_SEQUENCE = {
            PHASE_1_ATTACK,    // 阶段1攻击
            PHASE_2_ATTACK,    // 阶段2攻击
            PHASE_3_ATTACK,    // 阶段3攻击
            PHASE_4_ATTACK,    // 阶段4攻击
            PHASE_5_ATTACK,    // 阶段5攻击
            PHASE_1_ATTACK,    // 阶段1攻击
            PHASE_2_ATTACK,    // 阶段2攻击
            PHASE_3_ATTACK,    // 阶段3攻击
            PHASE_4_ATTACK,    // 阶段4攻击
            PHASE_5_ATTACK     // 阶段5攻击
    };

    // 锁血阈值 (血量百分比)
    private static final float[] LOCK_HEALTH_THRESHOLDS = {
            1.0f,  // 100%
            0.6f,  // 60%
            0.4f,  // 40%
            0.1f   // 10%
    };

    // 阶段2攻击计时器
    private int elementalSpearChargeTime = 0;
    private boolean elementalSpearCharging = false;
    private boolean elementalSpearSummoned = false;
    private boolean elementalTornadoSummoned = false;
    private boolean gaussLaserSummoned = false;

    // 阶段3攻击计时器
    private int teleportCount = 0;
    private int lastTeleportTime = 0;

    // 阶段4攻击计时器
    private boolean highEnergyBallsFired = false;
    private boolean elementalSpearFired = false;
    private boolean originLaserFired = false;

    // 阶段5攻击计时器
    private boolean flyingAboveTarget = false;
    private int lastScatterTime = 0;

    public AttackExecutor(EndOfNightmare boss) {
        this.boss = boss;
    }

    /**
     * 执行序列中的下一个攻击
     */
    public void executeNextAttackInSequence() {
        if (isAttacking) {
            System.out.println("正在执行攻击，无法开始新攻击");
            return;
        }

        int nextAttackId = ATTACK_SEQUENCE[sequenceIndex];
        sequenceIndex = (sequenceIndex + 1) % ATTACK_SEQUENCE.length;

        executeAttack(nextAttackId);
    }

    /**
     * 执行指定攻击
     */
    public void executeAttack(int attackId) {
        if (isAttacking) {
            System.out.println("正在执行攻击，无法开始新攻击ID: " + attackId);
            return;
        }

        this.currentAttackId = attackId;
        this.attackExecutionTime = 0;
        this.isAttacking = true;

        // 重置所有攻击状态
        resetAttackStates();

        System.out.println("开始执行攻击ID: " + attackId);

        // 根据攻击ID执行不同的攻击逻辑
        switch (attackId) {
            case PHASE_1_ATTACK -> executePhase1Attack();
            case PHASE_2_ATTACK -> executePhase2Attack();
            case PHASE_3_ATTACK -> executePhase3Attack();
            case PHASE_4_ATTACK -> executePhase4Attack();
            case PHASE_5_ATTACK -> executePhase5Attack();
            case ELEMENTAL_FEAST_ATTACK -> executeElementalFeastAttack();
            default -> {
                System.out.println("无效的攻击ID: " + attackId);
                isAttacking = false;
            }
        }
    }

    /**
     * 更新攻击执行
     */
    public void updateAttack() {
        if (!isAttacking) return;

        attackExecutionTime++;

        // 检查血量锁机制
        checkHealthLockMechanism();

        // 根据攻击ID更新不同的攻击逻辑
        switch (currentAttackId) {
            case PHASE_1_ATTACK -> updatePhase1Attack(attackExecutionTime);
            case PHASE_2_ATTACK -> updatePhase2Attack(attackExecutionTime);
            case PHASE_3_ATTACK -> updatePhase3Attack(attackExecutionTime);
            case PHASE_4_ATTACK -> updatePhase4Attack(attackExecutionTime);
            case PHASE_5_ATTACK -> updatePhase5Attack(attackExecutionTime);
            case ELEMENTAL_FEAST_ATTACK -> updateElementalFeastAttack(attackExecutionTime);
        }
    }

    /**
     * 检查攻击是否完成
     */
    public boolean isAttackComplete() {
        if (!isAttacking) return false;

        // 根据攻击ID检查是否完成
        switch (currentAttackId) {
            case PHASE_1_ATTACK -> { return attackExecutionTime >= ATTACK_DURATIONS[0]; }
            case PHASE_2_ATTACK -> { return attackExecutionTime >= ATTACK_DURATIONS[1]; }
            case PHASE_3_ATTACK -> { return attackExecutionTime >= ATTACK_DURATIONS[2]; }
            case PHASE_4_ATTACK -> { return attackExecutionTime >= ATTACK_DURATIONS[3]; }
            case PHASE_5_ATTACK -> { return attackExecutionTime >= ATTACK_DURATIONS[4]; }
            case ELEMENTAL_FEAST_ATTACK -> {
                if (attackExecutionTime >= ATTACK_DURATIONS[5]) {
                    if (isHealthLocked) {
                        endHealthLock();
                    }
                    return true;
                }
                return false;
            }
            default -> { return attackExecutionTime >= 100; } // 默认5秒
        }
    }

    /**
     * 检查血量锁机制
     */
    public void checkHealthLockMechanism() {
        if (isHealthLocked) return;

        float healthRatio = boss.getHealth() / boss.getMaxHealth();

        for (int i = 0; i < LOCK_HEALTH_THRESHOLDS.length; i++) {
            if (Math.abs(healthRatio - LOCK_HEALTH_THRESHOLDS[i]) < 0.01f) {
                startHealthLock();
                break;
            }
        }
    }

    /**
     * 开始锁血
     */
    private void startHealthLock() {
        isHealthLocked = true;
        healthLockDuration = 0;
        boss.setInvulnerableDuration(400); // 20秒无敌

        // 强制结束当前攻击
        stopAttack();

        // 开始元素盛宴
        executeAttack(ELEMENTAL_FEAST_ATTACK);

        System.out.println("触发锁血机制，开始元素盛宴");
    }

    /**
     * 处理锁血状态
     */
    private void handleLockedHealth() {
        healthLockDuration++;

        if (healthLockDuration >= 400) { // 20秒后结束锁血
            endHealthLock();
        }
    }

    /**
     * 结束锁血
     */
    public void endHealthLock() {
        isHealthLocked = false;
        healthLockDuration = 0;
        boss.setInvulnerableDuration(0); // 取消无敌

        // 回到攻击序列
        executeNextAttackInSequence();

        System.out.println("锁血结束，回到攻击序列");
    }

    /**
     * 停止当前攻击
     */
    public void stopAttack() {
        isAttacking = false;
        currentAttackId = -1;
        attackExecutionTime = 0;
        boss.setCasting(false);
        boss.setCharging(false);
        boss.setLaserCharging(false);
        boss.setImmobile(false);
    }

    /**
     * 检查是否正在攻击
     */
    public boolean isAttacking() {
        return isAttacking;
    }

    /**
     * 获取当前攻击ID
     */
    public int getCurrentAttackId() {
        return currentAttackId;
    }

    /**
     * 检查准备是否完成
     */
    public boolean isPreparationComplete() {
        return true;
    }

    /**
     * 重置所有攻击状态
     */
    private void resetAttackStates() {
        // 重置阶段2攻击状态
        elementalSpearChargeTime = 0;
        elementalSpearCharging = false;
        elementalSpearSummoned = false;
        elementalTornadoSummoned = false;
        gaussLaserSummoned = false;

        // 重置阶段3攻击状态
        teleportCount = 0;
        lastTeleportTime = 0;

        // 重置阶段4攻击状态
        highEnergyBallsFired = false;
        elementalSpearFired = false;
        originLaserFired = false;

        // 重置阶段5攻击状态
        flyingAboveTarget = false;
        lastScatterTime = 0;

        // 重置冲刺状态
        isRushing = false;
        rushDuration = 0;
    }

    /**
     * 阶段1攻击：boss面向玩家保持统一水平位置，间隔10格，
     * 每0.25秒向玩家发射元素飞弹，每1s向玩家发射元素大飞弹，
     * 每10s朝玩家冲刺一次（冲刺完成后恢复到原位置）。整个阶段持续30s。
     */
    private void executePhase1Attack() {
        boss.setCasting(true);
        System.out.println("开始阶段1攻击");
    }

    private void updatePhase1Attack(int executionTime) {
        LivingEntity target = boss.getTarget();
        if (target == null) return;

        // 处理冲刺状态
        if (isRushing) {
            handleRushAttack();
            return;
        }

        // 保持10格水平距离
        maintainDistance(target, 10.0);

        // 瞄准目标
        boss.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // 每0.25秒发射元素飞弹 (5 tick)
        if (executionTime % 5 == 0) {
            shootElementalMissile(target, false);
        }

        // 每1秒发射元素大飞弹 (20 tick)
        if (executionTime % 20 == 0) {
            shootElementalMissile(target, true);
        }

        // 每10秒冲刺一次 (200 tick)
        if (executionTime % 200 == 0 && executionTime > 0) {
            startRushAttack(target);
        }
    }

    private void startRushAttack(LivingEntity target) {
        isRushing = true;
        rushStartPos = boss.position();
        rushDuration = 0;
        boss.setCharging(true);

        Vec3 direction = target.position().subtract(boss.position()).normalize();
        boss.setDeltaMovement(direction.scale(2.0));
    }

    private void handleRushAttack() {
        rushDuration++;

        if (rushDuration >= RUSH_DURATION) {
            // 冲刺完成，恢复到原位置
            isRushing = false;
            boss.setCharging(false);
            boss.teleportTo(rushStartPos.x, rushStartPos.y, rushStartPos.z);
        }
    }

    /**
     * 阶段2攻击：boss使用元素长枪与元素龙卷，并发射高斯激光。
     * 元素长枪：boss蓄力5秒，在整个场地空中随机召唤5把元素长枪，然后插入地上，
     * 期间碰到玩家则造成320伤害，长枪不消失，以元素长枪为中心，半径2格的圆形生成冲上天的光柱，光柱触碰每秒200伤害。持续20秒。
     * 高斯激光：boss释放8道激光，其中有2道为追踪玩家，伤害280。碰撞后消失。寿命240秒
     * 元素龙卷：在地上召唤彩色的龙卷，以粒子效果构成，在地上随机方向行进，玩家触碰伤害120血每秒，存在15s。
     */
    private void executePhase2Attack() {
        boss.setCasting(true);
        System.out.println("开始阶段2攻击");
    }

    private void updatePhase2Attack(int executionTime) {
        LivingEntity target = boss.getTarget();
        if (target == null) return;

        // 瞄准目标
        boss.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // 元素长枪蓄力（5秒）
        if (executionTime < 100 && !elementalSpearCharging) {
            elementalSpearCharging = true;
            elementalSpearChargeTime = 0;
            System.out.println("开始元素长枪蓄力");
        }

        if (elementalSpearCharging) {
            elementalSpearChargeTime++;
            
            // 蓄力完成，召唤元素长枪
            if (elementalSpearChargeTime >= 100 && !elementalSpearSummoned) {
                summonElementalSpears(target);
                elementalSpearSummoned = true;
                elementalSpearCharging = false;
                System.out.println("召唤元素长枪");
            }
        }

        // 召唤元素龙卷（在蓄力期间召唤）
        if (executionTime == 50 && !elementalTornadoSummoned) {
            generateElementalTornado(target, 1);
            elementalTornadoSummoned = true;
            System.out.println("召唤元素龙卷");
        }

        // 发射高斯激光（在蓄力完成后发射）
        if (executionTime == 120 && !gaussLaserSummoned) {
            shootGaussLasers(target);
            gaussLaserSummoned = true;
            System.out.println("发射高斯激光");
        }
    }

    /**
     * 阶段3攻击：boss在场地内随机传送5次，每次间隔3秒，每次传送时朝玩家发射磁暴球。
     */
    private void executePhase3Attack() {
        boss.setCasting(true);
        System.out.println("开始阶段3攻击");
    }

    private void updatePhase3Attack(int executionTime) {
        LivingEntity target = boss.getTarget();
        if (target == null) return;

        // 每3秒传送一次 (60 tick)，总共5次
        if (executionTime % 60 == 0 && executionTime < 300) {
            int teleportCount = executionTime / 60;
            if (teleportCount <= 5) {
                executeTeleport();

                // 传送后立即发射磁暴球
                if (target != null) {
                    generateMagneticBurst(target, teleportCount);
                }
            }
        }
    }

    /**
     * 阶段4攻击：boss朝玩家发射大量高能元素球，并发射元素长枪，随后发射起源激光。
     * 起源激光：boss不能移动，而且激光可以缓慢朝玩家转向，距离100。
     */
    private void executePhase4Attack() {
        boss.setCasting(true);
        System.out.println("开始阶段4攻击");
    }

    private void updatePhase4Attack(int executionTime) {
        LivingEntity target = boss.getTarget();
        if (target == null) return;

        // 瞄准目标
        boss.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // 发射大量高能元素球（前3秒）
        if (executionTime < 60) {
            if (executionTime % 10 == 0) { // 每0.5秒发射一次
                shootHighEnergyElementBall(target);
            }
        }
        // 发射元素长枪
        else if (executionTime == 60) {
            summonElementalSpears(target);
        }
        // 发射起源激光
        else if (executionTime == 90) {
            shootOriginLaser(target);
        }
    }

    /**
     * 阶段5攻击：boss尝试快速飞行到玩家上方，玩家散射8个元素飞弹，每2秒发射一次，总共持续6秒。
     */
    private void executePhase5Attack() {
        boss.setCasting(true);
        System.out.println("开始阶段5攻击");
    }

    private void updatePhase5Attack(int executionTime) {
        LivingEntity target = boss.getTarget();
        if (target == null) return;

        // 快速飞到玩家上方
        if (executionTime < 20) {
            flyAboveTarget(target);
        }
        // 每2秒散射一次 (40 tick)，总共持续6秒（120 tick）
        else if (executionTime % 40 == 0 && executionTime < 140) {
            scatterElementalMissiles(target);
        }
    }

    /**
     * 元素盛宴攻击：在整个战斗场地生成大量元素飞弹，拥有随机的速度，方向为正东南西北上下的随机一方，元素盛宴持续20秒
     */
    private void executeElementalFeastAttack() {
        boss.setCasting(true);
        boss.setImmobile(true); // boss不能移动
        System.out.println("开始元素盛宴");
    }

    private void updateElementalFeastAttack(int executionTime) {
        // 每0.5秒生成一波飞弹 (10 tick)，持续20秒
        if (executionTime % 10 == 0 && executionTime < 400) {
            generateFeastMissiles(executionTime / 10);
        }

        // 处理锁血状态
        if (isHealthLocked) {
            handleLockedHealth();
        }

        // 元素盛宴攻击结束后，强制结束锁血和攻击
    if (executionTime >= 400) {
        if (isHealthLocked) {
            endHealthLock();
        }
        stopAttack();
        System.out.println("元素盛宴结束，持续时间20秒");
        
        // 确保攻击状态正确重置
        isAttacking = false;
        currentAttackId = -1;
        attackExecutionTime = 0;
    }
    }

    // ========== 基础攻击方法实现 ==========

    private void maintainDistance(LivingEntity target, double distance) {
        Vec3 bossPos = boss.position();
        Vec3 targetPos = target.position();
        Vec3 direction = targetPos.subtract(bossPos).normalize();

        // 计算理想位置（保持指定距离）
        Vec3 idealPos = targetPos.subtract(direction.scale(distance));

        // 保持与玩家相同的高度
        double targetY = targetPos.y;
        double currentY = bossPos.y;
        double heightDifference = targetY - currentY;

        if (Math.abs(heightDifference) > 2.0) {
            idealPos = new Vec3(idealPos.x, targetY, idealPos.z);
        } else {
            idealPos = new Vec3(idealPos.x, currentY, idealPos.z);
        }

        // 移动到理想位置
        Vec3 moveDirection = idealPos.subtract(bossPos).normalize().scale(0.5);
        boss.setDeltaMovement(moveDirection);
    }

    private void flyAboveTarget(LivingEntity target) {
        Vec3 targetPos = target.position();
        Vec3 idealPos = new Vec3(targetPos.x, targetPos.y + 8, targetPos.z);

        Vec3 moveDirection = idealPos.subtract(boss.position()).normalize().scale(0.2);
        boss.setDeltaMovement(moveDirection);
    }

    private void shootElementalMissile(LivingEntity target, boolean isLarge) {
        if (boss.hasLineOfSight(target)) {
            ElementalMissileProjectile missile = new ElementalMissileProjectile(
                    boss.level(), boss, isLarge ? 300f : 200f, isLarge
            );

            Vec3 bossPos = boss.position().add(0, 2, 0);
            Vec3 targetPos = target.position().add(0, 1, 0);
            Vec3 direction = targetPos.subtract(bossPos).normalize();

            missile.setPos(bossPos.x, bossPos.y, bossPos.z);
            missile.setDeltaMovement(direction.scale(1.5));
            missile.setNoGravity(true);

            boss.level().addFreshEntity(missile);
        }
    }

    private void shootGaussLasers(LivingEntity target) {
        Vec3 bossPos = boss.position().add(0, 2, 0);
        for (int i = 0; i < 8; i++) {
            boolean isTracking = (i < 2); // 前2道激光追踪玩家
            GaussLaserProjectile laser = new GaussLaserProjectile(
                    boss.level(), boss, 280f, isTracking, target
            );

            laser.setMaxAge(4800); // 240秒寿命
            
            double angle = (i - 3.5) * Math.PI / 8;
            Vec3 baseDirection = target.position().subtract(bossPos).normalize();
            Vec3 rotatedDirection = rotateVector(baseDirection, angle);

            laser.setPos(bossPos.x, bossPos.y, bossPos.z);
            laser.setDeltaMovement(rotatedDirection.scale(2.5));
            laser.setNoGravity(true);

            boss.level().addFreshEntity(laser);
        }
    }

    private void summonElementalSpears(LivingEntity target) {
        BlockPos arenaCenter = boss.getArenaCenter();
        if (arenaCenter == null) {
            arenaCenter = boss.blockPosition();
        }
        
        for (int i = 0; i < 5; i++) {
            double offsetX = (boss.getRandom().nextDouble() - 0.5) * 60;
            double offsetZ = (boss.getRandom().nextDouble() - 0.5) * 60;
            double spawnY = arenaCenter.getY() + 20 + boss.getRandom().nextDouble() * 30;

            ElementalSpearProjectile spear = new ElementalSpearProjectile(
                    boss.level(), boss, 320f
            );
            spear.setPos(arenaCenter.getX() + offsetX, spawnY, arenaCenter.getZ() + offsetZ);
            spear.setNoGravity(false);
            spear.setPierceLevel((byte)0);
            
            spear.setCreateLightPillar(true);
            spear.setLightPillarDamage(200f);
            spear.setLightPillarRadius(2.0f);
            spear.setLightPillarDuration(400); // 20秒

            boss.level().addFreshEntity(spear);
        }
    }

    private void executeTeleport() {
        if (boss.getArenaCenter() != null) {
            BlockPos arenaCenter = boss.getArenaCenter();
            double teleportX = arenaCenter.getX() + (boss.getRandom().nextDouble() - 0.5) * 40;
            double teleportZ = arenaCenter.getZ() + (boss.getRandom().nextDouble() - 0.5) * 40;
            double teleportY = arenaCenter.getY() + boss.getRandom().nextDouble() * 20;

            boss.teleportTo(teleportX, teleportY, teleportZ);
        }
    }

    private void generateMagneticBurst(LivingEntity target, int burstId) {
        Vec3 bossPos = boss.position();
        double radius = 8.0;
        double angle = 2 * Math.PI * burstId / 12;

        MagneticBurstProjectile magneticBall = new MagneticBurstProjectile(
                boss.level(), boss, 25.0f
        );
        magneticBall.setPos(
                bossPos.x + Math.cos(angle) * radius,
                bossPos.y + 2.0,
                bossPos.z + Math.sin(angle) * radius
        );

        Vec3 tangentDirection = new Vec3(-Math.sin(angle), 0, Math.cos(angle));
        magneticBall.setDeltaMovement(tangentDirection.scale(1.5));

        boss.level().addFreshEntity(magneticBall);
    }

    private void generateElementalTornado(LivingEntity target, int tornadoId) {
        Vec3 bossPos = boss.position();
        double spawnRadius = 15.0;
        double angle = boss.getRandom().nextDouble() * 2 * Math.PI;
        double distance = boss.getRandom().nextDouble() * spawnRadius;

        ElementalTornadoProjectile tornado = new ElementalTornadoProjectile(
                boss.level(), boss, 120f
        );
        
        tornado.setDamagePerTick(120f); // 每秒120伤害
        tornado.setDuration(300); // 15秒存在时间
        tornado.setColorfulParticles(true); // 彩色粒子效果
        tornado.setRandomMovement(true); // 随机方向行进
        
        tornado.setPos(
                bossPos.x + Math.cos(angle) * distance,
                bossPos.y + 1,
                bossPos.z + Math.sin(angle) * distance
        );

        Vec3 randomDirection = new Vec3(
                (boss.getRandom().nextDouble() - 0.5) * 0.3,
                0,
                (boss.getRandom().nextDouble() - 0.5) * 0.3
        );
        tornado.setDeltaMovement(randomDirection);

        boss.level().addFreshEntity(tornado);
    }

    private void shootOriginLaser(LivingEntity target) {
        if (boss.hasLineOfSight(target)) {
            OriginLaserProjectile laser = new OriginLaserProjectile(
                    boss.level(), boss, 350f, target
            );

            laser.setSlowTurning(true); // 缓慢转向
            laser.setMaxDistance(100.0f); // 最大距离100格
            laser.setBossImmobile(true); // boss不能移动

            Vec3 bossPos = boss.position().add(0, 2, 0);
            laser.setPos(bossPos.x, bossPos.y, bossPos.z);
            laser.setNoGravity(true);

            boss.level().addFreshEntity(laser);
        }
    }

    private void shootHighEnergyElementBall(LivingEntity target) {
        HighEnergyElementBallProjectile elementBall = new HighEnergyElementBallProjectile(
                boss.level(), boss, 320.0f, target
        );

        Vec3 bossPos = boss.position().add(0, 2, 0);
        Vec3 targetPos = target.position().add(0, 1, 0);
        Vec3 direction = targetPos.subtract(bossPos).normalize();

        elementBall.setPos(bossPos.x, bossPos.y, bossPos.z);
        elementBall.setDeltaMovement(direction.scale(1.5));
        elementBall.setNoGravity(true);

        boss.level().addFreshEntity(elementBall);
    }

    private void scatterElementalMissiles(LivingEntity target) {
        Vec3 bossPos = boss.position().add(0, 2, 0);

        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4;
            Vec3 direction = new Vec3(Math.cos(angle), -0.2, Math.sin(angle)).normalize();

            ElementalMissileProjectile missile = new ElementalMissileProjectile(
                    boss.level(), boss, 280f, false
            );
            missile.setPos(bossPos.x, bossPos.y, bossPos.z);
            missile.setDeltaMovement(direction.scale(1.5));
            missile.setNoGravity(true);

            boss.level().addFreshEntity(missile);
        }
    }

    private void generateFeastMissiles(int waveId) {
        for (int i = 0; i < 20; i++) {
            double spawnX = boss.getX() + (boss.getRandom().nextDouble() - 0.5) * 50;
            double spawnZ = boss.getZ() + (boss.getRandom().nextDouble() - 0.5) * 50;
            double spawnY = boss.getY() + boss.getRandom().nextDouble() * 30;

            int directionIndex = boss.getRandom().nextInt(6);
            Vec3 direction;
            switch (directionIndex) {
                case 0 -> direction = new Vec3(1, 0, 0);  // 东
                case 1 -> direction = new Vec3(0, 0, 1);  // 南
                case 2 -> direction = new Vec3(-1, 0, 0); // 西
                case 3 -> direction = new Vec3(0, 0, -1); // 北
                case 4 -> direction = new Vec3(0, 1, 0);  // 上
                case 5 -> direction = new Vec3(0, -1, 0); // 下
                default -> direction = new Vec3(1, 0, 0);
            }

            double speed = 0.3 + boss.getRandom().nextDouble() * 0.7;

            ElementalMissileProjectile missile = new ElementalMissileProjectile(
                    boss.level(), boss, 280f, false
            );
            missile.setPos(spawnX, spawnY, spawnZ);
            missile.setDeltaMovement(direction.scale(speed));
            missile.setNoGravity(true);

            boss.level().addFreshEntity(missile);
        }
    }

    // 工具方法
    private Vec3 rotateVector(Vec3 vector, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vec3(
                vector.x * cos - vector.z * sin,
                vector.y,
                vector.x * sin + vector.z * cos
        );
    }
}