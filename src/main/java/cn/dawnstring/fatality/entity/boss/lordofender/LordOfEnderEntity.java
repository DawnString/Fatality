package cn.dawnstring.fatality.entity.boss.lordofender;

import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LordOfEnderEntity extends Monster implements GeoEntity {

    private static final float MAX_MANA = 2000.0f;
    private static final float MANA_REGEN_PER_TICK = 5.0f;
    private static final float MANA_STUN_THRESHOLD = 10.0f;
    private static final float MANA_RESUME_THRESHOLD = 1600.0f;
    private static final int MAX_CRYSTALS = 8;
    private static final double TELEPORT_RANGE_MIN = 5.0;
    private static final double TELEPORT_RANGE_MAX = 20.0;
    private static final double TARGET_SEARCH_RANGE = 64.0;

    private static final int CRYSTAL_COST = 500;
    private static final int CRYSTAL_COOLDOWN = 200;
    private static final int SPEAR_COST = 200;
    private static final int SPEAR_COOLDOWN = 60;
    private static final int FIREBALL_COST = 150;
    private static final int FIREBALL_COOLDOWN = 40;
    private static final int ICE_PRISM_COST = 150;
    private static final int ICE_PRISM_COOLDOWN = 100;
    private static final int BUFF_COST = 400;
    private static final int BUFF_COOLDOWN = 600;
    private static final int ORB_COST = 100;
    private static final int ORB_COOLDOWN = 80;
    private static final int DEATH_RAY_COST = 100;
    private static final int DEATH_RAY_COOLDOWN = 300;
    private static final int TELEPORT_COST = 200;
    private static final int TELEPORT_COOLDOWN = 60;
    private static final int DEATH_RAY_BEAM_TICKS = 160;
    private static final int DEATH_RAY_CHARGE_TICKS = 60;
    private static final int BUFF_DURATION_TICKS = 300;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Random random = new Random();
    private boolean initialCrystalsSpawned = false;

    private float mana = MAX_MANA;
    private boolean isStunned = false;
    private boolean isChargingDeathRay = false;
    private boolean isUsingDeathRay = false;
    private int deathRayChargeTicks = 0;
    private int deathRayBeamTicks = 0;
    private boolean isBuffActive = false;
    private int buffTicks = 0;

    private int crystalCd = 0;
    private int spearCd = 0;
    private int fireballCd = 0;
    private int icePrismCd = 0;
    private int buffCd = 0;
    private int orbCd = 0;
    private int deathRayCd = 0;
    private int teleportCd = 0;
    private int skillPauseTicks = 0;

    private final List<EnderEnergyCrystal> activeCrystals = new ArrayList<>();
    private final ServerBossEvent bossEvent;

    public LordOfEnderEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        setNoGravity(true);
        this.bossEvent = new ServerBossEvent(
                Component.literal("§5§l末影领主"),
                BossEvent.BossBarColor.PURPLE,
                BossEvent.BossBarOverlay.PROGRESS
        );
        this.bossEvent.setCreateWorldFog(false);
        this.bossEvent.setDarkenScreen(false);
    }

    @Override
    public void travel(Vec3 vec3) {
        setDeltaMovement(Vec3.ZERO);
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 180000.0)
                .add(Attributes.ARMOR, 20.0)
                .add(Attributes.ARMOR_TOUGHNESS, 10.0)
                .add(Attributes.FOLLOW_RANGE, TARGET_SEARCH_RANGE)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (!level().isClientSide() && !initialCrystalsSpawned) {
            initialCrystalsSpawned = true;
            for (int i = 0; i < 2; i++) {
                spawnInitialCrystal();
            }
        }
    }

    @Override
    public void remove(net.minecraft.world.entity.Entity.RemovalReason reason) {
        if (!level().isClientSide()) {
            this.bossEvent.removeAllPlayers();
        }
        super.remove(reason);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    private void spawnInitialCrystal() {
        EnderEnergyCrystal crystal = ModEntities.ENDER_ENERGY_CRYSTAL.get().create(level());
        if (crystal != null) {
            double angle = random.nextDouble() * Math.PI * 2;
            double dist = 3.0;
            crystal.setPos(getX() + Math.cos(angle) * dist, getY() + 2.0, getZ() + Math.sin(angle) * dist);
            crystal.setOwner(this);
            level().addFreshEntity(crystal);
            activeCrystals.add(crystal);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;

        this.bossEvent.setProgress(Math.min(1.0f, getHealth() / getMaxHealth()));

        tickCooldowns();
        tickManaRegen();
        tickCrystals();
        tickBuff();
        tickDeathRayState();

        if (isChargingDeathRay || isUsingDeathRay) {
            LivingEntity target = getTarget();
            if (target != null) faceTarget(target);
            spawnDeathRayParticles(target);
            if (isUsingDeathRay && target != null) tickDeathRayDamage(target);
            return;
        }

        if (isStunned) {
            if (mana >= MANA_RESUME_THRESHOLD) isStunned = false;
            return;
        }

        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) return;

        if (skillPauseTicks > 0) {
            skillPauseTicks--;
            return;
        }

        faceTarget(target);
        tickStateMachine(target);
    }

    private void tickCooldowns() {
        if (crystalCd > 0) crystalCd--;
        if (spearCd > 0) spearCd--;
        if (fireballCd > 0) fireballCd--;
        if (icePrismCd > 0) icePrismCd--;
        if (buffCd > 0) buffCd--;
        if (orbCd > 0) orbCd--;
        if (deathRayCd > 0) deathRayCd--;
        if (teleportCd > 0) teleportCd--;
    }

    private void tickManaRegen() {
        mana = Math.min(MAX_MANA, mana + MANA_REGEN_PER_TICK);

        if (isUsingDeathRay) {
            mana -= 5.0f;
            if (mana <= MANA_STUN_THRESHOLD) {
                stopDeathRay();
                enterStun();
            }
        }

        if (mana <= MANA_STUN_THRESHOLD) {
            enterStun();
        }
    }

    private void enterStun() {
        isStunned = true;
        stopDeathRay();
    }

    private void tickCrystals() {
        activeCrystals.removeIf(c -> c == null || !c.isAlive());
    }

    private void tickBuff() {
        if (isBuffActive) {
            buffTicks--;
            if (buffTicks <= 0) isBuffActive = false;
        }
    }

    private void tickDeathRayState() {
        if (isChargingDeathRay) {
            deathRayChargeTicks--;
            if (deathRayChargeTicks <= 0) {
                isChargingDeathRay = false;
                isUsingDeathRay = true;
                deathRayBeamTicks = DEATH_RAY_BEAM_TICKS;
            }
        }
        if (isUsingDeathRay) {
            deathRayBeamTicks--;
            if (deathRayBeamTicks <= 0) stopDeathRay();
        }
    }

    private void stopDeathRay() {
        isChargingDeathRay = false;
        isUsingDeathRay = false;
    }

    private void tickDeathRayDamage(LivingEntity target) {
        AABB beamBox = getBoundingBox().inflate(64, 8, 64);
        for (Player p : level().getEntitiesOfClass(Player.class, beamBox)) {
            if (!p.isAlive() || p.isCreative()) continue;
            Vec3 toTarget = p.getEyePosition().subtract(getEyePosition()).normalize();
            Vec3 facing = target.getEyePosition().subtract(getEyePosition()).normalize();
            if (facing.dot(toTarget) > 0.95) {
                p.hurt(damageSources().magic(), 10.0f);
            }
        }
    }

    private void spawnDeathRayParticles(LivingEntity target) {
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 from = getEyePosition();
        Vec3 to = target != null ? target.getEyePosition() : from.add(0, 0, -10);
        Vec3 dir = to.subtract(from).normalize();
        double dist = Math.min(from.distanceTo(to), 64.0);

        if (isChargingDeathRay) {
            for (int i = 0; i < 3; i++) {
                double progress = random.nextDouble();
                Vec3 pos = from.add(dir.scale(progress * dist));
                sl.sendParticles(
                        new DustParticleOptions(new org.joml.Vector3f(0.5f, 0.0f, 1.0f), 1.5f),
                        pos.x, pos.y, pos.z, 1, 0.3, 0.3, 0.3, 0
                );
            }
        }

        if (isUsingDeathRay) {
            for (int i = 0; i < 5; i++) {
                double progress = i / 5.0;
                Vec3 pos = from.add(dir.scale(progress * dist));
                sl.sendParticles(ParticleTypes.DRAGON_BREATH,
                        pos.x, pos.y, pos.z, 1, 0.2, 0.2, 0.2, 0.02
                );
                sl.sendParticles(
                        new DustParticleOptions(new org.joml.Vector3f(0.8f, 0.0f, 1.0f), 2.0f),
                        pos.x, pos.y, pos.z, 1, 0.1, 0.1, 0.1, 0
                );
            }
            sl.sendParticles(ParticleTypes.END_ROD,
                    from.x, from.y, from.z, 2, 0.5, 0.5, 0.5, 0.02
            );
        }
    }

    private void faceTarget(LivingEntity target) {
        Vec3 diff = target.position().subtract(position()).normalize();
        float yaw = (float) (Math.atan2(diff.z, diff.x) * (180.0 / Math.PI)) - 90.0f;
        setYRot(yaw);
        yRotO = yaw;
        yHeadRot = yaw;
        yHeadRotO = yaw;
    }

    private void tickStateMachine(LivingEntity target) {
        int roll = random.nextInt(100);
        int cumulative = 0;

        if (isBuffActive && mana >= DEATH_RAY_COST && deathRayCd <= 0) {
            cumulative += 8;
            if (roll < cumulative) { startDeathRayCharge(); skillPauseTicks = 15; return; }
        }

        if (mana >= BUFF_COST && buffCd <= 0 && !isBuffActive) {
            cumulative += 10;
            if (roll < cumulative) { activateBuff(); skillPauseTicks = 20; return; }
        }

        if (mana >= CRYSTAL_COST && crystalCd <= 0 && activeCrystals.size() < MAX_CRYSTALS) {
            cumulative += 12;
            if (roll < cumulative) { summonCrystal(); skillPauseTicks = 10; return; }
        }

        if (mana >= TELEPORT_COST && teleportCd <= 0) {
            cumulative += 15;
            if (roll < cumulative) { teleportTo(target); skillPauseTicks = 15; return; }
        }

        if (mana >= ICE_PRISM_COST && icePrismCd <= 0) {
            cumulative += 12;
            if (roll < cumulative) { fireIcePrism(target); skillPauseTicks = 10; return; }
        }

        if (mana >= SPEAR_COST && spearCd <= 0) {
            cumulative += 15;
            if (roll < cumulative) { fireSpear(target); skillPauseTicks = 10; return; }
        }

        if (mana >= FIREBALL_COST && fireballCd <= 0) {
            cumulative += 15;
            if (roll < cumulative) { fireFireball(target); skillPauseTicks = 10; return; }
        }

        if (mana >= ORB_COST && orbCd <= 0) {
            cumulative += 13;
            if (roll < cumulative) { fireOrb(target); skillPauseTicks = 8; return; }
        }
    }

    private void summonCrystal() {
        mana -= CRYSTAL_COST;
        crystalCd = CRYSTAL_COOLDOWN;
        EnderEnergyCrystal crystal = ModEntities.ENDER_ENERGY_CRYSTAL.get().create(level());
        if (crystal != null) {
            Vec3 pos = position().add((random.nextDouble() - 0.5) * 12, 2 + random.nextDouble() * 4, (random.nextDouble() - 0.5) * 12);
            crystal.setPos(pos.x, pos.y, pos.z);
            crystal.setOwner(this);
            level().addFreshEntity(crystal);
            activeCrystals.add(crystal);
        }
    }

    private void fireSpear(LivingEntity target) {
        mana -= SPEAR_COST;
        spearCd = SPEAR_COOLDOWN;
        EnderSpearProjectile spear = ModEntities.ENDER_SPEAR.get().create(level());
        if (spear != null) {
            Vec3 pos = getEyePosition();
            Vec3 dir = target.getEyePosition().subtract(pos).normalize();
            spear.setPos(pos.x, pos.y, pos.z);
            spear.setOwner(this);
            spear.shoot(dir);
            level().addFreshEntity(spear);
        }
    }

    private void fireFireball(LivingEntity target) {
        mana -= FIREBALL_COST;
        fireballCd = FIREBALL_COOLDOWN;
        FireExplosionBall ball = ModEntities.FIRE_EXPLOSION_BALL.get().create(level());
        if (ball != null) {
            Vec3 pos = getEyePosition();
            Vec3 dir = target.position().subtract(pos).normalize();
            ball.setPos(pos.x, pos.y, pos.z);
            ball.setOwner(this);
            ball.shoot(dir);
            level().addFreshEntity(ball);
        }
    }

    private void fireIcePrism(LivingEntity target) {
        mana -= ICE_PRISM_COST;
        icePrismCd = ICE_PRISM_COOLDOWN;
        IcePrismCore core = ModEntities.ICE_PRISM_CORE.get().create(level());
        if (core != null) {
            Vec3 pos = getEyePosition();
            Vec3 dir = target.getEyePosition().subtract(pos).normalize();
            core.setPos(pos.x, pos.y, pos.z);
            core.setOwner(this);
            core.setTarget(target);
            core.shoot(dir);
            level().addFreshEntity(core);
        }
    }

    private void fireOrb(LivingEntity target) {
        mana -= ORB_COST;
        orbCd = ORB_COOLDOWN;
        EnderOrb orb = ModEntities.ENDER_ORB.get().create(level());
        if (orb != null) {
            Vec3 pos = getEyePosition();
            Vec3 dir = target.getEyePosition().subtract(pos).normalize();
            orb.setPos(pos.x, pos.y, pos.z);
            orb.setOwner(this);
            orb.setTarget(target);
            level().addFreshEntity(orb);
        }
    }

    private void activateBuff() {
        mana -= BUFF_COST;
        buffCd = BUFF_COOLDOWN;
        isBuffActive = true;
        buffTicks = BUFF_DURATION_TICKS;
    }

    private void startDeathRayCharge() {
        mana -= DEATH_RAY_COST;
        deathRayCd = DEATH_RAY_COOLDOWN;
        isChargingDeathRay = true;
        deathRayChargeTicks = DEATH_RAY_CHARGE_TICKS;
    }

    private void teleportTo(LivingEntity target) {
        mana -= TELEPORT_COST;
        teleportCd = TELEPORT_COOLDOWN;
        for (int i = 0; i < 16; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double dist = TELEPORT_RANGE_MIN + random.nextDouble() * (TELEPORT_RANGE_MAX - TELEPORT_RANGE_MIN);
            BlockPos pos = BlockPos.containing(target.getX() + Math.cos(angle) * dist, target.getY(), target.getZ() + Math.sin(angle) * dist);
            pos = findValidPosition(pos);
            if (pos != null) {
                teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                return;
            }
        }
    }

    private BlockPos findValidPosition(BlockPos pos) {
        for (int dy = 0; dy <= 4; dy++) {
            BlockPos c = pos.above(dy);
            if (level().getBlockState(c).isAir() && level().getBlockState(c.above()).isAir()) return c;
        }
        for (int dy = 0; dy <= 4; dy++) {
            BlockPos c = pos.below(dy);
            if (level().getBlockState(c).isAir() && level().getBlockState(c.above()).isAir()) return c;
        }
        return null;
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (isStunned) amount *= 1.15f;
        if (isUsingDeathRay) amount *= 0.6f;
        int crystalCount = activeCrystals.size();
        amount *= (1.0f - crystalCount * 0.05f);
        if (isBuffActive) amount *= 0.9f;
        return super.hurt(source, amount);
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 10, event ->
                event.setAndContinue(RawAnimation.begin().thenLoop("idle"))));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public float getDamageMultiplier() {
        float base = isBuffActive ? 1.25f : 1.0f;
        return base + activeCrystals.size() * 0.1f;
    }

    public boolean isStunned() { return isStunned; }
    public boolean isUsingDeathRay() { return isUsingDeathRay; }
    public boolean isChargingDeathRay() { return isChargingDeathRay; }
    public List<EnderEnergyCrystal> getActiveCrystals() { return activeCrystals; }
}
