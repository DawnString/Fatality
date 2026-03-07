package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class HolySpiritTotem extends AccessoryItem
{
    public HolySpiritTotem(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getRangedCriticalDamageBonus() {
        return 0.5f;
    }

    @Override
    public float getRangedDamageValueBonus() {
        return 50.0f;
    }

    @Override
    public float getCriticalChanceBonus() {
        return 0.1f;
    }
}
