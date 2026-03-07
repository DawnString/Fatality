package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class DivineCore extends AccessoryItem
{
    public DivineCore(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getPanelDamageBonus() {
        return 0.15f;
    }

    @Override
    public float getCriticalChanceBonus() {
        return 0.1f;
    }

    @Override
    public float getHealthPercentageBonus()
    {
        return -0.1f;
    }
}
