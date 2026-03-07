package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class MedalOfVengeance extends AccessoryItem
{
    public MedalOfVengeance(Properties properties) {
        super(properties);
    }

    @Override
    public float getPanelDamageBonus()
    {
        return 0.05f;
    }
}
