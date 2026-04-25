package cn.dawnstring.fatality.utils;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 玩家基础属性管理器
 * 管理玩家的个性化基础属性值（饰品加成的基础）
 */
public class PlayerBaseAttributes {
    
    private static final Map<UUID, PlayerBaseAttributeData> playerBaseAttributes = new HashMap<>();
    
    private static final String NBT_KEY_BASE_HEALTH_REGEN = "fatality:base_health_regen";
    private static final String NBT_KEY_BASE_MANA_REGEN = "fatality:base_mana_regen";
    private static final String NBT_KEY_BASE_MAX_MANA = "fatality:base_max_mana";
    
    /**
     * 获取玩家的基础生命恢复速率
     * 优先使用玩家个性化值，否则使用全局常量
     */
    public static float getBaseHealthRegenRate(Player player) {
        PlayerBaseAttributeData data = getPlayerData(player);
        if (data != null && data.baseHealthRegenRate > 0) {
            return data.baseHealthRegenRate;
        }
        return GameConstants.BASE_HEALTH_REGEN_RATE;
    }
    
    /**
     * 设置玩家的基础生命恢复速率
     */
    public static void setBaseHealthRegenRate(Player player, float value) {
        PlayerBaseAttributeData data = getOrCreatePlayerData(player);
        data.baseHealthRegenRate = value;
        
        if (!player.level().isClientSide) {
            player.getPersistentData().putFloat(NBT_KEY_BASE_HEALTH_REGEN, value);
        }
        
        invalidateAttributeCache(player);
    }
    
    /**
     * 获取玩家的基础法力恢复速率
     * 优先使用玩家个性化值，否则使用全局常量
     */
    public static float getBaseManaRegenRate(Player player) {
        PlayerBaseAttributeData data = getPlayerData(player);
        if (data != null && data.baseManaRegenRate > 0) {
            return data.baseManaRegenRate;
        }
        return GameConstants.BASE_MANA_REGEN_RATE;
    }
    
    /**
     * 设置玩家的基础法力恢复速率
     */
    public static void setBaseManaRegenRate(Player player, float value) {
        PlayerBaseAttributeData data = getOrCreatePlayerData(player);
        data.baseManaRegenRate = value;
        
        if (!player.level().isClientSide) {
            player.getPersistentData().putFloat(NBT_KEY_BASE_MANA_REGEN, value);
        }
        
        invalidateAttributeCache(player);
    }
    
    /**
     * 获取玩家的基础最大法力值
     * 优先使用玩家个性化值，否则使用全局常量
     */
    public static float getBaseMaxMana(Player player) {
        PlayerBaseAttributeData data = getPlayerData(player);
        if (data != null && data.baseMaxMana > 0) {
            return data.baseMaxMana;
        }
        return GameConstants.BASE_MAX_MANA;
    }
    
    /**
     * 设置玩家的基础最大法力值
     */
    public static void setBaseMaxMana(Player player, float value) {
        PlayerBaseAttributeData data = getOrCreatePlayerData(player);
        data.baseMaxMana = value;
        
        if (!player.level().isClientSide) {
            player.getPersistentData().putFloat(NBT_KEY_BASE_MAX_MANA, value);
        }
        
        invalidateAttributeCache(player);
    }
    
    /**
     * 增加玩家的基础生命恢复速率
     */
    public static void addBaseHealthRegenRate(Player player, float amount) {
        float current = getBaseHealthRegenRate(player);
        setBaseHealthRegenRate(player, current + amount);
    }
    
    /**
     * 增加玩家的基础法力恢复速率
     */
    public static void addBaseManaRegenRate(Player player, float amount) {
        float current = getBaseManaRegenRate(player);
        setBaseManaRegenRate(player, current + amount);
    }
    
    /**
     * 增加玩家的基础最大法力值
     */
    public static void addBaseMaxMana(Player player, float amount) {
        float current = getBaseMaxMana(player);
        setBaseMaxMana(player, current + amount);
    }
    
    /**
     * 从NBT加载玩家基础属性
     */
    public static void loadFromNBT(Player player) {
        CompoundTag nbt = player.getPersistentData();
        
        PlayerBaseAttributeData data = getOrCreatePlayerData(player);
        
        if (nbt.contains(NBT_KEY_BASE_HEALTH_REGEN)) {
            data.baseHealthRegenRate = nbt.getFloat(NBT_KEY_BASE_HEALTH_REGEN);
        }
        
        if (nbt.contains(NBT_KEY_BASE_MANA_REGEN)) {
            data.baseManaRegenRate = nbt.getFloat(NBT_KEY_BASE_MANA_REGEN);
        }
        
        if (nbt.contains(NBT_KEY_BASE_MAX_MANA)) {
            data.baseMaxMana = nbt.getFloat(NBT_KEY_BASE_MAX_MANA);
        }
    }
    
    /**
     * 保存玩家基础属性到NBT
     */
    public static void saveToNBT(Player player) {
        PlayerBaseAttributeData data = getPlayerData(player);
        if (data == null) {
            return;
        }
        
        CompoundTag nbt = player.getPersistentData();
        
        if (data.baseHealthRegenRate > 0) {
            nbt.putFloat(NBT_KEY_BASE_HEALTH_REGEN, data.baseHealthRegenRate);
        }
        
        if (data.baseManaRegenRate > 0) {
            nbt.putFloat(NBT_KEY_BASE_MANA_REGEN, data.baseManaRegenRate);
        }
        
        if (data.baseMaxMana > 0) {
            nbt.putFloat(NBT_KEY_BASE_MAX_MANA, data.baseMaxMana);
        }
    }
    
    /**
     * 清理玩家数据
     */
    public static void cleanupPlayer(Player player) {
        playerBaseAttributes.remove(player.getUUID());
        AttributeCache.clearPlayerCache(player);
    }
    
    /**
     * 获取玩家数据
     */
    private static PlayerBaseAttributeData getPlayerData(Player player) {
        return playerBaseAttributes.get(player.getUUID());
    }
    
    /**
     * 获取或创建玩家数据
     */
    private static PlayerBaseAttributeData getOrCreatePlayerData(Player player) {
        return playerBaseAttributes.computeIfAbsent(
            player.getUUID(), 
            k -> new PlayerBaseAttributeData()
        );
    }
    
    /**
     * 使属性缓存失效
     */
    private static void invalidateAttributeCache(Player player) {
        AttributeCache.clearPlayerCache(player);
    }
    
    /**
     * 玩家基础属性数据
     */
    private static class PlayerBaseAttributeData {
        float baseHealthRegenRate = 0.0f;
        float baseManaRegenRate = 0.0f;
        float baseMaxMana = 0.0f;
    }
    
    // 私有构造函数，防止实例化
    private PlayerBaseAttributes() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}