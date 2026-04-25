package cn.dawnstring.fatality.config;

/**
 * 架构配置类
 * 管理新架构的启用状态和配置选项
 */
public class ArchitectureConfig {
    
    // 新架构启用标志
    public static boolean ENABLE_EVENT_DRIVEN_ARCHITECTURE = true;
    public static boolean ENABLE_API_SEPARATION = true;
    public static boolean ENABLE_NEW_ATTRIBUTE_SYSTEM = true;
    public static boolean ENABLE_NEW_ACCESSORY_SYSTEM = true;
    public static boolean ENABLE_MODULAR_SYSTEMS = true;
    
    // 事件系统配置
    public static boolean ENABLE_EVENT_LOGGING = false;
    public static int EVENT_PROCESSING_THREADS = 2;
    
    // 属性系统配置
    public static boolean ENABLE_ATTRIBUTE_CACHING = true;
    public static int ATTRIBUTE_CACHE_DURATION = 20; // ticks
    
    // 饰品系统配置
    public static int MAX_ACCESSORY_SLOTS = 6;
    public static boolean ENABLE_ACCESSORY_EFFECT_STACKING = true;
    
    /**
     * 验证配置有效性
     */
    public static boolean validateConfig() {
        if (EVENT_PROCESSING_THREADS < 1) {
            EVENT_PROCESSING_THREADS = 1;
        }
        
        if (MAX_ACCESSORY_SLOTS < 1) {
            MAX_ACCESSORY_SLOTS = 1;
        }
        
        if (ATTRIBUTE_CACHE_DURATION < 1) {
            ATTRIBUTE_CACHE_DURATION = 1;
        }
        
        return true;
    }
    
    /**
     * 获取配置摘要
     */
    public static String getConfigSummary() {
        return String.format("""
            === Fatality Architecture Configuration ===
            Event-Driven Architecture: %s
            API Separation: %s
            New Attribute System: %s
            New Accessory System: %s
            Modular Systems: %s
            Max Accessory Slots: %d
            Event Logging: %s
            ===========================================
            """, 
            ENABLE_EVENT_DRIVEN_ARCHITECTURE ? "ENABLED" : "DISABLED",
            ENABLE_API_SEPARATION ? "ENABLED" : "DISABLED",
            ENABLE_NEW_ATTRIBUTE_SYSTEM ? "ENABLED" : "DISABLED",
            ENABLE_NEW_ACCESSORY_SYSTEM ? "ENABLED" : "DISABLED",
            ENABLE_MODULAR_SYSTEMS ? "ENABLED" : "DISABLED",
            MAX_ACCESSORY_SLOTS,
            ENABLE_EVENT_LOGGING ? "ENABLED" : "DISABLED"
        );
    }
}