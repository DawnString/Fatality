package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class WraithBracers extends AccessoryItem
{
    public WraithBracers(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getRangedCriticalDamageBonus() {
        return 0.15f;
    }

    @Override
    public float getRangedDamageValueBonus() {
        return 10.0f;
    }
}
