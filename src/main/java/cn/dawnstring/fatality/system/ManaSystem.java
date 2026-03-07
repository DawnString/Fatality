package cn.dawnstring.fatality.system;

import cn.dawnstring.fatality.network.ManaSyncHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import cn.dawnstring.fatality.inventory.AccessoryInventory;
import cn.dawnstring.fatality.items.AccessoryItem;
import cn.dawnstring.fatality.registry.ModEffects;

import java.util.HashMap;
import java.util.Map;

/**
 * 魔法系统 - 管理玩家的魔法值
 */
public class ManaSystem {
    public static final float BASE_MAX_MANA = 100.0f;
    public static final float MANA_REGENERATION_RATE = 2.0f; // 每秒恢复2点魔法（每0.5秒恢复1点）
    public static final float MAX_MANA_CAP = 400.0f; // 最大魔法值上限400点

    // 魔法值存储（临时存储，实际应该使用NBT持久化）
    private static final Map<String, Float> playerManaMap = new HashMap<>();
    // 存储玩家通过物品增加的魔法值
    private static final Map<String, Float> playerBonusManaMap = new HashMap<>();
    // 客户端魔法数据缓存（用于HUD显示）
    private static final Map<String, ClientManaData> clientManaDataMap = new HashMap<>();

    /**
     * 获取玩家的最大魔法值
     */
    public static float getMaxMana(Player player)
    {
        float maxMana = BASE_MAX_MANA;

        // 获取饰品栏
        var accessoryInventory = AccessoryInventory.get(player);
        if (accessoryInventory != null) {
            // 遍历饰品栏，计算魔法值加成
            for (int i = 0; i < accessoryInventory.getItemHandler().getSlots(); i++) {
                ItemStack accessory = accessoryInventory.getItemHandler().getStackInSlot(i);
                if (!accessory.isEmpty()) {
                    maxMana += getAccessoryManaBonus(accessory);
                }
            }
        }

        // 添加通过物品增加的魔法值（从持久化存储加载）
        float bonusMana = PlayerDataSystem.loadPlayerBonusMana(player);
        maxMana += bonusMana;

        // 确保不超过最大上限
        return Math.min(maxMana, MAX_MANA_CAP);
    }

    /**
     * 增加玩家的最大魔法值（通过物品使用）
     */
    public static boolean addBonusMana(Player player, float amount) {
        String playerId = getPlayerId(player);
        float currentBonus = PlayerDataSystem.loadPlayerBonusMana(player);
        float newBonus = currentBonus + amount;

        // 检查是否超过上限
        float baseMana = BASE_MAX_MANA;
        var accessoryInventory = AccessoryInventory.get(player);
        if (accessoryInventory != null) {
            for (int i = 0; i < accessoryInventory.getItemHandler().getSlots(); i++) {
                ItemStack accessory = accessoryInventory.getItemHandler().getStackInSlot(i);
                if (!accessory.isEmpty()) {
                    baseMana += getAccessoryManaBonus(accessory);
                }
            }
        }

        float totalMana = baseMana + newBonus;
        if (totalMana > MAX_MANA_CAP) {
            return false; // 超过上限
        }

        // 保存到持久化存储
        PlayerDataSystem.savePlayerBonusMana(player, newBonus);
        playerBonusManaMap.put(playerId, newBonus);
        return true;
    }

    /**
     * 获取玩家通过物品增加的魔法值
     */
    public static float getBonusMana(Player player) {
        String playerId = getPlayerId(player);
        if (playerBonusManaMap.containsKey(playerId)) {
            return playerBonusManaMap.get(playerId);
        } else {
            // 从持久化存储加载
            float bonusMana = PlayerDataSystem.loadPlayerBonusMana(player);
            playerBonusManaMap.put(playerId, bonusMana);
            return bonusMana;
        }
    }

    /**
     * 获取饰品对魔法值的加成（使用遍历方式）
     */
    private static float getAccessoryManaBonus(ItemStack accessory) {
        if (accessory.getItem() instanceof AccessoryItem accessoryItem) {
            return accessoryItem.getMaxManaBonus();
        }
        return 0.0f;
    }

    /**
     * 获取当前魔法值（持久化存储）
     */
    public static float getCurrentMana(Player player) {
        String playerId = getPlayerId(player);
        if (playerManaMap.containsKey(playerId)) {
            return playerManaMap.get(playerId);
        } else {
            // 从持久化存储加载
            float currentMana;
            if (player.level().isClientSide()) {
                // 在客户端，使用客户端数据同步系统
                currentMana = getClientCurrentMana(player);
            } else {
                // 在服务器端，从持久化存储加载
                currentMana = PlayerDataSystem.loadPlayerMana(player);
            }
            playerManaMap.put(playerId, currentMana);
            return currentMana;
        }
    }

