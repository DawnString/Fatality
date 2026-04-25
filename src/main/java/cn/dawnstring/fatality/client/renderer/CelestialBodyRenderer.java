package cn.dawnstring.fatality.client.renderer;

import cn.dawnstring.fatality.events.GameEvent;
import cn.dawnstring.fatality.events.GameEventManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 天体渲染器 - 处理太阳和月亮的特殊渲染效果
 */
@OnlyIn(Dist.CLIENT)
public class CelestialBodyRenderer {
    
    // 血月相位贴图（8种盈亏状态）
    private static final ResourceLocation BLOOD_MOON_PHASES = ResourceLocation.fromNamespaceAndPath("fatality", "textures/environment/blood_moon_phases.png");
    
    // 日食太阳贴图
    private static final ResourceLocation SOLAR_ECLIPSE_SUN = ResourceLocation.fromNamespaceAndPath("fatality", "textures/environment/sun_eclipse.png");
    
    // 默认太阳和月亮贴图
    private static final ResourceLocation DEFAULT_SUN = ResourceLocation.fromNamespaceAndPath("fatality", "textures/environment/sun.png");
    private static final ResourceLocation DEFAULT_MOON = ResourceLocation.fromNamespaceAndPath("fatality", "textures/environment/moon_phases.png");
    
    /**
     * 渲染太阳（带日食效果）
     */
    public static void renderSun(PoseStack poseStack, BufferBuilder buffer, float x, float y, float size, float rotation, float red, float green, float blue) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;
        
        // 检查是否有活跃的日食事件
        if (GameEventManager.hasActiveEvent(level) && 
            GameEventManager.getActiveEvent(level) != null &&
            GameEventManager.getActiveEvent(level).event == GameEvent.SOLAR_ECLIPSE) {
            
            // 使用日食太阳贴图
            renderCelestialBody(poseStack, buffer, SOLAR_ECLIPSE_SUN, x, y, size, rotation, 0.8f, 0.3f, 0.3f);
        } else {
            // 使用默认太阳贴图
            renderCelestialBody(poseStack, buffer, DEFAULT_SUN, x, y, size, rotation, red, green, blue);
        }
    }
    
    /**
     * 渲染月亮（带血月效果）
     */
    public static void renderMoon(PoseStack poseStack, BufferBuilder buffer, float x, float y, float size, float rotation, float red, float green, float blue, int moonPhase) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;
        
        // 检查是否有活跃的血月事件
        if (GameEventManager.hasActiveEvent(level) && 
            GameEventManager.getActiveEvent(level) != null &&
            GameEventManager.getActiveEvent(level).event == GameEvent.BLOOD_MOON) {
            
            // 使用血月贴图，根据月亮相位选择对应的血月相位
            renderBloodMoon(poseStack, buffer, x, y, size, rotation, moonPhase);
        } else {
            // 使用默认月亮贴图
            renderCelestialBody(poseStack, buffer, DEFAULT_MOON, x, y, size, rotation, red, green, blue);
        }
    }
    
    /**
     * 渲染血月（带8种相位）
     */
    private static void renderBloodMoon(PoseStack poseStack, BufferBuilder buffer, float x, float y, float size, float rotation, int moonPhase) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BLOOD_MOON_PHASES);
        
        // 血月颜色（红色调）
        RenderSystem.setShaderColor(1.0f, 0.3f, 0.3f, 1.0f);
        
        // 计算血月相位对应的UV坐标（8种相位，排列成4列2行）
        int phaseIndex = moonPhase % 8;
        float uOffset = (phaseIndex % 4) * 0.25f;
        float vOffset = (phaseIndex / 4) * 0.5f;
        
        // 渲染血月（使用完整的纹理区域）
        renderTexturedQuad(poseStack, buffer, x, y, size, rotation, uOffset, vOffset, 0.25f, 0.5f);
        
        // 恢复默认颜色
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
    
    /**
     * 通用天体渲染方法
     */
    private static void renderCelestialBody(PoseStack poseStack, BufferBuilder buffer, ResourceLocation texture, 
                                           float x, float y, float size, float rotation, 
                                           float red, float green, float blue) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(red, green, blue, 1.0f);
        
        // 渲染天体
        renderTexturedQuad(poseStack, buffer, x, y, size, rotation, 0.0f, 0.0f, 1.0f, 1.0f);
        
        // 恢复默认颜色
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
    
    /**
     * 渲染带纹理的四边形
     */
    private static void renderTexturedQuad(PoseStack poseStack, BufferBuilder buffer, float x, float y, float size, 
                                          float rotation, float u, float v, float uWidth, float vHeight) {
        poseStack.pushPose();
        poseStack.translate(x, y, 0.0f);
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(rotation));
        
        float halfSize = size / 2.0f;
        
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder tessBuffer = tesselator.getBuilder();
        
        tessBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        
        // 左上角
        tessBuffer.vertex(poseStack.last().pose(), -halfSize, -halfSize, 0.0f)
              .uv(u, v)
              .endVertex();
        
        // 右上角
        tessBuffer.vertex(poseStack.last().pose(), halfSize, -halfSize, 0.0f)
              .uv(u + uWidth, v)
              .endVertex();
        
        // 右下角
        tessBuffer.vertex(poseStack.last().pose(), halfSize, halfSize, 0.0f)
              .uv(u + uWidth, v + vHeight)
              .endVertex();
        
        // 左下角
        tessBuffer.vertex(poseStack.last().pose(), -halfSize, halfSize, 0.0f)
              .uv(u, v + vHeight)
              .endVertex();
        
        Tesselator.getInstance().end();
        poseStack.popPose();
    }
    
    /**
     * 应用环境光效果（血月和日食时的特殊光照）
     */
    public static void applyEnvironmentalLighting() {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;
        
        if (GameEventManager.hasActiveEvent(level)) {
            GameEventManager.ActiveEvent activeEvent = GameEventManager.getActiveEvent(level);
            if (activeEvent != null) {
                switch (activeEvent.event) {
                    case BLOOD_MOON:
                        // 血月效果：红色调环境光
                        RenderSystem.setShaderColor(1.2f, 0.6f, 0.8f, 1.0f);
                        break;
                    case SOLAR_ECLIPSE:
                        // 日食效果：暗色调环境光
                        RenderSystem.setShaderColor(0.4f, 0.4f, 0.6f, 1.0f);
                        break;
                }
            }
        }
    }
    
    /**
     * 重置环境光效果
     */
    public static void resetEnvironmentalLighting() {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}