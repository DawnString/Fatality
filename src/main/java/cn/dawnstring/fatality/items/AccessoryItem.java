package cn.dawnstring.fatality.items;

import cn.dawnstring.fatality.utils.TooltipHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AccessoryItem extends Item
{
    private String story = "";
    private String attributesDescription = "";

    // 存储当前饰品实例使用的UUID列表
    private final List<UUID> currentModifierUUIDs = new ArrayList<>();

    public AccessoryItem(Properties properties) {
        super(properties);
    }

    /**
     * 获取饰品提供的近战伤害加成（数值）
     * 子类可以重写此方法返回具体的加成值
     */
    public float getMeleeDamageValueBonus() {
        return 0.0f;
    }

    /**
     * 获取饰品提供的远程伤害加成（数值）
     * 子类可以重写此方法返回具体的加成值
     */
    public float getRangedDamageValueBonus() {
        return 0.0f;
    }

    /**
     * 获取饰品提供的魔法伤害加成（数值）
     * 子类可以重写此方法返回具体的加成值
     */
    public float getMagicDamageValueBonus() {
        return 0.0f;
    }

    /**
     * 获取饰品提供的面板伤害加成（数值）- 适用于所有伤害类型
     * 子类可以重写此方法返回具体的加成值
     */
    public float getPanelDamageValueBonus() {
        return 0.0f;
    }

    /**
     * 获取饰品提供的近战伤害加成（百分比）
     * 子类可以重写此方法返回具体的加成值
     */
    public float getMeleeDamageBonus() {
        return 0.0f;
    }

    /**
     * 获取饰品提供的远程伤害加成（百分比）
     * 子类可以重写此方法返回具体的加成值
     */
    public float getRangedDamageBonus() {
        return 0.0f;
    }

    /**
     * 获取饰品提供的魔法伤害加成（百分比）
     * 子类可以重写此方法返回具体的加成值
     */
    public float getMagicDamageBonus() {
        return 0.0f;
    }

    /**
     * 获取饰品提供的近战暴击伤害加成（百分比）
     * 子类可以重写此方法返回具体的加成值
     */
    public float getMeleeCriticalDamageBonus() {
        return 0.0f;
    }

    /**
     * 获取饰品提供的远程暴击伤害加成（百分比）
     * 子类可以重写此方法返回具体的加成值
     */
    public float getRangedCriticalDamageBonus() {
        return 0.0f;
    }

    /**
     * 获取饰品提供的魔法暴击伤害加成（百分比）
     * 子类可以重写此方法返回具体的加成值
     */
    public float getMagicCriticalDamageBonus() {
        return 0.0f;
    }

    /**
     * 获取饰品提供的总暴击率加成（百分比）- 不要将暴击率分开算
     * 子类可以重写此方法返回具体的加成值
     */
    public float getCriticalChanceBonus() {
        return 0.0f;
    }

    /**
     * 获取饰品提供的防御加成（护甲值）
     * 子类可以重写此方法返回具体的加成值
     */
    public float getDefenseBonus() {
        return 0.0f;
    }

    /**
     * 获取饰品提供的防御百分比加成（百分比）
     * 子类可以重写此方法返回具体的加成值
     */
    public float getDefensePercentageBonus() {
        return 0.0f;
    }

    /**
     * 获取饰品提供的血量数值加成
     * 子类可以重写此方法返回具体的加成值
     */
    public float getHealthBonus() {
        return 0.0f;
    }

    /**
     * 获取饰品提供的血量百分比加成（百分比）
     * 子类可以重写此方法返回具体的加成值
     */
    public float getHealthPercentageBonus() {
        return 0.0f;
    }

    /**
     * 获取饰品提供的攻击速度加成（百分比）
     * 子类可以重写此方法返回具体的加成值
     */
    public float getAttackSpeedBonus() {
        return 0.0f;
    }

    /**
     * 获取饰品提供的法力最大值加成
     * 子类可以重写此方法返回具体的加成值
     */
    public int getMaxManaBonus() {
        return 0;
    }

    /**
     * 获取饰品提供的伤害减免加成（百分比）
     * 子类可以重写此方法返回具体的加成值
     */
    public float getDamageReductionBonus() {
        return 0.0f;
    }

    /**
     * 获取饰品提供的面板伤害加成（百分比）- 适用于所有伤害类型
     * 子类可以重写此方法返回具体的加成值
     */
    public float getPanelDamageBonus() {
        return 0.0f;
    }

    /**
     * 获取饰品提供的移动速度加成（百分比）
     * 子类可以重写此方法返回具体的加成值
     */
    public float getMovementSpeedBonus() {
        return 0.0f;
    }

    /**
     * 获取饰品提供的回血速度加成（百分比）
     * 子类可以重写此方法返回具体的加成值
     */
    public float getHealthRegenerationBonus() {
        return 0.0f;
    }

    /**
     * 获取饰品提供的恢复法力速度加成（百分比）
     * 子类可以重写此方法返回具体的加成值
     */
    public float getManaRegenerationBonus() {
        return 0.0f;
    }

    // ========== 新增伤害与护甲相关属性方法 ==========

    /**
     * 获取饰品提供的攻击伤害百分比加成
     * 子类可以重写此方法返回具体的加成值
     */
    public float getAttackDamagePercentageBonus() {
        return 0.0f;
    }

    /**
     * 获取饰品提供的伤害浮动系数加成
     * 子类可以重写此方法返回具体的加成值
     */
    public float getDamageFluctuationBonus() {
        return 0.0f;
    }

    /**
     * 获取饰品提供的护甲值加成
     * 子类可以重写此方法返回具体的加成值
     */
    public float getArmorValueBonus() {
        return 0.0f;
    }

    /**
     * 获取饰品提供的伤害抗性加成
     * 子类可以重写此方法返回具体的加成值
     */
    public float getDamageResistanceBonus() {
        return 0.0f;
    }

    /**
     * 获取饰品提供的穿透抗性加成
     * 子类可以重写此方法返回具体的加成值
     */
    public float getPenetrationResistanceBonus() {
        return 0.0f;
    }

    /**
     * 获取饰品提供的抗穿透系数加成
     * 子类可以重写此方法返回具体的加成值
     */
    public float getPenetrationResistanceCoefficientBonus() {
        return 0.0f;
    }

    /**
     * 获取饰品提供的护甲韧性加成
     * 子类可以重写此方法返回具体的加成值
     */
    public float getArmorToughnessBonus() {
        return 0.0f;
    }

    /**
     * 设置饰品故事
     */
    public AccessoryItem setStory(String story) {
        this.story = story;
        return this;
    }

    /**
     * 设置饰品属性描述
     */
    public void setAttributesDescription(String attributesDescription) {
        this.attributesDescription = attributesDescription;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        // 构建详细的属性描述
        StringBuilder descriptionBuilder = new StringBuilder();

        // 数值加成部分
        // 近战伤害数值加成
        float meleeDamageValue = getMeleeDamageValueBonus();
        if (meleeDamageValue > 0) {
            descriptionBuilder.append(String.format("近战伤害: +%.1f\n", meleeDamageValue));
        }

        // 远程伤害数值加成
        float rangedDamageValue = getRangedDamageValueBonus();
        if (rangedDamageValue > 0) {
            descriptionBuilder.append(String.format("远程伤害: +%.1f\n", rangedDamageValue));
        }

        // 魔法伤害数值加成
        float magicDamageValue = getMagicDamageValueBonus();
        if (magicDamageValue > 0) {
            descriptionBuilder.append(String.format("魔法伤害: +%.1f\n", magicDamageValue));
        }

        // 面板伤害数值加成
        float panelDamageValue = getPanelDamageValueBonus();
        if (panelDamageValue > 0) {
            descriptionBuilder.append(String.format("面板伤害: +%.1f\n", panelDamageValue));
        }

        // 近战暴击伤害
        float meleeCritDamage = getMeleeCriticalDamageBonus();
        if (meleeCritDamage > 0) {
            descriptionBuilder.append(String.format("近战暴击伤害: +%.1f%%\n", meleeCritDamage * 100));
        }

        // 远程暴击伤害
        float rangedCritDamage = getRangedCriticalDamageBonus();
        if (rangedCritDamage > 0) {
            descriptionBuilder.append(String.format("远程暴击伤害: +%.1f%%\n", rangedCritDamage * 100));
        }

        // 魔法暴击伤害
        float magicCritDamage = getMagicCriticalDamageBonus();
        if (magicCritDamage > 0) {
            descriptionBuilder.append(String.format("魔法暴击伤害: +%.1f%%\n", magicCritDamage * 100));
        }

        // 总暴击率
        float critChance = getCriticalChanceBonus();
        if (critChance > 0) {
            descriptionBuilder.append(String.format("暴击率: +%.1f%%\n", critChance * 100));
        }

        // 防御（护甲值）
        float defense = getDefenseBonus();
        if (defense > 0) {
            descriptionBuilder.append(String.format("防御: +%.1f\n", defense));
        }

        // 防御百分比加成
        float defensePercentage = getDefensePercentageBonus();
        if (defensePercentage > 0) {
            descriptionBuilder.append(String.format("防御加成: +%.1f%%\n", defensePercentage * 100));
        }

        // 血量数值加成
        float healthBonus = getHealthBonus();
        if (healthBonus > 0) {
            descriptionBuilder.append(String.format("血量: +%.1f\n", healthBonus));
        }

        // 血量百分比加成
        float healthPercentage = getHealthPercentageBonus();
        if (healthPercentage > 0) {
            descriptionBuilder.append(String.format("血量加成: +%.1f%%\n", healthPercentage * 100));
        }
        // 血量百分比减少加成
        if (healthPercentage < 0) {
            descriptionBuilder.append(String.format("血量减少: %.1f%%\n", healthPercentage * 100));
        }

        // 攻击速度加成
        float attackSpeed = getAttackSpeedBonus();
        if (attackSpeed > 0) {
            descriptionBuilder.append(String.format("攻击速度: +%.1f%%\n", attackSpeed * 100));
        }

        // 法力最大值加成
        int maxMana = getMaxManaBonus();
        if (maxMana > 0) {
            descriptionBuilder.append(String.format("法力最大值: +%d\n", maxMana));
        }

        // 伤害减免
        float damageReduction = getDamageReductionBonus();
        if (damageReduction > 0) {
            descriptionBuilder.append(String.format("伤害减免: +%.1f%%\n", damageReduction * 100));
        }

        // 面板伤害加成
        float panelDamage = getPanelDamageBonus();
        if (panelDamage > 0) {
            descriptionBuilder.append(String.format("面板伤害: +%.1f%%\n", panelDamage * 100));
        }

        // 移动速度加成
        float movementSpeed = getMovementSpeedBonus();
        if (movementSpeed > 0) {
            descriptionBuilder.append(String.format("移动速度: +%.1f%%\n", movementSpeed * 100));
        }

        // 回血速度加成
        float healthRegen = getHealthRegenerationBonus();
        if (healthRegen > 0) {
            descriptionBuilder.append(String.format("回血速度: +%.1f%%\n", healthRegen * 100));
        }

        // 恢复法力速度加成
        float manaRegen = getManaRegenerationBonus();
        if (manaRegen > 0) {
            descriptionBuilder.append(String.format("法力恢复: +%.1f%%\n", manaRegen * 100));
        }

        // 如果有属性描述，添加到tooltip中
        if (descriptionBuilder.length() > 0) {
            this.attributesDescription = descriptionBuilder.toString().trim();
        }

        TooltipHelper.addDescriptiveTooltip(stack, level, tooltip, flag, story, attributesDescription);
    }

    /**
     * 统一应用饰品效果的方法
     * 子类可以重写此方法来实现额外的效果
     */
    public void applyEffects(Player player, ItemStack stack) {
        // 先移除可能存在的效果
        removeEffects(player, stack);

        // 应用所有属性加成
        applyAllAttributeBonuses(player);
    }

    /**
     * 统一移除饰品效果的方法
     * 子类可以重写此方法来实现额外的效果移除
     */
    public void removeEffects(Player player, ItemStack stack) {
        // 移除所有记录的属性修改器
        for (UUID uuid : currentModifierUUIDs) {
            removeAttributeModifierByUUID(player, uuid);
        }
        clearModifierUUIDs();
    }

    /**
     * 应用所有属性加成（统一方法）
     */
    private void applyAllAttributeBonuses(Player player) {
        // 生成基于饰品类型和玩家UUID的唯一标识符
        String baseId = getDescriptionId() + "-" + player.getUUID().toString().substring(0, 8);

        // 应用各种属性加成
        applyDamageValueBonuses(player, baseId);  // 新增：应用数值加成
        applyDamageBonuses(player, baseId);       // 原有：应用百分比加成
        applyDefenseBonuses(player, baseId);
        applyHealthBonuses(player, baseId);
        applySpeedBonuses(player, baseId);
        applyCriticalBonuses(player, baseId);
        applyManaBonuses(player, baseId);
    }

    /**
     * 应用伤害数值加成
     */
    private void applyDamageValueBonuses(Player player, String baseId) {
        // 近战伤害数值加成
        float meleeDamageValue = getMeleeDamageValueBonus();
        if (meleeDamageValue > 0) {
            UUID uuid = UUID.nameUUIDFromBytes((baseId + "-melee-damage-value").getBytes());
            addAttackBonus(player, meleeDamageValue, uuid);
        }

        // 远程伤害数值加成
        float rangedDamageValue = getRangedDamageValueBonus();
        if (rangedDamageValue > 0) {
            UUID uuid = UUID.nameUUIDFromBytes((baseId + "-ranged-damage-value").getBytes());
            addAttackBonus(player, rangedDamageValue, uuid);
        }

        // 魔法伤害数值加成
        float magicDamageValue = getMagicDamageValueBonus();
        if (magicDamageValue > 0) {
            UUID uuid = UUID.nameUUIDFromBytes((baseId + "-magic-damage-value").getBytes());
            addAttackBonus(player, magicDamageValue, uuid);
        }

        // 面板伤害数值加成
        float panelDamageValue = getPanelDamageValueBonus();
        if (panelDamageValue > 0) {
            UUID uuid = UUID.nameUUIDFromBytes((baseId + "-panel-damage-value").getBytes());
            addAttackBonus(player, panelDamageValue, uuid);
        }
    }

    /**
     * 应用伤害相关加成
     */
    private void applyDamageBonuses(Player player, String baseId) {
        // 近战伤害加成
        float meleeDamage = getMeleeDamageBonus();
        if (meleeDamage > 0) {
            UUID uuid = UUID.nameUUIDFromBytes((baseId + "-melee-damage").getBytes());
            addAttackBonus(player, player.getAttributeValue(Attributes.ATTACK_DAMAGE) * meleeDamage, uuid);
        }

        // 远程伤害加成
        float rangedDamage = getRangedDamageBonus();
        if (rangedDamage > 0) {
            UUID uuid = UUID.nameUUIDFromBytes((baseId + "-ranged-damage").getBytes());
            addAttackBonus(player, player.getAttributeValue(Attributes.ATTACK_DAMAGE) * rangedDamage, uuid);
        }

        // 魔法伤害加成
        float magicDamage = getMagicDamageBonus();
        if (magicDamage > 0) {
            UUID uuid = UUID.nameUUIDFromBytes((baseId + "-magic-damage").getBytes());
            addAttackBonus(player, player.getAttributeValue(Attributes.ATTACK_DAMAGE) * magicDamage, uuid);
        }

        // 面板伤害加成
        float panelDamage = getPanelDamageBonus();
        if (panelDamage > 0) {
            UUID uuid = UUID.nameUUIDFromBytes((baseId + "-panel-damage").getBytes());
            addAttackBonus(player, player.getAttributeValue(Attributes.ATTACK_DAMAGE) * panelDamage, uuid);
        }
    }

    /**
     * 应用防御相关加成
     */
    private void applyDefenseBonuses(Player player, String baseId) {
        // 防御数值加成
        float defense = getDefenseBonus();
        if (defense > 0) {
            UUID uuid = UUID.nameUUIDFromBytes((baseId + "-defense").getBytes());
            addArmorBonus(player, defense, uuid);
        }

        // 防御百分比加成
        float defensePercentage = getDefensePercentageBonus();
        if (defensePercentage > 0) {
            UUID uuid = UUID.nameUUIDFromBytes((baseId + "-defense-percentage").getBytes());
            addArmorBonus(player, player.getAttributeValue(Attributes.ARMOR) * defensePercentage, uuid);
        }

        // 伤害减免
        float damageReduction = getDamageReductionBonus();
        if (damageReduction > 0) {
            // 伤害减免需要特殊处理，这里暂时使用护甲值来模拟
            UUID uuid = UUID.nameUUIDFromBytes((baseId + "-damage-reduction").getBytes());
            addArmorBonus(player, player.getAttributeValue(Attributes.ARMOR) * damageReduction, uuid);
        }
    }

    /**
     * 应用生命值相关加成
     */
    private void applyHealthBonuses(Player player, String baseId) {
        // 血量数值加成
        float healthBonus = getHealthBonus();
        if (healthBonus > 0) {
            UUID uuid = UUID.nameUUIDFromBytes((baseId + "-health-bonus").getBytes());
            addHealthBonus(player, healthBonus, uuid);
        }

        // 血量百分比加成
        float healthPercentage = getHealthPercentageBonus();
        if (healthPercentage > 0)
        {
            UUID uuid = UUID.nameUUIDFromBytes((baseId + "-health-percentage").getBytes());
            addHealthBonus(player, player.getMaxHealth() * healthPercentage, uuid);
        }
        else if (healthPercentage < 0)
        {
            UUID uuid = UUID.nameUUIDFromBytes((baseId + "-health-percentage").getBytes());
            addHealthBonus(player, player.getMaxHealth() * healthPercentage, uuid);
        }
    }

    /**
     * 应用速度相关加成
     */
    private void applySpeedBonuses(Player player, String baseId) {
        // 攻击速度加成
        float attackSpeed = getAttackSpeedBonus();
        if (attackSpeed > 0) {
            UUID uuid = UUID.nameUUIDFromBytes((baseId + "-attack-speed").getBytes());
            addAttackSpeedBonus(player, attackSpeed, uuid);
        }

        // 移动速度加成
        float movementSpeed = getMovementSpeedBonus();
        if (movementSpeed > 0) {
            UUID uuid = UUID.nameUUIDFromBytes((baseId + "-movement-speed").getBytes());
            addSpeedBonus(player, movementSpeed, uuid);
        }
    }

    /**
     * 应用暴击相关加成
     */
    private void applyCriticalBonuses(Player player, String baseId) {
        // 暴击率加成
        float critChance = getCriticalChanceBonus();
        if (critChance > 0) {
            UUID uuid = UUID.nameUUIDFromBytes((baseId + "-crit-chance").getBytes());
            // 暴击率加成由BaseWeapon直接通过getCriticalChanceBonus()方法获取
            // 这里只需要记录UUID，保持与其他属性方法的一致性
            addModifierUUID(uuid);
        }

        // 各种暴击伤害加成
        float[] critDamages = {
                getMeleeCriticalDamageBonus(),
                getRangedCriticalDamageBonus(),
                getMagicCriticalDamageBonus()
        };

        String[] critTypes = {"melee-crit", "ranged-crit", "magic-crit"};

        for (int i = 0; i < critDamages.length; i++) {
            if (critDamages[i] > 0) {
                UUID uuid = UUID.nameUUIDFromBytes((baseId + "-" + critTypes[i]).getBytes());
                addAttackBonus(player, player.getAttributeValue(Attributes.ATTACK_DAMAGE) * critDamages[i] * 0.05f, uuid);
            }
        }
    }

    /**
     * 应用法力相关加成
     */
    private void applyManaBonuses(Player player, String baseId) {
        // 法力最大值加成（需要特殊属性处理）
        int maxMana = getMaxManaBonus();
        if (maxMana > 0) {
            // 法力值需要特殊属性，这里暂时使用生命值来模拟
            UUID uuid = UUID.nameUUIDFromBytes((baseId + "-max-mana").getBytes());
            addHealthBonus(player, maxMana * 0.1, uuid); // 每10点法力相当于1点生命值
        }
    }

    /**
     * 获取饰品使用的UUID列表（用于正确移除效果）
     */
    public List<UUID> getCurrentModifierUUIDs() {
        return new ArrayList<>(currentModifierUUIDs);
    }

    /**
     * 添加使用的UUID到列表中
     */
    protected void addModifierUUID(UUID uuid) {
        currentModifierUUIDs.add(uuid);
    }

    /**
     * 清空当前使用的UUID列表
     */
    protected void clearModifierUUIDs() {
        currentModifierUUIDs.clear();
    }

    // 修改辅助方法，添加UUID跟踪
    protected void addHealthBonus(Player player, double amount, UUID uuid) {
        var attribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (attribute != null) {
            // 先移除可能存在的修改器
            attribute.removeModifier(uuid);
            // 然后添加新的修改器
            attribute.addTransientModifier(new AttributeModifier(uuid, "Health Bonus", amount, AttributeModifier.Operation.ADDITION));
            // 记录使用的UUID
            addModifierUUID(uuid);
        }
    }

    protected void addAttackBonus(Player player, double amount, UUID uuid) {
        var attribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attribute != null) {
            attribute.removeModifier(uuid);
            attribute.addTransientModifier(new AttributeModifier(uuid, "Attack Bonus", amount, AttributeModifier.Operation.ADDITION));
            addModifierUUID(uuid);
        }
    }

    protected void addSpeedBonus(Player player, double amount, UUID uuid) {
        var attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null) {
            attribute.removeModifier(uuid);
            attribute.addTransientModifier(new AttributeModifier(uuid, "Speed Bonus", amount, AttributeModifier.Operation.MULTIPLY_TOTAL));
            addModifierUUID(uuid);
        }
    }

    protected void addArmorBonus(Player player, double amount, UUID uuid) {
        var attribute = player.getAttribute(Attributes.ARMOR);
        if (attribute != null) {
            attribute.removeModifier(uuid);
            attribute.addTransientModifier(new AttributeModifier(uuid, "Armor Bonus", amount, AttributeModifier.Operation.ADDITION));
            addModifierUUID(uuid);
        }
    }

    protected void addAttackSpeedBonus(Player player, double amount, UUID uuid) {
        var attribute = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attribute != null) {
            attribute.removeModifier(uuid);
            attribute.addTransientModifier(new AttributeModifier(uuid, "Attack Speed Bonus", amount, AttributeModifier.Operation.MULTIPLY_TOTAL));
            addModifierUUID(uuid);
        }
    }

    /**
     * 根据UUID移除属性修改器
     */
    protected void removeAttributeModifierByUUID(Player player, UUID uuid) {
        AttributeInstance[] attributes = {
                player.getAttribute(Attributes.MAX_HEALTH),
                player.getAttribute(Attributes.ATTACK_DAMAGE),
                player.getAttribute(Attributes.MOVEMENT_SPEED),
                player.getAttribute(Attributes.ARMOR),
                player.getAttribute(Attributes.ATTACK_SPEED)
        };

        for (AttributeInstance attribute : attributes) {
            if (attribute != null) {
                attribute.removeModifier(uuid);
            }
        }
    }

    public static boolean hasAccessoryEquipped(Player player, Class<? extends AccessoryItem> type) {
        var accessoryInventory = cn.dawnstring.fatality.inventory.AccessoryInventory.get(player);
        if (accessoryInventory == null) return false;
        for (int i = 0; i < accessoryInventory.getItemHandler().getSlots(); i++) {
            if (type.isInstance(accessoryInventory.getItemHandler().getStackInSlot(i).getItem())) {
                return true;
            }
        }
        return false;
    }

    protected void applyNegativeEffectImmunity(Player player) {
        MobEffect[] negativeEffects = {
                MobEffects.POISON, MobEffects.WITHER, MobEffects.MOVEMENT_SLOWDOWN,
                MobEffects.DIG_SLOWDOWN, MobEffects.CONFUSION, MobEffects.HUNGER,
                MobEffects.BLINDNESS, MobEffects.WEAKNESS, MobEffects.DARKNESS
        };
        for (MobEffect effect : negativeEffects) {
            if (player.hasEffect(effect)) {
                player.removeEffect(effect);
            }
        }
    }

    protected void removeNegativeEffectImmunity(Player player) {
    }

    /**
     * 启用创造模式飞行
     * 子类可以调用此方法来启用飞行能力
     */
    protected void enableCreativeFlight(Player player) {
        // 只在非创造模式下启用飞行
        if (!player.isCreative()) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }
    }

    /**
     * 禁用创造模式飞行
     * 子类可以调用此方法来禁用飞行能力
     */
    protected void disableCreativeFlight(Player player) {
        // 只在非创造模式下禁用飞行
        if (!player.isCreative()) {
            player.getAbilities().mayfly = false;
            // 如果玩家正在飞行，强制降落
            if (player.getAbilities().flying) {
                player.getAbilities().flying = false;
            }
            player.onUpdateAbilities();
        }
    }
}