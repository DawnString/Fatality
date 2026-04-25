package cn.dawnstring.fatality.entity.boss;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseBoss extends LivingEntity {
    
    private static final EntityDataAccessor<Integer> DATA_PHASE = 
        SynchedEntityData.defineId(BaseBoss.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_MAX_PHASE = 
        SynchedEntityData.defineId(BaseBoss.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_BERSERK = 
        SynchedEntityData.defineId(BaseBoss.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_BERSERK_TIME = 
        SynchedEntityData.defineId(BaseBoss.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_CURRENT_STATE = 
        SynchedEntityData.defineId(BaseBoss.class, EntityDataSerializers.INT);
    
    protected BossState currentState;
    protected int currentPhase;
    protected int maxPhase;
    protected boolean isBerserk;
    protected float berserkTime;
    protected float berserkThreshold;
    protected float berserkDuration;
    protected long berserkStartTime;
    
    protected int stateTimer;
    protected int attackCooldown;
    protected int attackInterval;
    
    protected LivingEntity target;
    protected Map<String, Object> stateData;
    
    public BaseBoss(EntityType<? extends BaseBoss> type, Level level) {
        super(type, level);
        this.currentState = BossState.IDLE;
        this.currentPhase = 1;
        this.maxPhase = getMaxPhases();
        this.isBerserk = false;
        this.berserkTime = 0;
        this.berserkThreshold = getBerserkThreshold();
        this.berserkDuration = getBerserkDuration();
        this.berserkStartTime = 0;
        this.stateTimer = 0;
        this.attackCooldown = 0;
        this.attackInterval = (int) getDefaultAttackInterval();
        this.target = null;
        this.stateData = new HashMap<>();
        
        //this.setNoAi(true);
    }
    
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_PHASE, 1);
        this.entityData.define(DATA_MAX_PHASE, getMaxPhases());
        this.entityData.define(DATA_BERSERK, false);
        this.entityData.define(DATA_BERSERK_TIME, 0.0f);
        this.entityData.define(DATA_CURRENT_STATE, BossState.IDLE.ordinal());
    }
    
    @Override
    public void tick() {
        super.tick();
        
        if (!this.level().isClientSide()) {
            updateTarget();
            updateBerserkState();
            updatePhase();
            updateState();
            executeStateBehavior();
            updateAttackCooldown();
        }
    }
    
    protected void updateTarget() {
        if (this.target == null || !this.target.isAlive()) {
            this.target = this.level().getNearestPlayer(this, getTargetRange());
        }
    }
    
    protected void updateBerserkState() {
        float healthPercent = getHealth() / getMaxHealth();
        
        if (!isBerserk && healthPercent <= berserkThreshold) {
            enterBerserk();
        }
        
        if (isBerserk) {
            long elapsed = System.currentTimeMillis() - berserkStartTime;
            if (elapsed >= berserkDuration) {
                exitBerserk();
            } else {
                berserkTime = (float) elapsed / 1000.0f;
                this.entityData.set(DATA_BERSERK_TIME, berserkTime);
            }
        }
    }
    
    protected void enterBerserk() {
        isBerserk = true;
        berserkStartTime = System.currentTimeMillis();
        this.entityData.set(DATA_BERSERK, true);
        onEnterBerserk();
    }
    
    protected void exitBerserk() {
        isBerserk = false;
        berserkTime = 0;
        this.entityData.set(DATA_BERSERK, false);
        this.entityData.set(DATA_BERSERK_TIME, 0.0f);
        onExitBerserk();
    }
    
    protected void updatePhase() {
        float healthPercent = getHealth() / getMaxHealth();
        int newPhase = calculatePhase(healthPercent);
        
        if (newPhase != currentPhase && newPhase >= 1 && newPhase <= maxPhase) {
            setPhase(newPhase);
        }
    }
    
    protected int calculatePhase(float healthPercent) {
        for (int i = maxPhase; i >= 1; i--) {
            if (healthPercent <= getPhaseThreshold(i)) {
                return i;
            }
        }
        return 1;
    }
    
    protected void setPhase(int phase) {
        if (phase < 1 || phase > maxPhase) return;
        
        int oldPhase = currentPhase;
        currentPhase = phase;
        this.entityData.set(DATA_PHASE, phase);
        
        onPhaseChange(oldPhase, phase);
    }
    
    protected void updateState() {
        stateTimer++;
        
        BossState newState = determineNextState();
        if (newState != currentState) {
            changeState(newState);
        }
    }
    
    protected BossState determineNextState() {
        if (target == null || !target.isAlive()) {
            return BossState.IDLE;
        }
        
        double distanceToTarget = this.distanceTo(target);
        
        if (currentState == BossState.IDLE) {
            if (distanceToTarget <= getAggroRange()) {
                return BossState.CHASE;
            }
        } else if (currentState == BossState.CHASE) {
            if (distanceToTarget <= getAttackRange()) {
                return BossState.ATTACK;
            } else if (distanceToTarget > getAggroRange() * 2) {
                return BossState.IDLE;
            }
        } else if (currentState == BossState.ATTACK) {
            if (distanceToTarget > getAttackRange() * 1.5) {
                return BossState.CHASE;
            } else if (shouldUseSpecialAttack()) {
                return BossState.SPECIAL_ATTACK;
            }
        } else if (currentState == BossState.SPECIAL_ATTACK) {
            if (stateTimer >= getSpecialAttackDuration()) {
                return BossState.CHASE;
            }
        } else if (currentState == BossState.TELEPORT) {
            if (stateTimer >= getTeleportDuration()) {
                return BossState.ATTACK;
            }
        } else if (currentState == BossState.SUMMON) {
            if (stateTimer >= getSummonDuration()) {
                return BossState.CHASE;
            }
        }
        
        return currentState;
    }
    
    protected void changeState(BossState newState) {
        BossState oldState = currentState;
        currentState = newState;
        stateTimer = 0;
        this.entityData.set(DATA_CURRENT_STATE, newState.ordinal());
        
        onStateChange(oldState, newState);
    }
    
    protected void executeStateBehavior() {
        switch (currentState) {
            case IDLE:
                executeIdleBehavior();
                break;
            case CHASE:
                executeChaseBehavior();
                break;
            case ATTACK:
                executeAttackBehavior();
                break;
            case SPECIAL_ATTACK:
                executeSpecialAttackBehavior();
                break;
            case TELEPORT:
                executeTeleportBehavior();
                break;
            case SUMMON:
                executeSummonBehavior();
                break;
        }
    }
    
    protected void updateAttackCooldown() {
        if (attackCooldown > 0) {
            attackCooldown--;
        }
    }
    
    protected boolean canAttack() {
        return attackCooldown <= 0;
    }
    
    protected void resetAttackCooldown() {
        attackCooldown = attackInterval;
    }
    
    protected boolean shouldUseSpecialAttack() {
        if (isBerserk) {
            return Math.random() < 0.3f;
        }
        return Math.random() < getSpecialAttackChance();
    }
    
    protected void executeIdleBehavior() {
        if (target != null && target.isAlive()) {
            double distance = this.distanceTo(target);
            if (distance <= getAggroRange()) {
                changeState(BossState.CHASE);
            }
        }
    }
    
    protected void executeChaseBehavior() {
        if (target != null && target.isAlive()) {
            double speed = getChaseSpeed();
            if (isBerserk) {
                speed *= getBerserkSpeedMultiplier();
            }
            
            Vec3 direction = target.position().subtract(this.position()).normalize();
            this.setDeltaMovement(direction.scale(speed));
            
            //this.lookAt(target, 30.0f, 30.0f);
        }
    }
    
    protected void executeAttackBehavior() {
        if (target != null && target.isAlive() && canAttack()) {
            performAttack(target);
            resetAttackCooldown();
        }
    }
    
    protected void executeSpecialAttackBehavior() {
        if (stateTimer == 1) {
            performSpecialAttack();
        }
    }
    
    protected void executeTeleportBehavior() {
        if (stateTimer == 1) {
            performTeleport();
        }
    }
    
    protected void executeSummonBehavior() {
        if (stateTimer == 1) {
            performSummon();
        }
    }
    
    protected void performAttack(LivingEntity target) {
        float damage = getAttackDamage();
        if (isBerserk) {
            damage *= getBerserkDamageMultiplier();
        }
        
        target.hurt(this.damageSources().mobAttack(this), damage);
    }
    
    protected void performSpecialAttack() {
    }
    
    protected void performTeleport() {
    }
    
    protected void performSummon() {
    }
    
    protected void onEnterBerserk() {
        attackInterval = (int) (getDefaultAttackInterval() * 0.5f);
    }
    
    protected void onExitBerserk() {
        attackInterval = (int) getDefaultAttackInterval();
    }
    
    protected void onPhaseChange(int oldPhase, int newPhase) {
    }
    
    protected void onStateChange(BossState oldState, BossState newState) {
    }
    
    public static AttributeSupplier.Builder createBossAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 500.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
    }
    
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Phase", currentPhase);
        compound.putInt("MaxPhase", maxPhase);
        compound.putBoolean("Berserk", isBerserk);
        compound.putFloat("BerserkTime", berserkTime);
        compound.putInt("CurrentState", currentState.ordinal());
        compound.putInt("StateTimer", stateTimer);
        compound.putInt("AttackCooldown", attackCooldown);
    }
    
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        currentPhase = compound.getInt("Phase");
        maxPhase = compound.getInt("MaxPhase");
        isBerserk = compound.getBoolean("Berserk");
        berserkTime = compound.getFloat("BerserkTime");
        currentState = BossState.values()[compound.getInt("CurrentState")];
        stateTimer = compound.getInt("StateTimer");
        attackCooldown = compound.getInt("AttackCooldown");
        
        this.entityData.set(DATA_PHASE, currentPhase);
        this.entityData.set(DATA_MAX_PHASE, maxPhase);
        this.entityData.set(DATA_BERSERK, isBerserk);
        this.entityData.set(DATA_BERSERK_TIME, berserkTime);
        this.entityData.set(DATA_CURRENT_STATE, currentState.ordinal());
    }

    /**
    @Override
    public MobType getMobType() {
        return MobType.BOSS;
    }
    **/
    
    protected Object getStateData(String key) {
        return stateData.get(key);
    }
    
    protected void setStateData(String key, Object value) {
        stateData.put(key, value);
    }
    
    public int getCurrentPhase() {
        return currentPhase;
    }
    
    public int getMaxPhases() {
        return 3;
    }
    
    public boolean isBerserk() {
        return isBerserk;
    }
    
    public BossState getCurrentState() {
        return currentState;
    }
    
    public LivingEntity getTargetEntity() {
        return target;
    }
    
    protected float getPhaseThreshold(int phase) {
        return 1.0f - (phase * (1.0f / maxPhase));
    }
    
    protected float getBerserkThreshold() {
        return 0.3f;
    }
    
    protected long getBerserkDuration() {
        return 30000L;
    }
    
    protected float getBerserkSpeedMultiplier() {
        return 1.5f;
    }
    
    protected float getBerserkDamageMultiplier() {
        return 1.5f;
    }
    
    protected float getDefaultAttackInterval() {
        return 60;
    }
    
    protected float getAttackDamage() {
        return 10.0f;
    }
    
    protected float getChaseSpeed() {
        return 0.3f;
    }
    
    protected double getTargetRange() {
        return 64.0;
    }
    
    protected double getAggroRange() {
        return 32.0;
    }
    
    protected double getAttackRange() {
        return 4.0;
    }
    
    protected float getSpecialAttackChance() {
        return 0.1f;
    }
    
    protected int getSpecialAttackDuration() {
        return 60;
    }
    
    protected int getTeleportDuration() {
        return 40;
    }
    
    protected int getSummonDuration() {
        return 60;
    }
    
    public enum BossState {
        IDLE,
        CHASE,
        ATTACK,
        SPECIAL_ATTACK,
        TELEPORT,
        SUMMON
    }
}