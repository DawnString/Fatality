package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class MechanicalEyeOfIntelligence extends AccessoryItem
{
    public MechanicalEyeOfIntelligence(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getRangedCriticalDamageBonus() {
        return 0.08f;
    }
}
