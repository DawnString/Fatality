package cn.dawnstring.fatality.core.systems;

import cn.dawnstring.fatality.api.accessories.IAccessorySystem;
import cn.dawnstring.fatality.api.attributes.IAttributeSystem;
import cn.dawnstring.fatality.core.accessories.AccessorySystemImpl;
import cn.dawnstring.fatality.core.attributes.AttributeSystemImpl;
import net.minecraft.world.entity.player.Player;

/**
 * 系统管理器
 * 统一管理所有核心系统，提供便捷的访问接口
 */
public class SystemManager {
    
    private static final SystemManager INSTANCE = new SystemManager();
    
    private final IAttributeSystem attributeSystem;
    private final IAccessorySystem accessorySystem;
    
    private SystemManager() {
        this.attributeSystem = AttributeSystemImpl.getInstance();
        this.accessorySystem = AccessorySystemImpl.getInstance();
    }
    
    public static SystemManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * 获取属性系统
     */
    public IAttributeSystem getAttributeSystem() {
        return attributeSystem;
    }
    
    /**
     * 获取饰品系统
     */
    public IAccessorySystem getAccessorySystem() {
        return accessorySystem;
    }
    
    /**
     * 初始化系统
     */
    public void initialize() {
        // 注册默认的事件监听器
        registerDefaultEventListeners();
        
        // 初始化各个系统
        System.out.println("Fatality System Manager initialized successfully");
    }
    
    /**
     * 清理玩家数据
     */
    public void cleanupPlayerData(Player player) {
        if (attributeSystem instanceof AttributeSystemImpl) {
            ((AttributeSystemImpl) attributeSystem).cleanupPlayerData(player);
        }
        
        if (accessorySystem instanceof AccessorySystemImpl) {
            ((AccessorySystemImpl) accessorySystem).cleanupPlayerData(player);
        }
    }
    
    /**
     * 注册默认的事件监听器
     */
    private void registerDefaultEventListeners() {
        // 这里可以注册一些系统级别的事件监听器
        // 例如：玩家退出时清理数据、属性变化时更新UI等
    }
}