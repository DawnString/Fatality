package cn.dawnstring.fatality.client.renderer;

import cn.dawnstring.fatality.entity.projectile.HighEnergyElementBallProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class HighEnergyElementBallProjectileRenderer extends EntityRenderer<HighEnergyElementBallProjectile> {

    private static final Vector3f ENERGY_BALL_COLOR = new Vector3f(1.0f, 0.4f, 0.1f); // 橙红色能量颜色

    public HighEnergyElementBallProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(HighEnergyElementBallProjectile entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        // 调整位置到投掷物中心
        poseStack.translate(0, 0.5, 0);

        // 获取插值后的旋转角度
        float yRot = entity.getYRot();
        float xRot = entity.getXRot();

        // 设置旋转：Y轴旋转让模型朝向水平方向
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot - 90.0F));

        // X轴旋转让模型在竖直方向朝向玩家视角
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));

        // 应用变换（旋转和缩放）
        applyTransformations(poseStack, entity, partialTicks);

        // 渲染多层能量光晕
        renderMultiLayerEnergyHalo(poseStack, bufferSource, packedLight, entity.tickCount);

        // 渲染能量轨迹效果
        renderEnergyTrail(poseStack, bufferSource, packedLight, entity);

        // 渲染能量核心效果
        renderEnergyCore(poseStack, bufferSource, packedLight, entity.tickCount);

        // 渲染能量环效果
        renderEnergyRing(poseStack, bufferSource, packedLight, entity.tickCount);

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(HighEnergyElementBallProjectile entity) {
        // 返回一个空的纹理位置，因为我们使用自定义渲染
        return ResourceLocation.fromNamespaceAndPath("fatality", "textures/empty.png");
    }
    
    private void applyTransformations(PoseStack poseStack, HighEnergyElementBallProjectile entity, float partialTicks) {
        // 添加旋转动画效果
        float rotation = (entity.tickCount + partialTicks) * 20.0F; // 高能元素球旋转速度
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        
        // 添加脉动缩放效果
        float pulseScale = 1.0f + Mth.sin(entity.tickCount * 0.4f) * 0.25f;
        poseStack.scale(pulseScale, pulseScale, pulseScale);
    }
    
    private void renderMultiLayerEnergyHalo(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int tickCount) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath("fatality", "textures/entity/high_energy_ball.png")));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 多层能量光晕效果
        float haloRadius1 = 0.9f + Mth.sin(tickCount * 0.25f) * 0.2f;
        float haloRadius2 = 0.7f + Mth.sin(tickCount * 0.3f) * 0.15f;
        float haloRadius3 = 0.5f + Mth.sin(tickCount * 0.35f) * 0.12f;
        
        int haloSegments = 32;
        float alpha1 = 0.5f + Mth.sin(tickCount * 0.35f) * 0.4f;
        float alpha2 = 0.6f + Mth.sin(tickCount * 0.4f) * 0.5f;
        float alpha3 = 0.7f + Mth.sin(tickCount * 0.45f) * 0.6f;
        
        // 外层能量光环
        renderEnergyHaloRing(matrix, vertexConsumer, packedLight, pose, haloRadius1, haloSegments, alpha1);
        
        // 中层能量光环
        renderEnergyHaloRing(matrix, vertexConsumer, packedLight, pose, haloRadius2, haloSegments, alpha2);
        
        // 内层能量光环
        renderEnergyHaloRing(matrix, vertexConsumer, packedLight, pose, haloRadius3, haloSegments, alpha3);
    }
    
    private void renderEnergyHaloRing(Matrix4f matrix, VertexConsumer vertexConsumer, int packedLight, PoseStack.Pose pose, float radius, int segments, float alpha) {
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (2 * Math.PI * i / segments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / segments);
            
            float x1 = radius * Mth.cos(angle1);
            float z1 = radius * Mth.sin(angle1);
            float x2 = radius * Mth.cos(angle2);
            float z2 = radius * Mth.sin(angle2);
            
            vertexConsumer.vertex(matrix, x1, 0, z1)
                .color(ENERGY_BALL_COLOR.x(), ENERGY_BALL_COLOR.y(), ENERGY_BALL_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, x2, 0, z2)
                .color(ENERGY_BALL_COLOR.x(), ENERGY_BALL_COLOR.y(), ENERGY_BALL_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, 0, 0, 0)
                .color(ENERGY_BALL_COLOR.x(), ENERGY_BALL_COLOR.y(), ENERGY_BALL_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }
    
    private void renderEnergyTrail(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, HighEnergyElementBallProjectile entity) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath("fatality", "textures/entity/high_energy_ball.png")));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 能量轨迹效果
        float trailLength = Math.min(entity.tickCount * 0.2f, 4.0f);
        
        if (trailLength > 0.1f) {
            // 轨迹起始点（基于能量球的移动方向）
            float startX = -trailLength;
            float startZ = 0;
            
            // 轨迹宽度和透明度渐变
            float trailWidth = 0.3f;
            
            // 渲染轨迹线
            vertexConsumer.vertex(matrix, startX - trailWidth, 0, startZ)
                .color(ENERGY_BALL_COLOR.x(), ENERGY_BALL_COLOR.y(), ENERGY_BALL_COLOR.z(), 0.6f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, startX + trailWidth, 0, startZ)
                .color(ENERGY_BALL_COLOR.x(), ENERGY_BALL_COLOR.y(), ENERGY_BALL_COLOR.z(), 0.6f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, 0, 0, 0)
                .color(ENERGY_BALL_COLOR.x(), ENERGY_BALL_COLOR.y(), ENERGY_BALL_COLOR.z(), 0.6f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }
    
    private void renderEnergyCore(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int tickCount) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath("fatality", "textures/entity/high_energy_ball.png")));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 能量核心效果 - 内层光晕
        float coreRadius = 0.4f + Mth.sin(tickCount * 0.5f) * 0.1f;
        int coreSegments = 24;
        float coreAlpha = 0.9f + Mth.sin(tickCount * 0.6f) * 0.3f;
        
        for (int i = 0; i < coreSegments; i++) {
            float angle1 = (float) (2 * Math.PI * i / coreSegments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / coreSegments);
            
            float x1 = coreRadius * Mth.cos(angle1);
            float z1 = coreRadius * Mth.sin(angle1);
            float x2 = coreRadius * Mth.cos(angle2);
            float z2 = coreRadius * Mth.sin(angle2);
            
            vertexConsumer.vertex(matrix, x1, 0, z1)
                .color(1.0f, 0.6f, 0.2f, coreAlpha) // 更亮的能量核心
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, x2, 0, z2)
                .color(1.0f, 0.6f, 0.2f, coreAlpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, 0, 0, 0)
                .color(1.0f, 0.6f, 0.2f, coreAlpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }
    
    private void renderEnergyRing(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int tickCount) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath("fatality", "textures/entity/high_energy_ball.png")));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 能量环效果 - 围绕核心的旋转环
        float ringRadius = 0.6f + Mth.sin(tickCount * 0.4f) * 0.15f;
        int ringSegments = 20;
        float ringAlpha = 0.8f + Mth.sin(tickCount * 0.5f) * 0.4f;
        
        // 旋转角度
        float rotation = tickCount * 0.5f;
        
        for (int i = 0; i < ringSegments; i++) {
            float angle1 = (float) (2 * Math.PI * i / ringSegments) + rotation;
            float angle2 = (float) (2 * Math.PI * (i + 1) / ringSegments) + rotation;
            
            float x1 = ringRadius * Mth.cos(angle1);
            float z1 = ringRadius * Mth.sin(angle1);
            float x2 = ringRadius * Mth.cos(angle2);
            float z2 = ringRadius * Mth.sin(angle2);
            
            vertexConsumer.vertex(matrix, x1, 0, z1)
                .color(1.0f, 0.8f, 0.3f, ringAlpha) // 金色能量环
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, x2, 0, z2)
                .color(1.0f, 0.8f, 0.3f, ringAlpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, 0, 0, 0)
                .color(1.0f, 0.8f, 0.3f, ringAlpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }
}