package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;
import cn.dawnstring.fatality.utils.CooldownAbility;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class EclipseMirror extends AccessoryItem
{
    private static final String ABILITY_ID = "eclipse_mirror_reduction";
    private static final long COOLDOWN_DURATION = 60000;
    private static final float DAMAGE_REDUCTION_PERCENTAGE = 0.60f;

    public EclipseMirror(Properties properties)
    {
        super(properties);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (hasAccessoryEquipped(player, EclipseMirror.class)) {
                if (!CooldownAbility.canTrigger(ABILITY_ID, player, COOLDOWN_DURATION)) {
                    return;
                }
                float originalDamage = event.getAmount();
                float reducedDamage = originalDamage * (1 - DAMAGE_REDUCTION_PERCENTAGE);
                event.setAmount(reducedDamage);
                spawnDamageReductionParticles(player);
            }
        }
    }

    private static void spawnDamageReductionParticles(Player player) {
        if (player.level().isClientSide()) {
            Vec3 pos = player.position();
            int particleCount = 24;
            double radius = 1.8;

            for (int i = 0; i < particleCount; i++) {
                double angle = 2 * Math.PI * i / particleCount;
                double x = pos.x + Math.cos(angle) * radius;
                double y = pos.y + 1.2;
                double z = pos.z + Math.sin(angle) * radius;
                double dx = Math.cos(angle) * 0.15;
                double dy = 0.08;
                double dz = Math.sin(angle) * 0.15;

                player.level().addParticle(ParticleTypes.SMOKE,
                        x, y, z, dx, dy, dz);
            }

            for (int i = 0; i < 12; i++) {
                double x = pos.x + (player.getRandom().nextDouble() - 0.5) * 2.5;
                double y = pos.y + player.getRandom().nextDouble() * 1.8;
                double z = pos.z + (player.getRandom().nextDouble() - 0.5) * 2.5;

                player.level().addParticle(ParticleTypes.LARGE_SMOKE,
                        x, y, z, 0.0, 0.06, 0.0);
            }

            for (int i = 0; i < 8; i++) {
                double x = pos.x + (player.getRandom().nextDouble() - 0.5) * 1.5;
                double y = pos.y + player.getRandom().nextDouble() * 1.0;
                double z = pos.z + (player.getRandom().nextDouble() - 0.5) * 1.5;

                player.level().addParticle(ParticleTypes.SQUID_INK,
                        x, y, z, 0.0, 0.04, 0.0);
            }
        }
    }
}
