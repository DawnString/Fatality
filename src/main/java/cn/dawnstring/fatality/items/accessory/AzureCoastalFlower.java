package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class AzureCoastalFlower extends AccessoryItem
{
    public AzureCoastalFlower(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getMagicCriticalDamageBonus()
    {
        return 0.35f;
    }

    @Override
    public float getManaRegenerationBonus()
    {
        return 0.05f;
    }

    @Override
    public float getMagicDamageValueBonus()
    {
        return 30.0f;
    }
}
