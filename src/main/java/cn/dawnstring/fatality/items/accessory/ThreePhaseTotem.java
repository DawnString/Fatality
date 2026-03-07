package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class ThreePhaseTotem extends AccessoryItem
{
    public ThreePhaseTotem(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getRangedCriticalDamageBonus()
    {
        return 0.15f;
    }

    @Override
    public float getRangedDamageValueBonus()
    {
        return 15.0f;
    }
}
