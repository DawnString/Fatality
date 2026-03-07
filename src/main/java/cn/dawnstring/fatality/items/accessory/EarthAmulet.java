package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class EarthAmulet extends AccessoryItem
{
    public EarthAmulet(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getHealthRegenerationBonus()
    {
        return 0.05f;
    }

    @Override
    public float getHealthBonus()
    {
        return 80.0f;
    }

    @Override
    public float getDefenseBonus()
    {
        return 20.0f;
    }
}
