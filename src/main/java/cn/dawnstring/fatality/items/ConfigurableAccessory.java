package cn.dawnstring.fatality.items;

public class ConfigurableAccessory extends AccessoryItem {

    private final AccessoryStats stats;

    public ConfigurableAccessory(Properties properties, AccessoryStats stats) {
        super(properties);
        this.stats = stats;
    }

    @Override
    public float getMeleeDamageValueBonus() { return stats.meleeDamageValueBonus; }
    @Override
    public float getRangedDamageValueBonus() { return stats.rangedDamageValueBonus; }
    @Override
    public float getMagicDamageValueBonus() { return stats.magicDamageValueBonus; }
    @Override
    public float getPanelDamageValueBonus() { return stats.panelDamageValueBonus; }
    @Override
    public float getMeleeDamageBonus() { return stats.meleeDamageBonus; }
    @Override
    public float getRangedDamageBonus() { return stats.rangedDamageBonus; }
    @Override
    public float getMagicDamageBonus() { return stats.magicDamageBonus; }
    @Override
    public float getMeleeCriticalDamageBonus() { return stats.meleeCriticalDamageBonus; }
    @Override
    public float getRangedCriticalDamageBonus() { return stats.rangedCriticalDamageBonus; }
    @Override
    public float getMagicCriticalDamageBonus() { return stats.magicCriticalDamageBonus; }
    @Override
    public float getCriticalChanceBonus() { return stats.criticalChanceBonus; }
    @Override
    public float getDefenseBonus() { return stats.defenseBonus; }
    @Override
    public float getDefensePercentageBonus() { return stats.defensePercentageBonus; }
    @Override
    public float getHealthBonus() { return stats.healthBonus; }
    @Override
    public float getHealthPercentageBonus() { return stats.healthPercentageBonus; }
    @Override
    public float getAttackSpeedBonus() { return stats.attackSpeedBonus; }
    @Override
    public int getMaxManaBonus() { return stats.maxManaBonus; }
    @Override
    public float getDamageReductionBonus() { return stats.damageReductionBonus; }
    @Override
    public float getPanelDamageBonus() { return stats.panelDamageBonus; }
    @Override
    public float getMovementSpeedBonus() { return stats.movementSpeedBonus; }
    @Override
    public float getHealthRegenerationBonus() { return stats.healthRegenerationBonus; }
    @Override
    public float getManaRegenerationBonus() { return stats.manaRegenerationBonus; }
    @Override
    public float getAttackDamagePercentageBonus() { return stats.attackDamagePercentageBonus; }
    @Override
    public float getDamageFluctuationBonus() { return stats.damageFluctuationBonus; }
    @Override
    public float getArmorValueBonus() { return stats.armorValueBonus; }
    @Override
    public float getDamageResistanceBonus() { return stats.damageResistanceBonus; }
    @Override
    public float getPenetrationResistanceBonus() { return stats.penetrationResistanceBonus; }
    @Override
    public float getPenetrationResistanceCoefficientBonus() { return stats.penetrationResistanceCoefficientBonus; }
    @Override
    public float getArmorToughnessBonus() { return stats.armorToughnessBonus; }
}
