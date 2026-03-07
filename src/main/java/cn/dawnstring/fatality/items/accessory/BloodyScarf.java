package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class BloodyScarf extends AccessoryItem
{
    public BloodyScarf(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getDamageReductionBonus() {
        return 0.05f;
    }

}
