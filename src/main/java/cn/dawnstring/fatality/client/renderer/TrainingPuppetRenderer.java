package cn.dawnstring.fatality.client.renderer;

import cn.dawnstring.fatality.client.model.TrainingPuppetModel;
import cn.dawnstring.fatality.entity.TrainingPuppet;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * 训练人偶渲染器 - 使用自定义模型渲染训练人偶
 */
public class TrainingPuppetRenderer extends LivingEntityRenderer<TrainingPuppet, TrainingPuppetModel<TrainingPuppet>> {

    public TrainingPuppetRenderer(EntityRendererProvider.Context context) {
        super(context, new TrainingPuppetModel<>(context.bakeLayer(TrainingPuppetModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public void render(TrainingPuppet entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        // 添加轻微的上下浮动动画效果
        float floatOffset = (float) Math.sin((entity.tickCount + partialTicks) * 0.1F) * 0.05F;
        poseStack.translate(0, floatOffset, 0);

        // 调用父类渲染方法，使用自定义模型
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);

        poseStack.popPose();
    }

    @Override
    protected void setupRotations(TrainingPuppet entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        // 让训练人偶始终面向玩家
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F - rotationYaw));
    }

    @Override
    public ResourceLocation getTextureLocation(TrainingPuppet entity) {
        // 返回训练人偶的纹理位置
        return ResourceLocation.fromNamespaceAndPath("fatality", "textures/entity/training_puppet.png");
    }
}