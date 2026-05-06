package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class OminousCurse extends AccessoryItem
{
    public OminousCurse(Properties properties)
    {
        super(properties);
    }

    @Override
    public void applyEffects(Player player, ItemStack stack) {
        super.applyEffects(player, stack);
        applyNegativeEffectImmunity(player);
    }

    @Override
    public void removeEffects(Player player, ItemStack stack) {
        removeNegativeEffectImmunity(player);
        super.removeEffects(player, stack);
    }
}
