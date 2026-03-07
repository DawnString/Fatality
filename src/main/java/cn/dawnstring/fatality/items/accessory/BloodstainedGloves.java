package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class BloodstainedGloves extends AccessoryItem
{
    public BloodstainedGloves(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getAttackSpeedBonus() {
        return 0.08f;
    }

    @Override
    public float getMeleeDamageValueBonus() {
        return 10.0f;
    }
    @Override
    public float getCriticalChanceBonus() {
        return 0.07f;
    }
}
