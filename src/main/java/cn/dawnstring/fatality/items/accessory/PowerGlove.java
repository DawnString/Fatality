package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class PowerGlove extends AccessoryItem
{
    public PowerGlove(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getAttackSpeedBonus()
    {
        return 0.08f;
    }

    @Override
    public float getMeleeDamageValueBonus()
    {
        return 5.0f;
    }

    @Override
    public float getCriticalChanceBonus()
    {
        return 0.05f;
    }
}
