package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class NecroticBlossom extends AccessoryItem
{
    public NecroticBlossom(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getMagicCriticalDamageBonus() {
        return 0.6f;
    }

    @Override
    public float getManaRegenerationBonus() {
        return 0.15f;
    }

    @Override
    public float getMagicDamageValueBonus() {
        return 40.0f;
    }
}
