package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class MagicArrow extends AccessoryItem
{
    public MagicArrow(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getRangedDamageValueBonus()
    {
        return 6.0f;
    }
}
