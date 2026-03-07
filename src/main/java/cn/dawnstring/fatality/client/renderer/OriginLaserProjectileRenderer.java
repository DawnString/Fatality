package cn.dawnstring.fatality.client.renderer;

import cn.dawnstring.fatality.Fatality;
import cn.dawnstring.fatality.client.model.ModModelLayers;
import cn.dawnstring.fatality.client.model.OriginLaserModel;
import cn.dawnstring.fatality.entity.projectile.OriginLaserProjectile;
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

public class OriginLaserProjectileRenderer extends EntityRenderer<OriginLaserProjectile> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Fatality.MODID, "textures/entity/origin_laser.png");
    private final OriginLaserModel model;
    
    // 起源激光颜色 - 纯净的白色能量束
    private static final Vector3f ORIGIN_LASER_COLOR = new Vector3f(1.0f, 1.0f, 1.0f);

    public OriginLaserProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new OriginLaserModel(context.bakeLayer(ModModelLayers.ORIGIN_LASER_LAYER));
    }

    @Override
    public void render(OriginLaserProjectile entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        // 应用旋转和缩放
        applyTransformations(entity, partialTicks, poseStack);
        
        // 渲染起源能量光环
        renderOriginHalo(poseStack, bufferSource, packedLight, entity.tickCount);
        
        // 渲染起源激光轨迹
        renderOriginLaserTrail(poseStack, bufferSource, packedLight, entity);
        
        // 渲染起源核心能量
        renderOriginCore(poseStack, bufferSource, packedLight, entity.tickCount);

        // 调整大小 - 起源激光更大更显眼
        poseStack.scale(2.0f, 2.0f, 2.0f);

        // 添加外层起源光晕
        poseStack.pushPose();
        poseStack.scale(1.4f, 1.4f, 1.4f);
        VertexConsumer outerGlowConsumer = bufferSource.getBuffer(this.model.renderType(TEXTURE));
        this.model.renderToBuffer(poseStack, outerGlowConsumer, packedLight, OverlayTexture.NO_OVERLAY, ORIGIN_LASER_COLOR.x(), ORIGIN_LASER_COLOR.y(), ORIGIN_LASER_COLOR.z(), 0.3F);
        poseStack.popPose();

        // 添加中层起源光晕
        poseStack.pushPose();
        poseStack.scale(1.2f, 1.2f, 1.2f);
        VertexConsumer middleGlowConsumer = bufferSource.getBuffer(this.model.renderType(TEXTURE));
        this.model.renderToBuffer(poseStack, middleGlowConsumer, packedLight, OverlayTexture.NO_OVERLAY, ORIGIN_LASER_COLOR.x(), ORIGIN_LASER_COLOR.y(), ORIGIN_LASER_COLOR.z(), 0.5F);
        poseStack.popPose();

        // 主模型渲染
        VertexConsumer vertexConsumer = bufferSource.getBuffer(this.model.renderType(TEXTURE));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, ORIGIN_LASER_COLOR.x(), ORIGIN_LASER_COLOR.y(), ORIGIN_LASER_COLOR.z(), 0.9F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }
    
    private void applyTransformations(OriginLaserProjectile entity, float partialTicks, PoseStack poseStack) {
        // 应用旋转
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
        
        // 添加旋转动画
        float rotation = (entity.tickCount + partialTicks) * 12.0f; // 中等旋转速度
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        
        // 添加脉动缩放效果
        float pulse = Mth.sin((entity.tickCount + partialTicks) * 0.25f) * 0.1f + 0.9f;
        poseStack.scale(pulse, pulse, pulse);
    }
    
    private void renderOriginHalo(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int tickCount) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 起源能量光环效果
        float haloRadius = 1.8f + Mth.sin(tickCount * 0.18f) * 0.25f;
        int haloSegments = 40;
        float alpha = 0.4f + Mth.sin(tickCount * 0.22f) * 0.3f;
        
        for (int i = 0; i < haloSegments; i++) {
            float angle1 = (float) (2 * Math.PI * i / haloSegments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / haloSegments);
            
            float x1 = haloRadius * Mth.cos(angle1);
            float z1 = haloRadius * Mth.sin(angle1);
            float x2 = haloRadius * Mth.cos(angle2);
            float z2 = haloRadius * Mth.sin(angle2);
            
            vertexConsumer.vertex(matrix, x1, 0, z1)
                .color(ORIGIN_LASER_COLOR.x(), ORIGIN_LASER_COLOR.y(), ORIGIN_LASER_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, x2, 0, z2)
                .color(ORIGIN_LASER_COLOR.x(), ORIGIN_LASER_COLOR.y(), ORIGIN_LASER_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, 0, 0, 0)
                .color(ORIGIN_LASER_COLOR.x(), ORIGIN_LASER_COLOR.y(), ORIGIN_LASER_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }
    
    private void renderOriginLaserTrail(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, OriginLaserProjectile entity) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 起源激光轨迹效果
        Vec3 motion = entity.getDeltaMovement();
        float trailLength = Math.min(entity.tickCount * 0.25f, 6.0f);
        
        if (trailLength > 0.1f) {
            // 轨迹起始点
            float startX = (float) (-motion.x * trailLength);
            float startY = (float) (-motion.y * trailLength);
            float startZ = (float) (-motion.z * trailLength);
            
            // 轨迹宽度和透明度渐变
            float trailWidth = 0.25f;
            
            // 渲染轨迹线
            vertexConsumer.vertex(matrix, startX - trailWidth, startY, startZ)
                .color(ORIGIN_LASER_COLOR.x(), ORIGIN_LASER_COLOR.y(), ORIGIN_LASER_COLOR.z(), 0.35f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, startX + trailWidth, startY, startZ)
                .color(ORIGIN_LASER_COLOR.x(), ORIGIN_LASER_COLOR.y(), ORIGIN_LASER_COLOR.z(), 0.35f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, 0, 0, 0)
                .color(ORIGIN_LASER_COLOR.x(), ORIGIN_LASER_COLOR.y(), ORIGIN_LASER_COLOR.z(), 0.35f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }
    
    private void renderOriginCore(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int tickCount) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 起源核心能量效果 - 内层光晕
        float coreRadius = 0.8f + Mth.sin(tickCount * 0.35f) * 0.15f;
        int coreSegments = 28;
        float coreAlpha = 0.7f + Mth.sin(tickCount * 0.45f) * 0.2f;
        
        for (int i = 0; i < coreSegments; i++) {
            float angle1 = (float) (2 * Math.PI * i / coreSegments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / coreSegments);
            
            float x1 = coreRadius * Mth.cos(angle1);
            float z1 = coreRadius * Mth.sin(angle1);
            float x2 = coreRadius * Mth.cos(angle2);
            float z2 = coreRadius * Mth.sin(angle2);
            
            vertexConsumer.vertex(matrix, x1, 0, z1)
                .color(0.8f, 0.9f, 1.0f, coreAlpha) // 淡蓝色核心
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, x2, 0, z2)
                .color(0.8f, 0.9f, 1.0f, coreAlpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, 0, 0, 0)
                .color(0.8f, 0.9f, 1.0f, coreAlpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }

    @Override
    public ResourceLocation getTextureLocation(OriginLaserProjectile entity) {
        return TEXTURE;
    }
}