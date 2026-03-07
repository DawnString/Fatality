package cn.dawnstring.fatality.entity.basemonster.spirit;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class Spirit extends Monster
{
    private static final float FLYING_SPEED = 0.6f;
    private static final float ATTACK_DAMAGE = 8.0f;
    private static final float MAX_HEALTH = 40.0f;
    private static final double ATTACK_RANGE = 3.0;
    
    public Spirit(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 10, true);
        this.setNoGravity(true);
    }
    
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.FLYING_SPEED, FLYING_SPEED)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }
    
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SpiritAttackGoal(this));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
    
    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, level);
        flyingpathnavigation.setCanOpenDoors(false);
        flyingpathnavigation.setCanFloat(true);
        flyingpathnavigation.setCanPassDoors(true);
        return flyingpathnavigation;
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // 飞行生物的特殊逻辑：保持在空中
        if (!this.level().isClientSide && this.isAlive()) {
            // 如果太靠近地面，向上飞
            BlockPos belowPos = this.blockPosition().below();
            if (this.level().getBlockState(belowPos).isSolidRender(this.level(), belowPos)) {
                this.setDeltaMovement(this.getDeltaMovement().add(0, 0.1, 0));
            }
        }
    }
    
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PHANTOM_AMBIENT;
    }
    
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PHANTOM_HURT;
    }
    
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PHANTOM_DEATH;
    }
    
    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
        // 飞行生物没有脚步声
    }
    
    @Override
    public boolean causeFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false; // 飞行生物不会受到摔落伤害
    }
    
    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        // 飞行生物不会受到摔落伤害
    }
    
    // 自定义攻击目标
    private static class SpiritAttackGoal extends Goal {
        private final Spirit spirit;
        private int attackTime;
        
        public SpiritAttackGoal(Spirit spirit) {
            this.spirit = spirit;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }
        
        @Override
        public boolean canUse() {
            LivingEntity target = this.spirit.getTarget();
            return target != null && target.isAlive();
        }
        
        @Override
        public void start() {
            this.attackTime = 0;
        }
        
        @Override
        public void tick() {
            LivingEntity target = this.spirit.getTarget();
            if (target == null) return;
            
            // 计算与目标的距离
            double distance = this.spirit.distanceToSqr(target);
            
            // 移动到攻击范围内
            if (distance > ATTACK_RANGE * ATTACK_RANGE) {
                this.spirit.getNavigation().moveTo(target, 1.0);
            } else {
                this.spirit.getNavigation().stop();
                
                // 攻击逻辑
                this.attackTime++;
                if (this.attackTime >= 20) { // 每20tick攻击一次
                    if (this.spirit.hasLineOfSight(target)) {
                        this.spirit.doHurtTarget(target);
                        this.attackTime = 0;
                    }
                }
            }
            
            // 飞行生物的特殊移动逻辑：保持在空中适当高度
            Vec3 targetPos = target.position();
            Vec3 spiritPos = this.spirit.position();
            
            // 计算理想高度（目标上方3-5格）
            double idealHeight = targetPos.y + 3.0 + this.spirit.getRandom().nextDouble() * 2.0;
            double currentHeight = spiritPos.y;
            
            // 调整高度
            if (currentHeight < idealHeight - 1.0) {
                this.spirit.setDeltaMovement(this.spirit.getDeltaMovement().add(0, 0.05, 0));
            } else if (currentHeight > idealHeight + 1.0) {
                this.spirit.setDeltaMovement(this.spirit.getDeltaMovement().add(0, -0.05, 0));
            }
        }
    }
}