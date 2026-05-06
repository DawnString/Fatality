package cn.dawnstring.fatality.mixins;

import cn.dawnstring.fatality.entity.boss.enderdragon.DragonCombatMode;
import cn.dawnstring.fatality.entity.boss.enderdragon.DragonFlameBall;
import cn.dawnstring.fatality.registry.ModEffects;
import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(EnderDragon.class)
public class EnderDragonMixin {

    private static final float DASH_DAMAGE = 200.0f;
    private static final float BREATH_DAMAGE = 140.0f;
    private static final float FLAME_BALL_DAMAGE = 150.0f;
    private static final float PROTECTIVE_BALL_DAMAGE = 150.0f;

    private static final int DASH_DURATION_TICKS = 40;
    private static final int DASH_COOLDOWN_TICKS = 60;
    private static final int BREATH_MIN_TICKS = 100;
    private static final int BREATH_MAX_TICKS = 160;
    private static final int BREATH_COOLDOWN_TICKS = 200;
    private static final int FLAME_BALL_INTERVAL_TICKS = 60;
    private static final int PROTECTIVE_BALL_COOLDOWN_TICKS = 300;
    private static final int PROTECTIVE_BALL_LIFETIME = 300;
    private static final int FLAME_BALL_LIFETIME = 400;

    private static final double DASH_SPEED = 1.8;
    private static final double EVASIVE_ORBIT_SPEED = 0.6;
    private static final double EVASIVE_FLEE_SPEED = 1.0;
    private static final double MELEE_ORBIT_SPEED = 0.5;
    private static final double TARGET_SEARCH_RANGE = 128.0;
    private static final double ORBIT_RADIUS = 22.0;
    private static final double PREDICTION_FACTOR = 0.5;

    private DragonCombatMode combatMode = DragonCombatMode.EVASIVE;
    private int modeSwitchTimer = 0;
    private int modeSwitchInterval = 400;

    private int dashCooldown = 0;
    private int dashDuration = 0;
    private boolean isDashing = false;

    private int breathCooldown = 0;
    private int breathDuration = 0;
    private boolean isBreathing = false;

    private int flameBallTimer = 0;
    private int protectiveBallCooldown = 0;

    private List<DragonFlameBall> protectiveBalls = new ArrayList<>();
    private double orbitAngle = 0;

    private boolean hasAiOverride = false;

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void onAiStep(CallbackInfo ci) {
        EnderDragon dragon = (EnderDragon)(Object)this;

        if (dragon.level().isClientSide()) {
            return;
        }

        LivingEntity target = resolveTarget(dragon);
        if (target == null || !target.isAlive()) {
            hasAiOverride = false;
            return;
        }

        hasAiOverride = true;

        tickCooldowns();
        tickModeSwitch();
        tickDash(dragon);
        tickBreath(dragon);
        tickProtectiveBallsCleanup();

        if (isBreathing) {
            tickBreathAttack(dragon, target);
        }

        if (isDashing) {
            tickDashMovement(dragon, target);
        }

        switch (combatMode) {
            case EVASIVE -> tickEvasiveMode(dragon, target);
            case MELEE -> tickMeleeMode(dragon, target);
        }

        handleCollisionDamage(dragon, target);
    }

