package cn.dawnstring.fatality.client.renderer;

import cn.dawnstring.fatality.entity.VileDarknessEffect;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VileDarknessEffectRenderer extends EntityRenderer<VileDarknessEffect> {

    public VileDarknessEffectRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(VileDarknessEffect entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // 空渲染方法，因为VileDarknessEffect主要通过粒子系统表现
    }

    @Override
    public ResourceLocation getTextureLocation(VileDarknessEffect entity) {
        // 返回一个空的纹理位置，因为我们主要使用粒子系统
        return ResourceLocation.fromNamespaceAndPath("fatality", "textures/empty.png");
    }
}