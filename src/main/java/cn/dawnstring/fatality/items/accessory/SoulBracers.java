package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class SoulBracers extends AccessoryItem
{
    public SoulBracers(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getRangedDamageValueBonus() {
        return 30.0f;
    }

    @Override
    public float getRangedCriticalDamageBonus() {
        return 0.55f;
    }

    @Override
    public float getCriticalChanceBonus() {
        return 0.1f;
    }
}
