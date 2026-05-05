package cn.dawnstring.fatality.system;

import cn.dawnstring.fatality.api.attributes.AttributeCalculator;
import cn.dawnstring.fatality.api.attributes.AttributeModifier;
import cn.dawnstring.fatality.api.attributes.IAttributeSystem;
import cn.dawnstring.fatality.api.events.PlayerAttributeEvent;
import cn.dawnstring.fatality.api.systems.IModSystem;
import cn.dawnstring.fatality.core.events.FatalityEventBus;
import cn.dawnstring.fatality.items.AccessoryItem;
import cn.dawnstring.fatality.items.accessory.HeartOfTheElements;
import cn.dawnstring.fatality.items.accessory.MechanicalHeart;
import cn.dawnstring.fatality.utils.AttributeCache;
import cn.dawnstring.fatality.utils.GameConstants;
import cn.dawnstring.fatality.utils.PlayerBaseAttributes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import cn.dawnstring.fatality.inventory.AccessoryInventory;
import cn.dawnstring.fatality.system.HealthRegenerationHandler;
import cn.dawnstring.fatality.system.ManaSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 属性系统 - 管理玩家的各种属性计算（重构版）
 * 基于事件驱动架构重构
 */
public class AttributeSystem implements IAttributeSystem, IModSystem
{
    // 属性计算器映射
    private static final Map<String, AttributeCalculator> ATTRIBUTE_CALCULATORS = new ConcurrentHashMap<>();
    
    // 属性修改器映射
    private static final Map<Player, Map<String, AttributeModifier>> PLAYER_MODIFIERS = new ConcurrentHashMap<>();

    static {
        // ========== 基础属性计算器 ==========
        registerAttributeStatic("health_regen", AttributeSystem::calculateHealthRegeneration);
        registerAttributeStatic("mana_regen", AttributeSystem::calculateManaRegeneration);
        registerAttributeStatic("attack_speed", AttributeSystem::calculateAttackSpeed);
        registerAttributeStatic("crit_chance", AttributeSystem::calculateCritChance);
        registerAttributeStatic("damage_reduction", AttributeSystem::calculateDamageReduction);
        registerAttributeStatic("movement_speed", AttributeSystem::calculateMovementSpeed);
        registerAttributeStatic("luck", AttributeSystem::calculateLuck);
        
        // ========== 伤害数值计算器 ==========
        registerAttributeStatic("melee_damage_value", AttributeSystem::calculateMeleeDamageValue);
        registerAttributeStatic("ranged_damage_value", AttributeSystem::calculateRangedDamageValue);
        registerAttributeStatic("magic_damage_value", AttributeSystem::calculateMagicDamageValue);
        registerAttributeStatic("panel_damage_value", AttributeSystem::calculatePanelDamageValue);
        
        // ========== 伤害百分比计算器 ==========
        registerAttributeStatic("melee_damage", AttributeSystem::calculateMeleeDamage);
        registerAttributeStatic("ranged_damage", AttributeSystem::calculateRangedDamage);
        registerAttributeStatic("magic_damage", AttributeSystem::calculateMagicDamage);
        registerAttributeStatic("panel_damage", AttributeSystem::calculatePanelDamage);
        
        // ========== 暴击伤害计算器 ==========
        registerAttributeStatic("crit_damage", AttributeSystem::calculateCritDamage);
        registerAttributeStatic("melee_crit_damage", AttributeSystem::calculateMeleeCritDamage);
        registerAttributeStatic("ranged_crit_damage", AttributeSystem::calculateRangedCritDamage);
        registerAttributeStatic("magic_crit_damage", AttributeSystem::calculateMagicCritDamage);
        
        // ========== 防御属性计算器 ==========
        registerAttributeStatic("defense", AttributeSystem::calculateDefense);
        registerAttributeStatic("defense_percentage", AttributeSystem::calculateDefensePercentage);
        
        // ========== 生命值计算器 ==========
        registerAttributeStatic("health", AttributeSystem::calculateHealth);
        registerAttributeStatic("health_percentage", AttributeSystem::calculateHealthPercentage);
        
        // ========== 法力值计算器 ==========
        registerAttributeStatic("max_mana", AttributeSystem::calculateMaxMana);
        
        // ========== 伤害与护甲相关计算器 ==========
        registerAttributeStatic("attack_damage_percentage", AttributeSystem::calculateAttackDamagePercentage);
        registerAttributeStatic("damage_fluctuation", AttributeSystem::calculateDamageFluctuation);
        registerAttributeStatic("armor_value", AttributeSystem::calculateArmorValue);
        registerAttributeStatic("damage_resistance", AttributeSystem::calculateDamageResistance);
        registerAttributeStatic("penetration_resistance", AttributeSystem::calculatePenetrationResistance);
        registerAttributeStatic("penetration_resistance_coefficient", AttributeSystem::calculatePenetrationResistanceCoefficient);
        registerAttributeStatic("armor_toughness", AttributeSystem::calculateArmorToughness);
    }

