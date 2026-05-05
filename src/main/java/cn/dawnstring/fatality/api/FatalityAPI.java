package cn.dawnstring.fatality.api;

import cn.dawnstring.fatality.api.accessories.IAccessorySystem;
import cn.dawnstring.fatality.api.attributes.IAttributeSystem;
import cn.dawnstring.fatality.api.plugins.IPlugin;
import cn.dawnstring.fatality.api.systems.IModSystem;
import cn.dawnstring.fatality.core.plugins.PluginManager;
import cn.dawnstring.fatality.core.systems.SystemRegistry;
import cn.dawnstring.fatality.system.AccessorySystem;
import cn.dawnstring.fatality.system.AttributeSystem;

/**
 * Fatality API
 * 提供统一的API接口供附属模组使用
 */
public class FatalityAPI {
    
    private static final String VERSION = "1.0.0";
    
    /**
     * 获取API版本
     * @return 版本字符串
     */
    public static String getVersion() {
        return VERSION;
    }
    
    /**
     * 获取属性系统
     * @return 属性系统实例
     */
    public static IAttributeSystem getAttributeSystem() {
        return AttributeSystem.getInstance();
    }
    
    /**
     * 获取饰品系统
     * @return 饰品系统实例
     */
    public static IAccessorySystem getAccessorySystem() {
        return AccessorySystem.getInstance();
    }
    
    /**
     * 获取系统
     * @param systemClass 系统类型
     * @return 系统实例
     */
    public static <T extends IModSystem> T getSystem(Class<T> systemClass) {
        return SystemRegistry.getSystem(systemClass);
    }
    
    /**
     * 获取系统
     * @param systemId 系统ID
     * @return 系统实例
     */
    public static IModSystem getSystem(String systemId) {
        return SystemRegistry.getSystem(systemId);
    }
    
    /**
     * 注册插件
     * @param plugin 插件实例
     */
    public static void registerPlugin(IPlugin plugin) {
        PluginManager.getInstance().registerPlugin(plugin);
    }
    
    /**
     * 获取插件
     * @param pluginId 插件ID
     * @return 插件实例
     */
    public static IPlugin getPlugin(String pluginId) {
        return PluginManager.getInstance().getPlugin(pluginId);
    }
    
    /**
     * 检查插件是否已启用
     * @param pluginId 插件ID
     * @return 是否已启用
     */
    public static boolean isPluginEnabled(String pluginId) {
        return PluginManager.getInstance().isPluginEnabled(pluginId);
    }
    
    /**
     * 检查系统是否已注册
     * @param systemId 系统ID
     * @return 是否已注册
     */
    public static boolean hasSystem(String systemId) {
        return SystemRegistry.hasSystem(systemId);
    }
    
    /**
     * 获取所有已注册的系统ID
     * @return 系统ID集合
     */
    public static java.util.Set<String> getSystemIds() {
        return SystemRegistry.getSystemIds();
    }
    
    /**
     * 获取所有已注册的插件ID
     * @return 插件ID集合
     */
    public static java.util.Set<String> getPluginIds() {
        return PluginManager.getInstance().getPluginIds();
    }
}