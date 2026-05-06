package cn.dawnstring.fatality.client.renderer.boss.lordofender;

import cn.dawnstring.fatality.entity.boss.lordofender.IcePrism;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class IcePrismRenderer extends GeoEntityRenderer<IcePrism> {
    public IcePrismRenderer(EntityRendererProvider.Context ctx) { super(ctx, new IcePrismModel()); }
}
