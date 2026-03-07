package cn.dawnstring.fatality.client.renderer;

import cn.dawnstring.fatality.entity.projectile.WaveBreakerProjectile;
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
public class WaveBreakerProjectileRenderer extends EntityRenderer<WaveBreakerProjectile> {

    private final ItemStack weaponStack;

    public WaveBreakerProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.weaponStack = new ItemStack(ModItems.YOUS_WAVE_BREAKER.get());
    }

    @Override
    public void render(WaveBreakerProjectile entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // 如果已经爆炸，不渲染模型
        if (entity.hasExploded()) {
            return;
        }

        poseStack.pushPose();

        // 调整位置到投掷物中心
        poseStack.translate(0, 0.5, 0);

        // 获取插值后的旋转角度
        float yRot = entity.getYRot();
        float xRot = entity.getXRot();

        // 设置旋转：Y轴旋转让模型朝向水平方向
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot - 90.0F));

        // X轴旋转让模型在竖直方向朝向玩家视角
        // 减去90度是因为默认物品模型是垂直的，我们需要根据俯仰角度调整
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));

        // 设置物品大小
        poseStack.scale(1.5F, 1.5F, 1.5F);

        // 渲染物品模型
        Minecraft.getInstance().getItemRenderer().renderStatic(
                weaponStack,
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
    public ResourceLocation getTextureLocation(WaveBreakerProjectile entity) {
        // 返回一个空的纹理位置，因为我们使用物品渲染器
        return ResourceLocation.fromNamespaceAndPath("fatality", "textures/empty.png");
    }
}