package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class SoulRing extends AccessoryItem
{
    public SoulRing(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getCriticalChanceBonus() {
        return 0.07f;
    }
}
