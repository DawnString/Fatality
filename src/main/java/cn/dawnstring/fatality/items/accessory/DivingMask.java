package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class DivingMask extends AccessoryItem
{
    public DivingMask(Properties properties)
    {
        super(properties);
    }

    @Override
    public void applyEffects(Player player, ItemStack stack)
    {
        // 先调用父类方法应用基础效果
        super.applyEffects(player, stack);
        player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 20, 0, true, true));
    }

    public void removeEffects(Player player, ItemStack stack)
    {
        // 移除水下呼吸效果
        player.removeEffect(MobEffects.WATER_BREATHING);

        // 调用父类方法移除基础效果
        super.removeEffects(player, stack);
    }
}
