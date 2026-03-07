package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class FlameSpiritFlower extends AccessoryItem
{
    public FlameSpiritFlower(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getMagicCriticalDamageBonus()
    {
        return 0.45f;
    }

    @Override
    public float getManaRegenerationBonus()
    {
        return 0.1f;
    }

    @Override
    public float getMagicDamageValueBonus()
    {
        return 35.0f;
    }
}
