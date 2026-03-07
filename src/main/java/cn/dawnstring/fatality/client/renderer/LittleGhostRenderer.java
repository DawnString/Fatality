package cn.dawnstring.fatality.client.renderer;

import cn.dawnstring.fatality.Fatality;
import cn.dawnstring.fatality.client.model.ModModelLayers;
import cn.dawnstring.fatality.entity.basemonster.littleghost.LittleGhost;
import cn.dawnstring.fatality.entity.basemonster.littleghost.LittleGhostModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class LittleGhostRenderer extends MobRenderer<LittleGhost, LittleGhostModel<LittleGhost>> {
    
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Fatality.MODID, "textures/entity/little_ghost.png");

    public LittleGhostRenderer(EntityRendererProvider.Context context) {
        super(context, new LittleGhostModel<>(context.bakeLayer(ModModelLayers.LITTLE_GHOST_LAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(LittleGhost entity) {
        return TEXTURE;
    }
}