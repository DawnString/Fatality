package cn.dawnstring.fatality.utils;

/**
 * 游戏常量定义
 * 统一管理游戏中的各种常量，避免魔法数字
 */
public class GameConstants {
    
    // ========== 时间相关常量 ==========
    public static final float TICK_INTERVAL = 0.05f; // 每tick的时间间隔（20tick/秒）
    public static final int SYNC_INTERVAL_TICKS = 20; // 同步间隔（每20tick同步一次）
    public static final float SECONDS_PER_TICK = 1.0f / 20.0f; // 每tick的秒数
    
    // ========== 饰品相关常量 ==========
    public static final int DEFAULT_ACCESSORY_SLOTS = 6; // 默认饰品槽位数量
    public static final int MAX_ACCESSORY_SLOTS = 10; // 最大饰品槽位数量
    
    // ========== 属性相关常量 ==========
    public static final float BASE_HEALTH_REGEN_RATE = 1.0f; // 基础生命恢复速率（每秒1点）
    public static final float BASE_MANA_REGEN_RATE = 2.0f; // 基础法力恢复速率（每秒2点）
    public static final float BASE_MAX_MANA = 100.0f; // 基础最大法力值
    public static final float MAX_MANA_CAP = 400.0f; // 最大法力值上限
    
    // ========== 效果相关常量 ==========
    public static final float TREATMENT_SATURATION_PENALTY = 0.5f; // 治疗饱和效果惩罚（减少50%恢复）
    public static final float MAGIC_FADE_PENALTY = 0.8f; // 魔法衰减效果惩罚（减少20%恢复）
    public static final float HEALTH_REGEN_DELAY_SECONDS = 3.0f; // 受伤后生命恢复延迟（3秒）
    
    // ========== 生命之环相关常量 ==========
    public static final float LIFE_RING_HEAL_AMOUNT = 10.0f; // 生命之环治疗量
    public static final float LIFE_RING_DAMAGE_AMOUNT = 5.0f; // 生命之环伤害量
    public static final int LIFE_RING_EFFECT_INTERVAL = 20; // 生命之环效果间隔（1秒）
    
    // ========== 数值比较常量 ==========
    public static final float FLOAT_COMPARISON_TOLERANCE = 0.001f; // 浮点数比较容差
    
    // ========== 优先级常量 ==========
    public static final int HIGH_PRIORITY = 100;
    public static final int NORMAL_PRIORITY = 50;
    public static final int LOW_PRIORITY = 10;
    
    // 私有构造函数，防止实例化
    private GameConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}