package cn.dawnstring.fatality.system;

import cn.dawnstring.fatality.Fatality;
import cn.dawnstring.fatality.api.systems.IModSystem;
import cn.dawnstring.fatality.core.events.FatalityEventBus;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class LifeRingEffectManager implements IModSystem {

    private static final Logger LOGGER = Fatality.LOGGER;
    private static final LifeRingEffectManager INSTANCE = new LifeRingEffectManager();
    private static final Map<UUID, LifeRingEffect> activeEffects = new HashMap<>();

    public static LifeRingEffectManager getInstance() {
        return INSTANCE;
    }

    @Override
    public String getSystemId() {
        return "life_ring";
    }

    @Override
    public void initialize() {
        LOGGER.info("LifeRingEffect system initialized");
    }

    public static void startLifeRingEffect(Level level, Vec3 center, int duration, float radius, Player owner) {
        LifeRingEffect effect = new LifeRingEffect(level, center, duration, radius, owner);
        activeEffects.put(effect.getId(), effect);
    }

    public static void stopLifeRingEffect(UUID effectId) {
        activeEffects.remove(effectId);
    }

    @Override
    public void onServerTick() {
        Iterator<Map.Entry<UUID, LifeRingEffect>> iterator = activeEffects.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, LifeRingEffect> entry = iterator.next();
            LifeRingEffect effect = entry.getValue();
            if (!effect.update()) {
                iterator.remove();
            }
        }
    }

    private static class LifeRingEffect {
        private final UUID id;
        private final Level level;
        private final Vec3 center;
        private final float radius;
        private final Player owner;
        private int ticksRemaining;
        private int effectTicks;

        public LifeRingEffect(Level level, Vec3 center, int duration, float radius, Player owner) {
            this.id = UUID.randomUUID();
            this.level = level;
            this.center = center;
            this.radius = radius;
            this.owner = owner;
            this.ticksRemaining = duration;
            this.effectTicks = 0;
        }

        public UUID getId() { return id; }

        public boolean update() {
            ticksRemaining--;
            effectTicks++;

            if (ticksRemaining <= 0) {
                return false;
            }

            if (level.isClientSide()) return true;

            if (effectTicks % 20 == 0) {
                AABB area = new AABB(
                    center.x - radius, center.y - 2, center.z - radius,
                    center.x + radius, center.y + 2, center.z + radius
                );

                List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);
                for (LivingEntity entity : entities) {
                    if (entity.equals(owner)) {
                        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 1));
                        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 0));
                    } else if (!(entity instanceof Player)) {
                        entity.hurt(level.damageSources().magic(), 4.0f);
                    }
                }

                level.playSound(null, center.x, center.y, center.z,
                    SoundEvents.BEACON_AMBIENT, SoundSource.AMBIENT, 0.5f, 1.0f);
            }

            if (effectTicks % 5 == 0) {
                for (int i = 0; i < 3; i++) {
                    double angle = random().nextDouble() * Math.PI * 2;
                    double x = center.x + Math.cos(angle) * radius * random().nextDouble();
                    double z = center.z + Math.sin(angle) * radius * random().nextDouble();
                    level.addParticle(ParticleTypes.HEART, x, center.y, z, 0, 0.1, 0);
                }
            }

            return true;
        }

        private Random random() { return new Random(); }
    }
}
