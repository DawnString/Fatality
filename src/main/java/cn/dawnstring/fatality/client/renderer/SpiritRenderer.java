package cn.dawnstring.fatality.client.renderer;

import cn.dawnstring.fatality.entity.basemonster.spirit.Spirit;
import cn.dawnstring.fatality.entity.basemonster.spirit.SpiritModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Spirit 渲染器 - 使用自定义模型渲染 Spirit 实体
 */
public class SpiritRenderer extends MobRenderer<Spirit, SpiritModel<Spirit>> {
    
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("fatality", "textures/entity/spirit.png");
    
    public SpiritRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiritModel<>(context.bakeLayer(SpiritModel.LAYER_LOCATION)), 0.5F);
    }
    
    @Override
    public void render(Spirit entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        
        // 添加飞行生物的轻微浮动效果
        float floatOffset = (float) Math.sin((entity.tickCount + partialTicks) * 0.2F) * 0.1F;
        poseStack.translate(0, floatOffset, 0);
        
        // 调用父类渲染方法，使用自定义模型
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        
        poseStack.popPose();
    }
    
    @Override
    protected void setupRotations(Spirit entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        // 让 Spirit 始终面向玩家
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F - rotationYaw));
    }
    
    @Override
    public ResourceLocation getTextureLocation(Spirit entity) {
        return TEXTURE;
    }
    
    @Override
    protected boolean isShaking(Spirit entity) {
        // 飞行生物不会颤抖
        return false;
    }
}