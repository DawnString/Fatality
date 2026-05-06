package cn.dawnstring.fatality.entity.boss.lordofender;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Random;

public class EnderEnergyCrystal extends Entity implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final float MAX_HEALTH = 6000.0f;
    private static final int HEAL_INTERVAL = 20;
    private LordOfEnderEntity owner;
    private float health = MAX_HEALTH;
    private int age = 0;

    private final Random random = new Random();

    public EnderEnergyCrystal(EntityType<?> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;

        age++;
        if (health <= 0) {
            kill();
            return;
        }

        if (owner != null && owner.isAlive() && age % HEAL_INTERVAL == 0) {
            owner.heal(1.0f);
        }
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (source.is(net.minecraft.world.damagesource.DamageTypes.EXPLOSION)) {
            amount *= 2.0f;
        }
        health -= amount;
        spawnHitParticles();
        if (health <= 0) {
            spawnDestroyParticles();
            kill();
            if (owner != null) {
                owner.getActiveCrystals().remove(this);
            }
        }
        return true;
    }

    private void spawnHitParticles() {
        if (!(level() instanceof ServerLevel sl)) return;
        for (int i = 0; i < 6; i++) {
            sl.sendParticles(
                    new DustParticleOptions(new Vector3f(0.6f, 0.0f, 1.0f), 1.2f),
                    getX() + (random.nextDouble() - 0.5) * 1.5,
                    getY() + (random.nextDouble() - 0.5) * 1.5,
                    getZ() + (random.nextDouble() - 0.5) * 1.5,
                    1, 0, 0, 0, 0
            );
        }
        sl.sendParticles(ParticleTypes.END_ROD,
                getX(), getY() + 0.5, getZ(), 3,
                0.5, 0.5, 0.5, 0.02
        );
    }

    private void spawnDestroyParticles() {
        if (!(level() instanceof ServerLevel sl)) return;
        for (int i = 0; i < 20; i++) {
            sl.sendParticles(
                    new DustParticleOptions(new Vector3f(0.8f, 0.0f, 1.0f), 2.0f),
                    getX() + (random.nextDouble() - 0.5) * 3,
                    getY() + (random.nextDouble() - 0.5) * 3,
                    getZ() + (random.nextDouble() - 0.5) * 3,
                    1, 0, 0, 0, 0
            );
        }
        sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                getX(), getY() + 0.5, getZ(), 1, 0, 0, 0, 0
        );
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.fixed(1.5f, 1.5f);
    }

    public void setOwner(LordOfEnderEntity owner) {
        this.owner = owner;
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        health = tag.getFloat("CrystalHealth");
        if (tag.hasUUID("Owner")) {
            Entity e = ((net.minecraft.server.level.ServerLevel)level()).getEntity(tag.getUUID("Owner"));

            if (e instanceof LordOfEnderEntity loe) owner = loe;
        }
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        tag.putFloat("CrystalHealth", health);
        if (owner != null) tag.putUUID("Owner", owner.getUUID());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, event ->
                event.setAndContinue(RawAnimation.begin().thenPlay("idle"))));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
