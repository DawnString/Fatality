package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class NightmareMedal extends AccessoryItem
{
    public NightmareMedal(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getMagicCriticalDamageBonus() {
        return 0.55f;
    }

    @Override
    public float getManaRegenerationBonus() {
        return 0.15f;
    }
    @Override
    public float getCriticalChanceBonus() {
        return 0.2f;
    }
}
