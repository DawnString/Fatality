package cn.dawnstring.fatality.client.renderer;

import cn.dawnstring.fatality.entity.TornadoEffect;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TornadoEffectRenderer extends EntityRenderer<TornadoEffect> {

    public TornadoEffectRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(TornadoEffect entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
    }

    @Override
    public ResourceLocation getTextureLocation(TornadoEffect entity) {
        // 返回一个空的纹理位置，因为我们主要使用粒子系统
        return ResourceLocation.fromNamespaceAndPath("fatality", "textures/empty.png");
    }
}