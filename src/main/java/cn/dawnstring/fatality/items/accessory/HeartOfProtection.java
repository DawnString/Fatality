package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class HeartOfProtection extends AccessoryItem
{
    public HeartOfProtection(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getDefenseBonus() {
        return 10.0f;
    }

    @Override
    public float getHealthBonus() {
        return 10.0f;
    }

    @Override
    public float getHealthRegenerationBonus() {
        return 0.1f;
    }

    @Override
    public float getCriticalChanceBonus() {
        return 0.1f;
    }

    @Override
    public float getPanelDamageBonus() {
        return 0.05f;
    }
}