    @Inject(method = "aiStep", at = @At("RETURN"))
    private void onAiStepReturn(CallbackInfo ci) {
        if (!hasAiOverride) return;

        EnderDragon dragon = (EnderDragon)(Object)this;
        if (dragon.level().isClientSide()) return;

        LivingEntity target = resolveTarget(dragon);
        if (target == null) return;

        if (isBreathing) {
            dragon.setDeltaMovement(Vec3.ZERO);
            faceTarget(dragon, target.getEyePosition());
            return;
        }

        if (isDashing) {
            Vec3 direction = target.position().subtract(dragon.position()).normalize();
            dragon.setDeltaMovement(direction.scale(DASH_SPEED));
            faceTarget(dragon, target.position());
            return;
        }

        double distance = dragon.distanceTo(target);

        switch (combatMode) {
            case EVASIVE -> {
                if (distance < 20.0) {
                    Vec3 dir = dragon.position().subtract(target.position()).add(0, 0.2, 0).normalize();
                    dragon.setDeltaMovement(dir.scale(EVASIVE_FLEE_SPEED));
                } else {
                    orbitAngle += 0.03;
                    Vec3 toTarget = target.position().subtract(dragon.position());
                    double horizontalDist = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
                    Vec3 orbitDir = new Vec3(
                            -Math.sin(orbitAngle) * toTarget.z / horizontalDist - Math.cos(orbitAngle) * toTarget.x / horizontalDist,
                            0.1,
                            Math.sin(orbitAngle) * toTarget.x / horizontalDist - Math.cos(orbitAngle) * toTarget.z / horizontalDist
                    ).normalize();
                    dragon.setDeltaMovement(orbitDir.scale(EVASIVE_ORBIT_SPEED));
                }
                faceTarget(dragon, target.getEyePosition());
            }
            case MELEE -> {
                if (distance > 40.0 && !isDashing) {
                    Vec3 dir = target.position().subtract(dragon.position()).normalize();
                    dragon.setDeltaMovement(dir.scale(MELEE_ORBIT_SPEED * 1.5));
                } else if (distance > 15.0) {
                    orbitAngle += 0.02;
                    Vec3 toTarget = target.position().subtract(dragon.position());
                    double horizontalDist = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
                    Vec3 orbitDir;
                    double angleFromTarget = Math.atan2(toTarget.z, toTarget.x);
                    double desiredAngle = angleFromTarget + Math.PI / 2;
                    orbitDir = new Vec3(
                            Math.cos(desiredAngle) * ORBIT_RADIUS / Math.max(1, horizontalDist) - toTarget.x / Math.max(1, horizontalDist),
                            0.05,
                            Math.sin(desiredAngle) * ORBIT_RADIUS / Math.max(1, horizontalDist) - toTarget.z / Math.max(1, horizontalDist)
                    ).normalize();
                    dragon.setDeltaMovement(orbitDir.scale(MELEE_ORBIT_SPEED));
                } else {
                    dragon.setDeltaMovement(Vec3.ZERO);
                }
                faceTarget(dragon, target.position());
            }
        }
    }

    private LivingEntity resolveTarget(EnderDragon dragon) {
        LivingEntity target = dragon.getTarget();
        if (target != null && target.isAlive()) {
            return target;
        }

        Player nearest = dragon.level().getNearestPlayer(dragon, TARGET_SEARCH_RANGE);
        if (nearest != null && nearest.isAlive()) {
            dragon.setTarget(nearest);
            return nearest;
        }

        return null;
    }

    private void tickModeSwitch() {
        modeSwitchTimer++;
        if (modeSwitchTimer >= modeSwitchInterval) {
            modeSwitchTimer = 0;
            modeSwitchInterval = 300 + dragonRandom().nextInt(201);
            combatMode = combatMode == DragonCombatMode.EVASIVE ? DragonCombatMode.MELEE : DragonCombatMode.EVASIVE;
        }
    }

    private void tickCooldowns() {
        if (dashCooldown > 0) dashCooldown--;
        if (breathCooldown > 0) breathCooldown--;
        flameBallTimer++;
        if (protectiveBallCooldown > 0) protectiveBallCooldown--;
    }

    private void tickDash(EnderDragon dragon) {
        if (isDashing) {
            dashDuration--;
            if (dashDuration <= 0) {
                isDashing = false;
            }
        }
    }

    private void tickBreath(EnderDragon dragon) {
        if (isBreathing) {
            breathDuration--;
            if (breathDuration <= 0) {
                isBreathing = false;
            }
        }
    }

    private void tickProtectiveBallsCleanup() {
        protectiveBalls.removeIf(ball -> ball == null || !ball.isAlive());
    }

    private void tickEvasiveMode(EnderDragon dragon, LivingEntity target) {
        if (flameBallTimer >= FLAME_BALL_INTERVAL_TICKS) {
            flameBallTimer = 0;
            useFlameBallAttack(dragon, target);
        }
    }

