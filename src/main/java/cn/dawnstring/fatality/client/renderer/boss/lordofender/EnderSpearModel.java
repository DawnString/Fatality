package cn.dawnstring.fatality.client.renderer.boss.lordofender;

import cn.dawnstring.fatality.Fatality;
import cn.dawnstring.fatality.entity.boss.lordofender.EnderSpearProjectile;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class EnderSpearModel extends GeoModel<EnderSpearProjectile> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(Fatality.MODID, "geo/ender_spear.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Fatality.MODID, "textures/entity/ender_spear.png");
    private static final ResourceLocation ANIM = ResourceLocation.fromNamespaceAndPath(Fatality.MODID, "animations/ender_spear.animation.json");

    @Override public ResourceLocation getModelResource(EnderSpearProjectile o) { return MODEL; }
    @Override public ResourceLocation getTextureResource(EnderSpearProjectile o) { return TEXTURE; }
    @Override public ResourceLocation getAnimationResource(EnderSpearProjectile o) { return ANIM; }
}