    /**
     * 统一获取属性值的方法（带缓存）
     */
    public static float getAttributeValue(Player player, String attributeName) {
        // 尝试从缓存获取
        float cachedValue = AttributeCache.getCachedAttribute(player, attributeName);
        if (!Float.isNaN(cachedValue)) {
            return cachedValue; // 返回缓存值
        }
        
        // 缓存未命中，计算属性值
        AttributeCalculator calculator = ATTRIBUTE_CALCULATORS.get(attributeName);
        if (calculator != null) {
            float baseValue = calculator.calculate(player);
            float modifiedValue = applyModifiers(player, attributeName, baseValue);
            
            // 缓存计算结果
            AttributeCache.cacheAttribute(player, attributeName, modifiedValue);
            
            return modifiedValue;
        }
        return 0.0f;
    }

    @Override
    public float getAttribute(Player player, String attributeId) {
        return getAttributeValue(player, attributeId);
    }

    @Override
    public void registerAttribute(String attributeId, AttributeCalculator calculator) {
        ATTRIBUTE_CALCULATORS.put(attributeId, calculator);
    }
    
    /**
     * 静态方法注册属性（兼容性）
     */
    public static void registerAttributeStatic(String attributeId, AttributeCalculator calculator) {
        ATTRIBUTE_CALCULATORS.put(attributeId, calculator);
    }
    
    /**
     * 获取单例实例
     */
    public static AttributeSystem getInstance() {
        return INSTANCE;
    }
    
    @Override
    public String getSystemId() {
        return "attribute";
    }
    
    @Override
    public void initialize() {
        // 系统初始化逻辑
    }
    
    @Override
    public void onPlayerJoin(Player player) {
        initializePlayer(player);
    }
    
    @Override
    public void onPlayerLeave(Player player) {
        cleanupPlayerData(player);
    }
    
    private static final AttributeSystem INSTANCE = new AttributeSystem();

    @Override
    public void addAttributeModifier(Player player, AttributeModifier modifier) {
        Map<String, AttributeModifier> modifiers = PLAYER_MODIFIERS.computeIfAbsent(player, k -> new ConcurrentHashMap<>());
        
        // 保存旧值用于事件触发
        float oldValue = getAttributeValue(player, modifier.getAttributeId());
        
        modifiers.put(modifier.getModifierId(), modifier);
        
        // 触发属性变化事件
        float newValue = getAttributeValue(player, modifier.getAttributeId());
        if (oldValue != newValue) {
            FatalityEventBus.getInstance().post(new PlayerAttributeEvent(
                player, modifier.getAttributeId(), oldValue, newValue, 
                PlayerAttributeEvent.AttributeChangeSource.SYSTEM
            ));
        }
    }

