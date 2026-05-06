package cn.dawnstring.fatality.system;

import cn.dawnstring.fatality.Fatality;
import cn.dawnstring.fatality.api.systems.IModSystem;
import cn.dawnstring.fatality.registry.ModEffects;
import cn.dawnstring.fatality.system.accessories.AccessoryEffectHandlerManager;
import cn.dawnstring.fatality.utils.GameConstants;
import cn.dawnstring.fatality.utils.PlayerBaseAttributes;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.Logger;

public class HealthRegenerationHandler implements IModSystem {

    private static final Logger LOGGER = Fatality.LOGGER;
    private static final HealthRegenerationHandler INSTANCE = new HealthRegenerationHandler();

    public static HealthRegenerationHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public String getSystemId() {
        return "health_regen";
    }

    @Override
    public void initialize() {
        LOGGER.info("HealthRegeneration system initialized");
    }

    @Override
    public void onPlayerTick(Player player) {
        if (player.level().isClientSide()) return;
        if (player.isDeadOrDying()) return;

        AccessoryEffectHandlerManager.getInstance().updateAccessoryEffects(player);
    }

    public static float calculateActualRegenRate(Player player) {
        float baseRate = PlayerBaseAttributes.getBaseHealthRegenRate(player);
        float accessoryBonus = AttributeSystem.getHealthRegenerationRate(player) - baseRate;
        float actualRegenRate = baseRate + accessoryBonus;

        if (player.hasEffect(ModEffects.TREATMENT_SATURATION.get())) {
            actualRegenRate *= GameConstants.TREATMENT_SATURATION_PENALTY;
        }

        return actualRegenRate;
    }
}
