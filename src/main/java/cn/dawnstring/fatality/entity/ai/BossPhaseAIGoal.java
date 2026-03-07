package cn.dawnstring.fatality.entity.ai;

import cn.dawnstring.fatality.entity.BaseBoss;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Boss阶段AI目标基类
 * 根据Boss的当前阶段执行不同的AI行为
 */
public abstract class BossPhaseAIGoal extends Goal {
    protected final BaseBoss boss;
    protected final int[] validPhases; // 该AI行为有效的阶段

    public BossPhaseAIGoal(BaseBoss boss, int... validPhases) {
        this.boss = boss;
        this.validPhases = validPhases;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // 检查当前阶段是否允许使用此AI
        if (!isValidPhase()) return false;

        // 检查Boss是否在有效群系内
        if (!boss.isInValidBiome()) return false;

        LivingEntity target = boss.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse() && !boss.isTransitioning;
    }

    /**
     * 检查当前阶段是否有效
     */
    protected boolean isValidPhase() {
        int currentPhase = boss.getCurrentPhase();
        for (int phase : validPhases) {
            if (phase == currentPhase) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取当前阶段的配置
     */
    protected BaseBoss.BossPhase getPhaseConfig() {
        return boss.getCurrentPhaseConfig();
    }
}