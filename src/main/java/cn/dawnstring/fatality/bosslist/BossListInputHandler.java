package cn.dawnstring.fatality.bosslist;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "fatality", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class BossListInputHandler {
    
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        
        if (BossListKeyBinding.OPEN_BOSS_LIST.consumeClick()) {
            if (minecraft.screen == null) { // 确保没有其他界面打开
                minecraft.setScreen(new BossListScreen());
            }
        }
    }
}