package cn.dawnstring.fatality.client.renderer;

import cn.dawnstring.fatality.Fatality;
import cn.dawnstring.fatality.client.model.GaussLaserModel;
import cn.dawnstring.fatality.client.model.ModModelLayers;
import cn.dawnstring.fatality.client.shader.ProjectileShaderManager;
import cn.dawnstring.fatality.entity.projectile.GaussLaserProjectile;
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

public class GaussLaserProjectileRenderer extends EntityRenderer<GaussLaserProjectile> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Fatality.MODID, "textures/entity/gauss_laser.png");
    private final GaussLaserModel model;
    
    // 高斯激光颜色 - 蓝紫色能量束
    private static final Vector3f LASER_COLOR = new Vector3f(0.4f, 0.2f, 0.8f);

    public GaussLaserProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new GaussLaserModel(context.bakeLayer(ModModelLayers.GAUSS_LASER_LAYER));
    }

    @Override
    public void render(GaussLaserProjectile entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        // 应用旋转和缩放
        applyTransformations(entity, partialTicks, poseStack);
        
        // 使用着色器渲染能量光晕
        renderEnergyHaloWithShader(poseStack, bufferSource, packedLight, entity.tickCount, entity.position());
        
        // 使用着色器渲染激光轨迹
        renderLaserTrailWithShader(poseStack, bufferSource, packedLight, entity);

        // 调整大小 - 高斯激光更大更显眼
        poseStack.scale(1.5f, 1.5f, 1.5f);

        // 添加能量光晕效果
        poseStack.pushPose();
        poseStack.scale(1.2f, 1.2f, 1.2f);
        VertexConsumer glowConsumer = bufferSource.getBuffer(this.model.renderType(TEXTURE));
        this.model.renderToBuffer(poseStack, glowConsumer, packedLight, OverlayTexture.NO_OVERLAY, LASER_COLOR.x(), LASER_COLOR.y(), LASER_COLOR.z(), 0.4F);
        poseStack.popPose();

        // 主模型渲染
        VertexConsumer vertexConsumer = bufferSource.getBuffer(this.model.renderType(TEXTURE));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, LASER_COLOR.x(), LASER_COLOR.y(), LASER_COLOR.z(), 1.0F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }
    
    private void applyTransformations(GaussLaserProjectile entity, float partialTicks, PoseStack poseStack) {
        // 应用旋转
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
        
        // 添加旋转动画
        float rotation = (entity.tickCount + partialTicks) * 15.0f; // 中等旋转速度
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        
        // 添加脉动缩放效果
        float pulse = Mth.sin((entity.tickCount + partialTicks) * 0.3f) * 0.15f + 0.85f;
        poseStack.scale(pulse, pulse, pulse);
    }
    
    private void renderEnergyHalo(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int tickCount) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 能量光晕效果
        float haloRadius = 1.2f + Mth.sin(tickCount * 0.2f) * 0.2f;
        int haloSegments = 32;
        float alpha = 0.5f + Mth.sin(tickCount * 0.25f) * 0.3f;
        
        for (int i = 0; i < haloSegments; i++) {
            float angle1 = (float) (2 * Math.PI * i / haloSegments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / haloSegments);
            
            float x1 = haloRadius * Mth.cos(angle1);
            float z1 = haloRadius * Mth.sin(angle1);
            float x2 = haloRadius * Mth.cos(angle2);
            float z2 = haloRadius * Mth.sin(angle2);
            
            vertexConsumer.vertex(matrix, x1, 0, z1)
                .color(LASER_COLOR.x(), LASER_COLOR.y(), LASER_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, x2, 0, z2)
                .color(LASER_COLOR.x(), LASER_COLOR.y(), LASER_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, 0, 0, 0)
                .color(LASER_COLOR.x(), LASER_COLOR.y(), LASER_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }
    
    private void renderLaserTrail(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, GaussLaserProjectile entity) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 激光轨迹效果
        Vec3 motion = entity.getDeltaMovement();
        float trailLength = Math.min(entity.tickCount * 0.2f, 5.0f);
        
        if (trailLength > 0.1f) {
            // 轨迹起始点
            float startX = (float) (-motion.x * trailLength);
            float startY = (float) (-motion.y * trailLength);
            float startZ = (float) (-motion.z * trailLength);
            
            // 轨迹宽度和透明度渐变
            float trailWidth = 0.2f;
            
            // 渲染轨迹线
            vertexConsumer.vertex(matrix, startX - trailWidth, startY, startZ)
                .color(LASER_COLOR.x(), LASER_COLOR.y(), LASER_COLOR.z(), 0.4f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, startX + trailWidth, startY, startZ)
                .color(LASER_COLOR.x(), LASER_COLOR.y(), LASER_COLOR.z(), 0.4f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, 0, 0, 0)
                .color(LASER_COLOR.x(), LASER_COLOR.y(), LASER_COLOR.z(), 0.4f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }

    @Override
    public ResourceLocation getTextureLocation(GaussLaserProjectile entity) {
        return TEXTURE;
    }
    
    // ========== 着色器渲染方法 ==========
    
    /**
     * 使用着色器渲染能量光晕
     */
    private void renderEnergyHaloWithShader(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int tickCount, net.minecraft.world.phys.Vec3 position) {
        // 使用着色器渲染能量光晕
        VertexConsumer haloConsumer = bufferSource.getBuffer(ProjectileShaderManager.createGaussLaserRenderType());
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 能量光晕效果
        float haloRadius = 1.2f + Mth.sin(tickCount * 0.2f) * 0.2f;
        int haloSegments = 32;
        float alpha = 0.5f + Mth.sin(tickCount * 0.25f) * 0.3f;
        
        for (int i = 0; i < haloSegments; i++) {
            float angle1 = (float) (2 * Math.PI * i / haloSegments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / haloSegments);
            
            float x1 = haloRadius * Mth.cos(angle1);
            float z1 = haloRadius * Mth.sin(angle1);
            float x2 = haloRadius * Mth.cos(angle2);
            float z2 = haloRadius * Mth.sin(angle2);
            
            haloConsumer.vertex(matrix, x1, 0, z1)
                .color(LASER_COLOR.x(), LASER_COLOR.y(), LASER_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            haloConsumer.vertex(matrix, x2, 0, z2)
                .color(LASER_COLOR.x(), LASER_COLOR.y(), LASER_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            haloConsumer.vertex(matrix, 0, 0, 0)
                .color(LASER_COLOR.x(), LASER_COLOR.y(), LASER_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }
    
    /**
     * 使用着色器渲染激光轨迹
     */
    private void renderLaserTrailWithShader(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, GaussLaserProjectile entity) {
        // 使用着色器渲染激光轨迹
        VertexConsumer trailConsumer = bufferSource.getBuffer(ProjectileShaderManager.createGaussLaserRenderType());
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 激光轨迹效果
        Vec3 motion = entity.getDeltaMovement();
        float trailLength = Math.min(entity.tickCount * 0.2f, 5.0f);
        
        if (trailLength > 0.1f) {
            // 轨迹起始点
            float startX = (float) (-motion.x * trailLength);
            float startY = (float) (-motion.y * trailLength);
            float startZ = (float) (-motion.z * trailLength);
            
            // 轨迹宽度和透明度渐变
            float trailWidth = 0.2f;
            
            // 渲染轨迹线
            trailConsumer.vertex(matrix, startX - trailWidth, startY, startZ)
                .color(LASER_COLOR.x(), LASER_COLOR.y(), LASER_COLOR.z(), 0.4f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            trailConsumer.vertex(matrix, startX + trailWidth, startY, startZ)
                .color(LASER_COLOR.x(), LASER_COLOR.y(), LASER_COLOR.z(), 0.4f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            trailConsumer.vertex(matrix, 0, 0, 0)
                .color(LASER_COLOR.x(), LASER_COLOR.y(), LASER_COLOR.z(), 0.4f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }
}