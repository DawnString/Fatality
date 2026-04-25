package cn.dawnstring.fatality.system;

import cn.dawnstring.fatality.api.accessories.AccessoryEffectHandler;
import cn.dawnstring.fatality.api.accessories.IAccessorySystem;
import cn.dawnstring.fatality.api.attributes.AttributeModifier;
import cn.dawnstring.fatality.api.attributes.AttributeModifier.ModifierType;
import cn.dawnstring.fatality.api.events.AccessoryEquipEvent;
import cn.dawnstring.fatality.api.events.AccessoryUnequipEvent;
import cn.dawnstring.fatality.core.events.FatalityEventBus;
import cn.dawnstring.fatality.items.AccessoryItem;
import cn.dawnstring.fatality.utils.GameConstants;
import cn.dawnstring.fatality.utils.ValidationUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 饰品系统 - 管理玩家的饰品装备和效果（重构版）
 * 基于事件驱动架构重构
 */
public class AccessorySystem implements IAccessorySystem {
    
    private static final AccessorySystem INSTANCE = new AccessorySystem();
    
    private final Map<Player, ItemStack[]> equippedAccessories = new ConcurrentHashMap<>();
    private final Map<Player, Set<String>> playerModifierIds = new ConcurrentHashMap<>();
    private final List<AccessoryEffectHandler> effectHandlers = new ArrayList<>();
    
    private AccessorySystem() {
        // 私有构造函数
    }
    
    public static AccessorySystem getInstance() {
        return INSTANCE;
    }
    
    @Override
    public boolean equipAccessory(Player player, ItemStack accessory, int slot) {
        ValidationUtils.validatePlayer(player);
        ValidationUtils.validateItemStack(accessory);
        ValidationUtils.validateSlot(slot, getAccessorySlotCount(player));
        
        if (!canEquipAccessory(player, accessory, slot)) {
            return false;
        }
        
        ItemStack[] accessories = getOrCreateAccessoryArray(player);
        
        // 检查槽位是否已占用
        if (accessories[slot] != null && !accessories[slot].isEmpty()) {
            return false;
        }
        
        accessories[slot] = accessory.copy();
        
        // 触发饰品装备事件
        FatalityEventBus.getInstance().post(new AccessoryEquipEvent(player, accessory, slot));
        
        // 应用饰品效果
        applyAccessoryEffects(player);
        
        return true;
    }
    
    @Override
    public ItemStack unequipAccessory(Player player, int slot) {
        ItemStack[] accessories = equippedAccessories.get(player);
        if (accessories == null || slot < 0 || slot >= accessories.length) {
            return ItemStack.EMPTY;
        }
        
        ItemStack accessory = accessories[slot];
        if (accessory.isEmpty()) {
            return ItemStack.EMPTY;
        }
        
        accessories[slot] = ItemStack.EMPTY;
        
        // 触发饰品卸下事件
        FatalityEventBus.getInstance().post(new AccessoryUnequipEvent(player, accessory, slot));
        
        // 重新应用饰品效果
        applyAccessoryEffects(player);
        
        return accessory;
    }
    
    @Override
    public List<ItemStack> getEquippedAccessories(Player player) {
        ItemStack[] accessories = equippedAccessories.get(player);
        if (accessories == null) {
            return Collections.emptyList();
        }
        
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack accessory : accessories) {
            if (accessory != null && !accessory.isEmpty()) {
                result.add(accessory);
            }
        }
        
