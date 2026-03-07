package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class WaterSpiritGloves extends AccessoryItem
{
    public WaterSpiritGloves(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getAttackSpeedBonus()
    {
        return 0.25f;
    }

    @Override
    public float getMeleeDamageValueBonus()
    {
        return 30.0f;
    }

    @Override
    public float getCriticalChanceBonus()
    {
        return 0.15f;
    }
}
