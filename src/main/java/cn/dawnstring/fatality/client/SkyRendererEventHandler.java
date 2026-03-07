package cn.dawnstring.fatality.client;

import cn.dawnstring.fatality.client.renderer.CelestialBodyRenderer;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 天空渲染事件处理器 - 拦截并修改太阳和月亮的渲染
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = "fatality", value = Dist.CLIENT)
public class SkyRendererEventHandler {
    
    // 标记是否已经替换了渲染
    private static boolean renderingModified = false;
    
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            // 在天空渲染之后应用环境光效果
            CelestialBodyRenderer.applyEnvironmentalLighting();
        }
    }
    
    /**
     * 拦截太阳渲染的方法
     */
    public static void interceptSunRendering(PoseStack poseStack, BufferBuilder buffer, 
                                            float x, float y, float size, float rotation, 
                                            float red, float green, float blue) {
        // 使用自定义太阳渲染器
        CelestialBodyRenderer.renderSun(poseStack, buffer, x, y, size, rotation, red, green, blue);
        renderingModified = true;
    }
    
    /**
     * 拦截月亮渲染的方法
     */
    public static void interceptMoonRendering(PoseStack poseStack, BufferBuilder buffer, 
                                             float x, float y, float size, float rotation, 
                                             float red, float green, float blue, int moonPhase) {
        // 使用自定义月亮渲染器
        CelestialBodyRenderer.renderMoon(poseStack, buffer, x, y, size, rotation, red, green, blue, moonPhase);
        renderingModified = true;
    }
    
    /**
     * 重置渲染状态
     */
    public static void resetRenderingState() {
        renderingModified = false;
        CelestialBodyRenderer.resetEnvironmentalLighting();
    }
    
    /**
     * 检查是否已经修改了渲染
     */
    public static boolean isRenderingModified() {
        return renderingModified;
    }
}