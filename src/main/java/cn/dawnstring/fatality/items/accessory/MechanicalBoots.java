package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class MechanicalBoots extends AccessoryItem
{
    public MechanicalBoots(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getMovementSpeedBonus()
    {
        return 0.1f;
    }
}
