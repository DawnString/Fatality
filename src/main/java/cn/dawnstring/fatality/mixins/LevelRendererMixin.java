package cn.dawnstring.fatality.mixins;

import cn.dawnstring.fatality.client.handler.SkyRendererEventHandler;
import cn.dawnstring.fatality.client.renderer.CelestialBodyRenderer;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * LevelRenderer Mixin - 拦截天空渲染调用
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    
    /**
     * 拦截天空渲染，在太阳和月亮渲染之前注入自定义渲染逻辑
     */
    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void interceptRenderSky(PoseStack poseStack, Matrix4f projectionMatrix, float partialTick, Camera camera, boolean isFoggy, Runnable skyFogSetup, CallbackInfo ci) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;
        
        // 检查是否有活跃的事件需要自定义渲染
        if (SkyRendererEventHandler.shouldInterceptRendering()) {
            // 取消原版天空渲染
            ci.cancel();
            
            // 执行自定义天空渲染
            renderCustomSky(poseStack, projectionMatrix, partialTick, camera, isFoggy, skyFogSetup);
        }
    }
    
    /**
     * 自定义天空渲染逻辑
     */
    private void renderCustomSky(PoseStack poseStack, Matrix4f projectionMatrix, float partialTick, Camera camera, boolean isFoggy, Runnable skyFogSetup) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;
        
        // 设置天空雾效
        skyFogSetup.run();
        
        // 获取时间参数
        float timeOfDay = level.getTimeOfDay(partialTick);
        
        // 渲染太阳（如果需要）
        if (SkyRendererEventHandler.shouldRenderCustomSun()) {
            renderCustomSun(poseStack, timeOfDay);
        }
        
        // 渲染月亮（如果需要）
        if (SkyRendererEventHandler.shouldRenderCustomMoon()) {
            renderCustomMoon(poseStack, timeOfDay, level.getMoonPhase());
        }
        
        // 重置渲染拦截状态
        SkyRendererEventHandler.resetRenderingInterception();
    }
    
    /**
     * 自定义太阳渲染
     */
    private void renderCustomSun(PoseStack poseStack, float timeOfDay) {
        float sunAngle = timeOfDay * ((float)Math.PI * 2F);
        float sunX = -Mth.sin(sunAngle) * 100.0F;
        float sunY = Mth.cos(sunAngle) * 100.0F;
        float sunSize = 30.0F;
        
        // 使用自定义太阳渲染器
        BufferBuilder bufferBuilder = new BufferBuilder(256);
        CelestialBodyRenderer.renderSun(poseStack, bufferBuilder, sunX, sunY, sunSize, 0.0F, 1.0F, 1.0F, 0.8F);
    }
    
    /**
     * 自定义月亮渲染
     */
    private void renderCustomMoon(PoseStack poseStack, float timeOfDay, int moonPhase) {
        float moonAngle = timeOfDay * ((float)Math.PI * 2F);
        float moonX = -Mth.sin(moonAngle) * 100.0F;
        float moonY = Mth.cos(moonAngle) * 100.0F;
        float moonSize = 20.0F;
        
        // 使用自定义月亮渲染器
        BufferBuilder bufferBuilder = new BufferBuilder(256);
        CelestialBodyRenderer.renderMoon(poseStack, bufferBuilder, moonX, moonY, moonSize, 0.0F, 1.0F, 1.0F, 0.8F, moonPhase);
    }
}