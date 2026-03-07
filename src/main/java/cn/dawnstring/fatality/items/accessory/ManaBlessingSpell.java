package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class ManaBlessingSpell extends AccessoryItem
{
    public ManaBlessingSpell(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getMagicCriticalDamageBonus()
    {
        return 0.10f;
    }
}
