package cn.dawnstring.fatality.client.renderer.boss.lordofender;

import cn.dawnstring.fatality.entity.boss.lordofender.LordOfEnderEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class LordOfEnderRenderer extends GeoEntityRenderer<LordOfEnderEntity> {

    public LordOfEnderRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new LordOfEnderModel());
    }
}
