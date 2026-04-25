package cn.dawnstring.fatality.core.accessories;

import cn.dawnstring.fatality.api.accessories.AccessoryEffectHandler;
import cn.dawnstring.fatality.api.accessories.IAccessorySystem;
import cn.dawnstring.fatality.api.attributes.AttributeModifier;
import cn.dawnstring.fatality.api.attributes.AttributeModifier.ModifierType;
import cn.dawnstring.fatality.api.events.AccessoryEquipEvent;
import cn.dawnstring.fatality.api.events.AccessoryEvent;
import cn.dawnstring.fatality.api.events.AccessoryUnequipEvent;
import cn.dawnstring.fatality.api.events.PlayerAttributeEvent;
import cn.dawnstring.fatality.core.attributes.AttributeSystemImpl;
import cn.dawnstring.fatality.core.events.FatalityEventBus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 饰品系统实现类
 */
public class AccessorySystemImpl implements IAccessorySystem {
    
    private static final AccessorySystemImpl INSTANCE = new AccessorySystemImpl();
    
    private final Map<Player, ItemStack[]> equippedAccessories = new ConcurrentHashMap<>();
    private final List<AccessoryEffectHandler> effectHandlers = new ArrayList<>();
    private final int MAX_SLOTS = 6; // 最大饰品槽位数量
    
    private AccessorySystemImpl() {
        // 私有构造函数
    }
    
    public static AccessorySystemImpl getInstance() {
        return INSTANCE;
    }
    
    @Override
    public boolean equipAccessory(Player player, ItemStack accessory, int slot) {
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
        return MAX_SLOTS; // 可根据玩家等级或其他条件扩展
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
        AttributeSystemImpl attributeSystem = AttributeSystemImpl.getInstance();
        
        // 这里需要根据实际情况实现，暂时使用简单的方式
        // 在实际实现中，应该记录每个饰品添加的修改器ID
    }
    
    /**
     * 应用饰品的属性加成
     */
    private void applyAttributeBonuses(Player player, ItemStack accessory, int slot) {
        // 这里需要根据饰品的具体属性来应用加成
        // 暂时使用示例实现
        
        AttributeSystemImpl attributeSystem = AttributeSystemImpl.getInstance();
        String accessoryId = accessory.getItem().getDescriptionId();
        
        // 示例：根据饰品类型应用不同的属性加成
        if (accessoryId.contains("attack")) {
            attributeSystem.addAttributeModifier(player, new AttributeModifier(
                "accessory_" + slot + "_attack", "attack_damage", 5.0f,
                ModifierType.FLAT, -1, accessoryId
            ));
        }
        
        if (accessoryId.contains("defense")) {
            attributeSystem.addAttributeModifier(player, new AttributeModifier(
                "accessory_" + slot + "_defense", "defense", 3.0f,
                ModifierType.FLAT, -1, accessoryId
            ));
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
     * 检查物品是否为饰品
     */
    private boolean isAccessoryItem(ItemStack itemStack) {
        // 这里需要根据实际实现来判断
        // 暂时返回true，实际实现中应该检查物品类型
        return true;
    }
    
    /**
     * 获取或创建玩家的饰品数组
     */
    private ItemStack[] getOrCreateAccessoryArray(Player player) {
        return equippedAccessories.computeIfAbsent(player, k -> new ItemStack[MAX_SLOTS]);
    }
    
    /**
     * 清理玩家数据（当玩家退出时调用）
     */
    public void cleanupPlayerData(Player player) {
        equippedAccessories.remove(player);
    }
}