package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class HolySpiritBoots extends AccessoryItem
{
    public HolySpiritBoots(Properties properties)
    {
        super(properties);
    }

    @Override
    public void applyEffects(Player player, ItemStack stack)
    {
        // 先调用父类方法应用基础效果
        super.applyEffects(player, stack);
        player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 20, 0, true, true));
    }

    public void removeEffects(Player player, ItemStack stack)
    {
        // 移除饱和效果
        player.removeEffect(MobEffects.SATURATION);

        // 调用父类方法移除基础效果
        super.removeEffects(player, stack);
    }
}
