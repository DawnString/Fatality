package cn.dawnstring.fatality;

import cn.dawnstring.fatality.client.MainMenuReplacer;
import cn.dawnstring.fatality.config.ArchitectureConfig;
import cn.dawnstring.fatality.core.config.ConfigManager;
import cn.dawnstring.fatality.core.plugins.PluginManager;
import cn.dawnstring.fatality.core.systems.SystemRegistry;
import cn.dawnstring.fatality.integration.MigrationHelper;
import cn.dawnstring.fatality.integration.forge.ForgeIntegration;
import cn.dawnstring.fatality.system.LifeRingEffectManager;
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

    public static Logger LOGGER =  LogManager.getLogger(MODID);

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

        // 初始化新架构配置
        initializeNewArchitecture();

        // 初始化配置管理器
        ConfigManager.getInstance().initialize();

        // 注册所有物品、容器等
        ModRegistry.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        // 注册到Forge事件总线
        MinecraftForge.EVENT_BUS.register(this);
        
        // 注册魔法值恢复处理器（已迁移到新架构）
        MinecraftForge.EVENT_BUS.register(ManaRegenerationHandler.class);
        
        // 注册生命之环效果管理器（已迁移到新架构）
        MinecraftForge.EVENT_BUS.register(LifeRingEffectManager.class);

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

    /**
     * 初始化新架构系统
     */
    private void initializeNewArchitecture() {
        // 验证配置有效性
        ArchitectureConfig.validateConfig();
        
        // 打印配置摘要
        System.out.println(ArchitectureConfig.getConfigSummary());
        
        // 如果启用新架构，初始化相关系统
        if (ArchitectureConfig.ENABLE_EVENT_DRIVEN_ARCHITECTURE) {
            // 初始化系统注册表
            initializeSystemRegistry();
            
            // 初始化插件管理器
            initializePluginManager();
            
            // 初始化Forge集成
            ForgeIntegration.initialize();
            
            // 打印迁移状态
            MigrationHelper.printMigrationStatus();
        }
        
        System.out.println("Fatality Mod initialized with new architecture");
    }
    
    /**
     * 初始化系统注册表
     */
    private void initializeSystemRegistry() {
        // 注册核心系统
        SystemRegistry.register(cn.dawnstring.fatality.system.AttributeSystem.getInstance(), new String[]{}, 0);
        SystemRegistry.register(cn.dawnstring.fatality.system.AccessorySystem.getInstance(), new String[]{"attribute"}, 1);
        
        // 注册模块系统
        SystemRegistry.register(cn.dawnstring.fatality.modules.combat.CombatSystem.getInstance(), new String[]{"attribute", "accessory"}, 2);
        SystemRegistry.register(cn.dawnstring.fatality.modules.boss.BossSystem.getInstance(), new String[]{"attribute", "combat"}, 3);
        
        // 初始化所有系统
        SystemRegistry.initializeAll();
    }
    
    /**
     * 初始化插件管理器
     */
    private void initializePluginManager() {
        PluginManager.getInstance().initialize();
    }
}