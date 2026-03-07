package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class SpiritWristguard extends AccessoryItem
{
    public SpiritWristguard(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getRangedCriticalDamageBonus() {
        return 0.65f;
    }

    @Override
    public float getRangedDamageValueBonus() {
        return 35.0f;
    }

    @Override
    public float getCriticalChanceBonus() {
        return 0.15f;
    }
}