    /**
     * 设置当前魔法值
     */
    public static void setCurrentMana(Player player, float mana) {
        String playerId = getPlayerId(player);
        float maxMana = getMaxMana(player);
        // 确保魔法值在合理范围内
        float clampedMana = Math.max(0, Math.min(mana, maxMana));
        playerManaMap.put(playerId, clampedMana);

        // 保存到持久化存储
        PlayerDataSystem.savePlayerMana(player, clampedMana);
    }

    /**
     * 消耗魔法值
     */
    public static boolean consumeMana(Player player, float amount) {
        if (amount <= 0) {
            return true; // 消耗0点魔法值总是成功
        }
        
        float currentMana = getCurrentMana(player);
        
        // 使用容差比较，避免浮点数精度问题
        // 当魔法值非常接近消耗值时（差值小于0.001），也认为足够
        if (currentMana >= amount || Math.abs(currentMana - amount) < 0.001f) {
            // 确保不会消耗负的魔法值
            float newMana = Math.max(0, currentMana - amount);
            setCurrentMana(player, newMana);
            
            // 立即同步魔法数据到客户端
            if (!player.level().isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                ManaSyncHandler.syncManaDataToClient(serverPlayer);
            }
            
            return true;
        }
        return false;
    }


    /**
     * 恢复魔法值（每0.5秒恢复1点）
     */
    public static void regenerateMana(Player player, float deltaTime) {
        float currentMana = getCurrentMana(player);
        float maxMana = getMaxMana(player);
        if (currentMana < maxMana) {
            // 使用AttributeSystem获取可配置的恢复速率
            float regenerationRate = AttributeSystem.getManaRegenerationRate(player);
            
            // 检查玩家是否有魔力衰减效果，如果有则减少20%恢复速率
            if (player.hasEffect(ModEffects.MAGIC_FADE.get())) {
                regenerationRate *= 0.8f; // 减少20%
            }
            
            float newMana = Math.min(maxMana, currentMana + regenerationRate * deltaTime);
            setCurrentMana(player, newMana);
        }
    }

    /**
     * 获取玩家唯一标识符
     */
    private static String getPlayerId(Player player) {
        return player.getUUID().toString();
    }
    
    /**
     * 检查玩家是否有足够的魔法值（不实际消耗）
     */
    public static boolean hasEnoughMana(Player player, float amount) {
        if (amount <= 0) {
            return true;
        }
        float currentMana = getCurrentMana(player);
        // 使用容差比较，避免浮点数精度问题
        return currentMana >= amount || Math.abs(currentMana - amount) < 0.001f;
    }
    
    /**
     * 获取玩家的魔法值百分比（0.0 - 1.0）
     */
    public static float getManaPercentage(Player player) {
        float currentMana = getCurrentMana(player);
        float maxMana = getMaxMana(player);
        if (maxMana <= 0) {
            return 0.0f;
        }
        return Math.max(0.0f, Math.min(1.0f, currentMana / maxMana));
    }
    
    /**
     * 安全地消耗魔法值，如果魔法值不足则返回false且不消耗任何魔法值
     */
    public static boolean safeConsumeMana(Player player, float amount) {
        if (!hasEnoughMana(player, amount)) {
            return false;
        }
        return consumeMana(player, amount);
    }
    
    /**
     * 恢复魔法值到指定值（不超过最大值）
     */
    public static void restoreMana(Player player, float amount) {
        float currentMana = getCurrentMana(player);
        float maxMana = getMaxMana(player);
        float newMana = Math.min(maxMana, currentMana + amount);
        setCurrentMana(player, newMana);
    }
    
    /**
     * 设置魔法值为最大值
     */
    public static void restoreFullMana(Player player) {
        setCurrentMana(player, getMaxMana(player));
    }

    // ==================== 客户端数据同步方法 ====================

    /**
     * 更新客户端魔法数据（由网络包调用）
     */
    public static void updateClientManaData(String playerId, float currentMana, float maxMana, float bonusMana) {
        clientManaDataMap.put(playerId, new ClientManaData(currentMana, maxMana, bonusMana));
    }

    /**
     * 获取客户端的当前魔法值（优先使用同步数据）
     */
    public static float getClientCurrentMana(Player player) {
        String playerId = getPlayerId(player);
        ClientManaData clientData = clientManaDataMap.get(playerId);
        if (clientData != null) {
            return clientData.currentMana;
        }
        // 如果没有同步数据，使用默认逻辑
        return getCurrentMana(player);
    }

    /**
     * 获取客户端的最大魔法值（优先使用同步数据）
     */
    public static float getClientMaxMana(Player player) {
        String playerId = getPlayerId(player);
        ClientManaData clientData = clientManaDataMap.get(playerId);
        if (clientData != null) {
            return clientData.maxMana;
        }
        // 如果没有同步数据，使用默认逻辑
        return getMaxMana(player);
    }

    /**
     * 客户端魔法数据类
     */
    private static class ClientManaData {
        public final float currentMana;
        public final float maxMana;
        public final float bonusMana;

        public ClientManaData(float currentMana, float maxMana, float bonusMana) {
            this.currentMana = currentMana;
            this.maxMana = maxMana;
            this.bonusMana = bonusMana;
        }
    }
}