        return result;
    }
    
    @Override
    public ItemStack getAccessoryInSlot(Player player, int slot) {
        ItemStack[] accessories = equippedAccessories.get(player);
        if (accessories == null || slot < 0 || slot >= accessories.length) {
            return ItemStack.EMPTY;
        }
        
        return accessories[slot] != null ? accessories[slot] : ItemStack.EMPTY;
    }
    
    @Override
    public boolean canEquipAccessory(Player player, ItemStack accessory, int slot) {
        if (player == null || accessory.isEmpty()) {
            return false;
        }
        
        if (slot < 0 || slot >= getAccessorySlotCount(player)) {
            return false;
        }
        
        // 检查饰品类型是否支持
        if (!isAccessoryItem(accessory)) {
            return false;
        }
        
        // 检查槽位是否已占用
        ItemStack currentAccessory = getAccessoryInSlot(player, slot);
        return currentAccessory.isEmpty();
    }
    
    @Override
    public int getAccessorySlotCount(Player player) {
        // 根据玩家等级、成就等条件扩展槽位数量
        int baseSlots = GameConstants.DEFAULT_ACCESSORY_SLOTS;
        
        // 根据玩家等级增加槽位
        int playerLevel = player.experienceLevel;
        if (playerLevel >= 50) {
            baseSlots += 1;
        }
        if (playerLevel >= 100) {
            baseSlots += 1;
        }
        
        // 可以根据成就或其他条件进一步扩展槽位数量
        // 例如：完成特定成就解锁额外槽位
        
        return baseSlots;
    }
    
    @Override
    public void registerEffectHandler(AccessoryEffectHandler effectHandler) {
        effectHandlers.add(effectHandler);
        effectHandlers.sort(Comparator.comparingInt(AccessoryEffectHandler::getPriority));
    }
    
    @Override
    public void applyAccessoryEffects(Player player) {
        // 先移除所有饰品效果
        removeAccessoryEffects(player);
        
        // 应用每个饰品的属性加成
        List<ItemStack> accessories = getEquippedAccessories(player);
        for (int i = 0; i < accessories.size(); i++) {
            ItemStack accessory = accessories.get(i);
            applyAttributeBonuses(player, accessory, i);
            applySpecialEffects(player, accessory, i);
        }
    }
    
    @Override
    public void removeAccessoryEffects(Player player) {
        // 移除所有饰品相关的属性修改器
        AttributeSystem attributeSystem = AttributeSystem.getInstance();
        Set<String> modifierIds = playerModifierIds.get(player);
        
        if (modifierIds != null) {
            for (String modifierId : modifierIds) {
                attributeSystem.removeAttributeModifier(player, modifierId);
            }
            modifierIds.clear();
        }
        
        // 移除特殊效果
        List<ItemStack> accessories = getEquippedAccessories(player);
        for (int i = 0; i < accessories.size(); i++) {
            ItemStack accessory = accessories.get(i);
            removeSpecialEffects(player, accessory, i);
        }
    }
    
    /**
     * 应用饰品的属性加成
     */
    private void applyAttributeBonuses(Player player, ItemStack accessory, int slot) {
        if (!(accessory.getItem() instanceof AccessoryItem accessoryItem)) {
            return;
        }
        
        AttributeSystem attributeSystem = AttributeSystem.getInstance();
        Set<String> modifierIds = playerModifierIds.computeIfAbsent(player, k -> new HashSet<>());
        
        // ========== 基础属性加成 ==========
        applyAttributeBonus(attributeSystem, modifierIds, player, accessoryItem, slot, "health_regen", accessoryItem.getHealthRegenerationBonus(), ModifierType.FLAT);
        applyAttributeBonus(attributeSystem, modifierIds, player, accessoryItem, slot, "mana_regen", accessoryItem.getManaRegenerationBonus(), ModifierType.FLAT);
        applyAttributeBonus(attributeSystem, modifierIds, player, accessoryItem, slot, "attack_speed", accessoryItem.getAttackSpeedBonus(), ModifierType.FLAT);
        applyAttributeBonus(attributeSystem, modifierIds, player, accessoryItem, slot, "crit_chance", accessoryItem.getCriticalChanceBonus(), ModifierType.FLAT);
        applyAttributeBonus(attributeSystem, modifierIds, player, accessoryItem, slot, "damage_reduction", accessoryItem.getDamageReductionBonus(), ModifierType.FLAT);
        applyAttributeBonus(attributeSystem, modifierIds, player, accessoryItem, slot, "movement_speed", accessoryItem.getMovementSpeedBonus(), ModifierType.FLAT);
        
        // ========== 伤害数值加成 ==========
        applyAttributeBonus(attributeSystem, modifierIds, player, accessoryItem, slot, "melee_damage_value", accessoryItem.getMeleeDamageValueBonus(), ModifierType.FLAT);
        applyAttributeBonus(attributeSystem, modifierIds, player, accessoryItem, slot, "ranged_damage_value", accessoryItem.getRangedDamageValueBonus(), ModifierType.FLAT);
        applyAttributeBonus(attributeSystem, modifierIds, player, accessoryItem, slot, "magic_damage_value", accessoryItem.getMagicDamageValueBonus(), ModifierType.FLAT);
        applyAttributeBonus(attributeSystem, modifierIds, player, accessoryItem, slot, "panel_damage_value", accessoryItem.getPanelDamageValueBonus(), ModifierType.FLAT);
        
        // ========== 伤害百分比加成 ==========
        applyAttributeBonus(attributeSystem, modifierIds, player, accessoryItem, slot, "melee_damage", accessoryItem.getMeleeDamageBonus(), ModifierType.FLAT);
        applyAttributeBonus(attributeSystem, modifierIds, player, accessoryItem, slot, "ranged_damage", accessoryItem.getRangedDamageBonus(), ModifierType.FLAT);
        applyAttributeBonus(attributeSystem, modifierIds, player, accessoryItem, slot, "magic_damage", accessoryItem.getMagicDamageBonus(), ModifierType.FLAT);
        applyAttributeBonus(attributeSystem, modifierIds, player, accessoryItem, slot, "panel_damage", accessoryItem.getPanelDamageBonus(), ModifierType.FLAT);
        
        // ========== 暴击伤害加成 ==========
        applyAttributeBonus(attributeSystem, modifierIds, player, accessoryItem, slot, "crit_damage", accessoryItem.getMeleeCriticalDamageBonus() + accessoryItem.getRangedCriticalDamageBonus() + accessoryItem.getMagicCriticalDamageBonus(), ModifierType.FLAT);
        applyAttributeBonus(attributeSystem, modifierIds, player, accessoryItem, slot, "melee_crit_damage", accessoryItem.getMeleeCriticalDamageBonus(), ModifierType.FLAT);
        applyAttributeBonus(attributeSystem, modifierIds, player, accessoryItem, slot, "ranged_crit_damage", accessoryItem.getRangedCriticalDamageBonus(), ModifierType.FLAT);
        applyAttributeBonus(attributeSystem, modifierIds, player, accessoryItem, slot, "magic_crit_damage", accessoryItem.getMagicCriticalDamageBonus(), ModifierType.FLAT);
        
        // ========== 防御属性加成 ==========
        applyAttributeBonus(attributeSystem, modifierIds, player, accessoryItem, slot, "defense", accessoryItem.getDefenseBonus(), ModifierType.FLAT);
        applyAttributeBonus(attributeSystem, modifierIds, player, accessoryItem, slot, "defense_percentage", accessoryItem.getDefensePercentageBonus(), ModifierType.FLAT);
        
        // ========== 生命值加成 ==========
        applyAttributeBonus(attributeSystem, modifierIds, player, accessoryItem, slot, "health", accessoryItem.getHealthBonus(), ModifierType.FLAT);
        applyAttributeBonus(attributeSystem, modifierIds, player, accessoryItem, slot, "health_percentage", accessoryItem.getHealthPercentageBonus(), ModifierType.FLAT);
        
        // ========== 法力值加成 ==========
        applyAttributeBonus(attributeSystem, modifierIds, player, accessoryItem, slot, "max_mana", accessoryItem.getMaxManaBonus(), ModifierType.FLAT);
        
        // ========== 伤害与护甲相关属性 ==========
        applyAttributeBonus(attributeSystem, modifierIds, player, accessoryItem, slot, "attack_damage_percentage", accessoryItem.getAttackDamagePercentageBonus(), ModifierType.FLAT);
        applyAttributeBonus(attributeSystem, modifierIds, player, accessoryItem, slot, "damage_fluctuation", accessoryItem.getDamageFluctuationBonus(), ModifierType.FLAT);
        applyAttributeBonus(attributeSystem, modifierIds, player, accessoryItem, slot, "armor_value", accessoryItem.getArmorValueBonus(), ModifierType.FLAT);
        applyAttributeBonus(attributeSystem, modifierIds, player, accessoryItem, slot, "damage_resistance", accessoryItem.getDamageResistanceBonus(), ModifierType.FLAT);
        applyAttributeBonus(attributeSystem, modifierIds, player, accessoryItem, slot, "penetration_resistance", accessoryItem.getPenetrationResistanceBonus(), ModifierType.FLAT);
        applyAttributeBonus(attributeSystem, modifierIds, player, accessoryItem, slot, "penetration_resistance_coefficient", accessoryItem.getPenetrationResistanceCoefficientBonus(), ModifierType.FLAT);
        applyAttributeBonus(attributeSystem, modifierIds, player, accessoryItem, slot, "armor_toughness", accessoryItem.getArmorToughnessBonus(), ModifierType.FLAT);
    }
    
    /**
     * 应用单个属性加成
     */
    private void applyAttributeBonus(AttributeSystem attributeSystem, Set<String> modifierIds, Player player, 
                                    AccessoryItem accessoryItem, int slot, String attributeId, float bonus, ModifierType type) {
        if (bonus != 0.0f) {
            String modifierId = "accessory_" + slot + "_" + attributeId + "_" + accessoryItem.getClass().getSimpleName();
            AttributeModifier modifier = new AttributeModifier(
                modifierId, attributeId, bonus, type, -1, accessoryItem.getDescriptionId()
            );
            attributeSystem.addAttributeModifier(player, modifier);
            modifierIds.add(modifierId);
        }
    }
    
    /**
     * 应用饰品的特殊效果
     */
    private void applySpecialEffects(Player player, ItemStack accessory, int slot) {
        for (AccessoryEffectHandler handler : effectHandlers) {
            if (handler.supports(accessory)) {
                handler.applyEffect(player, accessory, slot);
            }
        }
    }
    
    /**
     * 移除饰品的特殊效果
     */
    private void removeSpecialEffects(Player player, ItemStack accessory, int slot) {
        for (AccessoryEffectHandler handler : effectHandlers) {
            if (handler.supports(accessory)) {
                handler.removeEffect(player, accessory, slot);
            }
        }
    }
    
    /**
     * 检查物品是否为饰品
     */
    private boolean isAccessoryItem(ItemStack itemStack) {
        return itemStack.getItem() instanceof AccessoryItem;
    }
    
    /**
     * 获取或创建玩家的饰品数组
     */
    private ItemStack[] getOrCreateAccessoryArray(Player player) {
        return equippedAccessories.computeIfAbsent(player, k -> new ItemStack[getAccessorySlotCount(player)]);
    }
    
    /**
     * 初始化玩家数据（当玩家登录时调用）
     */
    public void initializePlayer(Player player) {
        // 初始化玩家饰品数据
        if (!equippedAccessories.containsKey(player)) {
            equippedAccessories.put(player, new ItemStack[GameConstants.DEFAULT_ACCESSORY_SLOTS]);
        }
        if (!playerModifierIds.containsKey(player)) {
            playerModifierIds.put(player, new HashSet<>());
        }
        
        // 初始化饰品效果
        ItemStack[] accessories = getOrCreateAccessoryArray(player);
        for (int i = 0; i < accessories.length; i++) {
            ItemStack accessory = accessories[i];
            if (accessory != null && !accessory.isEmpty()) {
                applyAttributeBonuses(player, accessory, i);
            }
        }
    }
    
    /**
     * 清理玩家数据（当玩家退出时调用）
     */
    public void cleanupPlayerData(Player player) {
        equippedAccessories.remove(player);
        playerModifierIds.remove(player);
    }
    
    /**
     * 清理玩家（兼容性方法）
     */
    public void cleanupPlayer(Player player) {
        cleanupPlayerData(player);
    }
    
    /**
     * 兼容性方法 - 保持与旧系统的API兼容
     */
    public static List<ItemStack> getPlayerAccessories(Player player) {
        return getInstance().getEquippedAccessories(player);
    }
    
    public static boolean hasAccessoryEquipped(Player player, ItemStack accessory) {
        List<ItemStack> accessories = getPlayerAccessories(player);
        return accessories.contains(accessory);
    }
}