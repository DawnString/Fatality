package cn.dawnstring.fatality.client.renderer;

import cn.dawnstring.fatality.entity.projectile.FireballProjectile;
import cn.dawnstring.fatality.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FireProjectileRenderer extends EntityRenderer<FireballProjectile> {

    private final ItemStack fireballItem;

    public FireProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        // 使用火球法杖作为投射物的渲染模型
        this.fireballItem = new ItemStack(ModItems.FIRE_STAFF.get());
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

        // 设置物品大小 - 火球应该比原武器小一些
        poseStack.scale(0.8F, 0.8F, 0.8F);

        // 添加旋转动画效果，让火球看起来在旋转
        float rotation = (entity.tickCount + partialTicks) * 10.0F;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

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
}