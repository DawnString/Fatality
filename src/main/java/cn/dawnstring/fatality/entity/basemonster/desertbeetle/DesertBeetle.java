package cn.dawnstring.fatality.entity.basemonster.desertbeetle;

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

public class DesertBeetle extends Monster {
    private static final float ATTACK_DAMAGE = 6.0f;
    private static final float MAX_HEALTH = 30.0f;
    private static final double ATTACK_RANGE = 2.5;
    public int attackAnimationTick = 0;
    
    public DesertBeetle(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }
    
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.ARMOR, 4.0);
    }
    
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new DesertBeetleAttackGoal(this));
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
        return SoundEvents.SPIDER_AMBIENT;
    }
    
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SPIDER_HURT;
    }
    
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SPIDER_DEATH;
    }
    
    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
        this.playSound(SoundEvents.SPIDER_STEP, 0.15F, 1.0F);
    }
    
    @Override
    public MobType getMobType() {
        return MobType.ARTHROPOD;
    }
    
    public boolean isAttacking() {
        return this.attackAnimationTick > 0;
    }
    
    // 自定义攻击目标
    private static class DesertBeetleAttackGoal extends Goal {
        private final DesertBeetle beetle;
        private int attackTime;
        
        public DesertBeetleAttackGoal(DesertBeetle beetle) {
            this.beetle = beetle;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }
        
        @Override
        public boolean canUse() {
            LivingEntity target = this.beetle.getTarget();
            return target != null && target.isAlive();
        }
        
        @Override
        public void start() {
            this.attackTime = 0;
        }
        
        @Override
        public void tick() {
            LivingEntity target = this.beetle.getTarget();
            if (target == null) return;
            
            // 计算与目标的距离
            double distance = this.beetle.distanceToSqr(target);
            
            // 移动到攻击范围内
            if (distance > ATTACK_RANGE * ATTACK_RANGE) {
                this.beetle.getNavigation().moveTo(target, 1.0);
            } else {
                this.beetle.getNavigation().stop();
                
                // 攻击逻辑
                this.attackTime++;
                if (this.attackTime >= 20) { // 每20tick攻击一次
                    if (this.beetle.hasLineOfSight(target)) {
                        this.beetle.doHurtTarget(target);
                        this.beetle.attackAnimationTick = 10; // 设置攻击动画时间
                        this.attackTime = 0;
                    }
                }
            }
        }
    }
}