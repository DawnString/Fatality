package cn.dawnstring.fatality.system.accessories;

import cn.dawnstring.fatality.items.AccessoryItem;
import cn.dawnstring.fatality.system.ManaSystem;
import cn.dawnstring.fatality.utils.GameConstants;
import cn.dawnstring.fatality.utils.PlayerBaseAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 法力恢复效果处理器
 * 处理提供法力恢复加成的饰品
 */
public class ManaRegenerationEffectHandler extends BaseAccessoryEffectHandler {
    
    // 存储玩家法力恢复效果的数据：玩家UUID -> 槽位 -> 加成值
    private final Map<UUID, Map<Integer, Float>> playerManaRegenBonuses = new HashMap<>();
    
    public ManaRegenerationEffectHandler(Class<? extends AccessoryItem> accessoryClass) {
        super("mana_regen", accessoryClass);
    }
    
    @Override
    public void applyEffect(Player player, ItemStack accessory, int slot) {
        if (accessory.getItem() instanceof AccessoryItem accessoryItem) {
            float manaRegenBonus = accessoryItem.getManaRegenerationBonus();
            if (manaRegenBonus > 0) {
                applyManaRegenerationEffect(player, manaRegenBonus, slot);
            }
        }
    }
    
    @Override
    public void removeEffect(Player player, ItemStack accessory, int slot) {
        removeManaRegenerationEffect(player, slot);
    }
    
    private void applyManaRegenerationEffect(Player player, float bonus, int slot) {
        Map<Integer, Float> slotBonuses = getOrCreatePlayerBonusMap(playerManaRegenBonuses, player);
        
        updateBonus(slotBonuses, slot, bonus, () -> updateManaRegenerationRate(player));
    }
    
    private void removeManaRegenerationEffect(Player player, int slot) {
        Map<Integer, Float> slotBonuses = playerManaRegenBonuses.get(player.getUUID());
        if (slotBonuses != null) {
            removeBonus(slotBonuses, slot, () -> {
                if (slotBonuses.isEmpty()) {
                    playerManaRegenBonuses.remove(player.getUUID());
                }
                updateManaRegenerationRate(player);
            });
        }
    }
    
    private void updateManaRegenerationRate(Player player) {
        float totalBonus = 0.0f;
        
        Map<Integer, Float> slotBonuses = playerManaRegenBonuses.get(player.getUUID());
        if (slotBonuses != null) {
            for (float bonus : slotBonuses.values()) {
                totalBonus += bonus;
            }
        }
        
        float baseRegenRate = PlayerBaseAttributes.getBaseManaRegenRate(player);
        float newRegenRate = baseRegenRate * (1.0f + totalBonus / 100.0f);
        
        applyDynamicManaRegeneration(player, newRegenRate);
    }
    
    private void applyDynamicManaRegeneration(Player player, float regenRate) {
        // 实现动态法力恢复逻辑
        // 通过修改玩家NBT数据来存储动态恢复速率
        if (!player.level().isClientSide) {
            // 服务器端逻辑：将动态恢复速率存储到玩家NBT中
            player.getPersistentData().putFloat("fatality:dynamic_mana_regen_rate", regenRate);
        }
    }
    
    @Override
    public void updateEffect(Player player, ItemStack accessory, int slot) {
        // 每tick更新法力恢复效果
        if (!player.level().isClientSide) {
            // 获取当前动态恢复速率
            float currentRegenRate = getCurrentRegenRate(player);
            
            // 计算每tick恢复的法力值（每秒恢复regenRate点）
            float regenPerTick = currentRegenRate / 20.0f; // 20 ticks = 1秒
            
            // 获取当前法力值和最大法力值
            float currentMana = ManaSystem.getCurrentMana(player);
            float maxMana = ManaSystem.getMaxMana(player);
            
            // 如果当前法力值小于最大法力值，则恢复法力
            if (currentMana < maxMana) {
                float newMana = Math.min(currentMana + regenPerTick, maxMana);
                ManaSystem.setCurrentMana(player, newMana);
            }
        }
    }
    
    private float getCurrentRegenRate(Player player) {
        float totalBonus = 0.0f;
        
        Map<Integer, Float> slotBonuses = playerManaRegenBonuses.get(player.getUUID());
        if (slotBonuses != null) {
            for (float bonus : slotBonuses.values()) {
                totalBonus += bonus;
            }
        }
        
        float baseRegenRate = PlayerBaseAttributes.getBaseManaRegenRate(player);
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
        playerManaRegenBonuses.remove(playerId);
    }
}