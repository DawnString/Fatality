package cn.dawnstring.fatality;

import cn.dawnstring.fatality.client.DamageIndicatorRenderer;
import cn.dawnstring.fatality.client.MainMenuReplacer;
import cn.dawnstring.fatality.registry.EntityAttributeRegistry;
import cn.dawnstring.fatality.system.ManaRegenerationHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import cn.dawnstring.fatality.gamestage.GameStageCommand;
import cn.dawnstring.fatality.events.GameEventCommand;
import cn.dawnstring.fatality.network.NetworkManager;
import cn.dawnstring.fatality.registry.ModRegistry;
import cn.dawnstring.fatality.client.CustomHudRenderer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(Fatality.MODID)
public class Fatality {
    public static final String MODID = "fatality";
    public static final String VERSION = "0.1b";

    public static final String DEV_ITEM_DES = "§d-开发者物品-";
    public static final String DOT_ITEM_DES = "§d-赞助者物品-";
    public static final String DEBUG_ITEM_DES = "§c-调试物品-";

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);

    // 注册主菜单背景音乐
    public static final RegistryObject<SoundEvent> MAIN_MENU_MUSIC = SOUND_EVENTS.register("main_menu_music",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "main_menu_music")));
    // 注册战斗曲
    public static final RegistryObject<SoundEvent> END_OF_NIGHTMARE_FIGHT_MUSIC = SOUND_EVENTS.register("end_of_nightmare_fight_music",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "end_of_nightmare_fight_music")));

    public Fatality(FMLJavaModLoadingContext modLoadingContext)
    {
        IEventBus modEventBus = modLoadingContext.getModEventBus();

        // 注册所有物品、容器等
        ModRegistry.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        // 注册到Forge事件总线
        MinecraftForge.EVENT_BUS.register(this);
        // 注册魔法值恢复处理器
        MinecraftForge.EVENT_BUS.register(ManaRegenerationHandler.class);

        // 注册声音事件
        SOUND_EVENTS.register(modEventBus);

        // 只在客户端环境中注册主菜单替换器
        if (FMLEnvironment.dist == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.register(MainMenuReplacer.class);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // 只在客户端环境中注册网络包
            if (FMLEnvironment.dist == Dist.CLIENT) {
                NetworkManager.register();
            }
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // 注册自定义HUD渲染器
            MinecraftForge.EVENT_BUS.register(new cn.dawnstring.fatality.client.CustomHudRenderer());

            // 注册伤害数值指示器渲染器
            MinecraftForge.EVENT_BUS.register(new cn.dawnstring.fatality.client.DamageIndicatorRenderer());
        });
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        GameStageCommand.register(event.getDispatcher());
        GameEventCommand.register(event.getDispatcher());
    }
}