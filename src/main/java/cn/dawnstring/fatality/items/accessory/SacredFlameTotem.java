package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class SacredFlameTotem extends AccessoryItem
{
    public SacredFlameTotem(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getRangedCriticalDamageBonus()
    {
        return 0.35f;
    }

    @Override
    public float getRangedDamageValueBonus()
    {
        return 40.0f;
    }
}
