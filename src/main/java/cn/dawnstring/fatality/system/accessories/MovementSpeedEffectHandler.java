package cn.dawnstring.fatality.system.accessories;

import cn.dawnstring.fatality.items.AccessoryItem;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 移动速度效果处理器
 * 处理提供移动速度加成的饰品
 */
public class MovementSpeedEffectHandler extends BaseAccessoryEffectHandler {
    
    // 存储玩家移动速度效果的数据：玩家UUID -> 槽位 -> 加成值
    private final Map<UUID, Map<Integer, Float>> playerMovementSpeedBonuses = new HashMap<>();
    
    public MovementSpeedEffectHandler(Class<? extends AccessoryItem> accessoryClass) {
        super("movement_speed", accessoryClass);
    }
    
    @Override
    public void applyEffect(Player player, ItemStack accessory, int slot) {
        if (accessory.getItem() instanceof AccessoryItem accessoryItem) {
            float movementSpeedBonus = accessoryItem.getMovementSpeedBonus();
            if (movementSpeedBonus != 0.0f) {
                applyMovementSpeedEffect(player, movementSpeedBonus, slot);
            }
        }
    }
    
    @Override
    public void removeEffect(Player player, ItemStack accessory, int slot) {
        removeMovementSpeedEffect(player, slot);
    }
    
    private void applyMovementSpeedEffect(Player player, float bonus, int slot) {
        UUID playerId = player.getUUID();
        
        // 获取或创建该玩家的移动速度加成映射
        Map<Integer, Float> slotBonuses = playerMovementSpeedBonuses.computeIfAbsent(playerId, k -> new HashMap<>());
        
        // 记录该槽位的移动速度加成
        slotBonuses.put(slot, bonus);
        
        // 更新移动速度加成
        updateMovementSpeedBonuses(player);
    }
    
    private void removeMovementSpeedEffect(Player player, int slot) {
        UUID playerId = player.getUUID();
        
        // 获取该玩家的移动速度加成映射
        Map<Integer, Float> slotBonuses = playerMovementSpeedBonuses.get(playerId);
        if (slotBonuses != null) {
            // 移除该槽位的移动速度加成
            slotBonuses.remove(slot);
            
            // 如果没有其他槽位的加成，清理映射
            if (slotBonuses.isEmpty()) {
                playerMovementSpeedBonuses.remove(playerId);
            }
            
            // 更新移动速度加成
            updateMovementSpeedBonuses(player);
        }
    }
    
    private void updateMovementSpeedBonuses(Player player) {
        UUID playerId = player.getUUID();
        float totalBonus = 0.0f;
        
        // 计算所有槽位的移动速度加成总和
        Map<Integer, Float> slotBonuses = playerMovementSpeedBonuses.get(playerId);
        if (slotBonuses != null) {
            for (float bonus : slotBonuses.values()) {
                totalBonus += bonus;
            }
        }
        
        // 将移动速度加成应用到玩家属性
        applyMovementSpeedBonusesToPlayer(player, totalBonus);
    }
    
    private void applyMovementSpeedBonusesToPlayer(Player player, float bonus) {
        // 应用移动速度加成
        applyAttributeModifier(player, Attributes.MOVEMENT_SPEED, "accessory_movement_speed", bonus);
        
        // 将移动速度加成数据存储到玩家NBT中，供其他系统使用
        if (!player.level().isClientSide) {
            player.getPersistentData().putFloat("fatality:movement_speed_bonus", bonus);
        }
    }
    
    private void applyAttributeModifier(Player player, Attribute attribute, String modifierName, float value) {
        if (value == 0.0f) {
            return;
        }
        
        AttributeInstance attributeInstance = player.getAttribute(attribute);
        if (attributeInstance != null) {
            // 移除旧的修改器
            UUID modifierUUID = UUID.nameUUIDFromBytes(("fatality:" + modifierName).getBytes());
            AttributeModifier oldModifier = attributeInstance.getModifier(modifierUUID);
            if (oldModifier != null) {
                attributeInstance.removeModifier(oldModifier);
            }
            
            // 添加新的修改器
            AttributeModifier newModifier = new AttributeModifier(
                modifierUUID,
                "Fatality Accessory Bonus",
                value,
                AttributeModifier.Operation.ADDITION
            );
            attributeInstance.addPermanentModifier(newModifier);
        }
    }
    
    @Override
    public void updateEffect(Player player, ItemStack accessory, int slot) {
        // 每tick更新移动速度效果
        if (!player.level().isClientSide) {
            // 重新计算并应用移动速度加成
            updateMovementSpeedBonuses(player);
        }
    }
    
    @Override
    public int getPriority() {
        return 45; // 移动速度效果优先级中等
    }
    
    /**
     * 清理玩家数据
     */
    public void cleanupPlayerData(Player player) {
        UUID playerId = player.getUUID();
        playerMovementSpeedBonuses.remove(playerId);
    }
}