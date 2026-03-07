package cn.dawnstring.fatality.client.renderer;

import cn.dawnstring.fatality.Fatality;
import cn.dawnstring.fatality.client.model.ElementalSpearModel;
import cn.dawnstring.fatality.client.model.ModModelLayers;
import cn.dawnstring.fatality.client.shader.ProjectileShaderManager;
import cn.dawnstring.fatality.entity.projectile.ElementalSpearProjectile;
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

public class ElementalSpearProjectileRenderer extends EntityRenderer<ElementalSpearProjectile> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Fatality.MODID, "textures/entity/elemental_spear.png");
    private final ElementalSpearModel model;
    private static final Vector3f SPEAR_COLOR = new Vector3f(0.2f, 0.8f, 0.3f); // 绿色元素长枪颜色

    public ElementalSpearProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ElementalSpearModel(context.bakeLayer(ModModelLayers.ELEMENTAL_SPEAR_LAYER));
    }

    @Override
    public void render(ElementalSpearProjectile entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        // 应用旋转和缩放
        applyTransformations(entity, partialTicks, poseStack);
        
        // 使用着色器渲染元素长枪能量光环
        renderElementalHaloWithShader(poseStack, bufferSource, packedLight, entity.tickCount, entity.position());
        
        // 渲染元素长枪轨迹
        renderSpearTrail(poseStack, bufferSource, packedLight, entity);
        
        // 渲染元素长枪尖端能量
        renderSpearTipEnergy(poseStack, bufferSource, packedLight, entity.tickCount);

        // 调整大小 - 元素长枪更大更显眼
        poseStack.scale(1.5f, 1.5f, 1.5f);

        // 使用着色器渲染外层元素光晕
        poseStack.pushPose();
        poseStack.scale(1.3f, 1.3f, 1.3f);
        VertexConsumer outerGlowConsumer = bufferSource.getBuffer(ProjectileShaderManager.createElementalSpearRenderType());
        this.model.renderToBuffer(poseStack, outerGlowConsumer, packedLight, OverlayTexture.NO_OVERLAY, SPEAR_COLOR.x(), SPEAR_COLOR.y(), SPEAR_COLOR.z(), 0.3F);
        poseStack.popPose();

        // 使用着色器渲染中层元素光晕
        poseStack.pushPose();
        poseStack.scale(1.15f, 1.15f, 1.15f);
        VertexConsumer middleGlowConsumer = bufferSource.getBuffer(ProjectileShaderManager.createElementalSpearRenderType());
        this.model.renderToBuffer(poseStack, middleGlowConsumer, packedLight, OverlayTexture.NO_OVERLAY, SPEAR_COLOR.x(), SPEAR_COLOR.y(), SPEAR_COLOR.z(), 0.5F);
        poseStack.popPose();

        // 主模型渲染 - 使用着色器
        VertexConsumer vertexConsumer = bufferSource.getBuffer(ProjectileShaderManager.createElementalSpearRenderType());
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, SPEAR_COLOR.x(), SPEAR_COLOR.y(), SPEAR_COLOR.z(), 0.9F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }
    
    private void applyTransformations(ElementalSpearProjectile entity, float partialTicks, PoseStack poseStack) {
        // 应用旋转
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
        
        // 添加旋转动画 - 长枪快速旋转
        float rotation = (entity.tickCount + partialTicks) * 20.0f; // 快速旋转
        poseStack.mulPose(Axis.XP.rotationDegrees(rotation));
        
        // 添加脉动缩放效果
        float pulse = Mth.sin((entity.tickCount + partialTicks) * 0.3f) * 0.1f + 0.9f;
        poseStack.scale(pulse, pulse, pulse);
    }
    
    private void renderElementalHaloWithShader(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int tickCount, Vec3 entityPos) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(ProjectileShaderManager.createElementalSpearRenderType());
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 元素能量光环效果 - 使用着色器增强
        float haloRadius = 0.8f + Mth.sin(tickCount * 0.2f) * 0.15f;
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
                .color(SPEAR_COLOR.x(), SPEAR_COLOR.y(), SPEAR_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, x2, 0, z2)
                .color(SPEAR_COLOR.x(), SPEAR_COLOR.y(), SPEAR_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, 0, 0, 0)
                .color(SPEAR_COLOR.x(), SPEAR_COLOR.y(), SPEAR_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }
    
    private void renderSpearTrail(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, ElementalSpearProjectile entity) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 元素长枪轨迹效果
        Vec3 motion = entity.getDeltaMovement();
        float trailLength = Math.min(entity.tickCount * 0.2f, 4.0f);
        
        if (trailLength > 0.1f) {
            // 轨迹起始点
            float startX = (float) (-motion.x * trailLength);
            float startY = (float) (-motion.y * trailLength);
            float startZ = (float) (-motion.z * trailLength);
            
            // 轨迹宽度和透明度渐变
            float trailWidth = 0.15f;
            
            // 渲染轨迹线
            vertexConsumer.vertex(matrix, startX - trailWidth, startY, startZ)
                .color(SPEAR_COLOR.x(), SPEAR_COLOR.y(), SPEAR_COLOR.z(), 0.4f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, startX + trailWidth, startY, startZ)
                .color(SPEAR_COLOR.x(), SPEAR_COLOR.y(), SPEAR_COLOR.z(), 0.4f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, 0, 0, 0)
                .color(SPEAR_COLOR.x(), SPEAR_COLOR.y(), SPEAR_COLOR.z(), 0.4f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }
    
    private void renderSpearTipEnergy(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int tickCount) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 元素长枪尖端能量效果 - 向前延伸的锥形能量
        float tipLength = 1.2f + Mth.sin(tickCount * 0.4f) * 0.3f;
        float tipWidth = 0.3f + Mth.sin(tickCount * 0.35f) * 0.1f;
        float tipAlpha = 0.6f + Mth.sin(tickCount * 0.5f) * 0.3f;
        
        // 渲染尖端能量锥形
        vertexConsumer.vertex(matrix, -tipWidth, 0, -tipLength)
            .color(SPEAR_COLOR.x(), SPEAR_COLOR.y(), SPEAR_COLOR.z(), tipAlpha)
            .uv(0, 0)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(packedLight)
            .normal(pose.normal(), 0, 1, 0)
            .endVertex();
            
        vertexConsumer.vertex(matrix, tipWidth, 0, -tipLength)
            .color(SPEAR_COLOR.x(), SPEAR_COLOR.y(), SPEAR_COLOR.z(), tipAlpha)
            .uv(0, 0)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(packedLight)
            .normal(pose.normal(), 0, 1, 0)
            .endVertex();
            
        vertexConsumer.vertex(matrix, 0, 0, 0)
            .color(SPEAR_COLOR.x(), SPEAR_COLOR.y(), SPEAR_COLOR.z(), tipAlpha)
            .uv(0, 0)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(packedLight)
            .normal(pose.normal(), 0, 1, 0)
            .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(ElementalSpearProjectile entity) {
        return TEXTURE;
    }
}