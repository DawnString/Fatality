package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;
import cn.dawnstring.fatality.registry.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class FlameSpiritEssence extends AccessoryItem
{
    public FlameSpiritEssence(Properties properties)
    {
        super(properties);
    }

    // 攻击事件处理 - 当玩家攻击目标时触发
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 检查伤害来源是否为玩家
        if (event.getSource().getEntity() instanceof Player player) {
            // 检查玩家是否佩戴
            if (hasFlameSpiritEssenceEquipped(player)) {
                // 获取被攻击的目标
                LivingEntity target = event.getEntity();

                // 对目标施加效果，持续2秒（40 ticks）
                MobEffectInstance spiritualFireEffect = new MobEffectInstance(
                        ModEffects.SPIRITUAL_FIRE_BURN.get(),
                        40, // 2秒持续时间（20 ticks/秒 * 2秒）
                        0,   // 效果等级0
                        false, // 不显示粒子效果
                        true   // 显示图标
                );

                target.addEffect(spiritualFireEffect);
            }
        }
    }

    // 检查玩家是否佩戴
    private static boolean hasFlameSpiritEssenceEquipped(Player player) {
        var accessoryInventory = cn.dawnstring.fatality.inventory.AccessoryInventory.get(player);
        if (accessoryInventory != null) {
            for (int i = 0; i < accessoryInventory.getItemHandler().getSlots(); i++) {
                ItemStack accessory = accessoryInventory.getItemHandler().getStackInSlot(i);
                if (accessory.getItem() instanceof FlameSpiritEssence) {
                    return true;
                }
            }
        }
        return false;
    }

}
