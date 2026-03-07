package cn.dawnstring.fatality.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = "fatality", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class MainMenuReplacer {

    private static boolean hasReplaced = false;

    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        Screen screen = event.getScreen();

        // 如果检测到原版主菜单，并且还没有替换过
        if (screen instanceof TitleScreen && !hasReplaced) {
            // 替换为自定义主菜单
            event.setNewScreen(new CustomMainMenu());
            hasReplaced = true;
        }
    }

    @SubscribeEvent
    public static void onScreenClose(ScreenEvent.Closing event) {
        // 当屏幕关闭时重置替换状态
        if (event.getScreen() instanceof CustomMainMenu) {
            hasReplaced = false;
        }
    }
}