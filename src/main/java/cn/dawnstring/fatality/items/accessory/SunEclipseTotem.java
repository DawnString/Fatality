package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class SunEclipseTotem extends AccessoryItem
{
    public SunEclipseTotem(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getRangedDamageValueBonus()
    {
        return 15.0f;
    }
}
