package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class DragonEngravedHilt extends AccessoryItem
{
    public DragonEngravedHilt(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getAttackSpeedBonus()
    {
        return 0.15f;
    }

    @Override
    public float getDefenseBonus()
    {
        return 10.0f;
    }

    @Override
    public float getPanelDamageBonus()
    {
        return 0.05f;
    }
}
