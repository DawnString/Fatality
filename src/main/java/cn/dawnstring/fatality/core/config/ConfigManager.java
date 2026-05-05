package cn.dawnstring.fatality.core.config;

import cn.dawnstring.fatality.Fatality;
import net.minecraftforge.common.ForgeConfigSpec;

/**
 * 配置管理器
 * 统一管理所有模组配置
 */
public class ConfigManager {
    
    private static final ConfigManager INSTANCE = new ConfigManager();
    
    private ForgeConfigSpec.Builder builder;
    private ForgeConfigSpec spec;
    
    private ConfigManager() {
        // 私有构造函数
    }
    
    public static ConfigManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * 初始化配置
     */
    public void initialize() {
        builder = new ForgeConfigSpec.Builder();
        
        builder.comment("Fatality Mod Configuration");
        builder.push("general");
        
        registerGeneralConfig();
        
        builder.pop();
        builder.push("accessories");
        
        registerAccessoryConfig();
        
        builder.pop();
        builder.push("attributes");
        
        registerAttributeConfig();
        
        builder.pop();
        builder.push("combat");
        
        registerCombatConfig();
        
        builder.pop();
        
        spec = builder.build();
        
        Fatality.LOGGER.info("Configuration initialized");
    }
    
    /**
     * 注册通用配置
     */
    private void registerGeneralConfig() {
        builder.comment("General mod settings");
        
        builder.comment("Enable debug mode");
        builder.define("debugMode", false);
        
        builder.comment("Enable plugin system");
        builder.define("enablePlugins", true);
        
        builder.comment("Enable event system");
        builder.define("enableEvents", true);
    }
    
    /**
     * 注册饰品配置
     */
    private void registerAccessoryConfig() {
        builder.comment("Accessory system settings");
        
        builder.comment("Default accessory slots");
        builder.defineInRange("defaultSlots", 6, 1, 20);
        
        builder.comment("Enable accessory effects");
        builder.define("enableEffects", true);
        
        builder.comment("Enable accessory tooltips");
        builder.define("enableTooltips", true);
    }
    
    /**
     * 注册属性配置
     */
    private void registerAttributeConfig() {
        builder.comment("Attribute system settings");
        
        builder.comment("Enable attribute caching");
        builder.define("enableCaching", true);
        
        builder.comment("Cache duration in ticks");
        builder.defineInRange("cacheDuration", 20, 1, 1200);
        
        builder.comment("Enable attribute events");
        builder.define("enableEvents", true);
    }
    
    /**
     * 注册战斗配置
     */
    private void registerCombatConfig() {
        builder.comment("Combat system settings");
        
        builder.comment("Enable damage calculation");
        builder.define("enableDamageCalculation", true);
        
        builder.comment("Enable critical hits");
        builder.define("enableCriticalHits", true);
        
        builder.comment("Enable damage indicators");
        builder.define("enableDamageIndicators", true);
    }
    
    /**
     * 获取配置规范
     * @return 配置规范
     */
    public ForgeConfigSpec getSpec() {
        return spec;
    }
    
    /**
     * 获取配置值
     * @param path 配置路径
     * @return 配置值
     */
    public Object getConfigValue(String path) {
        return spec.getValues().get(path);
    }
    
    /**
     * 获取布尔配置值
     * @param path 配置路径
     * @return 配置值
     */
    public boolean getBoolean(String path) {
        Object value = getConfigValue(path);
        return value instanceof Boolean ? (Boolean) value : false;
    }
    
    /**
     * 获取整数配置值
     * @param path 配置路径
     * @return 配置值
     */
    public int getInt(String path) {
        Object value = getConfigValue(path);
        return value instanceof Integer ? (Integer) value : 0;
    }
    
    /**
     * 获取浮点数配置值
     * @param path 配置路径
     * @return 配置值
     */
    public double getDouble(String path) {
        Object value = getConfigValue(path);
        return value instanceof Double ? (Double) value : 0.0;
    }
    
    /**
     * 获取字符串配置值
     * @param path 配置路径
     * @return 配置值
     */
    public String getString(String path) {
        Object value = getConfigValue(path);
        return value != null ? value.toString() : "";
    }
}