    @Override
    public void removeAttributeModifier(Player player, String modifierId) {
        Map<String, AttributeModifier> modifiers = PLAYER_MODIFIERS.get(player);
        if (modifiers != null) {
            AttributeModifier modifier = modifiers.get(modifierId);
            if (modifier != null) {
                // 保存旧值用于事件触发
                float oldValue = getAttributeValue(player, modifier.getAttributeId());
                
                modifiers.remove(modifierId);
                
                // 触发属性变化事件
                float newValue = getAttributeValue(player, modifier.getAttributeId());
                if (oldValue != newValue) {
                    FatalityEventBus.getInstance().post(new PlayerAttributeEvent(
                        player, modifier.getAttributeId(), oldValue, newValue,
                        PlayerAttributeEvent.AttributeChangeSource.SYSTEM
                    ));
                }
            }
        }
    }

    @Override
    public Iterable<String> getRegisteredAttributes() {
        return ATTRIBUTE_CALCULATORS.keySet();
    }

    @Override
    public boolean isAttributeRegistered(String attributeId) {
        return ATTRIBUTE_CALCULATORS.containsKey(attributeId);
    }

    /**
     * 应用属性修改器
     */
    private static float applyModifiers(Player player, String attributeId, float baseValue) {
        Map<String, AttributeModifier> modifiers = PLAYER_MODIFIERS.get(player);
        if (modifiers == null) {
            return baseValue;
        }
        
        float result = baseValue;
        
        for (AttributeModifier modifier : modifiers.values()) {
            if (modifier.getAttributeId().equals(attributeId)) {
                switch (modifier.getType()) {
                    case FLAT:
                        result += modifier.getValue();
                        break;
                    case MULTIPLY:
                        result *= modifier.getValue();
                        break;
                    case PERCENTAGE:
                        result *= (1.0f + modifier.getValue() / 100.0f);
                        break;
                }
            }
        }
        
        return result;
    }

    // ========== 具体属性计算方法 ==========

    /**
     * 计算生命恢复速度
     */
    private static float calculateHealthRegeneration(Player player) {
        float baseRegen = PlayerBaseAttributes.getBaseHealthRegenRate(player);
        float accessoryBonus = calculateAccessoryBonus(player, AccessoryItem::getHealthRegenerationBonus);
        return baseRegen + accessoryBonus;
    }

    /**
     * 计算法力恢复速度
     */
    private static float calculateManaRegeneration(Player player) {
        float baseRegen = PlayerBaseAttributes.getBaseManaRegenRate(player);
        float accessoryBonus = calculateAccessoryBonus(player, AccessoryItem::getManaRegenerationBonus);
        return baseRegen + accessoryBonus;
    }

    /**
     * 计算攻击力
     */
    private static float calculateAttackDamage(Player player) {
        float baseDamage = 1.0f; // 基础攻击力
        float accessoryBonus = calculateAccessoryBonus(player, AccessoryItem::getMeleeDamageBonus);
        return baseDamage + accessoryBonus;
    }

    /**
     * 计算攻击速度
     */
    private static float calculateAttackSpeed(Player player) {
        float baseSpeed = (float) player.getAttributeValue(Attributes.ATTACK_SPEED);
        float accessoryBonus = calculateAccessoryBonus(player, AccessoryItem::getAttackSpeedBonus);
        return baseSpeed + accessoryBonus;
    }

    /**
     * 计算暴击率
     */
    private static float calculateCritChance(Player player) {
        return calculateAccessoryBonus(player, AccessoryItem::getCriticalChanceBonus);
    }

    /**
     * 计算伤害减免
     */
    private static float calculateDamageReduction(Player player) {
        return calculateAccessoryBonus(player, AccessoryItem::getDamageReductionBonus);
    }

    /**
     * 计算移动速度
     */
    private static float calculateMovementSpeed(Player player) {
        float baseSpeed = (float) player.getAttributeValue(Attributes.MOVEMENT_SPEED);
        float accessoryBonus = calculateAccessoryBonus(player, AccessoryItem::getMovementSpeedBonus);
        return baseSpeed + accessoryBonus;
    }

