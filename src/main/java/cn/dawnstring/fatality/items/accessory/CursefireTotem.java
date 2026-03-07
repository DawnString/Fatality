package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class CursefireTotem extends AccessoryItem
{
    public CursefireTotem(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getRangedCriticalDamageBonus()
    {
        return 0.2f;
    }

    @Override
    public float getRangedDamageValueBonus()
    {
        return 20.0f;
    }
}
