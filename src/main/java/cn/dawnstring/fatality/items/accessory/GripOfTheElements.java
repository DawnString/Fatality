package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class GripOfTheElements extends AccessoryItem
{
    public GripOfTheElements(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getAttackSpeedBonus() {
        return 0.4f;
    }

    @Override
    public float getMeleeDamageValueBonus() {
        return 60.0f;
    }

    @Override
    public float getCriticalChanceBonus() {
        return 0.25f;
    }
}
