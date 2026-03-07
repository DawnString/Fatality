package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;

public class BootsOfTheJourney extends AccessoryItem
{
    public BootsOfTheJourney(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getMovementSpeedBonus() {
        return 0.08f;
    }

    @Override
    public float getHealthRegenerationBonus() {
        return 0.05f;
    }

    @Override
    public float getDefenseBonus() {
        return 5.0f;
    }
}
