package cn.dawnstring.fatality.system.accessories;

import cn.dawnstring.fatality.api.accessories.AccessoryEffectHandler;
import cn.dawnstring.fatality.items.AccessoryItem;
import cn.dawnstring.fatality.utils.GameConstants;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 基础饰品效果处理器
 * 提供通用的饰品效果处理功能
 */
public abstract class BaseAccessoryEffectHandler implements AccessoryEffectHandler {
    
    protected final String accessoryId;
    protected final Class<? extends AccessoryItem> accessoryClass;
    
    public BaseAccessoryEffectHandler(String accessoryId, Class<? extends AccessoryItem> accessoryClass) {
        this.accessoryId = accessoryId;
        this.accessoryClass = accessoryClass;
    }
    
    @Override
    public boolean supports(ItemStack accessory) {
        return accessoryClass.isInstance(accessory.getItem());
    }
    
    @Override
    public abstract void applyEffect(Player player, ItemStack accessory, int slot);
    
    @Override
    public abstract void removeEffect(Player player, ItemStack accessory, int slot);
    
    @Override
    public int getPriority() {
        return GameConstants.NORMAL_PRIORITY;
    }
    
    /**
     * 初始化玩家数据（当玩家登录时调用）
     * 默认实现为空，子类可以覆盖此方法
     */
    public void initializePlayerData(Player player) {
        // 默认实现为空
    }
    
    /**
     * 清理玩家数据（当玩家退出时调用）
     * 默认实现为空，子类可以覆盖此方法
     */
    public void cleanupPlayerData(Player player) {
        // 默认实现为空
    }
    
    /**
     * 获取或创建玩家加成映射
     */
    protected <T> Map<Integer, T> getOrCreatePlayerBonusMap(
        Map<UUID, Map<Integer, T>> playerBonuses, 
        Player player
    ) {
        return playerBonuses.computeIfAbsent(player.getUUID(), k -> new HashMap<>());
    }
    
    /**
     * 更新加成值
     */
    protected void updateBonus(
        Map<Integer, Float> slotBonuses, 
        int slot, 
        float bonus,
        Runnable updateCallback
    ) {
        slotBonuses.put(slot, bonus);
        if (updateCallback != null) {
            updateCallback.run();
        }
    }
    
    /**
     * 移除加成值
     */
    protected void removeBonus(
        Map<Integer, Float> slotBonuses, 
        int slot,
        Runnable updateCallback
    ) {
        slotBonuses.remove(slot);
        if (updateCallback != null) {
            updateCallback.run();
        }
    }
}