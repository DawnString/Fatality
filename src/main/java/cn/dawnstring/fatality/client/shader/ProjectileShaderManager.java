package cn.dawnstring.fatality.client.shader;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ProjectileShaderManager {
    
    // 渲染类型 - 使用预定义的透明渲染类型
    public static RenderType createElementalSpearRenderType() {
        return RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath("fatality", "textures/entity/elemental_spear.png"));
    }
    
    public static RenderType createElementalTornadoRenderType() {
        return RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath("fatality", "textures/entity/elemental_tornado.png"));
    }
    
    public static RenderType createGaussLaserRenderType() {
        return RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath("fatality", "textures/entity/gauss_laser.png"));
    }
    
    public static RenderType createHighEnergyBallRenderType() {
        return RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath("fatality", "textures/entity/high_energy_ball.png"));
    }
    
    public static RenderType createOriginLaserRenderType() {
        return RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath("fatality", "textures/entity/origin_laser.png"));
    }
}