package cn.dawnstring.fatality.client.renderer.boss.enderdragon;

import cn.dawnstring.fatality.entity.boss.enderdragon.DragonFlameBall;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DragonFlameBallRenderer extends GeoEntityRenderer<DragonFlameBall> {

    public DragonFlameBallRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DragonFlameBallModel());
    }

    @Override
    public ResourceLocation getTextureLocation(DragonFlameBall entity) {
        return ResourceLocation.fromNamespaceAndPath("fatality", "textures/entity/dragonflame_ball.png");
    }
}