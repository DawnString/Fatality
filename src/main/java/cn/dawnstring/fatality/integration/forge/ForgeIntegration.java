package cn.dawnstring.fatality.integration.forge;

import cn.dawnstring.fatality.core.systems.SystemRegistry;
import cn.dawnstring.fatality.system.accessories.AccessoryEffectHandlerManager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Forge集成模块 - 迁移到新的事件驱动架构
 * 将新的架构系统集成到Forge事件系统中
 */
@Mod.EventBusSubscriber
public class ForgeIntegration {
    
    private static boolean initialized = false;
    
    /**
     * 初始化所有系统
     */
    public static void initialize() {
        if (initialized) {
            return;
        }

        AccessoryEffectHandlerManager.getInstance();
        
        initialized = true;
        System.out.println("Fatality Forge Integration initialized successfully");
    }
    
    /**
     * 玩家登录事件处理
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!initialized) {
            initialize();
        }
        
        Player player = event.getEntity();
        
        // 通知所有系统玩家登录
        SystemRegistry.onPlayerJoin(player);
        
        // 初始化饰品效果处理器
        AccessoryEffectHandlerManager.getInstance().initializePlayer(player);
        
        System.out.println("Player logged in: " + player.getName().getString());
    }
    
    /**
     * 玩家退出事件处理
     */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        
        // 通知所有系统玩家退出
        SystemRegistry.onPlayerLeave(player);
        
        // 清理饰品效果处理器数据
        AccessoryEffectHandlerManager.getInstance().cleanupPlayerData(player);
        
        System.out.println("Player logged out: " + player.getName().getString());
    }
    
    /**
     * 服务器tick事件处理
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            // 通知所有系统服务器tick
            SystemRegistry.onServerTick();
        }
    }
    
    /**
     * 世界加载事件处理
     */
    @SubscribeEvent
    public static void onWorldLoad(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        
        // 重新初始化玩家数据
        SystemRegistry.onPlayerJoin(player);
        
        // 处理玩家维度变化
        System.out.println("Player changed dimension: " + player.getName().getString());
    }
}