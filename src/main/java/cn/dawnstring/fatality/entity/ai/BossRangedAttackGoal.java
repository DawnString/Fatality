package cn.dawnstring.fatality.entity.ai;

import cn.dawnstring.fatality.entity.BaseBoss;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

/**
 * Boss远程攻击AI行为
 * 在特定阶段执行远程攻击
 */
public class BossRangedAttackGoal extends BossPhaseAIGoal {
    private final double attackRange;
    private final double minAttackRange;
    private final int attackCooldown;
    private int attackTime = 0;

    public BossRangedAttackGoal(BaseBoss boss, double attackRange, double minAttackRange,
                                int attackCooldown, int... validPhases) {
        super(boss, validPhases);
        this.attackRange = attackRange;
        this.minAttackRange = minAttackRange;
        this.attackCooldown = attackCooldown;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && checkDistance();
    }

    @Override
    public void tick() {
        LivingEntity target = boss.getTarget();
        if (target == null) return;

        // 看向目标
        boss.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // 攻击冷却
        if (++attackTime >= attackCooldown) {
            performRangedAttack(target);
            attackTime = 0;
        }
    }

    /**
     * 检查距离是否在攻击范围内
     */
    private boolean checkDistance() {
        LivingEntity target = boss.getTarget();
        if (target == null) return false;

        double distance = boss.distanceToSqr(target);
        return distance >= minAttackRange * minAttackRange &&
                distance <= attackRange * attackRange;
    }

    /**
     * 执行远程攻击
     */
    protected void performRangedAttack(LivingEntity target) {
        // 这里可以实现具体的远程攻击逻辑
        // 例如：发射火球、箭矢、自定义投掷物等

        Vec3 bossPos = boss.position();
        Vec3 targetPos = target.position();
        Vec3 direction = targetPos.subtract(bossPos).normalize();

        // 根据阶段调整攻击强度
        BaseBoss.BossPhase phaseConfig = getPhaseConfig();
        if (phaseConfig != null) {
            double damageMultiplier = phaseConfig.getAttackDamage() / 20.0; // 基于基础攻击力计算倍数

            // 创建并发射投掷物
            createAndLaunchProjectile(target, direction, damageMultiplier);
        }

        // 播放攻击音效
        boss.playSound(net.minecraft.sounds.SoundEvents.BLAZE_SHOOT, 1.0F, 1.0F);
    }

    /**
     * 创建并发射投掷物 - 子类可以重写
     */
    protected void createAndLaunchProjectile(LivingEntity target, Vec3 direction, double damageMultiplier) {
        // 默认实现：可以在这里创建具体的投掷物
        // 例如：SmallFireball、Arrow等
        // 子类应该重写此方法来实现具体的攻击行为
    }
}