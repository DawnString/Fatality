package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class NatureMedal extends AccessoryItem
{
    public NatureMedal(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getMagicCriticalDamageBonus() {
        return 0.45f;
    }

    @Override
    public float getManaRegenerationBonus() {
        return 0.1f;
    }
}
