package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class StarCloak extends AccessoryItem
{
    public StarCloak(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getMagicCriticalDamageBonus()
    {
        return 0.08f;
    }
}
