package cn.dawnstring.fatality.api.accessories;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 饰品效果处理器接口
 * 用于处理特定饰品的特殊效果
 */
public interface AccessoryEffectHandler {
    
    /**
     * 检查是否支持处理该饰品
     * @param accessory 饰品物品
     * @return 是否支持
     */
    boolean supports(ItemStack accessory);
    
    /**
     * 应用饰品效果
     * @param player 玩家
     * @param accessory 饰品物品
     * @param slot 饰品槽位
     */
    void applyEffect(Player player, ItemStack accessory, int slot);
    
    /**
     * 移除饰品效果
     * @param player 玩家
     * @param accessory 饰品物品
     * @param slot 饰品槽位
     */
    void removeEffect(Player player, ItemStack accessory, int slot);
    
    /**
     * 更新饰品效果（每tick调用）
     * @param player 玩家
     * @param accessory 饰品物品
     * @param slot 饰品槽位
     */
    default void updateEffect(Player player, ItemStack accessory, int slot) {
        // 默认不执行任何操作
    }
    
    /**
     * 获取处理器优先级
     * @return 优先级（数值越小优先级越高）
     */
    default int getPriority() {
        return 100;
    }
}