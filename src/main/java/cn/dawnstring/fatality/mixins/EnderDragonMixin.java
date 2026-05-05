package cn.dawnstring.fatality.mixins;

import cn.dawnstring.fatality.entity.boss.enderdragon.DragonCombatMode;
import cn.dawnstring.fatality.entity.boss.enderdragon.DragonFlameBall;
import cn.dawnstring.fatality.registry.ModEffects;
import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mixin(EnderDragon.class)
public class EnderDragonMixin {

    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("7f101d4e-8f4b-4c9b-8b1c-1a2b3c4d5e6f");
    private static final AttributeModifier SPEED_BOOST = new AttributeModifier(
            SPEED_MODIFIER_UUID,
            "Dragon dash speed boost",
            0.4,
            AttributeModifier.Operation.MULTIPLY_BASE
    );

    private static final float MAX_HEALTH = 115200.0f;
    private static final float DASH_DAMAGE = 200.0f;
    private static final float BREATH_DAMAGE = 140.0f;
    private static final float FLAME_BALL_DAMAGE = 150.0f;
    private static final float PROTECTIVE_BALL_DAMAGE = 150.0f;

    private DragonCombatMode combatMode = DragonCombatMode.EVASIVE;
    private int modeSwitchTimer = 0;
    private int modeSwitchInterval = 400;

    private int dashCooldown = 0;
    private int dashDuration = 0;
    private boolean isDashing = false;

    private int breathCooldown = 0;
    private int breathDuration = 0;
    private boolean isBreathing = false;

    private int flameBallCooldown = 0;
    private int protectiveBallCooldown = 0;

    private List<DragonFlameBall> protectiveBalls = new ArrayList<>();

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void onAiStep(CallbackInfo ci) {
        EnderDragon dragon = (EnderDragon)(Object)this;

        if (dragon.level().isClientSide()) {
            return;
        }

        LivingEntity target = dragon.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        updateCombatMode();
        updateDash(dragon);
        updateBreath(dragon);
        updateCooldowns();
        updateProtectiveBalls();

        switch (combatMode) {
            case EVASIVE:
                executeEvasiveAI(dragon, target);
                break;
            case MELEE:
                executeMeleeAI(dragon, target);
                break;
        }

        handleCollisionDamage(dragon, target);
    }

    private void updateCombatMode() {
        modeSwitchTimer++;

        if (modeSwitchTimer >= modeSwitchInterval) {
            modeSwitchTimer = 0;
            modeSwitchInterval = 300 + (int)(Math.random() * 100);

            combatMode = combatMode == DragonCombatMode.EVASIVE ? DragonCombatMode.MELEE : DragonCombatMode.EVASIVE;
        }
    }

    private void updateDash(EnderDragon dragon) {
        if (isDashing) {
            dashDuration--;

            if (dashDuration <= 0) {
                isDashing = false;
                dragon.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPEED_MODIFIER_UUID);
            }
        }

