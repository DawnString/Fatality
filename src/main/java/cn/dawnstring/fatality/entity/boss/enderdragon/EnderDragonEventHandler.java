package cn.dawnstring.fatality.entity.boss.enderdragon;

import cn.dawnstring.fatality.Fatality;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Fatality.MODID)
public class EnderDragonEventHandler {

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        
        if (entity.getType() == EntityType.ENDER_DRAGON && entity instanceof EnderDragon enderDragon) {
            Fatality.LOGGER.info("Found vanilla Ender Dragon, setting health to 115200");
            enderDragon.getAttribute(Attributes.MAX_HEALTH).setBaseValue(115200.0);
            enderDragon.setHealth(115200.0f);
        }
    }
}