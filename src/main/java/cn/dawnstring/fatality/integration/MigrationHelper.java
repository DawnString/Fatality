package cn.dawnstring.fatality.integration;

import cn.dawnstring.fatality.system.AccessorySystem;
import cn.dawnstring.fatality.system.AttributeSystem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 迁移助手类
 * 帮助将现有代码逐步迁移到新的架构中
 */
public class MigrationHelper {
    
    /**
     * 启用新属性系统
     */
    public static void enableNewAttributeSystem() {
        System.out.println("New Attribute System enabled");
    }
    
    /**
     * 启用新饰品系统
     */
    public static void enableNewAccessorySystem() {
        System.out.println("New Accessory System enabled");
    }
    
    /**
     * 兼容性方法 - 属性系统
     */
    public static float getHealthRegenerationRate(Player player) {
        return AttributeSystem.getHealthRegenerationRate(player);
    }
    
    public static float getManaRegenerationRate(Player player) {
        return AttributeSystem.getManaRegenerationRate(player);
    }
    
    public static float getAttackDamage(Player player) {
        return AttributeSystem.getAttackDamage(player);
    }
    
    public static float getCritChance(Player player) {
        return AttributeSystem.getCritChance(player);
    }
    
    public static float getCritDamage(Player player) {
        return AttributeSystem.getCritDamage(player);
    }
    
    /**
     * 兼容性方法 - 饰品系统
     */
    public static List<ItemStack> getPlayerAccessories(Player player) {
        return AccessorySystem.getPlayerAccessories(player);
    }
    
    public static boolean hasAccessoryEquipped(Player player, ItemStack accessory) {
        return AccessorySystem.hasAccessoryEquipped(player, accessory);
    }
    
    /**
     * 检查新系统是否可用
     */
    public static boolean isNewAttributeSystemAvailable() {
        return true; // 暂时返回true，实际实现中可以根据配置决定
    }
    
    public static boolean isNewAccessorySystemAvailable() {
        return true; // 暂时返回true，实际实现中可以根据配置决定
    }
    
    /**
     * 迁移状态报告
     */
    public static void printMigrationStatus() {
        System.out.println("=== Fatality Migration Status ===");
        System.out.println("New Attribute System: " + (isNewAttributeSystemAvailable() ? "ENABLED" : "DISABLED"));
        System.out.println("New Accessory System: " + (isNewAccessorySystemAvailable() ? "ENABLED" : "DISABLED"));
        System.out.println("Event-Driven Architecture: ENABLED");
        System.out.println("API Separation: ENABLED");
        System.out.println("==================================");
    }
}