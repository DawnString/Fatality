package cn.dawnstring.fatality.client.renderer;

import cn.dawnstring.fatality.Fatality;
import cn.dawnstring.fatality.client.model.MagneticBurstModel;
import cn.dawnstring.fatality.client.model.ModModelLayers;
import cn.dawnstring.fatality.entity.projectile.MagneticBurstProjectile;
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
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class MagneticBurstProjectileRenderer extends EntityRenderer<MagneticBurstProjectile> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Fatality.MODID, "textures/entity/magnetic_burst.png");
    private final MagneticBurstModel model;
    private static final Vector3f MAGNETIC_COLOR = new Vector3f(0.7f, 0.3f, 0.9f); // 紫色磁暴球颜色

    public MagneticBurstProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new MagneticBurstModel(context.bakeLayer(ModModelLayers.MAGNETIC_BURST_LAYER));
    }

    @Override
    public void render(MagneticBurstProjectile entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        // 应用旋转和缩放
        applyTransformations(entity, partialTicks, poseStack);
        
        // 渲染磁暴能量光环
        renderMagneticHalo(poseStack, bufferSource, packedLight, entity.tickCount);
        
        // 渲染磁暴能量轨迹
        renderMagneticTrail(poseStack, bufferSource, packedLight, entity);
        
        // 渲染磁暴核心能量
        renderMagneticCore(poseStack, bufferSource, packedLight, entity.tickCount);

        // 调整大小 - 磁暴球更大更显眼
        poseStack.scale(1.8f, 1.8f, 1.8f);

        // 添加外层磁暴光晕
        poseStack.pushPose();
        poseStack.scale(1.5f, 1.5f, 1.5f);
        VertexConsumer outerGlowConsumer = bufferSource.getBuffer(this.model.renderType(TEXTURE));
        this.model.renderToBuffer(poseStack, outerGlowConsumer, packedLight, OverlayTexture.NO_OVERLAY, MAGNETIC_COLOR.x(), MAGNETIC_COLOR.y(), MAGNETIC_COLOR.z(), 0.3F);
        poseStack.popPose();

        // 添加中层磁暴光晕
        poseStack.pushPose();
        poseStack.scale(1.25f, 1.25f, 1.25f);
        VertexConsumer middleGlowConsumer = bufferSource.getBuffer(this.model.renderType(TEXTURE));
        this.model.renderToBuffer(poseStack, middleGlowConsumer, packedLight, OverlayTexture.NO_OVERLAY, MAGNETIC_COLOR.x(), MAGNETIC_COLOR.y(), MAGNETIC_COLOR.z(), 0.5F);
        poseStack.popPose();

        // 主模型渲染
        VertexConsumer vertexConsumer = bufferSource.getBuffer(this.model.renderType(TEXTURE));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, MAGNETIC_COLOR.x(), MAGNETIC_COLOR.y(), MAGNETIC_COLOR.z(), 0.9F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }
    
    private void applyTransformations(MagneticBurstProjectile entity, float partialTicks, PoseStack poseStack) {
        // 应用旋转
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
        
        // 添加旋转动画 - 磁暴球多轴旋转
        float rotationX = (entity.tickCount + partialTicks) * 8.0f;
        float rotationY = (entity.tickCount + partialTicks) * 10.0f;
        float rotationZ = (entity.tickCount + partialTicks) * 6.0f;
        poseStack.mulPose(Axis.XP.rotationDegrees(rotationX));
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationY));
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotationZ));
        
        // 添加脉动缩放效果
        float pulse = Mth.sin((entity.tickCount + partialTicks) * 0.2f) * 0.15f + 0.85f;
        poseStack.scale(pulse, pulse, pulse);
    }
    
    private void renderMagneticHalo(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int tickCount) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 磁暴能量光环效果
        float haloRadius = 1.2f + Mth.sin(tickCount * 0.15f) * 0.2f;
        int haloSegments = 36;
        float alpha = 0.4f + Mth.sin(tickCount * 0.18f) * 0.3f;
        
        for (int i = 0; i < haloSegments; i++) {
            float angle1 = (float) (2 * Math.PI * i / haloSegments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / haloSegments);
            
            float x1 = haloRadius * Mth.cos(angle1);
            float z1 = haloRadius * Mth.sin(angle1);
            float x2 = haloRadius * Mth.cos(angle2);
            float z2 = haloRadius * Mth.sin(angle2);
            
            vertexConsumer.vertex(matrix, x1, 0, z1)
                .color(MAGNETIC_COLOR.x(), MAGNETIC_COLOR.y(), MAGNETIC_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, x2, 0, z2)
                .color(MAGNETIC_COLOR.x(), MAGNETIC_COLOR.y(), MAGNETIC_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, 0, 0, 0)
                .color(MAGNETIC_COLOR.x(), MAGNETIC_COLOR.y(), MAGNETIC_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }
    
    private void renderMagneticTrail(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, MagneticBurstProjectile entity) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 磁暴能量轨迹效果
        Vec3 motion = entity.getDeltaMovement();
        float trailLength = Math.min(entity.tickCount * 0.15f, 5.0f);
        
        if (trailLength > 0.1f) {
            // 轨迹起始点
            float startX = (float) (-motion.x * trailLength);
            float startY = (float) (-motion.y * trailLength);
            float startZ = (float) (-motion.z * trailLength);
            
            // 轨迹宽度和透明度渐变
            float trailWidth = 0.2f;
            
            // 渲染轨迹线
            vertexConsumer.vertex(matrix, startX - trailWidth, startY, startZ)
                .color(MAGNETIC_COLOR.x(), MAGNETIC_COLOR.y(), MAGNETIC_COLOR.z(), 0.3f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, startX + trailWidth, startY, startZ)
                .color(MAGNETIC_COLOR.x(), MAGNETIC_COLOR.y(), MAGNETIC_COLOR.z(), 0.3f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, 0, 0, 0)
                .color(MAGNETIC_COLOR.x(), MAGNETIC_COLOR.y(), MAGNETIC_COLOR.z(), 0.3f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }
    
    private void renderMagneticCore(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int tickCount) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 磁暴核心能量效果 - 内层光晕
        float coreRadius = 0.6f + Mth.sin(tickCount * 0.25f) * 0.1f;
        int coreSegments = 24;
        float coreAlpha = 0.8f + Mth.sin(tickCount * 0.3f) * 0.2f;
        
        for (int i = 0; i < coreSegments; i++) {
            float angle1 = (float) (2 * Math.PI * i / coreSegments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / coreSegments);
            
            float x1 = coreRadius * Mth.cos(angle1);
            float z1 = coreRadius * Mth.sin(angle1);
            float x2 = coreRadius * Mth.cos(angle2);
            float z2 = coreRadius * Mth.sin(angle2);
            
            vertexConsumer.vertex(matrix, x1, 0, z1)
                .color(0.9f, 0.5f, 1.0f, coreAlpha) // 亮紫色核心
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, x2, 0, z2)
                .color(0.9f, 0.5f, 1.0f, coreAlpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, 0, 0, 0)
                .color(0.9f, 0.5f, 1.0f, coreAlpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }

    @Override
    public ResourceLocation getTextureLocation(MagneticBurstProjectile entity) {
        return TEXTURE;
    }
}