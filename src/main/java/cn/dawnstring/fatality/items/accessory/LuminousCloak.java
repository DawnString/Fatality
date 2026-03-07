package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class LuminousCloak extends AccessoryItem
{
    public LuminousCloak(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getMagicCriticalDamageBonus() {
        return 0.12f;
    }
}
