package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class CorruptScarf extends AccessoryItem
{
    public CorruptScarf(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getDamageReductionBonus()
    {
        return 0.07f;
    }
}
