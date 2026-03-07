package cn.dawnstring.fatality.client.renderer;

import cn.dawnstring.fatality.Fatality;
import cn.dawnstring.fatality.client.model.ElementalMissileModel;
import cn.dawnstring.fatality.client.model.ModModelLayers;
import cn.dawnstring.fatality.entity.projectile.ElementalMissileProjectile;
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

public class ElementalMissileProjectileRenderer extends EntityRenderer<ElementalMissileProjectile> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Fatality.MODID, "textures/entity/elemental_missile.png");
    private final ElementalMissileModel model;
    
    // 元素飞弹颜色 - 炽热的橙色能量
    private static final Vector3f ENERGY_COLOR = new Vector3f(1.0f, 0.5f, 0.0f);

    public ElementalMissileProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ElementalMissileModel(context.bakeLayer(ModModelLayers.ELEMENTAL_MISSILE_LAYER));
    }

    @Override
    public void render(ElementalMissileProjectile entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        // 应用旋转和缩放
        applyTransformations(entity, partialTicks, poseStack);
        
        // 渲染动态能量光晕
        renderDynamicEnergyHalo(poseStack, bufferSource, packedLight, entity.tickCount);
        
        // 渲染能量轨迹
        renderEnergyTrail(poseStack, bufferSource, packedLight, entity);
        
        // 渲染核心能量效果
        renderCoreEnergy(poseStack, bufferSource, packedLight, entity.tickCount);

        // 调整大小 - 元素飞弹稍大
        poseStack.scale(1.2f, 1.2f, 1.2f);

        // 添加外层能量光晕
        poseStack.pushPose();
        poseStack.scale(1.15f, 1.15f, 1.15f);
        VertexConsumer outerGlowConsumer = bufferSource.getBuffer(this.model.renderType(TEXTURE));
        this.model.renderToBuffer(poseStack, outerGlowConsumer, packedLight, OverlayTexture.NO_OVERLAY, ENERGY_COLOR.x(), ENERGY_COLOR.y(), ENERGY_COLOR.z(), 0.3F);
        poseStack.popPose();

        // 主模型渲染
        VertexConsumer vertexConsumer = bufferSource.getBuffer(this.model.renderType(TEXTURE));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, ENERGY_COLOR.x(), ENERGY_COLOR.y(), ENERGY_COLOR.z(), 0.8F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }
    
    private void applyTransformations(ElementalMissileProjectile entity, float partialTicks, PoseStack poseStack) {
        // 应用旋转
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
        
        // 添加旋转动画
        float rotation = (entity.tickCount + partialTicks) * 15.0f; // 中等速度旋转
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        
        // 添加脉动缩放效果
        float pulse = Mth.sin((entity.tickCount + partialTicks) * 0.25f) * 0.15f + 0.85f;
        poseStack.scale(pulse, pulse, pulse);
    }
    
    private void renderDynamicEnergyHalo(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int tickCount) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 动态能量光晕效果 - 多层光环
        float haloRadius1 = 0.7f + Mth.sin(tickCount * 0.1f) * 0.1f;
        float haloRadius2 = 0.5f + Mth.sin(tickCount * 0.15f) * 0.08f;
        
        int haloSegments = 24;
        float alpha1 = 0.4f + Mth.sin(tickCount * 0.2f) * 0.2f;
        float alpha2 = 0.6f + Mth.sin(tickCount * 0.25f) * 0.3f;
        
        // 外层光环
        renderHaloRing(matrix, vertexConsumer, packedLight, pose, haloRadius1, haloSegments, alpha1);
        
        // 内层光环
        renderHaloRing(matrix, vertexConsumer, packedLight, pose, haloRadius2, haloSegments, alpha2);
    }
    
    private void renderHaloRing(Matrix4f matrix, VertexConsumer vertexConsumer, int packedLight, PoseStack.Pose pose, float radius, int segments, float alpha) {
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (2 * Math.PI * i / segments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / segments);
            
            float x1 = radius * Mth.cos(angle1);
            float z1 = radius * Mth.sin(angle1);
            float x2 = radius * Mth.cos(angle2);
            float z2 = radius * Mth.sin(angle2);
            
            vertexConsumer.vertex(matrix, x1, 0, z1)
                .color(ENERGY_COLOR.x(), ENERGY_COLOR.y(), ENERGY_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, x2, 0, z2)
                .color(ENERGY_COLOR.x(), ENERGY_COLOR.y(), ENERGY_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, 0, 0, 0)
                .color(ENERGY_COLOR.x(), ENERGY_COLOR.y(), ENERGY_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }
    
    private void renderEnergyTrail(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, ElementalMissileProjectile entity) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 能量轨迹效果
        Vec3 motion = entity.getDeltaMovement();
        float trailLength = Math.min(entity.tickCount * 0.12f, 3.5f);
        
        if (trailLength > 0.1f) {
            // 轨迹起始点
            float startX = (float) (-motion.x * trailLength);
            float startY = (float) (-motion.y * trailLength);
            float startZ = (float) (-motion.z * trailLength);
            
            // 轨迹宽度和透明度渐变
            float trailWidth = 0.15f;
            
            // 渲染轨迹线
            vertexConsumer.vertex(matrix, startX - trailWidth, startY, startZ)
                .color(ENERGY_COLOR.x(), ENERGY_COLOR.y(), ENERGY_COLOR.z(), 0.3f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, startX + trailWidth, startY, startZ)
                .color(ENERGY_COLOR.x(), ENERGY_COLOR.y(), ENERGY_COLOR.z(), 0.3f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, 0, 0, 0)
                .color(ENERGY_COLOR.x(), ENERGY_COLOR.y(), ENERGY_COLOR.z(), 0.3f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }
    
    private void renderCoreEnergy(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int tickCount) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 核心能量效果 - 内层光晕
        float coreRadius = 0.3f + Mth.sin(tickCount * 0.3f) * 0.05f;
        int coreSegments = 16;
        float coreAlpha = 0.8f + Mth.sin(tickCount * 0.4f) * 0.1f;
        
        for (int i = 0; i < coreSegments; i++) {
            float angle1 = (float) (2 * Math.PI * i / coreSegments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / coreSegments);
            
            float x1 = coreRadius * Mth.cos(angle1);
            float z1 = coreRadius * Mth.sin(angle1);
            float x2 = coreRadius * Mth.cos(angle2);
            float z2 = coreRadius * Mth.sin(angle2);
            
            vertexConsumer.vertex(matrix, x1, 0, z1)
                .color(1.0f, 0.7f, 0.2f, coreAlpha) // 更亮的黄色核心
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, x2, 0, z2)
                .color(1.0f, 0.7f, 0.2f, coreAlpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, 0, 0, 0)
                .color(1.0f, 0.7f, 0.2f, coreAlpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }

    @Override
    public ResourceLocation getTextureLocation(ElementalMissileProjectile entity) {
        return TEXTURE;
    }
}