package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class CurseflameBlossom extends AccessoryItem
{
    public CurseflameBlossom(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getMagicCriticalDamageBonus() {
        return 0.25f;
    }

    @Override
    public int getMaxManaBonus() {
        return 40;
    }

    @Override
    public float getMagicDamageValueBonus() {
        return 20.0f;
    }

    @Override
    public float getManaRegenerationBonus() {
        return 0.05f;
    }
}
