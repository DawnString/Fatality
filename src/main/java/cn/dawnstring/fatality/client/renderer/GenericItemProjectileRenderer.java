package cn.dawnstring.fatality.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GenericItemProjectileRenderer<T extends Entity> extends EntityRenderer<T> {

    private final ItemStack itemStack;
    private final float scale;

    public GenericItemProjectileRenderer(EntityRendererProvider.Context context, ItemStack itemStack, float scale) {
        super(context);
        this.itemStack = itemStack;
        this.scale = scale;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, com.mojang.blaze3d.vertex.PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        // 调整位置到投掷物中心
        poseStack.translate(0, 0.5, 0);

        // 设置旋转
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(entity.getYRot() - 90.0F));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(entity.getXRot()));

        // 设置物品大小
        poseStack.scale(scale, scale, scale);

        // 添加旋转动画效果
        float rotation = (entity.tickCount + partialTicks) * 10.0F;
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));

        // 渲染物品模型
        Minecraft.getInstance().getItemRenderer().renderStatic(
                itemStack,
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
    public ResourceLocation getTextureLocation(T entity) {
        // 返回一个空的纹理位置，因为我们使用物品渲染器
        return ResourceLocation.fromNamespaceAndPath("fatality", "textures/empty.png");
    }
}