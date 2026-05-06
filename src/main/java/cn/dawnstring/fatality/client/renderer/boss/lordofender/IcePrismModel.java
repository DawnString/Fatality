package cn.dawnstring.fatality.client.renderer.boss.lordofender;

import cn.dawnstring.fatality.Fatality;
import cn.dawnstring.fatality.entity.boss.lordofender.IcePrism;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class IcePrismModel extends GeoModel<IcePrism> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(Fatality.MODID, "geo/ice_prism.geo.json");
    private static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath(Fatality.MODID, "textures/entity/ice_prism.png");
    private static final ResourceLocation ANIM = ResourceLocation.fromNamespaceAndPath(Fatality.MODID, "animations/ice_prism.animation.json");
    @Override public ResourceLocation getModelResource(IcePrism o) { return MODEL; }
    @Override public ResourceLocation getTextureResource(IcePrism o) { return TEX; }
    @Override public ResourceLocation getAnimationResource(IcePrism o) { return ANIM; }
}
