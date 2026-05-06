package cn.dawnstring.fatality.client.renderer.boss.lordofender;

import cn.dawnstring.fatality.entity.boss.lordofender.EnderSpearProjectile;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class EnderSpearRenderer extends GeoEntityRenderer<EnderSpearProjectile> {
    public EnderSpearRenderer(EntityRendererProvider.Context ctx) { super(ctx, new EnderSpearModel()); }
}
