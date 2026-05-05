package cn.dawnstring.fatality.entity.boss.enderdragon;

import cn.dawnstring.fatality.Fatality;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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
            if (!(entity instanceof CustomEnderDragon)) {
                CustomEnderDragon customDragon = new CustomEnderDragon(event.getLevel());
                
                customDragon.moveTo(enderDragon.getX(), enderDragon.getY(), enderDragon.getZ(), enderDragon.getYRot(), enderDragon.getXRot());
                customDragon.setHealth(enderDragon.getHealth());
                
                event.setEntity(customDragon);
            }
        }
    }
}