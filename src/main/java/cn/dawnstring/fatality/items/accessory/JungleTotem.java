package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class JungleTotem extends AccessoryItem
{
    public JungleTotem(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getRangedCriticalDamageBonus() {
        return 0.45f;
    }

    @Override
    public float getRangedDamageValueBonus() {
        return 40.0f;
    }
}
