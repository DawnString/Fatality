package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class NecklaceOfLife extends AccessoryItem
{
    public NecklaceOfLife(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getHealthBonus()
    {
        return 10.0f;
    }
}
