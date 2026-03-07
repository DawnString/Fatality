package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class AzureMedal extends AccessoryItem
{
    public AzureMedal(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getMagicCriticalDamageBonus()
    {
        return 0.4f;
    }

    @Override
    public float getManaRegenerationBonus()
    {
        return 0.1f;
    }
}
