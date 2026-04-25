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
 * 防御效果处理器
 * 处理提供防御加成的饰品
 */
public class DefenseEffectHandler extends BaseAccessoryEffectHandler {
    
    // 存储玩家防御效果的数据：玩家UUID -> 槽位 -> 防御加成数据
    private final Map<UUID, Map<Integer, DefenseBonusData>> playerDefenseBonuses = new HashMap<>();
    
    // 防御加成数据类
    private static class DefenseBonusData {
        final float defense;
        final float defensePercentage;
        final float damageReduction;
        final float armorValue;
        final float armorToughness;
        final float damageResistance;
        final float penetrationResistance;
        final float penetrationResistanceCoefficient;
        
        DefenseBonusData(float defense, float defensePercentage, float damageReduction,
                       float armorValue, float armorToughness, float damageResistance,
                       float penetrationResistance, float penetrationResistanceCoefficient) {
            this.defense = defense;
            this.defensePercentage = defensePercentage;
            this.damageReduction = damageReduction;
            this.armorValue = armorValue;
            this.armorToughness = armorToughness;
            this.damageResistance = damageResistance;
            this.penetrationResistance = penetrationResistance;
            this.penetrationResistanceCoefficient = penetrationResistanceCoefficient;
        }
    }
    
    public DefenseEffectHandler(Class<? extends AccessoryItem> accessoryClass) {
        super("defense", accessoryClass);
    }
    
    @Override
    public void applyEffect(Player player, ItemStack accessory, int slot) {
        if (accessory.getItem() instanceof AccessoryItem accessoryItem) {
            // 创建防御加成数据
            DefenseBonusData bonusData = new DefenseBonusData(
                accessoryItem.getDefenseBonus(),
                accessoryItem.getDefensePercentageBonus(),
                accessoryItem.getDamageReductionBonus(),
                accessoryItem.getArmorValueBonus(),
                accessoryItem.getArmorToughnessBonus(),
                accessoryItem.getDamageResistanceBonus(),
                accessoryItem.getPenetrationResistanceBonus(),
                accessoryItem.getPenetrationResistanceCoefficientBonus()
            );
            
            // 应用防御效果
            applyDefenseEffects(player, bonusData, slot);
        }
    }
    
    @Override
    public void removeEffect(Player player, ItemStack accessory, int slot) {
        removeDefenseEffects(player, slot);
    }
    
    private void applyDefenseEffects(Player player, DefenseBonusData bonusData, int slot) {
        UUID playerId = player.getUUID();
        
        // 获取或创建该玩家的防御加成映射
        Map<Integer, DefenseBonusData> slotBonuses = playerDefenseBonuses.computeIfAbsent(playerId, k -> new HashMap<>());
        
        // 记录该槽位的防御加成数据
        slotBonuses.put(slot, bonusData);
        
        // 更新防御加成
        updateDefenseBonuses(player);
    }
    
    private void removeDefenseEffects(Player player, int slot) {
        UUID playerId = player.getUUID();
        
        // 获取该玩家的防御加成映射
        Map<Integer, DefenseBonusData> slotBonuses = playerDefenseBonuses.get(playerId);
        if (slotBonuses != null) {
            // 移除该槽位的防御加成
            slotBonuses.remove(slot);
            
            // 如果没有其他槽位的加成，清理映射
            if (slotBonuses.isEmpty()) {
                playerDefenseBonuses.remove(playerId);
            }
            
            // 更新防御加成
            updateDefenseBonuses(player);
        }
    }
    
    private void updateDefenseBonuses(Player player) {
        UUID playerId = player.getUUID();
        
        // 计算所有槽位的防御加成总和
        float totalDefense = 0.0f;
        float totalDefensePercentage = 0.0f;
        float totalDamageReduction = 0.0f;
        float totalArmorValue = 0.0f;
        float totalArmorToughness = 0.0f;
        float totalDamageResistance = 0.0f;
        float totalPenetrationResistance = 0.0f;
        float totalPenetrationResistanceCoefficient = 0.0f;
        
        Map<Integer, DefenseBonusData> slotBonuses = playerDefenseBonuses.get(playerId);
        if (slotBonuses != null) {
            for (DefenseBonusData bonusData : slotBonuses.values()) {
                totalDefense += bonusData.defense;
                totalDefensePercentage += bonusData.defensePercentage;
                totalDamageReduction += bonusData.damageReduction;
                totalArmorValue += bonusData.armorValue;
                totalArmorToughness += bonusData.armorToughness;
                totalDamageResistance += bonusData.damageResistance;
                totalPenetrationResistance += bonusData.penetrationResistance;
                totalPenetrationResistanceCoefficient += bonusData.penetrationResistanceCoefficient;
            }
        }
        
        // 将防御加成应用到玩家属性
        applyDefenseBonusesToPlayer(player, totalDefense, totalDefensePercentage, totalDamageReduction,
                                    totalArmorValue, totalArmorToughness, totalDamageResistance,
                                    totalPenetrationResistance, totalPenetrationResistanceCoefficient);
    }
    
    private void applyDefenseBonusesToPlayer(Player player, float defense, float defensePercentage, 
                                             float damageReduction, float armorValue, float armorToughness,
                                             float damageResistance, float penetrationResistance,
                                             float penetrationResistanceCoefficient) {
        // 应用护甲值加成
        applyAttributeModifier(player, Attributes.ARMOR, "accessory_armor", armorValue);
        
        // 应用护甲韧性加成
        applyAttributeModifier(player, Attributes.ARMOR_TOUGHNESS, "accessory_armor_toughness", armorToughness);
        
        // 将防御加成数据存储到玩家NBT中，供战斗系统使用
        if (!player.level().isClientSide) {
            player.getPersistentData().putFloat("fatality:defense_bonus", defense);
            player.getPersistentData().putFloat("fatality:defense_percentage_bonus", defensePercentage);
            player.getPersistentData().putFloat("fatality:damage_reduction_bonus", damageReduction);
            player.getPersistentData().putFloat("fatality:armor_value_bonus", armorValue);
            player.getPersistentData().putFloat("fatality:armor_toughness_bonus", armorToughness);
            player.getPersistentData().putFloat("fatality:damage_resistance_bonus", damageResistance);
            player.getPersistentData().putFloat("fatality:penetration_resistance_bonus", penetrationResistance);
            player.getPersistentData().putFloat("fatality:penetration_resistance_coefficient_bonus", penetrationResistanceCoefficient);
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
        // 每tick更新防御效果
        if (!player.level().isClientSide) {
            // 重新计算并应用防御加成
            updateDefenseBonuses(player);
        }
    }
    
    @Override
    public int getPriority() {
        return 35; // 防御效果优先级中等
    }
    
    /**
     * 清理玩家数据
     */
    public void cleanupPlayerData(Player player) {
        UUID playerId = player.getUUID();
        playerDefenseBonuses.remove(playerId);
    }
}