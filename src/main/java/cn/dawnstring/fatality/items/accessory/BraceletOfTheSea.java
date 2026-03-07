package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class BraceletOfTheSea extends AccessoryItem
{
    public BraceletOfTheSea(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getRangedDamageValueBonus()
    {
        return 20.0f;
    }

    @Override
    public float getRangedCriticalDamageBonus()
    {
        return 0.35f;
    }

    @Override
    public float getCriticalChanceBonus()
    {
        return 0.1f;
    }
}
