package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;
import cn.dawnstring.fatality.utils.CooldownAbility;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CompassOfTheSoul extends AccessoryItem
{
    private static final String ABILITY_ID = "compass_of_soul_revival";
    private static final long COOLDOWN_DURATION = 300000;
    private static final float HEALTH_RESTORE_PERCENTAGE = 0.20f;

    public CompassOfTheSoul(Properties properties)
    {
        super(properties);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (hasAccessoryEquipped(player, CompassOfTheSoul.class)) {
                if (!CooldownAbility.canTrigger(ABILITY_ID, player, COOLDOWN_DURATION)) {
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

    private static void spawnRevivalParticles(Player player) {
        if (player.level().isClientSide()) {
            Vec3 playerPos = player.position();

            for (int i = 0; i < 30; i++) {
                double x = playerPos.x + (player.getRandom().nextDouble() - 0.5) * 3.0;
                double y = playerPos.y + player.getRandom().nextDouble() * 2.0;
                double z = playerPos.z + (player.getRandom().nextDouble() - 0.5) * 3.0;

                player.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        x, y, z, 0.0, 0.1, 0.0);
            }

            for (int i = 0; i < 20; i++) {
                double angle = (i / 20.0) * 2 * Math.PI;
                double radius = 1.5;
                double x = playerPos.x + Math.cos(angle) * radius;
                double y = playerPos.y + 1.0;
                double z = playerPos.z + Math.sin(angle) * radius;

                player.level().addParticle(ParticleTypes.SOUL,
                        x, y, z, 0.0, 0.05, 0.0);
            }
        }
    }
}
