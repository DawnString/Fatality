package cn.dawnstring.fatality.api.attributes;

import net.minecraft.world.entity.player.Player;

/**
 * 属性系统API接口
 * 提供统一的属性获取和管理功能
 */
public interface IAttributeSystem {
    
    /**
     * 获取玩家属性值
     * @param player 玩家
     * @param attributeId 属性ID
     * @return 属性值
     */
    float getAttribute(Player player, String attributeId);
    
    /**
     * 注册属性计算器
     * @param attributeId 属性ID
     * @param calculator 属性计算器
     */
    void registerAttribute(String attributeId, AttributeCalculator calculator);
    
    /**
     * 添加属性修改器
     * @param player 玩家
     * @param modifier 属性修改器
     */
    void addAttributeModifier(Player player, AttributeModifier modifier);
    
    /**
     * 移除属性修改器
     * @param player 玩家
     * @param modifierId 修改器ID
     */
    void removeAttributeModifier(Player player, String modifierId);
    
    /**
     * 获取所有已注册的属性ID
     * @return 属性ID列表
     */
    Iterable<String> getRegisteredAttributes();
    
    /**
     * 检查属性是否已注册
     * @param attributeId 属性ID
     * @return 是否已注册
     */
    boolean isAttributeRegistered(String attributeId);
}