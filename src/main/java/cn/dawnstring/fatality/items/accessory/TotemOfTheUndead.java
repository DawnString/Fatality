package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class TotemOfTheUndead extends AccessoryItem
{
    public TotemOfTheUndead(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getRangedCriticalDamageBonus() {
        return 0.5f;
    }

    @Override
    public float getRangedDamageValueBonus() {
        return 45.0f;
    }
}
