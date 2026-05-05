package cn.dawnstring.fatality.client.renderer.boss.enderdragon;

import cn.dawnstring.fatality.entity.boss.enderdragon.DragonFlameBall;
import software.bernie.geckolib.model.GeoModel;

public class DragonFlameBallModel extends GeoModel<DragonFlameBall> {

    @Override
    public ResourceLocation getModelResource(DragonFlameBall animatable) {
        return ResourceLocation.fromNamespaceAndPath("fatality", "geo/dragonflame_ball.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DragonFlameBall animatable) {
        return ResourceLocation.fromNamespaceAndPath("fatality", "textures/entity/dragonflame_ball.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DragonFlameBall animatable) {
        return ResourceLocation.fromNamespaceAndPath("fatality", "animations/dragonflame_ball.animation.json");
    }
}