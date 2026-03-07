package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class FuneralFlower extends AccessoryItem
{
    public FuneralFlower(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getHealthBonus() {
        return 60.0f;
    }

    @Override
    public float getDefenseBonus() {
        return 10.0f;
    }

    @Override
    public float getHealthRegenerationBonus() {
        return 0.05f;
    }
}
