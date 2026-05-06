package cn.dawnstring.fatality.client.renderer.boss.lordofender;

import cn.dawnstring.fatality.entity.boss.lordofender.FireExplosionBall;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FireExplosionBallRenderer extends GeoEntityRenderer<FireExplosionBall> {
    public FireExplosionBallRenderer(EntityRendererProvider.Context ctx) { super(ctx, new FireExplosionBallModel()); }
}
