package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class KnightsShield extends AccessoryItem
{
    public KnightsShield(Properties properties) {
        super(properties);
    }

    @Override
    public float getDefenseBonus()
    {
        return 15.0f;
    }
}
