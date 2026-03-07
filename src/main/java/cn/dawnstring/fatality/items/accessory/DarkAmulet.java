package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class DarkAmulet extends AccessoryItem
{
    public DarkAmulet(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getPanelDamageBonus()
    {
        return 0.5f;
    }
    @Override
    public float getHealthPercentageBonus()
    {
        return -0.4f;
    }
}
