package cn.dawnstring.fatality.client.renderer;

import cn.dawnstring.fatality.entity.projectile.DisasterFlyingAxeProjectile;
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
public class DisasterFlyingAxeProjectileRenderer extends EntityRenderer<DisasterFlyingAxeProjectile> {

    private final ItemStack flyingAxeItem;
    
    // 灾难飞斧颜色 - 暗红色能量
    private static final Vector3f AXE_COLOR = new Vector3f(0.8f, 0.1f, 0.1f);

    public DisasterFlyingAxeProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        // 使用飞斧物品作为投射物的渲染模型
        this.flyingAxeItem = new ItemStack(ModItems.DISASTER_FLYING_AXE.get());
    }

    @Override
    public void render(DisasterFlyingAxeProjectile entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
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

        // 渲染能量光晕
        renderAxeHalo(poseStack, bufferSource, packedLight, entity.tickCount);
        
        // 渲染飞斧轨迹
        renderAxeTrail(poseStack, bufferSource, packedLight, entity);

        // 设置物品大小 - 飞斧投射物应该比原武器小一些
        poseStack.scale(0.6F, 0.6F, 0.6F);

        // 添加旋转动画效果，让飞斧看起来在快速旋转（更快的旋转速度）
        float rotation = (entity.tickCount + partialTicks) * 20.0F; // 更快的旋转速度
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        // 渲染物品模型
        Minecraft.getInstance().getItemRenderer().renderStatic(
                flyingAxeItem,
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
    public ResourceLocation getTextureLocation(DisasterFlyingAxeProjectile entity) {
        // 返回一个空的纹理位置，因为我们使用物品渲染器
        return ResourceLocation.fromNamespaceAndPath("fatality", "textures/empty.png");
    }
    
    private void renderAxeHalo(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int tickCount) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath("fatality", "textures/entity/disaster_flying_axe.png")));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 飞斧能量光晕效果
        float haloRadius = 0.8f + Mth.sin(tickCount * 0.2f) * 0.1f;
        int haloSegments = 16;
        float alpha = 0.5f + Mth.sin(tickCount * 0.25f) * 0.3f;
        
        for (int i = 0; i < haloSegments; i++) {
            float angle1 = (float) (2 * Math.PI * i / haloSegments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / haloSegments);
            
            float x1 = haloRadius * Mth.cos(angle1);
            float z1 = haloRadius * Mth.sin(angle1);
            float x2 = haloRadius * Mth.cos(angle2);
            float z2 = haloRadius * Mth.sin(angle2);
            
            vertexConsumer.vertex(matrix, x1, 0, z1)
                .color(AXE_COLOR.x(), AXE_COLOR.y(), AXE_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, x2, 0, z2)
                .color(AXE_COLOR.x(), AXE_COLOR.y(), AXE_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, 0, 0, 0)
                .color(AXE_COLOR.x(), AXE_COLOR.y(), AXE_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }
    
    private void renderAxeTrail(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, DisasterFlyingAxeProjectile entity) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath("fatality", "textures/entity/disaster_flying_axe.png")));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 飞斧轨迹效果
        float trailLength = Math.min(entity.tickCount * 0.1f, 3.0f);
        
        if (trailLength > 0.1f) {
            // 轨迹起始点（基于飞斧的旋转方向）
            float rotationAngle = (entity.tickCount) * 0.3f;
            float startX = -Mth.cos(rotationAngle) * trailLength;
            float startZ = -Mth.sin(rotationAngle) * trailLength;
            
            // 轨迹宽度和透明度渐变
            float trailWidth = 0.2f;
            
            // 渲染轨迹线
            vertexConsumer.vertex(matrix, startX - trailWidth, 0, startZ)
                .color(AXE_COLOR.x(), AXE_COLOR.y(), AXE_COLOR.z(), 0.4f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, startX + trailWidth, 0, startZ)
                .color(AXE_COLOR.x(), AXE_COLOR.y(), AXE_COLOR.z(), 0.4f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, 0, 0, 0)
                .color(AXE_COLOR.x(), AXE_COLOR.y(), AXE_COLOR.z(), 0.4f)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }
}