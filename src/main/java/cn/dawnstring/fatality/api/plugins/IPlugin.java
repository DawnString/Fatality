package cn.dawnstring.fatality.api.plugins;

import cn.dawnstring.fatality.api.systems.IModSystem;

/**
 * 插件接口
 * 允许附属模组扩展Fatality的功能
 */
public interface IPlugin {
    
    /**
     * 获取插件ID
     * @return 插件唯一标识符
     */
    String getPluginId();
    
    /**
     * 获取插件名称
     * @return 插件显示名称
     */
    default String getPluginName() {
        return getPluginId();
    }
    
    /**
     * 获取插件版本
     * @return 版本字符串
     */
    default String getVersion() {
        return "1.0.0";
    }
    
    /**
     * 获取依赖的插件ID数组
     * @return 依赖的插件ID
     */
    default String[] getDependencies() {
        return new String[0];
    }
    
    /**
     * 插件加载时调用
     * @param context 插件上下文
     */
    void onLoad(PluginContext context);
    
    /**
     * 插件启用时调用
     */
    void onEnable();
    
    /**
     * 插件禁用时调用
     */
    void onDisable();
    
    /**
     * 插件重载时调用
     */
    default void onReload() {}
    
    /**
     * 检查插件是否已启用
     * @return 是否已启用
     */
    default boolean isEnabled() {
        return PluginManager.getInstance().isPluginEnabled(getPluginId());
    }
    
    /**
     * 获取Fatality系统
     * @param systemClass 系统类型
     * @return 系统实例
     */
    default <T extends IModSystem> T getSystem(Class<T> systemClass) {
        return PluginManager.getInstance().getSystem(systemClass);
    }
    
    /**
     * 获取Fatality系统
     * @param systemId 系统ID
     * @return 系统实例
     */
    default IModSystem getSystem(String systemId) {
        return PluginManager.getInstance().getSystem(systemId);
    }
}