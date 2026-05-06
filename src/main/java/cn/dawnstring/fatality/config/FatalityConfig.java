package cn.dawnstring.fatality.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = "fatality", bus = Mod.EventBusSubscriber.Bus.MOD)
public class FatalityConfig {

    private static final Logger LOGGER = LogManager.getLogger("FatalityConfig");
    private static final ForgeConfigSpec COMMON_SPEC;
    private static final ForgeConfigSpec CLIENT_SPEC;
    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

    public static ForgeConfigSpec.IntValue MAX_ACCESSORY_SLOTS;
    public static ForgeConfigSpec.BooleanValue ENABLE_ACCESSORY_EFFECT_STACKING;
    public static ForgeConfigSpec.IntValue ATTRIBUTE_CACHE_DURATION;
    public static ForgeConfigSpec.BooleanValue ENABLE_ATTRIBUTE_CACHING;
    public static ForgeConfigSpec.BooleanValue ENABLE_EVENT_LOGGING;

    static {
        COMMON_BUILDER.comment("Fatality Mod Common Configuration").push("common");

        MAX_ACCESSORY_SLOTS = COMMON_BUILDER
                .comment("Maximum accessory slots (min: 1, max: 18)")
                .defineInRange("maxAccessorySlots", 6, 1, 18);

        ENABLE_ACCESSORY_EFFECT_STACKING = COMMON_BUILDER
                .comment("Allow accessory effect stacking")
                .define("enableAccessoryEffectStacking", true);

        ENABLE_ATTRIBUTE_CACHING = COMMON_BUILDER
                .comment("Enable attribute value caching")
                .define("enableAttributeCaching", true);

        ATTRIBUTE_CACHE_DURATION = COMMON_BUILDER
                .comment("Attribute cache duration in ticks (min: 1)")
                .defineInRange("attributeCacheDuration", 20, 1, 200);

        ENABLE_EVENT_LOGGING = COMMON_BUILDER
                .comment("Enable event logging for debugging")
                .define("enableEventLogging", false);

        COMMON_BUILDER.pop();
        COMMON_SPEC = COMMON_BUILDER.build();

        CLIENT_BUILDER.comment("Fatality Mod Client Configuration").push("client");
        CLIENT_BUILDER.pop();
        CLIENT_SPEC = CLIENT_BUILDER.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }

    @SubscribeEvent
    public static void onConfigLoading(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == COMMON_SPEC) {
            LOGGER.info("Fatality common config loaded");
            LOGGER.info(getConfigSummary());
        }
    }

    @SubscribeEvent
    public static void onConfigReloading(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == COMMON_SPEC) {
            LOGGER.info("Fatality common config reloaded");
            LOGGER.info(getConfigSummary());
        }
    }

    public static String getConfigSummary() {
        return String.format("""
            === Fatality Configuration ===
            Max Accessory Slots: %d
            Accessory Effect Stacking: %s
            Attribute Caching: %s
            Cache Duration: %d ticks
            Event Logging: %s
            ==============================
            """,
            MAX_ACCESSORY_SLOTS.get(),
            ENABLE_ACCESSORY_EFFECT_STACKING.get() ? "ENABLED" : "DISABLED",
            ENABLE_ATTRIBUTE_CACHING.get() ? "ENABLED" : "DISABLED",
            ATTRIBUTE_CACHE_DURATION.get(),
            ENABLE_EVENT_LOGGING.get() ? "ENABLED" : "DISABLED"
        );
    }
}
