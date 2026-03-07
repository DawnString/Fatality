package cn.dawnstring.fatality.client.renderer;

import cn.dawnstring.fatality.client.model.EndOfNightmareModel;
import cn.dawnstring.fatality.client.model.ModModelLayers;
import cn.dawnstring.fatality.entity.boss.endofnightmare.EndOfNightmare;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class EndOfNightmareRenderer extends MobRenderer<EndOfNightmare, EndOfNightmareModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("fatality", "textures/entity/end_of_nightmare.png");

    public EndOfNightmareRenderer(EntityRendererProvider.Context context) {
        super(context, new EndOfNightmareModel(context.bakeLayer(ModModelLayers.END_OF_NIGHTMARE_LAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(EndOfNightmare entity) {
        return TEXTURE;
    }
}
