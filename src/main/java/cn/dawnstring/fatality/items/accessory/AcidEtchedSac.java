package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;
import cn.dawnstring.fatality.registry.ModEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class AcidEtchedSac extends AccessoryItem
{
    // 效果持续时间（10秒，以ticks为单位）
    private static final int EFFECT_DURATION = 200; // 10秒 * 20 ticks/秒

    public AcidEtchedSac(Properties properties)
    {
        super(properties);

        setStory("蕴含腐蚀之力的神秘囊袋\n");
    }

    // 攻击事件处理 - 当玩家攻击目标时触发
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 检查伤害来源是否为玩家
        if (event.getSource().getEntity() instanceof Player player) {
            // 检查玩家是否佩戴酸蚀囊袋
            if (hasAcidEtchedSacEquipped(player)) {
                // 获取被攻击的目标
                LivingEntity target = event.getEntity();

                // 对目标施加护甲侵蚀效果，持续10秒
                MobEffectInstance armorErosionEffect = new MobEffectInstance(
                        ModEffects.ARMOR_EROSION.get(),
                        EFFECT_DURATION, // 10秒持续时间
                        0,               // 效果等级0
                        false,           // 不显示粒子效果
                        true             // 显示图标
                );

                target.addEffect(armorErosionEffect);
            }
        }
    }

    // 检查玩家是否佩戴酸蚀囊袋
    private static boolean hasAcidEtchedSacEquipped(Player player) {
        // 检查饰品栏
        var accessoryInventory = cn.dawnstring.fatality.inventory.AccessoryInventory.get(player);
        if (accessoryInventory != null) {
            for (int i = 0; i < accessoryInventory.getItemHandler().getSlots(); i++) {
                ItemStack accessory = accessoryInventory.getItemHandler().getStackInSlot(i);
                if (accessory.getItem() instanceof AcidEtchedSac) {
                    return true;
                }
            }
        }
        return false;
    }
}