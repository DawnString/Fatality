package cn.dawnstring.fatality.api.systems;

import net.minecraft.world.entity.player.Player;

/**
 * 基础系统接口
 * 定义所有系统必须实现的基本功能
 */
public interface IModSystem {
    
    /**
     * 获取系统ID
     * @return 系统唯一标识符
     */
    String getSystemId();
    
    /**
     * 系统初始化
     * 在模组加载时调用
     */
    void initialize();
    
    /**
     * 系统关闭
     * 在模组卸载时调用
     */
    default void shutdown() {}
    
    /**
     * 玩家登录时调用
     * @param player 登录的玩家
     */
    default void onPlayerJoin(Player player) {}
    
    /**
     * 玩家退出时调用
     * @param player 退出的玩家
     */
    default void onPlayerLeave(Player player) {}
    
    /**
     * 服务器tick时调用
     */
    default void onServerTick() {}
    
    /**
     * 客户端tick时调用
     */
    default void onClientTick() {}
}