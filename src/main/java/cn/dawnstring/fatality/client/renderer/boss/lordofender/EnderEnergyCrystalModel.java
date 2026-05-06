package cn.dawnstring.fatality.client.renderer.boss.lordofender;

import cn.dawnstring.fatality.Fatality;
import cn.dawnstring.fatality.entity.boss.lordofender.EnderEnergyCrystal;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class EnderEnergyCrystalModel extends GeoModel<EnderEnergyCrystal> {

    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(Fatality.MODID, "geo/ender_energy_crystal.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Fatality.MODID, "textures/entity/ender_energy_crystal.png");
    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(Fatality.MODID, "animations/ender_energy_crystal.animation.json");

    @Override
    public ResourceLocation getModelResource(EnderEnergyCrystal animatable) { return MODEL; }
    @Override
    public ResourceLocation getTextureResource(EnderEnergyCrystal animatable) { return TEXTURE; }
    @Override
    public ResourceLocation getAnimationResource(EnderEnergyCrystal animatable) { return ANIMATION; }
}