    /**
     * 计算总暴击伤害
     */
    private static float calculateCritDamage(Player player) {
        return calculateAccessoryBonus(player, accessory ->
                accessory.getMeleeCriticalDamageBonus() +
                        accessory.getRangedCriticalDamageBonus() +
                        accessory.getMagicCriticalDamageBonus()
        );
    }

    /**
     * 计算近战暴击伤害
     */
    private static float calculateMeleeCritDamage(Player player) {
        return calculateAccessoryBonus(player, AccessoryItem::getMeleeCriticalDamageBonus);
    }

    /**
     * 计算远程暴击伤害
     */
    private static float calculateRangedCritDamage(Player player) {
        return calculateAccessoryBonus(player, AccessoryItem::getRangedCriticalDamageBonus);
    }

    /**
     * 计算魔法暴击伤害
     */
    private static float calculateMagicCritDamage(Player player) {
        return calculateAccessoryBonus(player, AccessoryItem::getMagicCriticalDamageBonus);
    }

    /**
     * 计算魔法伤害加成（百分比）
     */
    private static float calculateMagicDamage(Player player) {
        return calculateAccessoryBonus(player, AccessoryItem::getMagicDamageBonus);
    }

    // ========== 新增伤害与护甲相关属性计算方法 ==========

    /**
     * 计算攻击伤害百分比加成
     */
    private static float calculateAttackDamagePercentage(Player player) {
        return calculateAccessoryBonus(player, AccessoryItem::getAttackDamagePercentageBonus);
    }

    /**
     * 计算伤害浮动系数
     */
    private static float calculateDamageFluctuation(Player player) {
        return calculateAccessoryBonus(player, AccessoryItem::getDamageFluctuationBonus);
    }

    /**
     * 计算护甲值
     */
    private static float calculateArmorValue(Player player) {
        float baseArmor = (float) player.getAttributeValue(Attributes.ARMOR);
        float accessoryBonus = calculateAccessoryBonus(player, AccessoryItem::getArmorValueBonus);
        return baseArmor + accessoryBonus;
    }

    /**
     * 计算伤害抗性
     */
    private static float calculateDamageResistance(Player player) {
        return calculateAccessoryBonus(player, AccessoryItem::getDamageResistanceBonus);
    }

    /**
     * 计算穿透抗性
     */
    private static float calculatePenetrationResistance(Player player) {
        return calculateAccessoryBonus(player, AccessoryItem::getPenetrationResistanceBonus);
    }

    /**
     * 计算抗穿透系数
     */
    private static float calculatePenetrationResistanceCoefficient(Player player) {
        return calculateAccessoryBonus(player, AccessoryItem::getPenetrationResistanceCoefficientBonus);
    }

    /**
     * 计算护甲韧性
     */
    private static float calculateArmorToughness(Player player) {
        float baseToughness = (float) player.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        float accessoryBonus = calculateAccessoryBonus(player, AccessoryItem::getArmorToughnessBonus);
        return baseToughness + accessoryBonus;
    }

    // ========== 缺失的计算方法 ==========

    /**
     * 计算近战伤害数值
     */
    private static float calculateMeleeDamageValue(Player player) {
        return calculateAccessoryBonus(player, AccessoryItem::getMeleeDamageValueBonus);
    }

    /**
     * 计算远程伤害数值
     */
    private static float calculateRangedDamageValue(Player player) {
        return calculateAccessoryBonus(player, AccessoryItem::getRangedDamageValueBonus);
    }

    /**
     * 计算魔法伤害数值
     */
    private static float calculateMagicDamageValue(Player player) {
        return calculateAccessoryBonus(player, AccessoryItem::getMagicDamageValueBonus);
    }

    /**
     * 计算面板伤害数值
     */
    private static float calculatePanelDamageValue(Player player) {
        return calculateAccessoryBonus(player, AccessoryItem::getPanelDamageValueBonus);
    }

    /**
     * 计算近战伤害百分比
     */
    private static float calculateMeleeDamage(Player player) {
        return calculateAccessoryBonus(player, AccessoryItem::getMeleeDamageBonus);
    }

