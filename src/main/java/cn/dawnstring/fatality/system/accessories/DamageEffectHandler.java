package cn.dawnstring.fatality.system.accessories;

import cn.dawnstring.fatality.items.AccessoryItem;
import cn.dawnstring.fatality.system.AttributeSystem;
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
 * 伤害效果处理器
 * 处理提供伤害加成的饰品
 */
public class DamageEffectHandler extends BaseAccessoryEffectHandler {
    
    // 存储玩家伤害效果的数据：玩家UUID -> 槽位 -> 伤害加成数据
    private final Map<UUID, Map<Integer, DamageBonusData>> playerDamageBonuses = new HashMap<>();
    
    // 伤害加成数据类
    private static class DamageBonusData {
        final float meleeDamage;
        final float rangedDamage;
        final float magicDamage;
        final float panelDamage;
        final float meleeCritDamage;
        final float rangedCritDamage;
        final float magicCritDamage;
        
        DamageBonusData(float meleeDamage, float rangedDamage, float magicDamage, 
                      float panelDamage, float meleeCritDamage, float rangedCritDamage, float magicCritDamage) {
            this.meleeDamage = meleeDamage;
            this.rangedDamage = rangedDamage;
            this.magicDamage = magicDamage;
            this.panelDamage = panelDamage;
            this.meleeCritDamage = meleeCritDamage;
            this.rangedCritDamage = rangedCritDamage;
            this.magicCritDamage = magicCritDamage;
        }
    }
    
    public DamageEffectHandler(Class<? extends AccessoryItem> accessoryClass) {
        super("damage", accessoryClass);
    }
    
    @Override
    public void applyEffect(Player player, ItemStack accessory, int slot) {
        if (accessory.getItem() instanceof AccessoryItem accessoryItem) {
            // 创建伤害加成数据
            DamageBonusData bonusData = new DamageBonusData(
                accessoryItem.getMeleeDamageBonus(),
                accessoryItem.getRangedDamageBonus(),
                accessoryItem.getMagicDamageBonus(),
                accessoryItem.getPanelDamageBonus(),
                accessoryItem.getMeleeCriticalDamageBonus(),
                accessoryItem.getRangedCriticalDamageBonus(),
                accessoryItem.getMagicCriticalDamageBonus()
            );
            
            // 应用伤害效果
            applyDamageEffects(player, bonusData, slot);
        }
    }
    
    @Override
    public void removeEffect(Player player, ItemStack accessory, int slot) {
        removeDamageEffects(player, slot);
    }
    
    private void applyDamageEffects(Player player, DamageBonusData bonusData, int slot) {
        UUID playerId = player.getUUID();
        
        // 获取或创建该玩家的伤害加成映射
        Map<Integer, DamageBonusData> slotBonuses = playerDamageBonuses.computeIfAbsent(playerId, k -> new HashMap<>());
        
        // 记录该槽位的伤害加成数据
        slotBonuses.put(slot, bonusData);
        
        // 更新伤害加成
        updateDamageBonuses(player);
    }
    
    private void removeDamageEffects(Player player, int slot) {
        UUID playerId = player.getUUID();
        
        // 获取该玩家的伤害加成映射
        Map<Integer, DamageBonusData> slotBonuses = playerDamageBonuses.get(playerId);
        if (slotBonuses != null) {
            // 移除该槽位的伤害加成
            slotBonuses.remove(slot);
            
            // 如果没有其他槽位的加成，清理映射
            if (slotBonuses.isEmpty()) {
                playerDamageBonuses.remove(playerId);
            }
            
            // 更新伤害加成
            updateDamageBonuses(player);
        }
    }
    
    private void updateDamageBonuses(Player player) {
        UUID playerId = player.getUUID();
        
        // 计算所有槽位的伤害加成总和
        float totalMeleeDamage = 0.0f;
        float totalRangedDamage = 0.0f;
        float totalMagicDamage = 0.0f;
        float totalPanelDamage = 0.0f;
        float totalMeleeCritDamage = 0.0f;
        float totalRangedCritDamage = 0.0f;
        float totalMagicCritDamage = 0.0f;
        
        Map<Integer, DamageBonusData> slotBonuses = playerDamageBonuses.get(playerId);
        if (slotBonuses != null) {
            for (DamageBonusData bonusData : slotBonuses.values()) {
                totalMeleeDamage += bonusData.meleeDamage;
                totalRangedDamage += bonusData.rangedDamage;
                totalMagicDamage += bonusData.magicDamage;
                totalPanelDamage += bonusData.panelDamage;
                totalMeleeCritDamage += bonusData.meleeCritDamage;
                totalRangedCritDamage += bonusData.rangedCritDamage;
                totalMagicCritDamage += bonusData.magicCritDamage;
            }
        }
        
        // 将伤害加成应用到玩家属性
        applyDamageBonusesToPlayer(player, totalMeleeDamage, totalRangedDamage, totalMagicDamage,
                                   totalPanelDamage, totalMeleeCritDamage, totalRangedCritDamage, totalMagicCritDamage);
    }
    
    private void applyDamageBonusesToPlayer(Player player, float meleeDamage, float rangedDamage, 
                                          float magicDamage, float panelDamage, float meleeCritDamage,
                                          float rangedCritDamage, float magicCritDamage) {
        // 应用攻击力加成
        applyAttributeModifier(player, Attributes.ATTACK_DAMAGE, "accessory_attack_damage", meleeDamage);
        
        // 应用攻击速度加成
        applyAttributeModifier(player, Attributes.ATTACK_SPEED, "accessory_attack_speed", 0.0f);
        
        // 应用暴击伤害加成
        applyAttributeModifier(player, Attributes.ATTACK_DAMAGE, "accessory_crit_damage", meleeCritDamage + rangedCritDamage + magicCritDamage);
        
        // 将伤害加成数据存储到玩家NBT中，供战斗系统使用
        if (!player.level().isClientSide) {
            player.getPersistentData().putFloat("fatality:melee_damage_bonus", meleeDamage);
            player.getPersistentData().putFloat("fatality:ranged_damage_bonus", rangedDamage);
            player.getPersistentData().putFloat("fatality:magic_damage_bonus", magicDamage);
            player.getPersistentData().putFloat("fatality:panel_damage_bonus", panelDamage);
            player.getPersistentData().putFloat("fatality:melee_crit_damage_bonus", meleeCritDamage);
            player.getPersistentData().putFloat("fatality:ranged_crit_damage_bonus", rangedCritDamage);
            player.getPersistentData().putFloat("fatality:magic_crit_damage_bonus", magicCritDamage);
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
        // 每tick更新伤害效果
        if (!player.level().isClientSide) {
            // 重新计算并应用伤害加成
            updateDamageBonuses(player);
        }
    }
    
    @Override
    public int getPriority() {
        return 40; // 伤害效果优先级中等
    }
    
    /**
     * 清理玩家数据
     */
    public void cleanupPlayerData(Player player) {
        UUID playerId = player.getUUID();
        playerDamageBonuses.remove(playerId);
    }
}