package cn.dawnstring.fatality.items;

public class AccessoryStats {

    public float meleeDamageValueBonus;
    public float rangedDamageValueBonus;
    public float magicDamageValueBonus;
    public float panelDamageValueBonus;
    public float meleeDamageBonus;
    public float rangedDamageBonus;
    public float magicDamageBonus;
    public float meleeCriticalDamageBonus;
    public float rangedCriticalDamageBonus;
    public float magicCriticalDamageBonus;
    public float criticalChanceBonus;
    public float defenseBonus;
    public float defensePercentageBonus;
    public float healthBonus;
    public float healthPercentageBonus;
    public float attackSpeedBonus;
    public int maxManaBonus;
    public float damageReductionBonus;
    public float panelDamageBonus;
    public float movementSpeedBonus;
    public float healthRegenerationBonus;
    public float manaRegenerationBonus;
    public float attackDamagePercentageBonus;
    public float damageFluctuationBonus;
    public float armorValueBonus;
    public float damageResistanceBonus;
    public float penetrationResistanceBonus;
    public float penetrationResistanceCoefficientBonus;
    public float armorToughnessBonus;

    private AccessoryStats() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AccessoryStats stats = new AccessoryStats();

        public Builder meleeDamageValueBonus(float v) { stats.meleeDamageValueBonus = v; return this; }
        public Builder rangedDamageValueBonus(float v) { stats.rangedDamageValueBonus = v; return this; }
        public Builder magicDamageValueBonus(float v) { stats.magicDamageValueBonus = v; return this; }
        public Builder panelDamageValueBonus(float v) { stats.panelDamageValueBonus = v; return this; }
        public Builder meleeDamageBonus(float v) { stats.meleeDamageBonus = v; return this; }
        public Builder rangedDamageBonus(float v) { stats.rangedDamageBonus = v; return this; }
        public Builder magicDamageBonus(float v) { stats.magicDamageBonus = v; return this; }
        public Builder meleeCriticalDamageBonus(float v) { stats.meleeCriticalDamageBonus = v; return this; }
        public Builder rangedCriticalDamageBonus(float v) { stats.rangedCriticalDamageBonus = v; return this; }
        public Builder magicCriticalDamageBonus(float v) { stats.magicCriticalDamageBonus = v; return this; }
        public Builder criticalChanceBonus(float v) { stats.criticalChanceBonus = v; return this; }
        public Builder defenseBonus(float v) { stats.defenseBonus = v; return this; }
        public Builder defensePercentageBonus(float v) { stats.defensePercentageBonus = v; return this; }
        public Builder healthBonus(float v) { stats.healthBonus = v; return this; }
        public Builder healthPercentageBonus(float v) { stats.healthPercentageBonus = v; return this; }
        public Builder attackSpeedBonus(float v) { stats.attackSpeedBonus = v; return this; }
        public Builder maxManaBonus(int v) { stats.maxManaBonus = v; return this; }
        public Builder damageReductionBonus(float v) { stats.damageReductionBonus = v; return this; }
        public Builder panelDamageBonus(float v) { stats.panelDamageBonus = v; return this; }
        public Builder movementSpeedBonus(float v) { stats.movementSpeedBonus = v; return this; }
        public Builder healthRegenerationBonus(float v) { stats.healthRegenerationBonus = v; return this; }
        public Builder manaRegenerationBonus(float v) { stats.manaRegenerationBonus = v; return this; }
        public Builder attackDamagePercentageBonus(float v) { stats.attackDamagePercentageBonus = v; return this; }
        public Builder damageFluctuationBonus(float v) { stats.damageFluctuationBonus = v; return this; }
        public Builder armorValueBonus(float v) { stats.armorValueBonus = v; return this; }
        public Builder damageResistanceBonus(float v) { stats.damageResistanceBonus = v; return this; }
        public Builder penetrationResistanceBonus(float v) { stats.penetrationResistanceBonus = v; return this; }
        public Builder penetrationResistanceCoefficientBonus(float v) { stats.penetrationResistanceCoefficientBonus = v; return this; }
        public Builder armorToughnessBonus(float v) { stats.armorToughnessBonus = v; return this; }

        public AccessoryStats build() {
            return stats;
        }
    }
}
