package cn.dawnstring.fatality.integration.forge;

import cn.dawnstring.fatality.Fatality;
import cn.dawnstring.fatality.core.systems.SystemRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = Fatality.MODID)
public class ForgeIntegration {

    private static final Logger LOGGER = Fatality.LOGGER;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            SystemRegistry.onPlayerTick(event.player);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            SystemRegistry.onServerTick();
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            SystemRegistry.onClientTick();
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        SystemRegistry.onPlayerJoin(player);
        LOGGER.info("Player logged in: {}", player.getName().getString());
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        SystemRegistry.onPlayerLeave(player);
        LOGGER.info("Player logged out: {}", player.getName().getString());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        SystemRegistry.onPlayerJoin(player);
        LOGGER.info("Player changed dimension: {}", player.getName().getString());
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity killed = event.getEntity();
        if (killed.level().isClientSide()) return;

        if (event.getSource().getEntity() instanceof Player) {
            SystemRegistry.onLivingDeath(killed);
        }
    }

    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            SystemRegistry.onWorldLoad(serverLevel);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        SystemRegistry.onPlayerRespawn(player);
        LOGGER.info("Player respawned: {}", player.getName().getString());
    }
}
