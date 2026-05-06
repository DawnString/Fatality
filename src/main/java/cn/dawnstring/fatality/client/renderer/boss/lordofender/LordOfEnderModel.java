package cn.dawnstring.fatality.client.renderer.boss.lordofender;

import cn.dawnstring.fatality.Fatality;
import cn.dawnstring.fatality.entity.boss.lordofender.LordOfEnderEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class LordOfEnderModel extends GeoModel<LordOfEnderEntity> {

    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(Fatality.MODID, "geo/lord_of_ender.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Fatality.MODID, "textures/entity/lord_of_ender.png");
    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(Fatality.MODID, "animations/lord_of_ender.animation.json");

    @Override
    public ResourceLocation getModelResource(LordOfEnderEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(LordOfEnderEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(LordOfEnderEntity animatable) {
        return ANIMATION;
    }
}
