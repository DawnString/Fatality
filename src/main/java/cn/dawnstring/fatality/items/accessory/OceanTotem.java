package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class OceanTotem extends AccessoryItem
{
    public OceanTotem(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getRangedCriticalDamageBonus()
    {
        return 0.3f;
    }

    @Override
    public float getRangedDamageValueBonus()
    {
        return 30.0f;
    }
}
