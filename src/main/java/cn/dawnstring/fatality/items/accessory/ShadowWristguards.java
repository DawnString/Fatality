package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class ShadowWristguards extends AccessoryItem
{
    public ShadowWristguards(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getMeleeCriticalDamageBonus() {
        return 0.25f;
    }

    @Override
    public float getMeleeDamageValueBonus() {
        return 15.0f;
    }

    @Override
    public float getCriticalChanceBonus() {
        return 0.05f;
    }
}
