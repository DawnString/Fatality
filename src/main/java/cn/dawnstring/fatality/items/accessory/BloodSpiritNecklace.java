package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class BloodSpiritNecklace extends AccessoryItem
{
    public BloodSpiritNecklace(Properties properties)
    {
        super(properties);
    }
    @Override
    public float getMeleeCriticalDamageBonus() {
        return 0.13f;
    }

    @Override
    public float getMeleeDamageValueBonus() {
        return 15.0f;
    }
}
