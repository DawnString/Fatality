package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class CannedHeart extends AccessoryItem
{
    public CannedHeart(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getHealthRegenerationBonus()
    {
        return 0.1f;
    }
}
