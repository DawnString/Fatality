package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class CertificateOfValor extends AccessoryItem
{
    public CertificateOfValor(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getCriticalChanceBonus()
    {
        return 0.1f;
    }

    @Override
    public float getHealthRegenerationBonus()
    {
        return 0.05f;
    }
}
