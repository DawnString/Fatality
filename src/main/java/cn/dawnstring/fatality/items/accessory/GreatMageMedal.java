package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class GreatMageMedal extends AccessoryItem
{
    public GreatMageMedal(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getMagicCriticalDamageBonus() {
        return 0.3f;
    }

    @Override
    public float getManaRegenerationBonus() {
        return 0.1f;
    }
}
