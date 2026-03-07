package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class UltimateYinStone extends AccessoryItem
{
    public UltimateYinStone(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getDefenseBonus()
    {
        return 30.0f;
    }

    @Override
    public float getHealthRegenerationBonus()
    {
        return 0.1f;
    }
}
