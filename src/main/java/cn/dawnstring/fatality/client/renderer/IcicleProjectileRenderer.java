package cn.dawnstring.fatality.client.renderer;

import cn.dawnstring.fatality.entity.projectile.IcicleProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class IcicleProjectileRenderer extends EntityRenderer<IcicleProjectile> {

    private static final Vector3f ICICLE_COLOR = new Vector3f(0.4f, 0.8f, 1.0f); // 冰蓝色
    private final BlockState iceBlockState;

    public IcicleProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.iceBlockState = Blocks.ICE.defaultBlockState();
    }

    @Override
    public void render(IcicleProjectile entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
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

        // 渲染多层冰晶光晕
        renderMultiLayerIceHalo(poseStack, bufferSource, packedLight, entity.tickCount);

        // 渲染冰晶轨迹效果
        renderIceTrail(poseStack, bufferSource, packedLight, entity);

        // 渲染冰晶核心效果
        renderIceCore(poseStack, bufferSource, packedLight, entity.tickCount);

        // 设置冰锥大小 - 调整为1.4倍大小
        poseStack.scale(1.4F, 1.4F, 1.4F);

        // 渲染冰方块模型
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        blockRenderer.renderSingleBlock(
                iceBlockState,
                poseStack,
                bufferSource,
                packedLight,
                OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(IcicleProjectile entity) {
        // 返回一个空的纹理位置，因为我们使用方块渲染器
        return ResourceLocation.fromNamespaceAndPath("fatality", "textures/empty.png");
    }
    
    private void applyTransformations(PoseStack poseStack, IcicleProjectile entity, float partialTicks) {
        // 添加旋转动画效果
        float rotation = (entity.tickCount + partialTicks) * 10.0F; // 冰锥旋转速度
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        
        // 添加脉动缩放效果
        float pulseScale = 1.0f + Mth.sin(entity.tickCount * 0.2f) * 0.15f;
        poseStack.scale(pulseScale, pulseScale, pulseScale);
    }
    
    private void renderMultiLayerIceHalo(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int tickCount) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath("fatality", "textures/entity/icicle.png")));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 多层冰晶光晕效果
        float haloRadius1 = 0.7f + Mth.sin(tickCount * 0.15f) * 0.12f;
        float haloRadius2 = 0.5f + Mth.sin(tickCount * 0.2f) * 0.1f;
        float haloRadius3 = 0.3f + Mth.sin(tickCount * 0.25f) * 0.08f;
        
        int haloSegments = 24;
        float alpha1 = 0.3f + Mth.sin(tickCount * 0.25f) * 0.25f;
        float alpha2 = 0.4f + Mth.sin(tickCount * 0.3f) * 0.35f;
        float alpha3 = 0.5f + Mth.sin(tickCount * 0.35f) * 0.45f;
        
        // 外层冰晶光环
        renderIceHaloRing(matrix, vertexConsumer, packedLight, pose, haloRadius1, haloSegments, alpha1);
        
        // 中层冰晶光环
        renderIceHaloRing(matrix, vertexConsumer, packedLight, pose, haloRadius2, haloSegments, alpha2);
        
        // 内层冰晶光环
        renderIceHaloRing(matrix, vertexConsumer, packedLight, pose, haloRadius3, haloSegments, alpha3);
    }
    
    private void renderIceHaloRing(Matrix4f matrix, VertexConsumer vertexConsumer, int packedLight, PoseStack.Pose pose, float radius, int segments, float alpha) {
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (2 * Math.PI * i / segments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / segments);
            
            float x1 = radius * Mth.cos(angle1);
            float z1 = radius * Mth.sin(angle1);
            float x2 = radius * Mth.cos(angle2);
            float z2 = radius * Mth.sin(angle2);
            
            vertexConsumer.vertex(matrix, x1, 0, z1)
                .color(ICICLE_COLOR.x(), ICICLE_COLOR.y(), ICICLE_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, x2, 0, z2)
                .color(ICICLE_COLOR.x(), ICICLE_COLOR.y(), ICICLE_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, 0, 0, 0)
                .color(ICICLE_COLOR.x(), ICICLE_COLOR.y(), ICICLE_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }
    
    private void renderIceTrail(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, IcicleProjectile entity) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath("fatality", "textures/entity/icicle.png")));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 冰晶轨迹效果
        float trailLength = Math.min(entity.tickCount * 0.1f, 2.5f);
        
        if (trailLength > 0.1f) {
            // 轨迹起始点（基于冰锥的移动方向）
            float startX = -trailLength;
            float startZ = 0;
            
            // 轨迹宽度和透明度渐变
            float trailWidth = 0.2f;
            
            // 渲染轨迹线
            vertexConsumer.vertex(matrix, startX - trailWidth, 0, startZ)
                .color(ICICLE_COLOR.x(), ICICLE_COLOR.y(), ICICLE_COLOR.z(), 0.4f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, startX + trailWidth, 0, startZ)
                .color(ICICLE_COLOR.x(), ICICLE_COLOR.y(), ICICLE_COLOR.z(), 0.4f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, 0, 0, 0)
                .color(ICICLE_COLOR.x(), ICICLE_COLOR.y(), ICICLE_COLOR.z(), 0.4f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }
    
    private void renderIceCore(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int tickCount) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath("fatality", "textures/entity/icicle.png")));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 冰晶核心效果 - 内层光晕
        float coreRadius = 0.2f + Mth.sin(tickCount * 0.3f) * 0.06f;
        int coreSegments = 16;
        float coreAlpha = 0.7f + Mth.sin(tickCount * 0.4f) * 0.3f;
        
        for (int i = 0; i < coreSegments; i++) {
            float angle1 = (float) (2 * Math.PI * i / coreSegments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / coreSegments);
            
            float x1 = coreRadius * Mth.cos(angle1);
            float z1 = coreRadius * Mth.sin(angle1);
            float x2 = coreRadius * Mth.cos(angle2);
            float z2 = coreRadius * Mth.sin(angle2);
            
            vertexConsumer.vertex(matrix, x1, 0, z1)
                .color(0.6f, 0.9f, 1.0f, coreAlpha) // 更亮的冰晶核心
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, x2, 0, z2)
                .color(0.6f, 0.9f, 1.0f, coreAlpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, 0, 0, 0)
                .color(0.6f, 0.9f, 1.0f, coreAlpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }
}