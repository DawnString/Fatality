package cn.dawnstring.fatality.entity.basemonster.goblin;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

public class Goblin extends Monster {
    private static final float ATTACK_DAMAGE = 5.0f;
    private static final float MAX_HEALTH = 25.0f;
    private static final double ATTACK_RANGE = 2.0;
    public int attackAnimationTick = 0;
    
    public Goblin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }
    
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.ARMOR, 2.0);
    }
    
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new GoblinAttackGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // 攻击动画计时器
        if (this.attackAnimationTick > 0) {
            this.attackAnimationTick--;
        }
    }
    
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_AMBIENT;
    }
    
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ZOMBIE_HURT;
    }
    
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_DEATH;
    }
    
    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
        this.playSound(SoundEvents.ZOMBIE_STEP, 0.15F, 1.0F);
    }
    
    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
    }
    
    public boolean isAttacking() {
        return this.attackAnimationTick > 0;
    }
    
    // 自定义攻击目标
    private static class GoblinAttackGoal extends Goal {
        private final Goblin goblin;
        private int attackTime;
        
        public GoblinAttackGoal(Goblin goblin) {
            this.goblin = goblin;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }
        
        @Override
        public boolean canUse() {
            LivingEntity target = this.goblin.getTarget();
            return target != null && target.isAlive();
        }
        
        @Override
        public void start() {
            this.attackTime = 0;
        }
        
        @Override
        public void tick() {
            LivingEntity target = this.goblin.getTarget();
            if (target == null) return;
            
            // 计算与目标的距离
            double distance = this.goblin.distanceToSqr(target);
            
            // 移动到攻击范围内
            if (distance > ATTACK_RANGE * ATTACK_RANGE) {
                this.goblin.getNavigation().moveTo(target, 1.0);
            } else {
                this.goblin.getNavigation().stop();
                
                // 攻击逻辑
                this.attackTime++;
                if (this.attackTime >= 25) { // 每25tick攻击一次
                    if (this.goblin.hasLineOfSight(target)) {
                        this.goblin.doHurtTarget(target);
                        this.goblin.attackAnimationTick = 10; // 设置攻击动画时间
                        this.attackTime = 0;
                    }
                }
            }
        }
    }
}