package cn.dawnstring.fatality.client.renderer;

import cn.dawnstring.fatality.entity.projectile.FireballProjectile;
import cn.dawnstring.fatality.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class FireballProjectileRenderer extends EntityRenderer<FireballProjectile> {

    private final ItemStack fireballItem;
    private static final Vector3f FIREBALL_COLOR = new Vector3f(1.0f, 0.4f, 0.1f); // 火焰颜色

    public FireballProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        // 使用火球法书作为投射物的渲染模型
        this.fireballItem = new ItemStack(ModItems.FIREBALL_SPELLBOOK.get());
    }

    @Override
    public void render(FireballProjectile entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
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

        // 渲染多层火焰光晕
        renderMultiLayerFireHalo(poseStack, bufferSource, packedLight, entity.tickCount);

        // 渲染火焰轨迹效果
        renderFireTrail(poseStack, bufferSource, packedLight, entity);

        // 渲染火焰核心效果
        renderFireCore(poseStack, bufferSource, packedLight, entity.tickCount);

        // 设置物品大小 - 火球调整为1.3倍大小
        poseStack.scale(1.3F, 1.3F, 1.3F);

        // 渲染物品模型
        Minecraft.getInstance().getItemRenderer().renderStatic(
                fireballItem,
                ItemDisplayContext.GROUND,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                entity.level(),
                entity.getId()
        );

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(FireballProjectile entity) {
        // 返回一个空的纹理位置，因为我们使用物品渲染器
        return ResourceLocation.fromNamespaceAndPath("fatality", "textures/empty.png");
    }
    
    private void applyTransformations(PoseStack poseStack, FireballProjectile entity, float partialTicks) {
        // 添加旋转动画效果
        float rotation = (entity.tickCount + partialTicks) * 15.0F; // 火球旋转速度
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        
        // 添加脉动缩放效果
        float pulseScale = 1.0f + Mth.sin(entity.tickCount * 0.3f) * 0.2f;
        poseStack.scale(pulseScale, pulseScale, pulseScale);
    }
    
    private void renderMultiLayerFireHalo(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int tickCount) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath("fatality", "textures/entity/fireball.png")));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 多层火焰光晕效果
        float haloRadius1 = 0.8f + Mth.sin(tickCount * 0.2f) * 0.15f;
        float haloRadius2 = 0.6f + Mth.sin(tickCount * 0.25f) * 0.12f;
        float haloRadius3 = 0.4f + Mth.sin(tickCount * 0.3f) * 0.1f;
        
        int haloSegments = 28;
        float alpha1 = 0.4f + Mth.sin(tickCount * 0.3f) * 0.3f;
        float alpha2 = 0.5f + Mth.sin(tickCount * 0.35f) * 0.4f;
        float alpha3 = 0.6f + Mth.sin(tickCount * 0.4f) * 0.5f;
        
        // 外层火焰光环
        renderHaloRing(matrix, vertexConsumer, packedLight, pose, haloRadius1, haloSegments, alpha1);
        
        // 中层火焰光环
        renderHaloRing(matrix, vertexConsumer, packedLight, pose, haloRadius2, haloSegments, alpha2);
        
        // 内层火焰光环
        renderHaloRing(matrix, vertexConsumer, packedLight, pose, haloRadius3, haloSegments, alpha3);
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
                .color(FIREBALL_COLOR.x(), FIREBALL_COLOR.y(), FIREBALL_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, x2, 0, z2)
                .color(FIREBALL_COLOR.x(), FIREBALL_COLOR.y(), FIREBALL_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, 0, 0, 0)
                .color(FIREBALL_COLOR.x(), FIREBALL_COLOR.y(), FIREBALL_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }
    
    private void renderFireTrail(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, FireballProjectile entity) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath("fatality", "textures/entity/fireball.png")));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 火焰轨迹效果
        float trailLength = Math.min(entity.tickCount * 0.15f, 3.0f);
        
        if (trailLength > 0.1f) {
            // 轨迹起始点（基于火球的移动方向）
            float startX = -trailLength;
            float startZ = 0;
            
            // 轨迹宽度和透明度渐变
            float trailWidth = 0.25f;
            
            // 渲染轨迹线
            vertexConsumer.vertex(matrix, startX - trailWidth, 0, startZ)
                .color(FIREBALL_COLOR.x(), FIREBALL_COLOR.y(), FIREBALL_COLOR.z(), 0.5f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, startX + trailWidth, 0, startZ)
                .color(FIREBALL_COLOR.x(), FIREBALL_COLOR.y(), FIREBALL_COLOR.z(), 0.5f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, 0, 0, 0)
                .color(FIREBALL_COLOR.x(), FIREBALL_COLOR.y(), FIREBALL_COLOR.z(), 0.5f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }
    
    private void renderFireCore(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int tickCount) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath("fatality", "textures/entity/fireball.png")));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 火焰核心效果 - 内层光晕
        float coreRadius = 0.3f + Mth.sin(tickCount * 0.4f) * 0.08f;
        int coreSegments = 20;
        float coreAlpha = 0.8f + Mth.sin(tickCount * 0.5f) * 0.2f;
        
        for (int i = 0; i < coreSegments; i++) {
            float angle1 = (float) (2 * Math.PI * i / coreSegments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / coreSegments);
            
            float x1 = coreRadius * Mth.cos(angle1);
            float z1 = coreRadius * Mth.sin(angle1);
            float x2 = coreRadius * Mth.cos(angle2);
            float z2 = coreRadius * Mth.sin(angle2);
            
            vertexConsumer.vertex(matrix, x1, 0, z1)
                .color(1.0f, 0.6f, 0.1f, coreAlpha) // 更亮的火焰核心
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, x2, 0, z2)
                .color(1.0f, 0.6f, 0.1f, coreAlpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, 0, 0, 0)
                .color(1.0f, 0.6f, 0.1f, coreAlpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }
}