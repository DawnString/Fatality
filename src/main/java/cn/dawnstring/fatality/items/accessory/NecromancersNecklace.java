package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class NecromancersNecklace extends AccessoryItem
{
    public NecromancersNecklace(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getMeleeCriticalDamageBonus() {
        return 1.0f;
    }

    @Override
    public float getMeleeDamageValueBonus() {
        return 50f;
    }

    @Override
    public float getCriticalChanceBonus() {
        return 0.15f;
    }
}