    /**
     * 计算远程伤害百分比
     */
    private static float calculateRangedDamage(Player player) {
        return calculateAccessoryBonus(player, AccessoryItem::getRangedDamageBonus);
    }

    /**
     * 计算面板伤害百分比
     */
    private static float calculatePanelDamage(Player player) {
        return calculateAccessoryBonus(player, AccessoryItem::getPanelDamageBonus);
    }

    /**
     * 计算防御值
     */
    private static float calculateDefense(Player player) {
        return calculateAccessoryBonus(player, AccessoryItem::getDefenseBonus);
    }

    /**
     * 计算防御百分比
     */
    private static float calculateDefensePercentage(Player player) {
        return calculateAccessoryBonus(player, AccessoryItem::getDefensePercentageBonus);
    }

    /**
     * 计算生命值
     */
    private static float calculateHealth(Player player) {
        return calculateAccessoryBonus(player, AccessoryItem::getHealthBonus);
    }

    /**
     * 计算生命值百分比
     */
    private static float calculateHealthPercentage(Player player) {
        return calculateAccessoryBonus(player, AccessoryItem::getHealthPercentageBonus);
    }

    /**
     * 计算最大法力值
     */
    private static float calculateMaxMana(Player player) {
        return calculateAccessoryBonus(player, accessoryItem -> (float) accessoryItem.getMaxManaBonus());
    }

    /**
     * 计算幸运值
     */
    private static float calculateLuck(Player player) {
        float baseLuck = (float) player.getAttributeValue(Attributes.LUCK);
        return baseLuck;
    }

    // ========== 辅助计算方法 ==========

    /**
     * 统一计算饰品加成（通用方法）
     */
    private static float calculateAccessoryBonus(Player player, Function<AccessoryItem, Float> bonusGetter) {
        float totalBonus = 0.0f;

        var accessoryInventory = AccessoryInventory.get(player);
        if (accessoryInventory != null) {
            for (int i = 0; i < accessoryInventory.getItemHandler().getSlots(); i++) {
                ItemStack accessory = accessoryInventory.getItemHandler().getStackInSlot(i);
                if (!accessory.isEmpty() && accessory.getItem() instanceof AccessoryItem accessoryItem) {
                    totalBonus += bonusGetter.apply(accessoryItem);
                }
            }
        }

        return totalBonus;
    }

    /**
     * 计算动态伤害加成（专门处理机械之心和元素之心的激活状态）
     */
    private static float calculateDynamicDamageBonus(Player player) {
        float totalBonus = 0.0f;

        var accessoryInventory = AccessoryInventory.get(player);
        if (accessoryInventory != null) {
            for (int i = 0; i < accessoryInventory.getItemHandler().getSlots(); i++) {
                ItemStack accessory = accessoryInventory.getItemHandler().getStackInSlot(i);
                if (!accessory.isEmpty()) {
                    // 处理机械之心的动态加成
                    if (accessory.getItem() instanceof MechanicalHeart mechanicalHeart) {
                        totalBonus += mechanicalHeart.getCurrentPanelDamageBonus(player);
                    }
                    // 处理元素之心的动态加成
                    else if (accessory.getItem() instanceof HeartOfTheElements elementalHeart) {
                        totalBonus += elementalHeart.getCurrentPanelDamageBonus(player);
                    }
                    // 处理其他饰品的固定加成
                    else if (accessory.getItem() instanceof AccessoryItem accessoryItem) {
                        totalBonus += accessoryItem.getPanelDamageBonus();
                    }
                }
            }
        }

        return totalBonus;
    }

