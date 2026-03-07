package cn.dawnstring.fatality.client.renderer;

import cn.dawnstring.fatality.Fatality;
import cn.dawnstring.fatality.client.model.EnhancedElementalMissileModel;
import cn.dawnstring.fatality.client.model.ModModelLayers;
import cn.dawnstring.fatality.entity.projectile.EnhancedElementalMissileProjectile;
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

public class EnhancedElementalMissileProjectileRenderer extends EntityRenderer<EnhancedElementalMissileProjectile> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Fatality.MODID, "textures/entity/enhanced_elemental_missile.png");
    private final EnhancedElementalMissileModel model;
    
    // 颜色数组 - 对应不同的导弹ID
    private static final Vector3f[] MISSILE_COLORS = {
        new Vector3f(1.0f, 0.0f, 0.0f), // 红色
        new Vector3f(0.0f, 1.0f, 0.0f), // 绿色
        new Vector3f(0.0f, 0.0f, 1.0f), // 蓝色
        new Vector3f(1.0f, 1.0f, 0.0f), // 黄色
        new Vector3f(1.0f, 0.0f, 1.0f)  // 紫色
    };

    public EnhancedElementalMissileProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new EnhancedElementalMissileModel(context.bakeLayer(ModModelLayers.ENHANCED_ELEMENTAL_MISSILE_LAYER));
    }

    @Override
    public void render(EnhancedElementalMissileProjectile entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        // 获取导弹ID和颜色
        int missileId = entity.getEntityData().get(EnhancedElementalMissileProjectile.DATA_MISSILE_ID);
        Vector3f color = MISSILE_COLORS[missileId % MISSILE_COLORS.length];
        
        // 应用旋转和位置
        applyTransformations(entity, partialTicks, poseStack);
        
        // 渲染动态光晕
        renderDynamicHalo(poseStack, bufferSource, color, packedLight, entity.tickCount);
        
        // 渲染轨迹效果
        renderTrailEffect(poseStack, bufferSource, color, packedLight, entity);
        
        // 渲染螺旋轨迹（如果启用螺旋运动）
        if (entity.getEntityData().get(EnhancedElementalMissileProjectile.DATA_IS_SPIRAL)) {
            renderSpiralTrail(poseStack, bufferSource, color, packedLight, entity);
        }

        // 调整大小 - 增强版导弹稍大
        poseStack.scale(1.2f, 1.2f, 1.2f);

        // 添加轻微的光晕效果
        poseStack.pushPose();
        poseStack.scale(1.1f, 1.1f, 1.1f);
        VertexConsumer glowConsumer = bufferSource.getBuffer(this.model.renderType(TEXTURE));
        this.model.renderToBuffer(poseStack, glowConsumer, packedLight, OverlayTexture.NO_OVERLAY, color.x(), color.y(), color.z(), 0.3F);
        poseStack.popPose();

        // 主模型渲染
        VertexConsumer vertexConsumer = bufferSource.getBuffer(this.model.renderType(TEXTURE));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, color.x(), color.y(), color.z(), 1.0F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    private void applyTransformations(EnhancedElementalMissileProjectile entity, float partialTicks, PoseStack poseStack) {
        // 应用旋转
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
        
        // 添加旋转动画
        float rotation = (entity.tickCount + partialTicks) * 20.0f; // 快速旋转
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        
        // 添加脉动缩放效果
        float pulse = Mth.sin((entity.tickCount + partialTicks) * 0.2f) * 0.1f + 0.9f;
        poseStack.scale(pulse, pulse, pulse);
    }

    private void renderDynamicHalo(PoseStack poseStack, MultiBufferSource bufferSource, Vector3f color, int packedLight, int tickCount) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 动态光晕效果
        float haloRadius = 0.8f + Mth.sin(tickCount * 0.1f) * 0.1f;
        int haloSegments = 24;
        float alpha = 0.4f + Mth.sin(tickCount * 0.15f) * 0.2f;
        
        for (int i = 0; i < haloSegments; i++) {
            float angle1 = (float) (2 * Math.PI * i / haloSegments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / haloSegments);
            
            float x1 = haloRadius * Mth.cos(angle1);
            float z1 = haloRadius * Mth.sin(angle1);
            float x2 = haloRadius * Mth.cos(angle2);
            float z2 = haloRadius * Mth.sin(angle2);
            
            vertexConsumer.vertex(matrix, x1, 0, z1)
                .color(color.x(), color.y(), color.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, x2, 0, z2)
                .color(color.x(), color.y(), color.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, 0, 0, 0)
                .color(color.x(), color.y(), color.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }

    private void renderTrailEffect(PoseStack poseStack, MultiBufferSource bufferSource, Vector3f color, int packedLight, EnhancedElementalMissileProjectile entity) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 轨迹效果 - 模拟粒子轨迹
        Vec3 motion = entity.getDeltaMovement();
        float trailLength = Math.min(entity.tickCount * 0.1f, 3.0f);
        
        if (trailLength > 0.1f) {
            // 轨迹起始点（稍微偏移）
            float startX = (float) (-motion.x * trailLength);
            float startY = (float) (-motion.y * trailLength);
            float startZ = (float) (-motion.z * trailLength);
            
            // 轨迹宽度
            float trailWidth = 0.1f;
            
            // 渲染轨迹线
            vertexConsumer.vertex(matrix, startX - trailWidth, startY, startZ)
                .color(color.x(), color.y(), color.z(), 0.3f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, startX + trailWidth, startY, startZ)
                .color(color.x(), color.y(), color.z(), 0.3f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, 0, 0, 0)
                .color(color.x(), color.y(), color.z(), 0.3f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }

    private void renderSpiralTrail(PoseStack poseStack, MultiBufferSource bufferSource, Vector3f color, int packedLight, EnhancedElementalMissileProjectile entity) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 螺旋轨迹效果
        float spiralRadius = 0.8f;
        int spiralPoints = 12;
        float spiralSpeed = 0.3f;
        
        for (int i = 0; i < spiralPoints; i++) {
            float angle = (float) (entity.tickCount * spiralSpeed + i * 2 * Math.PI / spiralPoints);
            float offsetX = spiralRadius * Mth.cos(angle);
            float offsetZ = spiralRadius * Mth.sin(angle);
            float offsetY = i * 0.1f;
            
            // 螺旋点的大小和透明度
            float pointSize = 0.05f;
            float alpha = 0.6f - (i * 0.05f);
            
            // 渲染螺旋点
            vertexConsumer.vertex(matrix, offsetX - pointSize, offsetY, offsetZ - pointSize)
                .color(color.x(), color.y(), color.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, offsetX + pointSize, offsetY, offsetZ - pointSize)
                .color(color.x(), color.y(), color.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, offsetX, offsetY, offsetZ + pointSize)
                .color(color.x(), color.y(), color.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }

    @Override
    public ResourceLocation getTextureLocation(EnhancedElementalMissileProjectile entity) {
        return TEXTURE;
    }
}