package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class RingOfVengeance extends AccessoryItem
{
    public RingOfVengeance(Properties properties) {
        super(properties);
    }

    @Override
    public float getPanelDamageBonus()
    {
        return 0.05f;
    }

    @Override
    public float getCriticalChanceBonus()
    {
        return 0.08f;
    }
}
