package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;
import cn.dawnstring.fatality.utils.CooldownAbility;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class StoneOfEarthElementalSpirit extends AccessoryItem
{
    private static final String ABILITY_ID = "stone_of_earth_spirit_immunity";
    private static final long COOLDOWN_DURATION = 120000;

    public StoneOfEarthElementalSpirit(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getHealthRegenerationBonus() {
        return 0.15f;
    }

    @Override
    public float getDefenseBonus() {
        return 35.0f;
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
            if (hasAccessoryEquipped(player, StoneOfEarthElementalSpirit.class)) {
                if (!CooldownAbility.canTrigger(ABILITY_ID, player, COOLDOWN_DURATION)) {
                    return;
                }
                event.setCanceled(true);
                spawnImmunityParticles(player);
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
}
