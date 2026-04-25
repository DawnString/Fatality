package cn.dawnstring.fatality.system.accessories;

import cn.dawnstring.fatality.items.AccessoryItem;
import cn.dawnstring.fatality.system.HealthRegenerationHandler;
import cn.dawnstring.fatality.utils.GameConstants;
import cn.dawnstring.fatality.utils.PlayerBaseAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 生命恢复效果处理器
 * 处理提供生命恢复加成的饰品
 */
public class HealthRegenerationEffectHandler extends BaseAccessoryEffectHandler {
    
    // 存储玩家生命恢复效果的数据：玩家UUID -> 槽位 -> 加成值
    private final Map<UUID, Map<Integer, Float>> playerHealthRegenBonuses = new HashMap<>();
    
    public HealthRegenerationEffectHandler(Class<? extends AccessoryItem> accessoryClass) {
        super("health_regen", accessoryClass);
    }
    
    @Override
    public void applyEffect(Player player, ItemStack accessory, int slot) {
        if (accessory.getItem() instanceof AccessoryItem accessoryItem) {
            float healthRegenBonus = accessoryItem.getHealthRegenerationBonus();
            if (healthRegenBonus > 0) {
                applyHealthRegenerationEffect(player, healthRegenBonus, slot);
            }
        }
    }
    
    @Override
    public void removeEffect(Player player, ItemStack accessory, int slot) {
        removeHealthRegenerationEffect(player, slot);
    }
    
    private void applyHealthRegenerationEffect(Player player, float bonus, int slot) {
        Map<Integer, Float> slotBonuses = getOrCreatePlayerBonusMap(playerHealthRegenBonuses, player);
        
        updateBonus(slotBonuses, slot, bonus, () -> updateHealthRegenerationRate(player));
    }
    
    private void removeHealthRegenerationEffect(Player player, int slot) {
        Map<Integer, Float> slotBonuses = playerHealthRegenBonuses.get(player.getUUID());
        if (slotBonuses != null) {
            removeBonus(slotBonuses, slot, () -> {
                if (slotBonuses.isEmpty()) {
                    playerHealthRegenBonuses.remove(player.getUUID());
                }
                updateHealthRegenerationRate(player);
            });
        }
    }
    
    private void updateHealthRegenerationRate(Player player) {
        float totalBonus = 0.0f;
        
        Map<Integer, Float> slotBonuses = playerHealthRegenBonuses.get(player.getUUID());
        if (slotBonuses != null) {
            for (float bonus : slotBonuses.values()) {
                totalBonus += bonus;
            }
        }
        
        float baseRegenRate = PlayerBaseAttributes.getBaseHealthRegenRate(player);
        float newRegenRate = baseRegenRate * (1.0f + totalBonus / 100.0f);
        
        applyDynamicHealthRegeneration(player, newRegenRate);
    }
    
    private void applyDynamicHealthRegeneration(Player player, float regenRate) {
        // 实现动态生命恢复逻辑
        // 通过修改玩家NBT数据来存储动态恢复速率
        if (!player.level().isClientSide) {
            // 服务器端逻辑：将动态恢复速率存储到玩家NBT中
            player.getPersistentData().putFloat("fatality:dynamic_health_regen_rate", regenRate);
        }
    }
    
    @Override
    public void updateEffect(Player player, ItemStack accessory, int slot) {
        // 每tick更新生命恢复效果
        if (!player.level().isClientSide) {
            // 获取当前动态恢复速率
            float currentRegenRate = getCurrentRegenRate(player);
            
            // 计算每tick恢复的生命值（每秒恢复regenRate点）
            float regenPerTick = currentRegenRate / 20.0f; // 20 ticks = 1秒
            
            // 获取当前生命值和最大生命值
            float currentHealth = player.getHealth();
            float maxHealth = player.getMaxHealth();
            
            // 如果当前生命值小于最大生命值，则恢复生命
            if (currentHealth < maxHealth) {
                float newHealth = Math.min(currentHealth + regenPerTick, maxHealth);
                player.setHealth(newHealth);
            }
        }
    }
    
    private float getCurrentRegenRate(Player player) {
        float totalBonus = 0.0f;
        
        Map<Integer, Float> slotBonuses = playerHealthRegenBonuses.get(player.getUUID());
        if (slotBonuses != null) {
            for (float bonus : slotBonuses.values()) {
                totalBonus += bonus;
            }
        }
        
        float baseRegenRate = PlayerBaseAttributes.getBaseHealthRegenRate(player);
        return baseRegenRate * (1.0f + totalBonus / 100.0f);
    }
    
    @Override
    public int getPriority() {
        return GameConstants.HIGH_PRIORITY;
    }
    
    /**
     * 清理玩家数据
     */
    public void cleanupPlayerData(Player player) {
        UUID playerId = player.getUUID();
        playerHealthRegenBonuses.remove(playerId);
    }
}