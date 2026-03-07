package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class SpiritNecklace extends AccessoryItem
{
    public SpiritNecklace(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getMeleeCriticalDamageBonus() {
        return 0.08f;
    }

    @Override
    public float getMeleeDamageValueBonus() {
        return 40f;
    }

    @Override
    public float getCriticalChanceBonus() {
        return 0.15f;
    }
}
