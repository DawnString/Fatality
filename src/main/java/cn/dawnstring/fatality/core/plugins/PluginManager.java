package cn.dawnstring.fatality.core.plugins;

import cn.dawnstring.fatality.Fatality;
import cn.dawnstring.fatality.api.plugins.IPlugin;
import cn.dawnstring.fatality.api.plugins.PluginContext;
import cn.dawnstring.fatality.api.systems.IModSystem;
import cn.dawnstring.fatality.core.events.FatalityEventBus;
import cn.dawnstring.fatality.core.events.UnifiedEventBus;
import cn.dawnstring.fatality.core.systems.SystemRegistry;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 插件管理器
 * 管理所有插件的加载、启用、禁用和生命周期
 */
public class PluginManager {
    
    private static final PluginManager INSTANCE = new PluginManager();
    
    private final Map<String, IPlugin> plugins = new ConcurrentHashMap<>();
    private final Map<String, PluginContext> contexts = new ConcurrentHashMap<>();
    private final Map<String, Boolean> enabledPlugins = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<Object>> eventListeners = new ConcurrentHashMap<>();
    
    private boolean initialized = false;
    
    private PluginManager() {
        // 私有构造函数
    }
    
    public static PluginManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * 初始化插件管理器
     */
    public void initialize() {
        if (initialized) {
            return;
        }
        
        Fatality.LOGGER.info("Plugin Manager initialized");
        initialized = true;
    }
    
    /**
     * 注册插件
     * @param plugin 要注册的插件
     */
    public void registerPlugin(IPlugin plugin) {
        String pluginId = plugin.getPluginId();
        
        if (plugins.containsKey(pluginId)) {
            throw new IllegalArgumentException("Plugin already registered: " + pluginId);
        }
        
        // 检查依赖
        String[] dependencies = plugin.getDependencies();
        for (String dep : dependencies) {
            if (!plugins.containsKey(dep)) {
                throw new IllegalStateException("Missing dependency: " + dep + " for plugin: " + pluginId);
            }
        }
        
        plugins.put(pluginId, plugin);
        enabledPlugins.put(pluginId, false);
        
        Fatality.LOGGER.info("Plugin registered: {} v{}", pluginId, plugin.getVersion());
    }
    
    /**
     * 加载所有插件
     */
    public void loadAllPlugins() {
        Fatality.LOGGER.info("Loading {} plugins...", plugins.size());
        
        for (IPlugin plugin : plugins.values()) {
            loadPlugin(plugin);
        }
        
        Fatality.LOGGER.info("All plugins loaded");
    }
    
    /**
     * 加载插件
     * @param plugin 要加载的插件
     */
    private void loadPlugin(IPlugin plugin) {
        String pluginId = plugin.getPluginId();
        
        try {
            PluginContext context = new PluginContext(pluginId, this);
            contexts.put(pluginId, context);
            
            plugin.onLoad(context);
            Fatality.LOGGER.info("Plugin loaded: {}", pluginId);
        } catch (Exception e) {
            Fatality.LOGGER.error("Failed to load plugin: " + pluginId, e);
        }
    }
    
    /**
     * 启用所有插件
     */
    public void enableAllPlugins() {
        Fatality.LOGGER.info("Enabling plugins...");
        
        for (IPlugin plugin : plugins.values()) {
            enablePlugin(plugin.getPluginId());
        }
        
        Fatality.LOGGER.info("All plugins enabled");
    }
    
    /**
     * 启用插件
     * @param pluginId 插件ID
     */
    public void enablePlugin(String pluginId) {
        IPlugin plugin = plugins.get(pluginId);
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin not found: " + pluginId);
        }
        
        if (enabledPlugins.get(pluginId)) {
            Fatality.LOGGER.warn("Plugin already enabled: {}", pluginId);
            return;
        }
        
