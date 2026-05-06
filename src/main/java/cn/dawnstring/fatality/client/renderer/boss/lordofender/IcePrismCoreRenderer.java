package cn.dawnstring.fatality.client.renderer.boss.lordofender;

import cn.dawnstring.fatality.entity.boss.lordofender.IcePrismCore;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class IcePrismCoreRenderer extends GeoEntityRenderer<IcePrismCore> {
    public IcePrismCoreRenderer(EntityRendererProvider.Context ctx) { super(ctx, new IcePrismCoreModel()); }
}
