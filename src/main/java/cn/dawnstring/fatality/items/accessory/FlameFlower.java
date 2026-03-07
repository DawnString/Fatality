package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class FlameFlower extends AccessoryItem
{
    public FlameFlower(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getManaRegenerationBonus()
    {
        return 0.05f;
    }
}
