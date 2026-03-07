package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class ElementalBloom extends AccessoryItem
{
    public ElementalBloom(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getMagicCriticalDamageBonus() {
        return 0.65f;
    }

    @Override
    public float getMagicDamageValueBonus() {
        return 45.0f;
    }

    @Override
    public float getManaRegenerationBonus() {
        return 0.15f;
    }
}