        try {
            plugin.onEnable();
            enabledPlugins.put(pluginId, true);
            Fatality.LOGGER.info("Plugin enabled: {}", pluginId);
        } catch (Exception e) {
            Fatality.LOGGER.error("Failed to enable plugin: " + pluginId, e);
        }
    }
    
    /**
     * 禁用插件
     * @param pluginId 插件ID
     */
    public void disablePlugin(String pluginId) {
        IPlugin plugin = plugins.get(pluginId);
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin not found: " + pluginId);
        }
        
        if (!enabledPlugins.get(pluginId)) {
            Fatality.LOGGER.warn("Plugin already disabled: {}", pluginId);
            return;
        }
        
        try {
            plugin.onDisable();
            enabledPlugins.put(pluginId, false);
            Fatality.LOGGER.info("Plugin disabled: {}", pluginId);
        } catch (Exception e) {
            Fatality.LOGGER.error("Failed to disable plugin: " + pluginId, e);
        }
    }
    
    /**
     * 重载插件
     * @param pluginId 插件ID
     */
    public void reloadPlugin(String pluginId) {
        IPlugin plugin = plugins.get(pluginId);
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin not found: " + pluginId);
        }
        
        try {
            plugin.onReload();
            Fatality.LOGGER.info("Plugin reloaded: {}", pluginId);
        } catch (Exception e) {
            Fatality.LOGGER.error("Failed to reload plugin: " + pluginId, e);
        }
    }
    
    /**
     * 获取插件
     * @param pluginId 插件ID
     * @return 插件实例
     */
    public IPlugin getPlugin(String pluginId) {
        return plugins.get(pluginId);
    }
    
    /**
     * 检查插件是否已启用
     * @param pluginId 插件ID
     * @return 是否已启用
     */
    public boolean isPluginEnabled(String pluginId) {
        return enabledPlugins.getOrDefault(pluginId, false);
    }
    
    /**
     * 获取所有插件ID
     * @return 插件ID集合
     */
    public Set<String> getPluginIds() {
        return Collections.unmodifiableSet(plugins.keySet());
    }
    
    /**
     * 获取Fatality系统
     * @param systemClass 系统类型
     * @return 系统实例
     */
    public <T extends IModSystem> T getSystem(Class<T> systemClass) {
        return SystemRegistry.getSystem(systemClass);
    }
    
    /**
     * 获取Fatality系统
     * @param systemId 系统ID
     * @return 系统实例
     */
    public IModSystem getSystem(String systemId) {
        return SystemRegistry.getSystem(systemId);
    }
    
    /**
     * 注册事件监听器
     * @param pluginId 插件ID
     * @param eventClass 事件类型
     * @param listener 监听器对象
     * @param <T> 事件类型
     */
    public <T> void registerEventListener(String pluginId, Class<T> eventClass, Object listener) {
        List<Object> listeners = eventListeners.computeIfAbsent(eventClass, k -> new ArrayList<>());
        listeners.add(listener);
        
        // 使用UnifiedEventBus支持Object类型的监听器
        UnifiedEventBus.getInstance().registerListener(eventClass, listener);
        Fatality.LOGGER.debug("Event listener registered for plugin: {}", pluginId);
    }
    
    /**
     * 注册事件监听器（函数式）
     * @param pluginId 插件ID
     * @param eventClass 事件类型
     * @param consumer 事件处理器
     * @param <T> 事件类型
     */
    public <T> void registerEventListener(String pluginId, Class<T> eventClass, java.util.function.Consumer<T> consumer) {
        List<Object> listeners = eventListeners.computeIfAbsent(eventClass, k -> new ArrayList<>());
        listeners.add(consumer);
        
        // 使用UnifiedEventBus支持Consumer类型的监听器
        UnifiedEventBus.getInstance().registerListener(eventClass, consumer);
        Fatality.LOGGER.debug("Event listener registered for plugin: {}", pluginId);
    }
    
    /**
     * 获取插件数据目录
     * @param pluginId 插件ID
     * @return 数据目录路径
     */
    public String getPluginDataDirectory(String pluginId) {
        File dataDir = new File("fatality/plugins/" + pluginId + "/data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        return dataDir.getAbsolutePath();
    }
    
    /**
     * 获取插件配置目录
     * @param pluginId 插件ID
     * @return 配置目录路径
     */
    public String getPluginConfigDirectory(String pluginId) {
        File configDir = new File("fatality/plugins/" + pluginId + "/config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return configDir.getAbsolutePath();
    }
    
    /**
     * 记录插件信息
     * @param pluginId 插件ID
     * @param message 消息
     */
    public void logPluginInfo(String pluginId, String message) {
        Fatality.LOGGER.info("[{}] {}", pluginId, message);
    }
    
    /**
     * 记录插件警告
     * @param pluginId 插件ID
     * @param message 消息
     */
    public void logPluginWarning(String pluginId, String message) {
        Fatality.LOGGER.warn("[{}] {}", pluginId, message);
    }
    
    /**
     * 记录插件错误
     * @param pluginId 插件ID
     * @param message 消息
     */
    public void logPluginError(String pluginId, String message) {
        Fatality.LOGGER.error("[{}] {}", pluginId, message);
    }
    
    /**
     * 记录插件错误
     * @param pluginId 插件ID
     * @param message 消息
     * @param throwable 异常
     */
    public void logPluginError(String pluginId, String message, Throwable throwable) {
        Fatality.LOGGER.error("[{}] {}", pluginId, message, throwable);
    }
    
    /**
     * 关闭插件管理器
     */
    public void shutdown() {
        Fatality.LOGGER.info("Shutting down Plugin Manager...");
        
        // 禁用所有插件
        for (String pluginId : plugins.keySet()) {
            if (enabledPlugins.get(pluginId)) {
                disablePlugin(pluginId);
            }
        }
        
        // 清理事件监听器
        eventListeners.clear();
        
        // 清理插件
        plugins.clear();
        contexts.clear();
        enabledPlugins.clear();
        
        initialized = false;
        Fatality.LOGGER.info("Plugin Manager shutdown complete");
    }
}