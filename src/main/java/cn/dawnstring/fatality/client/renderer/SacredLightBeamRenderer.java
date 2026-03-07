package cn.dawnstring.fatality.client.renderer;

import cn.dawnstring.fatality.entity.SacredLightBeam;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SacredLightBeamRenderer extends EntityRenderer<SacredLightBeam> {

    public SacredLightBeamRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(SacredLightBeam entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // 空渲染方法，因为SacredLightBeam主要通过粒子系统表现
    }

    @Override
    public ResourceLocation getTextureLocation(SacredLightBeam entity) {
        // 返回一个空的纹理位置，因为我们主要使用粒子系统
        return ResourceLocation.fromNamespaceAndPath("fatality", "textures/empty.png");
    }
}