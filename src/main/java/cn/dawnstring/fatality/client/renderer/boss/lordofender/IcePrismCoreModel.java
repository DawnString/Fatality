package cn.dawnstring.fatality.client.renderer.boss.lordofender;

import cn.dawnstring.fatality.Fatality;
import cn.dawnstring.fatality.entity.boss.lordofender.IcePrismCore;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class IcePrismCoreModel extends GeoModel<IcePrismCore> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(Fatality.MODID, "geo/ice_prism_core.geo.json");
    private static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath(Fatality.MODID, "textures/entity/ice_prism_core.png");
    private static final ResourceLocation ANIM = ResourceLocation.fromNamespaceAndPath(Fatality.MODID, "animations/ice_prism_core.animation.json");
    @Override public ResourceLocation getModelResource(IcePrismCore o) { return MODEL; }
    @Override public ResourceLocation getTextureResource(IcePrismCore o) { return TEX; }
    @Override public ResourceLocation getAnimationResource(IcePrismCore o) { return ANIM; }
}
