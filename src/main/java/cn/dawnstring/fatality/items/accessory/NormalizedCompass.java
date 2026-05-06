package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;
import cn.dawnstring.fatality.utils.CooldownAbility;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class NormalizedCompass extends AccessoryItem
{
    private static final String IMMUNITY_ABILITY_ID = "normalized_compass_immunity";
    private static final long IMMUNITY_COOLDOWN = 90000;

    private static final String REVIVAL_ABILITY_ID = "normalized_compass_revival";
    private static final long REVIVAL_COOLDOWN = 300000;
    private static final float HEALTH_RESTORE_PERCENTAGE = 0.20f;

    public NormalizedCompass(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getHealthBonus()
    {
        return 100.0f;
    }

    @Override
    public float getDefenseBonus() {
        return 40.0f;
    }

    @Override
    public float getHealthRegenerationBonus() {
        return 0.25f;
    }

    @Override
    public float getCriticalChanceBonus() {
        return 0.15f;
    }

    @Override
    public void applyEffects(Player player, ItemStack stack) {
        super.applyEffects(player, stack);
        applyNegativeEffectImmunity(player);
    }

    @Override
    public void removeEffects(Player player, ItemStack stack) {
        removeNegativeEffectImmunity(player);
        super.removeEffects(player, stack);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (hasAccessoryEquipped(player, NormalizedCompass.class)) {
                if (!CooldownAbility.canTrigger(IMMUNITY_ABILITY_ID, player, IMMUNITY_COOLDOWN)) {
                    return;
                }
                event.setCanceled(true);
                spawnImmunityParticles(player);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (hasAccessoryEquipped(player, NormalizedCompass.class)) {
                if (!CooldownAbility.canTrigger(REVIVAL_ABILITY_ID, player, REVIVAL_COOLDOWN)) {
                    return;
                }
                event.setCanceled(true);

                float maxHealth = player.getMaxHealth();
                float restoreAmount = maxHealth * HEALTH_RESTORE_PERCENTAGE;
                float newHealth = Math.min(maxHealth, player.getHealth() + restoreAmount);
                player.setHealth(newHealth);

                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0f, 1.0f);

                spawnRevivalParticles(player);
            }
        }
    }

    private static void spawnImmunityParticles(Player player) {
        if (player.level().isClientSide()) {
            Vec3 pos = player.position();
            int particleCount = 16;
            double radius = 1.5;

            for (int i = 0; i < particleCount; i++) {
                double angle = 2 * Math.PI * i / particleCount;
                double x = pos.x + Math.cos(angle) * radius;
                double y = pos.y + 1.0;
                double z = pos.z + Math.sin(angle) * radius;
                double dx = Math.cos(angle) * 0.2;
                double dy = 0.1;
                double dz = Math.sin(angle) * 0.2;

                player.level().addParticle(ParticleTypes.FLAME,
                        x, y, z, dx, dy, dz);
            }
        }
    }

    private static void spawnRevivalParticles(Player player) {
        if (player.level().isClientSide()) {
            Vec3 playerPos = player.position();

            for (int i = 0; i < 30; i++) {
                double x = playerPos.x + (player.getRandom().nextDouble() - 0.5) * 3.0;
                double y = playerPos.y + player.getRandom().nextDouble() * 2.0;
                double z = playerPos.z + (player.getRandom().nextDouble() - 0.5) * 3.0;

                player.level().addParticle(ParticleTypes.FLAME,
                        x, y, z, 0.0, 0.1, 0.0);
            }

            for (int i = 0; i < 20; i++) {
                double angle = (i / 20.0) * 2 * Math.PI;
                double radius = 1.5;
                double x = playerPos.x + Math.cos(angle) * radius;
                double y = playerPos.y + 1.0;
                double z = playerPos.z + Math.sin(angle) * radius;

                player.level().addParticle(ParticleTypes.END_ROD,
                        x, y, z, 0.0, 0.05, 0.0);
            }
        }
    }
}
