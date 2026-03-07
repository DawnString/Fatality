package cn.dawnstring.fatality.client.renderer;

import cn.dawnstring.fatality.client.model.ExampleBossModel;
import cn.dawnstring.fatality.client.model.ModModelLayers;
import cn.dawnstring.fatality.entity.boss.ExampleBoss;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ExampleBossRenderer extends MobRenderer<ExampleBoss, ExampleBossModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("fatality", "textures/entity/example_boss.png");

    public ExampleBossRenderer(EntityRendererProvider.Context context) {
        super(context, new ExampleBossModel(context.bakeLayer(ModModelLayers.EXAMPLE_BOSS_LAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(ExampleBoss entity) {
        return TEXTURE;
    }
}