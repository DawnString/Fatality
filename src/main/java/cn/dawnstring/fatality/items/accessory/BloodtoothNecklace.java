package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class BloodtoothNecklace extends AccessoryItem
{
    public BloodtoothNecklace(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getMeleeCriticalDamageBonus()
    {
        return 0.1f;
    }
}
