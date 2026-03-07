package cn.dawnstring.fatality.client.renderer;

import cn.dawnstring.fatality.entity.EnderSphere;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EnderSphereRenderer extends EntityRenderer<EnderSphere> {

    public EnderSphereRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EnderSphere entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // 空渲染方法，因为EnderSphere主要通过粒子系统表现
        // 粒子效果已经在EnderSphere的tick方法中生成
    }

    @Override
    public ResourceLocation getTextureLocation(EnderSphere entity) {
        // 返回一个空的纹理位置，因为我们使用粒子效果渲染
        return ResourceLocation.fromNamespaceAndPath("fatality", "textures/empty.png");
    }
}