package cn.dawnstring.fatality.client.renderer;

import cn.dawnstring.fatality.entity.projectile.ShadowProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShadowProjectileRenderer extends EntityRenderer<ShadowProjectile> {

    public ShadowProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ShadowProjectile entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // 暗影投射物不需要渲染模型，完全由粒子效果表现
        // 粒子效果已经在ShadowProjectile的tick方法中生成
    }

    @Override
    public ResourceLocation getTextureLocation(ShadowProjectile entity) {
        // 返回一个空的纹理位置，因为我们使用粒子效果渲染
        return ResourceLocation.fromNamespaceAndPath("fatality", "textures/empty.png");
    }
}