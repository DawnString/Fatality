package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class SpellOfFireManaCharm extends AccessoryItem
{
    public SpellOfFireManaCharm(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getMagicCriticalDamageBonus()
    {
        return 0.25f;
    }

    @Override
    public float getMagicDamageValueBonus()
    {
        return 20.0f;
    }

    @Override
    public int getMaxManaBonus()
    {
        return 30;
    }
}
