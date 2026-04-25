package cn.dawnstring.fatality.api.accessories;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 饰品系统API接口
 * 提供饰品装备、管理和效果应用功能
 */
public interface IAccessorySystem {
    
    /**
     * 装备饰品
     * @param player 玩家
     * @param accessory 饰品物品
     * @param slot 饰品槽位
     * @return 是否装备成功
     */
    boolean equipAccessory(Player player, ItemStack accessory, int slot);
    
    /**
     * 卸下饰品
     * @param player 玩家
     * @param slot 饰品槽位
     * @return 卸下的饰品
     */
    ItemStack unequipAccessory(Player player, int slot);
    
    /**
     * 获取已装备的饰品列表
     * @param player 玩家
     * @return 饰品列表
     */
    List<ItemStack> getEquippedAccessories(Player player);
    
    /**
     * 获取指定槽位的饰品
     * @param player 玩家
     * @param slot 槽位
     * @return 饰品物品
     */
    ItemStack getAccessoryInSlot(Player player, int slot);
    
    /**
     * 检查饰品是否可以装备
     * @param player 玩家
     * @param accessory 饰品
     * @param slot 槽位
     * @return 是否可以装备
     */
    boolean canEquipAccessory(Player player, ItemStack accessory, int slot);
    
    /**
     * 获取饰品槽位数量
     * @param player 玩家
     * @return 槽位数量
     */
    int getAccessorySlotCount(Player player);
    
    /**
     * 注册饰品效果处理器
     * @param effectHandler 效果处理器
     */
    void registerEffectHandler(AccessoryEffectHandler effectHandler);
    
    /**
     * 应用饰品效果
     * @param player 玩家
     */
    void applyAccessoryEffects(Player player);
    
    /**
     * 移除饰品效果
     * @param player 玩家
     */
    void removeAccessoryEffects(Player player);
}