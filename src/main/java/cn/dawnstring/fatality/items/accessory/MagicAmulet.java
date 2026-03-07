package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class MagicAmulet extends AccessoryItem
{
    public MagicAmulet(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getMagicDamageValueBonus() {
        return 20.0f;
    }

    @Override
    public float getMagicCriticalDamageBonus() {
        return 0.14f;
    }

    @Override
    public int getMaxManaBonus() {
        return 30;
    }
}
