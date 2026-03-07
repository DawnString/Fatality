package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class PhantomCharm extends AccessoryItem
{
    public PhantomCharm(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getCriticalChanceBonus() {
        return 0.08f;
    }
}
