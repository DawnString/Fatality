package cn.dawnstring.fatality.client.renderer;

import cn.dawnstring.fatality.client.model.BulletProjectileModel;
import cn.dawnstring.fatality.client.model.ModModelLayers;
import cn.dawnstring.fatality.entity.projectile.BulletProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BulletProjectileRenderer extends EntityRenderer<BulletProjectile> {

    private final BulletProjectileModel model;
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("fatality", "textures/entity/bullet_projectile.png");

    public BulletProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new BulletProjectileModel(context.bakeLayer(ModModelLayers.BULLET_PROJECTILE_LAYER));
    }

    @Override
    public void render(BulletProjectile entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        // 简化位置调整
        poseStack.translate(0, 0.15, 0);

        // 简化旋转逻辑
        float yRot = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
        float xRot = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());

        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yRot - 90.0F));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(xRot));

        // 简化缩放
        poseStack.scale(0.1F, 0.1F, 0.1F);

        // 简化动画设置
        this.model.setupAnim(entity, 0, 0, entity.tickCount + partialTicks, 0, 0);

        // 简化渲染调用
        this.model.renderToBuffer(poseStack, bufferSource.getBuffer(this.model.renderType(TEXTURE)),
                packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(BulletProjectile entity) {
        return TEXTURE;
    }
}