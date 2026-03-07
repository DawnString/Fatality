package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class SacredFlameGloves extends AccessoryItem
{
    public SacredFlameGloves(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getAttackSpeedBonus()
    {
        return 0.35f;
    }

    @Override
    public float getMeleeDamageValueBonus()
    {
        return 50.0f;
    }

    @Override
    public float getCriticalChanceBonus()
    {
        return 0.2f;
    }
}
