package cn.dawnstring.fatality.client.renderer;

import cn.dawnstring.fatality.entity.projectile.DartProjectile;
import cn.dawnstring.fatality.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DartProjectileRenderer extends EntityRenderer<DartProjectile> {

    private final ItemStack dartItem;
    private final ItemRenderer itemRenderer;

    public DartProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        // 使用飞镖物品作为投射物的渲染模型
        this.dartItem = new ItemStack(ModItems.DART.get());
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(DartProjectile entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        // 调整位置到投掷物中心
        poseStack.translate(0, 0.3, 0); // 比火球低一些

        // 获取插值后的旋转角度
        float yRot = entity.getYRot();
        float xRot = entity.getXRot();

        // 设置旋转：Y轴旋转让模型朝向水平方向
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot - 90.0F));

        // X轴旋转让模型在竖直方向朝向玩家视角
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));

        // 设置物品大小 - 飞镖应该很小
        poseStack.scale(0.4F, 0.4F, 0.4F);

        // 添加旋转动画效果，让飞镖看起来在旋转
        float rotation = (entity.tickCount + partialTicks) * 15.0F; // 更快旋转
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        // 渲染物品模型
        itemRenderer.renderStatic(dartItem, ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, entity.level(), 0);

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(DartProjectile entity) {
        // 返回一个空的纹理位置，因为我们使用物品渲染器
        return ResourceLocation.fromNamespaceAndPath("fatality", "textures/empty.png");
    }
}