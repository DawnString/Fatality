package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class SpellfireGauntlets extends AccessoryItem
{
    public SpellfireGauntlets(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getAttackSpeedBonus()
    {
        return 0.1f;
    }

    @Override
    public float getMeleeDamageValueBonus()
    {
        return 20.0f;
    }

    @Override
    public float getCriticalChanceBonus()
    {
        return 0.1f;
    }
}
