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
public class AcidEtchedSac extends AccessoryItem
{
    private static final int EFFECT_DURATION = 200;

    public AcidEtchedSac(Properties properties)
    {
        super(properties);
        setStory("蕴含腐蚀之力的神秘囊袋\n");
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            if (hasAccessoryEquipped(player, AcidEtchedSac.class)) {
                LivingEntity target = event.getEntity();
                MobEffectInstance armorErosionEffect = new MobEffectInstance(
                        ModEffects.ARMOR_EROSION.get(),
                        EFFECT_DURATION,
                        0,
                        false,
                        true
                );
                target.addEffect(armorErosionEffect);
            }
        }
    }
}
