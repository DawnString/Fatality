package cn.dawnstring.fatality.integration.forge;

import cn.dawnstring.fatality.core.systems.SystemManager;
import cn.dawnstring.fatality.modules.boss.BossSystem;
import cn.dawnstring.fatality.modules.combat.CombatSystem;
import cn.dawnstring.fatality.system.AccessorySystem;
import cn.dawnstring.fatality.system.AttributeSystem;
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
        
        // 初始化系统管理器
        SystemManager.getInstance().initialize();
        
        // 初始化各个模块
        BossSystem.getInstance().initialize();
        CombatSystem.getInstance().initialize();
        
        // 初始化饰品效果处理器管理器
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
        
        // 初始化饰品系统
        AccessorySystem.getInstance().initializePlayer(player);
        
        // 初始化属性系统
        AttributeSystem.getInstance().initializePlayer(player);
        
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
        
        // 清理饰品系统数据
        AccessorySystem.getInstance().cleanupPlayer(player);
        
        // 清理属性系统数据
        AttributeSystem.getInstance().cleanupPlayerData(player);
        
        // 清理饰品效果处理器数据
        AccessoryEffectHandlerManager.getInstance().cleanupPlayerData(player);
        
        // 清理系统管理器数据
        SystemManager.getInstance().cleanupPlayerData(player);
        
        System.out.println("Player logged out: " + player.getName().getString());
    }
    
    /**
     * 服务器tick事件处理
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            // 每tick执行一次的系统更新
            updateSystems();
        }
    }
    
    /**
     * 更新所有系统
     */
    private static void updateSystems() {
        // 这里可以添加需要每tick更新的系统逻辑
        // 例如：定时效果检查、状态更新等
    }
    
    /**
     * 世界加载事件处理
     */
    @SubscribeEvent
    public static void onWorldLoad(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        
        // 重新初始化饰品系统
        AccessorySystem.getInstance().initializePlayer(player);
        
        // 重新初始化属性系统
        AttributeSystem.getInstance().initializePlayer(player);
        
        // 处理玩家维度变化
        System.out.println("Player changed dimension: " + player.getName().getString());
    }
}