    private void tickMeleeMode(EnderDragon dragon, LivingEntity target) {
        double distance = dragon.distanceTo(target);

        if (distance > 40.0 && dashCooldown <= 0 && !isDashing) {
            startDash(dragon, target);
        }

        if (distance < 15.0 && dashCooldown <= 0 && !isDashing && dragonRandom().nextDouble() < 0.7) {
            startDash(dragon, target);
        }

        if (breathCooldown <= 0 && distance < 25.0 && !isDashing) {
            startBreathAttack(dragon, target);
        }

        if (protectiveBallCooldown <= 0 && !isDashing && dragonRandom().nextDouble() < 0.5) {
            spawnProtectiveBalls(dragon);
        }
    }

    private Vec3 predictTargetPosition(LivingEntity target) {
        Vec3 pos = target.position();
        Vec3 delta = target.getDeltaMovement();
        return pos.add(delta.scale(PREDICTION_FACTOR * 20));
    }

    private void startDash(EnderDragon dragon, LivingEntity target) {
        if (dashCooldown > 0 || isDashing) return;

        isDashing = true;
        dashDuration = DASH_DURATION_TICKS;
        dashCooldown = DASH_COOLDOWN_TICKS;

        Vec3 direction = target.position().subtract(dragon.position()).normalize();
        dragon.setDeltaMovement(direction.scale(DASH_SPEED));
        faceTarget(dragon, target.position());
    }

    private void tickDashMovement(EnderDragon dragon, LivingEntity target) {
        Vec3 direction = target.position().subtract(dragon.position()).normalize();
        dragon.setDeltaMovement(direction.scale(DASH_SPEED));
        faceTarget(dragon, target.position());
    }

    private void faceTarget(EnderDragon dragon, Vec3 targetPos) {
        Vec3 dragonPos = dragon.position();
        double dx = targetPos.x - dragonPos.x;
        double dz = targetPos.z - dragonPos.z;
        float yaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) + 90.0f;

        dragon.setYRot(yaw);
        dragon.yRotO = yaw;
        dragon.yHeadRot = yaw;
        dragon.yHeadRotO = yaw;

