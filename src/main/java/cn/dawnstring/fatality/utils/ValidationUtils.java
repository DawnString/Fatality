package cn.dawnstring.fatality.utils;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 输入验证工具类
 * 提供统一的参数验证方法
 */
public class ValidationUtils {
    
    /**
     * 验证玩家对象
     */
    public static void validatePlayer(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        if (player.isRemoved()) {
            throw new IllegalArgumentException("Player has been removed from world");
        }
    }
    
    /**
     * 验证槽位索引
     */
    public static void validateSlot(int slot, int maxSlots) {
        if (slot < 0) {
            throw new IllegalArgumentException("Slot cannot be negative: " + slot);
        }
        if (slot >= maxSlots) {
            throw new IllegalArgumentException(
                String.format("Slot %d is out of bounds. Valid range: 0-%d", slot, maxSlots - 1));
        }
    }
    
    /**
     * 验证物品堆
     */
    public static void validateItemStack(ItemStack itemStack) {
        if (itemStack == null) {
            throw new IllegalArgumentException("ItemStack cannot be null");
        }
    }
    
    /**
     * 验证属性名称
     */
    public static void validateAttributeName(String attributeName) {
        if (attributeName == null || attributeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Attribute name cannot be null or empty");
        }
    }
    
    /**
     * 验证数值范围
     */
    public static void validateRange(float value, float min, float max, String paramName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                String.format("%s must be between %.2f and %.2f, but was %.2f", 
                    paramName, min, max, value));
        }
    }
    
    /**
     * 验证正数
     */
    public static void validatePositive(float value, String paramName) {
        if (value <= 0) {
            throw new IllegalArgumentException(
                String.format("%s must be positive, but was %.2f", paramName, value));
        }
    }
    
    /**
     * 验证非负数
     */
    public static void validateNonNegative(float value, String paramName) {
        if (value < 0) {
            throw new IllegalArgumentException(
                String.format("%s cannot be negative, but was %.2f", paramName, value));
        }
    }
    
    /**
     * 验证浮点数相等性（使用容差）
     */
    public static boolean equalsWithTolerance(float a, float b, float tolerance) {
        return Math.abs(a - b) <= tolerance;
    }
    
    /**
     * 验证浮点数相等性（使用默认容差）
     */
    public static boolean equalsWithTolerance(float a, float b) {
        return equalsWithTolerance(a, b, GameConstants.FLOAT_COMPARISON_TOLERANCE);
    }
    
    // 私有构造函数，防止实例化
    private ValidationUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}