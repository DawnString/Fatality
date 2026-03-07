package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class TortoiseShield extends AccessoryItem
{
    public TortoiseShield(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getDefenseBonus()
    {
        return 10.0f;
    }
}
