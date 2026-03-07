package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class DragonBracers extends AccessoryItem
{
    public DragonBracers(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getRangedCriticalDamageBonus() {
        return 0.20f;
    }

    @Override
    public float getRangedDamageValueBonus() {
        return 10.0f;
    }

    @Override
    public float getCriticalChanceBonus() {
        return 0.05f;
    }
}
