package cn.dawnstring.fatality.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * 工具提示辅助类 - 统一管理物品描述显示逻辑
 * 按住Shift显示属性，按住Alt显示故事
 */
public class TooltipHelper {

    /**
     * 检查是否按住Alt键
     */
    private static boolean isAltDown() {
        long window = Minecraft.getInstance().getWindow().getWindow();
        return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS;
    }

    /**
     * 检查是否按住Shift键
     */
    private static boolean isShiftDown() {
        long window = Minecraft.getInstance().getWindow().getWindow();
        return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
    }

    /**
     * 添加物品描述工具提示
     * @param stack 物品堆栈
     * @param level 世界级别
     * @param tooltip 工具提示列表
     * @param flag 工具提示标志
     * @param story 物品故事
     * @param attributes 物品属性描述
     */
    public static void addDescriptiveTooltip(ItemStack stack, @Nullable Level level, List<Component> tooltip,
                                             TooltipFlag flag, String story, String attributes) {
        // 只在客户端显示自定义工具提示
        if (level == null || !level.isClientSide()) {
            return;
        }

        // 检查是否按住Shift键
        boolean isShiftDown = isShiftDown();
        // 检查是否按住Alt键
        boolean isAltDown = isAltDown();

        // 默认显示提示信息
        if (!isShiftDown && !isAltDown) {
            tooltip.add(Component.literal("§7按住§eAlt§7查看物品描述"));
            tooltip.add(Component.literal("§7按住§eShift§7查看物品属性"));
            return;
        }

        // 按住Alt显示物品故事
        if (isAltDown) {
            if (story != null && !story.isEmpty()) {
                tooltip.add(Component.literal("§6=== 物品描述 ==="));
                String[] storyLines = story.split("\n");
                for (String line : storyLines) {
                    tooltip.add(Component.literal("§e" + line));
                }
            } else {
                tooltip.add(Component.literal("§7暂无物品描述"));
            }
        }

        // 按住Shift显示物品属性
        if (isShiftDown) {
            if (attributes != null && !attributes.isEmpty()) {
                tooltip.add(Component.literal("§6=== 物品属性 ==="));
                String[] attrLines = attributes.split("\n");
                for (String line : attrLines) {
                    tooltip.add(Component.literal("§e" + line));
                }
            } else {
                tooltip.add(Component.literal("§7暂无物品属性"));
            }
        }
    }

    /**
     * 添加武器属性工具提示
     * @param stack 物品堆栈
     * @param level 世界级别
     * @param tooltip 工具提示列表
     * @param flag 工具提示标志
     * @param story 物品故事
     * @param baseDamageMultiplier 基础伤害倍率
     * @param criticalChance 暴击率
     * @param criticalDamageMultiplier 暴击伤害倍率
     * @param damageFluctuation 伤害浮动范围
     */
    public static void addWeaponTooltip(ItemStack stack, @Nullable Level level, List<Component> tooltip,
                                        TooltipFlag flag, String story,
                                        float baseDamageMultiplier, float criticalChance,
                                        float criticalDamageMultiplier, float damageFluctuation) {
        // 只在客户端显示自定义工具提示
        if (level == null || !level.isClientSide()) {
            return;
        }

        // 检查是否按住Shift键
        boolean isShiftDown = isShiftDown();
        // 检查是否按住Alt键
        boolean isAltDown = isAltDown();

        // 默认显示提示信息
        if (!isShiftDown && !isAltDown) {
            tooltip.add(Component.literal("§7按住§eShift§7查看武器属性"));
            tooltip.add(Component.literal("§7按住§eAlt§7查看物品描述"));
            return;
        }

        // 按住Shift显示武器属性
        if (isShiftDown) {
            tooltip.add(Component.literal("§6=== 武器属性 ==="));
            
            // 获取基础攻击伤害（从物品属性中获取）
            float attackDamage = getWeaponAttackDamage(stack);
            tooltip.add(Component.literal("§a攻击伤害: " + String.format("%.1f", attackDamage)));
            
            // 获取攻击间隔（从物品属性中获取）
            float attackSpeed = getWeaponAttackSpeed(stack);
            float attackInterval = getAttackIntervalFromAttackSpeed(attackSpeed);
            tooltip.add(Component.literal("§a攻击间隔: " + String.format("%.1f", attackInterval) + " 秒"));
            
            tooltip.add(Component.literal("§a基础伤害倍率: " + String.format("%.1f", baseDamageMultiplier)));
            tooltip.add(Component.literal("§a暴击率: " + String.format("%.1f%%", criticalChance * 100)));
            tooltip.add(Component.literal("§a暴击伤害: " + String.format("%.1f", criticalDamageMultiplier) + "倍"));
            tooltip.add(Component.literal("§a伤害浮动: ±" + String.format("%.0f%%", damageFluctuation * 100)));
        }

        // 按住Alt显示物品故事
        if (isAltDown) {
            if (story != null && !story.isEmpty()) {
                tooltip.add(Component.literal("§6=== 物品描述 ==="));
                String[] storyLines = story.split("\n");
                for (String line : storyLines) {
                    tooltip.add(Component.literal("§e" + line));
                }
            } else {
                tooltip.add(Component.literal("§7暂无物品描述"));
            }
        }
    }

    /**
     * 获取武器的攻击伤害
     */
    private static float getWeaponAttackDamage(ItemStack stack) {
        // 尝试从物品属性中获取攻击伤害
        var attributes = stack.getAttributeModifiers(EquipmentSlot.MAINHAND);
        var attackDamageAttribute = attributes.get(Attributes.ATTACK_DAMAGE);
        
        if (!attackDamageAttribute.isEmpty()) {
            // 获取第一个攻击伤害修饰符的值
            var modifier = attackDamageAttribute.iterator().next();
            return (float) modifier.getAmount();
        }
        
        // 如果无法获取，返回默认值
        return 0.0f;
    }

    /**
     * 获取武器的攻击速度
     */
    private static float getWeaponAttackSpeed(ItemStack stack) {
        // 尝试从物品属性中获取攻击速度
        var attributes = stack.getAttributeModifiers(EquipmentSlot.MAINHAND);
        var attackSpeedAttribute = attributes.get(Attributes.ATTACK_SPEED);
        
        if (!attackSpeedAttribute.isEmpty()) {
            // 获取第一个攻击速度修饰符的值
            var modifier = attackSpeedAttribute.iterator().next();
            return (float) modifier.getAmount();
        }
        
        // 如果无法获取，返回默认值
        return 4.0f; // Minecraft默认攻击速度
    }

    /**
     * 从攻击速度计算攻击间隔（秒）
     * Minecraft中攻击速度 = 1 / 攻击间隔
     */
    private static float getAttackIntervalFromAttackSpeed(float attackSpeed) {
        if (attackSpeed <= 0) {
            return 0.0f;
        }
        return 1.0f / attackSpeed;
    }
}