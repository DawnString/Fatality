package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class TrollGloves extends AccessoryItem
{
    public TrollGloves(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getMeleeCriticalDamageBonus() {
        return 0.1f;
    }

    @Override
    public float getMeleeDamageValueBonus() {
        return 5.0f;
    }
}
