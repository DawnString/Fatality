package cn.dawnstring.fatality.system;

import cn.dawnstring.fatality.items.AccessoryItem;
import cn.dawnstring.fatality.items.accessory.HeartOfTheElements;
import cn.dawnstring.fatality.items.accessory.MechanicalHeart;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import cn.dawnstring.fatality.inventory.AccessoryInventory;
import cn.dawnstring.fatality.system.HealthRegenerationHandler;
import cn.dawnstring.fatality.system.ManaSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 属性系统 - 管理玩家的各种属性计算（重构版）
 */
public class AttributeSystem {

    // 属性计算器映射
    private static final Map<String, Function<Player, Float>> ATTRIBUTE_CALCULATORS = new HashMap<>();

    static {
        // 初始化属性计算器
        ATTRIBUTE_CALCULATORS.put("health_regen", AttributeSystem::calculateHealthRegeneration);
        ATTRIBUTE_CALCULATORS.put("mana_regen", AttributeSystem::calculateManaRegeneration);
        ATTRIBUTE_CALCULATORS.put("attack_damage", AttributeSystem::calculateAttackDamage);
        ATTRIBUTE_CALCULATORS.put("attack_speed", AttributeSystem::calculateAttackSpeed);
        ATTRIBUTE_CALCULATORS.put("crit_chance", AttributeSystem::calculateCritChance);
        ATTRIBUTE_CALCULATORS.put("damage_reduction", AttributeSystem::calculateDamageReduction);
        ATTRIBUTE_CALCULATORS.put("movement_speed", AttributeSystem::calculateMovementSpeed);
        ATTRIBUTE_CALCULATORS.put("crit_damage", AttributeSystem::calculateCritDamage);
        ATTRIBUTE_CALCULATORS.put("melee_crit_damage", AttributeSystem::calculateMeleeCritDamage);
        ATTRIBUTE_CALCULATORS.put("ranged_crit_damage", AttributeSystem::calculateRangedCritDamage);
        ATTRIBUTE_CALCULATORS.put("magic_crit_damage", AttributeSystem::calculateMagicCritDamage);
        ATTRIBUTE_CALCULATORS.put("magic_damage", AttributeSystem::calculateMagicDamage);
        
        // 新增伤害与护甲相关属性计算器
        ATTRIBUTE_CALCULATORS.put("attack_damage_percentage", AttributeSystem::calculateAttackDamagePercentage);
        ATTRIBUTE_CALCULATORS.put("damage_fluctuation", AttributeSystem::calculateDamageFluctuation);
        ATTRIBUTE_CALCULATORS.put("armor_value", AttributeSystem::calculateArmorValue);
        ATTRIBUTE_CALCULATORS.put("damage_resistance", AttributeSystem::calculateDamageResistance);
        ATTRIBUTE_CALCULATORS.put("penetration_resistance", AttributeSystem::calculatePenetrationResistance);
        ATTRIBUTE_CALCULATORS.put("penetration_resistance_coefficient", AttributeSystem::calculatePenetrationResistanceCoefficient);
        ATTRIBUTE_CALCULATORS.put("armor_toughness", AttributeSystem::calculateArmorToughness);
    }

    /**
     * 统一获取属性值的方法
     */
    public static float getAttributeValue(Player player, String attributeName) {
        Function<Player, Float> calculator = ATTRIBUTE_CALCULATORS.get(attributeName);
        if (calculator != null) {
            return calculator.apply(player);
        }
        return 0.0f;
    }

    // ========== 具体属性计算方法 ==========

    /**
     * 计算生命恢复速度
     */
    private static float calculateHealthRegeneration(Player player) {
        float baseRegen = HealthRegenerationHandler.BASE_HEALTH_REGEN_RATE; // 基础每秒恢复1点生命值
        float accessoryBonus = calculateAccessoryBonus(player, AccessoryItem::getHealthRegenerationBonus);
        return baseRegen + accessoryBonus;
    }

    /**
     * 计算魔法恢复速度
     */
    private static float calculateManaRegeneration(Player player) {
        float baseRegen = ManaSystem.MANA_REGENERATION_RATE; // 基础每秒恢复2点魔法值
        float accessoryBonus = calculateAccessoryBonus(player, AccessoryItem::getManaRegenerationBonus);
        return baseRegen + accessoryBonus;
    }

    /**
     * 计算攻击伤害
     */
    private static float calculateAttackDamage(Player player) {
        float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float weaponBonus = calculateWeaponDamageBonus(player.getMainHandItem());
        float accessoryBonus = calculateDynamicDamageBonus(player);
        return baseDamage + weaponBonus + accessoryBonus;
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
        float baseCritChance = 0.05f; // 基础暴击率5%
        float accessoryBonus = calculateAccessoryBonus(player, AccessoryItem::getCriticalChanceBonus);
        return Math.min(baseCritChance + accessoryBonus, 1.0f); // 最大100%
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

        String itemName = weapon.getItem().getDescriptionId();

        // 根据武器类型给予不同的伤害加成
        if (itemName.contains("sword")) return 5.0f;
        if (itemName.contains("axe")) return 7.0f;
        if (itemName.contains("dagger")) return 3.0f;
        if (itemName.contains("scythe")) return 8.0f;

        return 0.0f;
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
     * 获取玩家减伤百分比（兼容性方法）
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
     * 获取玩家暴击伤害倍率（兼容性方法）
     */
    public static float getCritDamage(Player player) {
        return getAttributeValue(player, "crit_damage");
    }

    /**
     * 获取玩家近战暴击伤害倍率（兼容性方法）
     */
    public static float getMeleeCritDamage(Player player) {
        return getAttributeValue(player, "melee_crit_damage");
    }

    /**
     * 获取玩家远程暴击伤害倍率（兼容性方法）
     */
    public static float getRangedCritDamage(Player player) {
        return getAttributeValue(player, "ranged_crit_damage");
    }

    /**
     * 获取玩家魔法暴击伤害倍率（兼容性方法）
     */
    public static float getMagicCritDamage(Player player) {
        return getAttributeValue(player, "magic_crit_damage");
    }

    /**
     * 获取玩家魔法伤害加成（兼容性方法）
     */
    public static float getMagicDamageBonus(Player player) {
        return getAttributeValue(player, "magic_damage");
    }
}