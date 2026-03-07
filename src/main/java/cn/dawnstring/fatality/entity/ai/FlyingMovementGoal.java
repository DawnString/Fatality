package cn.dawnstring.fatality.entity.ai;

import cn.dawnstring.fatality.entity.boss.endofnightmare.EndOfNightmare;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;

public class FlyingMovementGoal extends Goal {
    private final EndOfNightmare boss;
    private static final float FLYING_HEIGHT = 20.0F; // 飞行高度

    public FlyingMovementGoal(EndOfNightmare boss) {
        this.boss = boss;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return this.boss.getTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void tick() {
        LivingEntity target = this.boss.getTarget();
        if (target == null) return;

        // 保持飞行高度
        double targetY = target.getY() + FLYING_HEIGHT;
        double currentY = this.boss.getY();

        if (Math.abs(currentY - targetY) > 2.0) {
            double yMovement = (targetY > currentY) ? 0.1 : -0.1;
            this.boss.setDeltaMovement(this.boss.getDeltaMovement().add(0, yMovement, 0));
        }

        // 水平移动以保持与目标的距离
        Vec3 targetPos = target.position();
        Vec3 bossPos = this.boss.position();
        Vec3 direction = targetPos.subtract(bossPos).normalize();

        double distance = bossPos.distanceTo(targetPos);
        double idealDistance = 15.0; // 理想距离

        if (distance > idealDistance + 5.0) {
            // 太远，靠近目标
            this.boss.setDeltaMovement(this.boss.getDeltaMovement().add(direction.scale(0.1)));
        } else if (distance < idealDistance - 5.0) {
            // 太近，远离目标
            this.boss.setDeltaMovement(this.boss.getDeltaMovement().add(direction.scale(-0.1)));
        }

        // 限制移动速度
        Vec3 currentMovement = this.boss.getDeltaMovement();
        if (currentMovement.length() > 0.5) {
            this.boss.setDeltaMovement(currentMovement.normalize().scale(0.5));
        }
    }
}