        double dy = targetPos.y - (dragonPos.y + 2.0);
        float pitch = (float) (Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * (180.0 / Math.PI));
        dragon.setXRot(pitch);
        dragon.xRotO = pitch;
    }

    private void startBreathAttack(EnderDragon dragon, LivingEntity target) {
        isBreathing = true;
        breathDuration = BREATH_MIN_TICKS + dragonRandom().nextInt(BREATH_MAX_TICKS - BREATH_MIN_TICKS + 1);
        breathCooldown = BREATH_COOLDOWN_TICKS;
        dragon.setDeltaMovement(Vec3.ZERO);
        faceTarget(dragon, target.getEyePosition());
    }

    private void tickBreathAttack(EnderDragon dragon, LivingEntity target) {
        Vec3 startPos = dragon.getEyePosition();
        Vec3 direction = target.getEyePosition().subtract(startPos).normalize();
        double range = 20.0;

        AABB breathArea = new AABB(
                startPos.x - range, startPos.y - 6, startPos.z - range,
                startPos.x + range, startPos.y + 6, startPos.z + range
        );

        List<LivingEntity> entities = dragon.level().getEntitiesOfClass(LivingEntity.class, breathArea);
        for (LivingEntity entity : entities) {
            if (entity == dragon) continue;
            if (entity instanceof Player player && player.isCreative()) continue;

            Vec3 toEntity = entity.position().subtract(startPos).normalize();
            double dotProduct = direction.dot(toEntity);

            if (dotProduct > 0.6) {
                entity.hurt(dragon.damageSources().mobAttack(dragon), BREATH_DAMAGE);
                entity.addEffect(new MobEffectInstance(ModEffects.DRAGONFIRE_BURN.get(), 100, 1));
            }
        }

        spawnBreathParticles(dragon, startPos, direction, range);
    }

    private void spawnBreathParticles(EnderDragon dragon, Vec3 startPos, Vec3 direction, double range) {
        for (int i = 0; i < 15; i++) {
            double progress = i / 15.0;
            Vec3 basePos = startPos.add(direction.scale(progress * range));
            double spread = 0.8;

            Vec3 particlePos = basePos.add(
                    (dragonRandom().nextDouble() - 0.5) * spread,
                    (dragonRandom().nextDouble() - 0.5) * spread,
                    (dragonRandom().nextDouble() - 0.5) * spread
            );

            Vec3 velocity = new Vec3(
                    direction.x * 0.2 + (dragonRandom().nextDouble() - 0.5) * 0.1,
                    direction.y * 0.2 + (dragonRandom().nextDouble() - 0.5) * 0.1,
                    direction.z * 0.2 + (dragonRandom().nextDouble() - 0.5) * 0.1
            );

            if (dragon.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        new DustParticleOptions(new Vector3f(0.8f, 0.2f, 1.0f), 1.5f),
                        particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0
                );
                serverLevel.sendParticles(
                        ParticleTypes.DRAGON_BREATH,
                        particlePos.x, particlePos.y, particlePos.z, 1,
                        velocity.x * 0.5, velocity.y * 0.5, velocity.z * 0.5, 0.1
                );
            } else {
                dragon.level().addParticle(
                        new DustParticleOptions(new Vector3f(0.8f, 0.2f, 1.0f), 1.5f),
                        particlePos.x, particlePos.y, particlePos.z, velocity.x, velocity.y, velocity.z
                );
                dragon.level().addParticle(
                        ParticleTypes.DRAGON_BREATH,
                        particlePos.x, particlePos.y, particlePos.z, velocity.x, velocity.y, velocity.z
                );
            }
        }
    }

    private void useFlameBallAttack(EnderDragon dragon, LivingEntity target) {
        Vec3 predictedPos = predictTargetPosition(target);

        for (int i = 0; i < 4; i++) {
            DragonFlameBall ball = ModEntities.DRAGON_FLAME_BALL.get().create(dragon.level());
            if (ball != null) {
                ball.setOwner(dragon);
                ball.setTarget(target);
                ball.setDamage(FLAME_BALL_DAMAGE);
                ball.setProtective(false);
                ball.setLifetime(FLAME_BALL_LIFETIME);

                double angle = i * (2.0 * Math.PI / 4.0);
                double radius = 14.0;
                double x = dragon.getX() + Math.cos(angle) * radius;
                double y = dragon.getY() + 1.0 + Math.sin(angle * 2) * 0.5;
                double z = dragon.getZ() + Math.sin(angle) * radius;

                ball.setPos(x, y, z);

                Vec3 direction = predictedPos.subtract(ball.position()).normalize();
                ball.setDeltaMovement(direction.scale(2.0));

                dragon.level().addFreshEntity(ball);
            }
        }
    }

    private void spawnProtectiveBalls(EnderDragon dragon) {
        protectiveBallCooldown = PROTECTIVE_BALL_COOLDOWN_TICKS;

        for (DragonFlameBall ball : protectiveBalls) {
            if (ball != null && ball.isAlive()) {
                ball.discard();
            }
        }
        protectiveBalls.clear();

        for (int i = 0; i < 3; i++) {
            DragonFlameBall ball = ModEntities.DRAGON_FLAME_BALL.get().create(dragon.level());
            if (ball != null) {
                ball.setOwner(dragon);
                ball.setDamage(PROTECTIVE_BALL_DAMAGE);
                ball.setProtective(true);
                ball.setLifetime(PROTECTIVE_BALL_LIFETIME);

                double angle = i * (2.0 * Math.PI / 3.0);
                double radius = 3.0;
                ball.setPos(
                        dragon.getX() + Math.cos(angle) * radius,
                        dragon.getY() + 2.0,
                        dragon.getZ() + Math.sin(angle) * radius
                );

                dragon.level().addFreshEntity(ball);
                protectiveBalls.add(ball);
            }
        }
    }

    private void handleCollisionDamage(EnderDragon dragon, LivingEntity target) {
        if (isDashing) {
            double distance = dragon.distanceTo(target);
            if (distance < 5.0) {
                target.hurt(dragon.damageSources().mobAttack(dragon), DASH_DAMAGE);
            }
        }
    }

    private java.util.Random dragonRandom() {
        return new java.util.Random(((EnderDragon)(Object)this).getRandom().nextLong());
    }
}
