package cn.dawnstring.fatality.entity.basemonster.littleghost;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class LittleGhost extends Monster {
    
    // 攻击状态管理
    private boolean hasAttacked = false;
    private int retreatCooldown = 0;
    private static final int RETREAT_DURATION = 20; // 1秒后退时间
    
    public LittleGhost(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D) // 20点生命值
                .add(Attributes.ATTACK_DAMAGE, 4.0D) // 4点攻击伤害
                .add(Attributes.MOVEMENT_SPEED, 0.35D) // 移动速度
                .add(Attributes.FOLLOW_RANGE, 16.0D) // 追踪范围
                .add(Attributes.ARMOR, 1.0D); // 1点护甲
    }

    @Override
    protected void registerGoals() {
        // 攻击目标选择
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        
        // 移动和攻击AI
        this.goalSelector.addGoal(2, new LittleGhostAttackGoal(this));
        this.goalSelector.addGoal(3, new MoveTowardsTargetGoal(this, 0.9D, 16.0F));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        
        // 处理后退冷却
        if (retreatCooldown > 0) {
            retreatCooldown--;
            if (retreatCooldown <= 0) {
                hasAttacked = false; // 冷却结束，可以再次攻击
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.GHAST_AMBIENT; // 使用恶魂环境音
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.GHAST_HURT; // 使用恶魂受伤音
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GHAST_DEATH; // 使用恶魂死亡音
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        // 幽灵生物没有脚步声
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEAD; // 亡灵生物
    }

    // 获取攻击状态
    public boolean hasAttacked() {
        return hasAttacked;
    }

    // 设置攻击状态
    public void setHasAttacked(boolean attacked) {
        this.hasAttacked = attacked;
        if (attacked) {
            this.retreatCooldown = RETREAT_DURATION; // 开始后退冷却
        }
    }

    // 是否在后退冷却中
    public boolean isInRetreatCooldown() {
        return retreatCooldown > 0;
    }

    // 获取后退冷却时间
    public int getRetreatCooldown() {
        return retreatCooldown;
    }

    // LittleGhost 专属攻击目标
    private static class LittleGhostAttackGoal extends MeleeAttackGoal {
        private final LittleGhost littleGhost;
        private int attackDelay = 0;
        private static final int ATTACK_INTERVAL = 20; // 1秒攻击间隔

        public LittleGhostAttackGoal(LittleGhost littleGhost) {
            super(littleGhost, 1.0D, true);
            this.littleGhost = littleGhost;
        }

        @Override
        public void start() {
            super.start();
            this.attackDelay = 0;
        }

        @Override
        public void tick() {
            LivingEntity target = this.littleGhost.getTarget();
            
            if (target == null) {
                return;
            }

            double distance = this.littleGhost.distanceToSqr(target);
            
            // 如果已经攻击过且不在后退冷却中，则后退
            if (littleGhost.hasAttacked() && !littleGhost.isInRetreatCooldown()) {
                // 后退逻辑：远离目标
                double dx = this.littleGhost.getX() - target.getX();
                double dz = this.littleGhost.getZ() - target.getZ();
                double length = Math.sqrt(dx * dx + dz * dz);
                
                if (length > 0) {
                    dx /= length;
                    dz /= length;
                    
                    // 后退移动
                    this.littleGhost.getNavigation().moveTo(
                        this.littleGhost.getX() + dx * 3, 
                        this.littleGhost.getY(), 
                        this.littleGhost.getZ() + dz * 3, 
                        1.5D
                    );
                }
                
                // 后退完成后重置攻击状态
                if (distance > 9.0D) { // 距离大于3格时
                    littleGhost.setHasAttacked(false);
                }
                
                return;
            }

            // 正常攻击逻辑
            if (distance < this.getAttackReachSqr(target)) {
                this.littleGhost.getLookControl().setLookAt(target, 30.0F, 30.0F);
                
                if (this.attackDelay <= 0) {
                    // 执行攻击
                    this.littleGhost.doHurtTarget(target);
                    this.attackDelay = ATTACK_INTERVAL;
                    
                    // 标记为已攻击，开始后退逻辑
                    littleGhost.setHasAttacked(true);
                }
            }

            if (this.attackDelay > 0) {
                this.attackDelay--;
            }

            super.tick();
        }

        @Override
        protected double getAttackReachSqr(LivingEntity target) {
            return this.littleGhost.getBbWidth() * 2.0F * this.littleGhost.getBbWidth() * 2.0F + target.getBbWidth();
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !littleGhost.isInRetreatCooldown();
        }
    }
}