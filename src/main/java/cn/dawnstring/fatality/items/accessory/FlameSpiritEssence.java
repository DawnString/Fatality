package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;
import cn.dawnstring.fatality.registry.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            if (hasAccessoryEquipped(player, FlameSpiritEssence.class)) {
                LivingEntity target = event.getEntity();
                MobEffectInstance spiritualFireEffect = new MobEffectInstance(
                        ModEffects.SPIRITUAL_FIRE_BURN.get(),
                        40,
                        0,
                        false,
                        true
                );
                target.addEffect(spiritualFireEffect);
            }
        }
    }
}
