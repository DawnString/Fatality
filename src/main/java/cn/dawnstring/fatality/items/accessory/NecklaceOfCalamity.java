package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class NecklaceOfCalamity extends AccessoryItem
{
    public NecklaceOfCalamity(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getRangedCriticalDamageBonus()
    {
        return 0.6f;
    }

    @Override
    public float getRangedDamageValueBonus()
    {
        return 40.0f;
    }

    @Override
    public float getCriticalChanceBonus()
    {
        return 0.15f;
    }
}
