package cn.dawnstring.fatality.api.attributes;

import net.minecraft.world.entity.player.Player;

/**
 * 属性计算器接口
 * 用于计算玩家特定属性的值
 */
@FunctionalInterface
public interface AttributeCalculator {
    
    /**
     * 计算属性值
     * @param player 玩家
     * @return 属性值
     */
    float calculate(Player player);
    
    /**
     * 获取计算器描述（用于调试和日志）
     * @return 描述信息
     */
    default String getDescription() {
        return getClass().getSimpleName();
    }
}