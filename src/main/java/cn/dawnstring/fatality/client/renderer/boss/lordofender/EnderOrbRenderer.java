package cn.dawnstring.fatality.client.renderer.boss.lordofender;

import cn.dawnstring.fatality.entity.boss.lordofender.EnderOrb;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class EnderOrbRenderer extends EntityRenderer<EnderOrb> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/entity/enderman/enderman.png");
    private static final float SIZE = 0.3f;

    public EnderOrbRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(EnderOrb entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffer, int light) {
        super.render(entity, yaw, partialTick, pose, buffer, light);

        pose.pushPose();
        Vec3 pos = entity.position();
        pose.translate(0, 0.3, 0);

        Matrix4f mat = pose.last().pose();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));

        float s = SIZE;
        consumer.vertex(mat, -s, -s, 0).color(180, 0, 255, 200).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 1, 0).endVertex();
        consumer.vertex(mat, s, -s, 0).color(180, 0, 255, 200).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 1, 0).endVertex();
        consumer.vertex(mat, s, s, 0).color(180, 0, 255, 200).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 1, 0).endVertex();
        consumer.vertex(mat, -s, s, 0).color(180, 0, 255, 200).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 1, 0).endVertex();

        pose.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(EnderOrb entity) {
        return TEXTURE;
    }
}
