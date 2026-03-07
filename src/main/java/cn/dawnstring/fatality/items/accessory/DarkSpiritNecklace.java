package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class DarkSpiritNecklace extends AccessoryItem
{
    public DarkSpiritNecklace(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getMeleeCriticalDamageBonus() {
        return 0.20f;
    }

    @Override
    public float getMeleeDamageValueBonus() {
        return 20.0f;
    }

    @Override
    public float getCriticalChanceBonus() {
        return 0.05f;
    }
}
