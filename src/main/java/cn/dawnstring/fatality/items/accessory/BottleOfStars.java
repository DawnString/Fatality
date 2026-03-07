package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class BottleOfStars extends AccessoryItem
{
    public BottleOfStars(Properties properties)
    {
        super(properties);
    }

    @Override
    public int getMaxManaBonus()
    {
        return 20;
    }
}
