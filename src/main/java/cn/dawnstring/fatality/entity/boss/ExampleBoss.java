package cn.dawnstring.fatality.entity.boss;

import cn.dawnstring.fatality.entity.BaseBoss;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * 示例Boss实现 - 演示血量阶段管理和群系限制
 */
public class ExampleBoss extends BaseBoss {

    public ExampleBoss(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20000.0)
                .add(Attributes.ATTACK_DAMAGE, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void registerGoals() {
        // 初始目标选择
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));

        // 初始AI行为
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Override
    protected void initializePhases() {
        // 定义3个阶段
        // 阶段1: 100%-70%血量
        phases.add(new BossPhase("初始阶段", 0.7f, 500.0, 20.0, 0.3, 10.0));

        // 阶段2: 70%-30%血量
        phases.add(new BossPhase("狂暴阶段", 0.3f, 400.0, 30.0, 0.4, 15.0));

        // 阶段3: 30%-0%血量
        phases.add(new BossPhase("最终阶段", 0.0f, 300.0, 40.0, 0.5, 20.0));
    }

    @Override
    protected void initializeValidBiomes() {
        // 限制Boss只能在特定群系内挑战
        // 例如：末地、下界、自定义群系等
        validBiomes.add(ResourceLocation.fromNamespaceAndPath("minecraft", "the_end"));
        validBiomes.add(ResourceLocation.fromNamespaceAndPath("minecraft", "nether_wastes"));
        validBiomes.add(ResourceLocation.fromNamespaceAndPath("minecraft", "soul_sand_valley"));
    }

    @Override
    protected void onPhaseTransitionComplete(int newPhase) {
        super.onPhaseTransitionComplete(newPhase);

        // 清除旧的目标选择器
        this.targetSelector.removeAllGoals(goal -> true);
        this.goalSelector.removeAllGoals(goal -> true);

        // 根据新阶段添加不同的AI行为
        switch (newPhase) {
            case 0 -> {
                // 阶段1: 基础攻击行为
                this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
                this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
                this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8));
                this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
                this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
            }
            case 1 -> {
                // 阶段2: 增加远程攻击和移动速度
                this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
                this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, false));
                this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0));
                this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 12.0F));
                this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
                // 可以添加自定义的远程攻击目标
            }
            case 2 -> {
                // 阶段3: 最终阶段的特殊行为
                this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
                this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.5, true)); // 强制近战
                this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.2));
                this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 16.0F));
                // 可以添加AOE攻击或其他特殊行为
            }
        }
    }

    @Override
    protected void onBiomeValidityChanged(boolean isValid) {
        super.onBiomeValidityChanged(isValid);

        if (!isValid) {
            // 不在有效群系内时，Boss会获得负面效果
            // 例如：持续扣血、降低属性等
            // 这里可以添加具体的负面效果逻辑
        }
    }
}