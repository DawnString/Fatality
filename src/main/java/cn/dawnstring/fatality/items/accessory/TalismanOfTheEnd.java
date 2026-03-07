package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class TalismanOfTheEnd extends AccessoryItem
{
    public TalismanOfTheEnd(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getMeleeCriticalDamageBonus() {
        return 1.1f;
    }

    @Override
    public float getMeleeDamageValueBonus() {
        return 60.0f;
    }

    @Override
    public float getCriticalChanceBonus() {
        return 0.15f;
    }
}
