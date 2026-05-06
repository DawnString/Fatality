package cn.dawnstring.fatality.system;

import cn.dawnstring.fatality.Fatality;
import cn.dawnstring.fatality.api.systems.IModSystem;
import cn.dawnstring.fatality.network.ManaSyncHandler;
import cn.dawnstring.fatality.system.accessories.AccessoryEffectHandlerManager;
import cn.dawnstring.fatality.utils.GameConstants;
import cn.dawnstring.fatality.utils.PlayerBaseAttributes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.Logger;

public class ManaRegenerationHandler implements IModSystem {

    private static final Logger LOGGER = Fatality.LOGGER;
    private static final ManaRegenerationHandler INSTANCE = new ManaRegenerationHandler();

    public static ManaRegenerationHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public String getSystemId() {
        return "mana_regen";
    }

    @Override
    public void initialize() {
        LOGGER.info("ManaRegeneration system initialized");
    }

    @Override
    public void onPlayerJoin(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            ManaSyncHandler.syncManaDataToClient(serverPlayer);
        }
    }

    @Override
    public void onPlayerTick(Player player) {
        if (player.level().isClientSide()) return;

        float oldMana = ManaSystem.getCurrentMana(player);
        ManaSystem.regenerateMana(player, GameConstants.TICK_INTERVAL);
        AccessoryEffectHandlerManager.getInstance().updateAccessoryEffects(player);

        float newMana = ManaSystem.getCurrentMana(player);
        if (oldMana != newMana && player instanceof ServerPlayer serverPlayer) {
            if (serverPlayer.tickCount % GameConstants.SYNC_INTERVAL_TICKS == 0) {
                ManaSyncHandler.syncManaDataToClient(serverPlayer);
            }
        }
    }

    public static float calculateActualManaRegenRate(Player player) {
        float baseRate = PlayerBaseAttributes.getBaseManaRegenRate(player);
        float accessoryBonus = AttributeSystem.getManaRegenerationRate(player) - baseRate;
        return baseRate + accessoryBonus;
    }
}
