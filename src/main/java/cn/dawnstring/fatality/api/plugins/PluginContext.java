package cn.dawnstring.fatality.api.plugins;

import cn.dawnstring.fatality.api.systems.IModSystem;
import cn.dawnstring.fatality.core.plugins.PluginManager;

/**
 * 插件上下文
 * 提供插件与Fatality系统交互的接口
 */
public class PluginContext {
    
    private final String pluginId;
    private final PluginManager pluginManager;
    
    public PluginContext(String pluginId, PluginManager pluginManager) {
        this.pluginId = pluginId;
        this.pluginManager = pluginManager;
    }
    
    /**
     * 获取插件ID
     * @return 插件ID
     */
    public String getPluginId() {
        return pluginId;
    }
    
    /**
     * 获取Fatality系统
     * @param systemClass 系统类型
     * @return 系统实例
     */
    public <T extends IModSystem> T getSystem(Class<T> systemClass) {
        return pluginManager.getSystem(systemClass);
    }
    
    /**
     * 获取Fatality系统
     * @param systemId 系统ID
     * @return 系统实例
     */
    public IModSystem getSystem(String systemId) {
        return pluginManager.getSystem(systemId);
    }
    
    /**
     * 注册事件监听器
     * @param eventClass 事件类型
     * @param listener 监听器对象
     * @param <T> 事件类型
     */
    public <T> void registerEventListener(Class<T> eventClass, Object listener) {
        pluginManager.registerEventListener(pluginId, eventClass, listener);
    }
    
    /**
     * 注册事件监听器（函数式）
     * @param eventClass 事件类型
     * @param consumer 事件处理器
     * @param <T> 事件类型
     */
    public <T> void registerEventListener(Class<T> eventClass, java.util.function.Consumer<T> consumer) {
        pluginManager.registerEventListener(pluginId, eventClass, consumer);
    }
    
    /**
     * 获取插件数据目录
     * @return 数据目录路径
     */
    public String getDataDirectory() {
        return pluginManager.getPluginDataDirectory(pluginId);
    }
    
    /**
     * 获取插件配置目录
     * @return 配置目录路径
     */
    public String getConfigDirectory() {
        return pluginManager.getPluginConfigDirectory(pluginId);
    }
    
    /**
     * 记录插件日志
     * @param message 日志消息
     */
    public void logInfo(String message) {
        pluginManager.logPluginInfo(pluginId, message);
    }
    
    /**
     * 记录插件警告
     * @param message 警告消息
     */
    public void logWarning(String message) {
        pluginManager.logPluginWarning(pluginId, message);
    }
    
    /**
     * 记录插件错误
     * @param message 错误消息
     */
    public void logError(String message) {
        pluginManager.logPluginError(pluginId, message);
    }
    
    /**
     * 记录插件错误
     * @param message 错误消息
     * @param throwable 异常
     */
    public void logError(String message, Throwable throwable) {
        pluginManager.logPluginError(pluginId, message, throwable);
    }
}