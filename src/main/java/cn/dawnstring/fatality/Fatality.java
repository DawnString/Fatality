package cn.dawnstring.fatality;

import cn.dawnstring.fatality.client.MainMenuReplacer;
import cn.dawnstring.fatality.config.FatalityConfig;
import cn.dawnstring.fatality.core.config.ConfigManager;
import cn.dawnstring.fatality.core.plugins.PluginManager;
import cn.dawnstring.fatality.core.systems.SystemRegistry;
import cn.dawnstring.fatality.events.GameEventManager;
import cn.dawnstring.fatality.events.GameEventCommand;
import cn.dawnstring.fatality.gamestage.GameStageCommand;
import cn.dawnstring.fatality.gamestage.GameStageManager;
import cn.dawnstring.fatality.network.NetworkManager;
import cn.dawnstring.fatality.registry.ModRegistry;
import cn.dawnstring.fatality.system.AccessorySystem;
import cn.dawnstring.fatality.system.AttributeSystem;
import cn.dawnstring.fatality.system.HealthRegenerationHandler;
import cn.dawnstring.fatality.system.LifeRingEffectManager;
import cn.dawnstring.fatality.system.ManaRegenerationHandler;
import cn.dawnstring.fatality.system.PlayerDataSystem;
import cn.dawnstring.fatality.modules.combat.CombatSystem;
import cn.dawnstring.fatality.modules.boss.BossSystem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Fatality.MODID)
public class Fatality {
    public static final String MODID = "fatality";
    public static final String VERSION = "1.0.0";

    public static final String DEV_ITEM_DES = "§d-开发者物品-";
    public static final String DOT_ITEM_DES = "§d-赞助者物品-";
    public static final String DEBUG_ITEM_DES = "§c-调试物品-";

    public static Logger LOGGER = LogManager.getLogger(MODID);

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);

    public static final RegistryObject<SoundEvent> MAIN_MENU_MUSIC = SOUND_EVENTS.register("main_menu_music",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "main_menu_music")));
    public static final RegistryObject<SoundEvent> END_OF_NIGHTMARE_FIGHT_MUSIC = SOUND_EVENTS.register("end_of_nightmare_fight_music",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "end_of_nightmare_fight_music")));

    public Fatality(FMLJavaModLoadingContext modLoadingContext)
    {
        IEventBus modEventBus = modLoadingContext.getModEventBus();

        FatalityConfig.register();

        initializeSystemRegistry();
        PluginManager.getInstance().initialize();

        ConfigManager.getInstance().initialize();
        ModRegistry.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.register(this);
        SOUND_EVENTS.register(modEventBus);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.register(MainMenuReplacer.class);
        }

        LOGGER.info("Fatality Mod initialized");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkManager::register);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MinecraftForge.EVENT_BUS.register(new cn.dawnstring.fatality.client.CustomHudRenderer());
            MinecraftForge.EVENT_BUS.register(new cn.dawnstring.fatality.client.DamageIndicatorRenderer());
        });
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        GameStageCommand.register(event.getDispatcher());
        GameEventCommand.register(event.getDispatcher());
    }

    private static void initializeSystemRegistry() {
        SystemRegistry.register(AttributeSystem.getInstance(), new String[]{}, 0);
        SystemRegistry.register(AccessorySystem.getInstance(), new String[]{"attribute"}, 1);

        SystemRegistry.register(HealthRegenerationHandler.getInstance(), new String[]{"attribute"}, 1);
        SystemRegistry.register(ManaRegenerationHandler.getInstance(), new String[]{"attribute"}, 1);
        SystemRegistry.register(LifeRingEffectManager.getInstance(), new String[]{}, 1);
        SystemRegistry.register(PlayerDataSystem.getInstance(), new String[]{}, 1);

        SystemRegistry.register(CombatSystem.getInstance(), new String[]{"attribute", "accessory"}, 2);
        SystemRegistry.register(BossSystem.getInstance(), new String[]{"attribute", "combat"}, 3);

        SystemRegistry.register(GameEventManager.getInstance(), new String[]{"game_stage"}, 2);
        SystemRegistry.register(GameStageManager.getInstance(), new String[]{}, 1);

        SystemRegistry.initializeAll();
    }
}
