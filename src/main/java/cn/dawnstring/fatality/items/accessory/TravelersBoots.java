package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class TravelersBoots extends AccessoryItem
{
    public TravelersBoots(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getMovementSpeedBonus()
    {
        return 0.05f;
    }
}
