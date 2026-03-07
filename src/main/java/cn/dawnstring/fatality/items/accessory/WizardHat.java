package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class WizardHat extends AccessoryItem
{
    public WizardHat(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getMagicDamageValueBonus()
    {
        return 9.0f;
    }
}
