package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class StarCharm extends AccessoryItem
{
    public StarCharm(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getMagicDamageValueBonus()
    {
        return 7.0f;
    }
}
