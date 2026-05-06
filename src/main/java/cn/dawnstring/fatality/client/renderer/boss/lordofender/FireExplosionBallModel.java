package cn.dawnstring.fatality.client.renderer.boss.lordofender;

import cn.dawnstring.fatality.Fatality;
import cn.dawnstring.fatality.entity.boss.lordofender.FireExplosionBall;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FireExplosionBallModel extends GeoModel<FireExplosionBall> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(Fatality.MODID, "geo/flame_explosive_ball.geo.json");
    private static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath(Fatality.MODID, "textures/entity/flame_explosive_ball.png");
    private static final ResourceLocation ANIM = ResourceLocation.fromNamespaceAndPath(Fatality.MODID, "animations/flame_explosive_ball.animation.json");
    @Override public ResourceLocation getModelResource(FireExplosionBall o) { return MODEL; }
    @Override public ResourceLocation getTextureResource(FireExplosionBall o) { return TEX; }
    @Override public ResourceLocation getAnimationResource(FireExplosionBall o) { return ANIM; }
}