        if (dashCooldown > 0) {
            dashCooldown--;
        }
    }

    private void startDash(EnderDragon dragon) {
        if (dashCooldown <= 0 && !isDashing) {
            isDashing = true;
            dashDuration = 60;
            dashCooldown = 200;

            dragon.getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(SPEED_BOOST);
        }
    }

    private void updateBreath(EnderDragon dragon) {
        if (isBreathing) {
            breathDuration--;

            if (breathDuration <= 0) {
                isBreathing = false;
            }
        }

        if (breathCooldown > 0) {
            breathCooldown--;
        }
    }

    private void updateCooldowns() {
        if (flameBallCooldown > 0) {
            flameBallCooldown--;
        }

        if (protectiveBallCooldown > 0) {
            protectiveBallCooldown--;
        }
    }

    private void updateProtectiveBalls() {
        protectiveBalls.removeIf(ball -> ball == null || ball.isRemoved());
    }

    private void executeEvasiveAI(EnderDragon dragon, LivingEntity target) {
        double distance = dragon.distanceTo(target);

        if (distance < 20.0) {
            moveAwayFromTarget(dragon, target);
        } else if (distance > 30.0) {
            moveTowardsTarget(dragon, target);
        }

        if (breathCooldown <= 0 && Math.random() < 0.05) {
            useBreathAttack(dragon, target);
        }

        if (flameBallCooldown <= 0 && Math.random() < 0.1) {
            useFlameBallAttack(dragon, target);
        }
    }

    private void executeMeleeAI(EnderDragon dragon, LivingEntity target) {
        double distance = dragon.distanceTo(target);

        if (distance > 40.0 && dashCooldown <= 0) {
            startDash(dragon);
        }

        if (distance < 15.0 && dashCooldown <= 0 && Math.random() < 0.7) {
            startDash(dragon);
        }

        if (protectiveBallCooldown <= 0 && Math.random() < 0.5) {
            spawnProtectiveBalls(dragon);
        }

        if (distance < 10.0) {
            moveTowardsTarget(dragon, target);
        } else if (distance > 30.0 && !isDashing) {
            moveTowardsTarget(dragon, target);
        }
    }

    private void moveTowardsTarget(EnderDragon dragon, LivingEntity target) {
        Vec3 direction = target.position().subtract(dragon.position()).normalize();
        double speed = isDashing ? 1.5 : 0.8;
        dragon.setDeltaMovement(direction.scale(speed));
    }

    private void moveAwayFromTarget(EnderDragon dragon, LivingEntity target) {
        Vec3 direction = dragon.position().subtract(target.position()).normalize();
        double speed = 0.6;
        dragon.setDeltaMovement(direction.scale(speed));
    }

    private void useBreathAttack(EnderDragon dragon, LivingEntity target) {
        isBreathing = true;
        breathDuration = 40;
        breathCooldown = 120;

        Vec3 startPos = dragon.getEyePosition();
        Vec3 direction = target.getEyePosition().subtract(startPos).normalize();
        double range = 15.0;

        AABB breathArea = new AABB(
                startPos.x - range, startPos.y - 2, startPos.z - range,
                startPos.x + range, startPos.y + 2, startPos.z + range
        );

        List<LivingEntity> entities = dragon.level().getEntitiesOfClass(LivingEntity.class, breathArea);
        for (LivingEntity entity : entities) {
            if (entity == dragon) continue;
            if (entity instanceof Player player && player.isCreative()) continue;

            Vec3 toEntity = entity.position().subtract(startPos).normalize();
            double dotProduct = direction.dot(toEntity);

            if (dotProduct > 0.5) {
                entity.hurt(dragon.damageSources().mobAttack(dragon), BREATH_DAMAGE);
                entity.addEffect(new MobEffectInstance(ModEffects.DRAGONFIRE_BURN.get(), 100, 1));
            }
        }

        spawnBreathParticles(dragon, startPos, direction, range);
    }

    private void spawnBreathParticles(EnderDragon dragon, Vec3 startPos, Vec3 direction, double range) {
        for (int i = 0; i < 20; i++) {
            double progress = i / 20.0;
            Vec3 particlePos = startPos.add(direction.scale(progress * range));

            dragon.level().addParticle(ParticleTypes.DRAGON_BREATH,
                    particlePos.x, particlePos.y, particlePos.z,
                    direction.x * 0.1, direction.y * 0.1, direction.z * 0.1
            );
        }
    }

    private void useFlameBallAttack(EnderDragon dragon, LivingEntity target) {
        flameBallCooldown = 200;

        for (int i = 0; i < 4; i++) {
            DragonFlameBall ball = ModEntities.DRAGON_FLAME_BALL.get().create(dragon.level());
            if (ball != null) {
                ball.setOwner(dragon);
                ball.setTarget(target);
                ball.setDamage(FLAME_BALL_DAMAGE);
                ball.setProtective(false);
                ball.setLifetime(400);

                Vec3 offset = new Vec3(
                        (Math.random() - 0.5) * 4,
                        (Math.random() - 0.5) * 2,
                        (Math.random() - 0.5) * 4
                );
                ball.setPos(dragon.getX() + offset.x, dragon.getY() + offset.y, dragon.getZ() + offset.z);

                dragon.level().addFreshEntity(ball);
            }
        }
    }

    private void spawnProtectiveBalls(EnderDragon dragon) {
        protectiveBallCooldown = 600;

        clearProtectiveBalls();

        for (int i = 0; i < 3; i++) {
            DragonFlameBall ball = ModEntities.DRAGON_FLAME_BALL.get().create(dragon.level());
            if (ball != null) {
                ball.setOwner(dragon);
                ball.setDamage(PROTECTIVE_BALL_DAMAGE);
                ball.setProtective(true);
                ball.setLifetime(300);

                double angle = (i * 2.0 * Math.PI / 3.0);
                double radius = 3.0;
                double x = dragon.getX() + Math.cos(angle) * radius;
                double y = dragon.getY() + 2.0;
                double z = dragon.getZ() + Math.sin(angle) * radius;

                ball.setPos(x, y, z);

                dragon.level().addFreshEntity(ball);
                protectiveBalls.add(ball);
            }
        }
    }

    private void clearProtectiveBalls() {
        for (DragonFlameBall ball : protectiveBalls) {
            if (ball != null && !ball.isRemoved()) {
                ball.discard();
            }
        }
        protectiveBalls.clear();
    }

    private void handleCollisionDamage(EnderDragon dragon, LivingEntity target) {
        if (isDashing) {
            double distance = dragon.distanceTo(target);
            if (distance < 5.0) {
                target.hurt(dragon.damageSources().mobAttack(dragon), DASH_DAMAGE);
            }
        }
    }
}