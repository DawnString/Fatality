package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class EssenceOfTheEarthSpirit extends AccessoryItem
{
    public EssenceOfTheEarthSpirit(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getHealthRegenerationBonus()
    {
        return 0.05f;
    }
}
