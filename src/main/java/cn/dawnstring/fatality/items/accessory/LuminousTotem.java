package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class LuminousTotem extends AccessoryItem
{
    public LuminousTotem(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getMagicDamageValueBonus() {
        return 10.0f;
    }
}
