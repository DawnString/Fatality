package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class TsunamiNecklace extends AccessoryItem
{
    public TsunamiNecklace(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getMeleeCriticalDamageBonus()
    {
        return 0.4f;
    }

    @Override
    public float getMeleeDamageValueBonus()
    {
        return 30.0f;
    }

    @Override
    public float getCriticalChanceBonus()
    {
        return 0.1f;
    }
}
