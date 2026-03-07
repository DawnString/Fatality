package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class AbyssDragonBall extends AccessoryItem
{
    public AbyssDragonBall(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getDefensePercentageBonus()
    {
        return 0.1f;
    }
}