    /**
     * 计算武器伤害加成
     */
    private static float calculateWeaponDamageBonus(ItemStack weapon) {
        if (weapon.isEmpty()) return 0.0f;

        // 获取武器的基础伤害
        float baseDamage = 1.0f;
        
        // 根据武器类型给予不同的伤害加成
        // 这里需要根据实际的武器系统实现
        String itemName = weapon.getItem().getDescriptionId();
        
        if (itemName.contains("sword")) baseDamage = 5.0f;
        else if (itemName.contains("axe")) baseDamage = 7.0f;
        else if (itemName.contains("dagger")) baseDamage = 3.0f;
        else if (itemName.contains("scythe")) baseDamage = 8.0f;
        else if (itemName.contains("bow")) baseDamage = 4.0f;
        else if (itemName.contains("crossbow")) baseDamage = 6.0f;
        else if (itemName.contains("staff")) baseDamage = 5.0f;
        else if (itemName.contains("wand")) baseDamage = 4.0f;

        return baseDamage;
    }

    // ========== 兼容性方法（保持原有API） ==========

    /**
     * 获取玩家回血速度（兼容性方法）
     */
    public static float getHealthRegenerationRate(Player player) {
        return getAttributeValue(player, "health_regen");
    }

    /**
     * 获取玩家回蓝速度（兼容性方法）
     */
    public static float getManaRegenerationRate(Player player) {
        return getAttributeValue(player, "mana_regen");
    }

    /**
     * 获取玩家攻击力（兼容性方法）
     */
    public static float getAttackDamage(Player player) {
        return getAttributeValue(player, "attack_damage");
    }

    /**
     * 获取玩家攻击速度（兼容性方法）
     */
    public static float getAttackSpeed(Player player) {
        return getAttributeValue(player, "attack_speed");
    }

    /**
     * 获取玩家暴击率（兼容性方法）
     */
    public static float getCritChance(Player player) {
        return getAttributeValue(player, "crit_chance");
    }

    /**
     * 获取玩家暴击伤害（兼容性方法）
     */
    public static float getCritDamage(Player player) {
        return getAttributeValue(player, "crit_damage");
    }

    /**
     * 获取玩家近战暴击伤害（兼容性方法）
     */
    public static float getMeleeCritDamage(Player player) {
        return getAttributeValue(player, "melee_crit_damage");
    }

    /**
     * 获取玩家远程暴击伤害（兼容性方法）
     */
    public static float getRangedCritDamage(Player player) {
        return getAttributeValue(player, "ranged_crit_damage");
    }

    /**
     * 获取玩家魔法暴击伤害（兼容性方法）
     */
    public static float getMagicCritDamage(Player player) {
        return getAttributeValue(player, "magic_crit_damage");
    }

    /**
     * 获取玩家伤害减免百分比（兼容性方法）
     */
    public static float getDamageReductionPercentage(Player player) {
        return getAttributeValue(player, "damage_reduction");
    }

    /**
     * 获取玩家移动速度（兼容性方法）
     */
    public static float getMovementSpeed(Player player) {
        return getAttributeValue(player, "movement_speed");
    }

    /**
     * 获取玩家幸运值（兼容性方法）
     */
    public static float getLuck(Player player) {
        return getAttributeValue(player, "luck");
    }

    /**
     * 初始化玩家数据（当玩家登录时调用）
     */
    public static void initializePlayer(Player player) {
        // 清除旧的缓存
        AttributeCache.clearPlayerCache(player);
        
        // 加载玩家基础属性
        PlayerBaseAttributes.loadFromNBT(player);
        
        // 初始化玩家属性修改器数据
        if (!PLAYER_MODIFIERS.containsKey(player)) {
            PLAYER_MODIFIERS.put(player, new ConcurrentHashMap<>());
        }
    }

    /**
     * 清理玩家数据（当玩家退出时调用）
     */
    public static void cleanupPlayerData(Player player) {
        // 保存玩家基础属性
        PlayerBaseAttributes.saveToNBT(player);
        
        // 清除玩家缓存
        AttributeCache.clearPlayerCache(player);
        
        // 清理玩家基础属性
        PlayerBaseAttributes.cleanupPlayer(player);
        
        PLAYER_MODIFIERS.remove(player);
    }
}