package cn.dawnstring.fatality.client.handler;

import cn.dawnstring.fatality.events.GameEvent;
import cn.dawnstring.fatality.events.GameEventManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 天空渲染事件处理器 - 控制太阳和月亮的自定义渲染
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = "fatality", value = Dist.CLIENT)
public class SkyRendererEventHandler {
    
    private static boolean sunRenderingIntercepted = false;
    private static boolean moonRenderingIntercepted = false;
    
    /**
     * 检查是否应该拦截天空渲染
     */
    public static boolean shouldInterceptRendering() {
        return sunRenderingIntercepted || moonRenderingIntercepted;
    }
    
    /**
     * 检查是否应该渲染自定义太阳
     */
    public static boolean shouldRenderCustomSun() {
        return sunRenderingIntercepted;
    }
    
    /**
     * 检查是否应该渲染自定义月亮
     */
    public static boolean shouldRenderCustomMoon() {
        return moonRenderingIntercepted;
    }
    
    /**
     * 拦截太阳渲染
     */
    public static void interceptSunRendering() {
        sunRenderingIntercepted = true;
    }
    
    /**
     * 拦截月亮渲染
     */
    public static void interceptMoonRendering() {
        moonRenderingIntercepted = true;
    }
    
    /**
     * 重置渲染拦截状态
     */
    public static void resetRenderingInterception() {
        sunRenderingIntercepted = false;
        moonRenderingIntercepted = false;
    }
    
    /**
     * 检查是否有活跃的日食事件
     */
    public static boolean isSolarEclipseActive() {
        Level level = Minecraft.getInstance().level;
        if (level == null) return false;
        
        GameEventManager.ActiveEvent activeEvent = GameEventManager.getActiveEvent(level);
        if (activeEvent != null) {
            return activeEvent.event == GameEvent.SOLAR_ECLIPSE;
        }
        return false;
    }
    
    /**
     * 检查是否有活跃的血月事件
     */
    public static boolean isBloodMoonActive() {
        Level level = Minecraft.getInstance().level;
        if (level == null) return false;
        
        GameEventManager.ActiveEvent activeEvent = GameEventManager.getActiveEvent(level);
        if (activeEvent != null) {
            return activeEvent.event == GameEvent.BLOOD_MOON;
        }
        return false;
    }
    
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            // 检查是否有活跃的事件需要自定义天空渲染
            if (isBloodMoonActive() || isSolarEclipseActive()) {
                // 根据事件类型设置拦截状态
                if (isBloodMoonActive()) {
                    interceptMoonRendering();
                }
                if (isSolarEclipseActive()) {
                    interceptSunRendering();
                }
            }
        }
    }
}