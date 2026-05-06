package cn.dawnstring.fatality.client.renderer.boss.lordofender;

import cn.dawnstring.fatality.entity.boss.lordofender.EnderEnergyCrystal;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class EnderEnergyCrystalRenderer extends GeoEntityRenderer<EnderEnergyCrystal> {
    public EnderEnergyCrystalRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new EnderEnergyCrystalModel());
    }
}
