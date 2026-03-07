package cn.dawnstring.fatality.client.renderer;

import cn.dawnstring.fatality.entity.basemonster.desertbeetle.DesertBeetle;
import cn.dawnstring.fatality.entity.basemonster.desertbeetle.DesertBeetleModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * DesertBeetle 渲染器 - 使用自定义模型渲染 DesertBeetle 实体
 */
public class DesertBeetleRenderer extends MobRenderer<DesertBeetle, DesertBeetleModel<DesertBeetle>> {
    
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("fatality", "textures/entity/desert_beetle.png");
    
    public DesertBeetleRenderer(EntityRendererProvider.Context context) {
        super(context, new DesertBeetleModel<>(context.bakeLayer(DesertBeetleModel.LAYER_LOCATION)), 0.5F);
    }
    
    @Override
    public void render(DesertBeetle entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        
        // 如果正在攻击，添加轻微的前倾效果
        if (entity.isAttacking()) {
            float attackProgress = (entity.attackAnimationTick + partialTicks) / 10.0f;
            float attackTilt = (float) Math.sin(attackProgress * Math.PI) * 0.2f;
            poseStack.translate(0, -attackTilt * 0.5, attackTilt);
        }
        
        // 调用父类渲染方法，使用自定义模型
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        
        poseStack.popPose();
    }
    
    @Override
    protected void setupRotations(DesertBeetle entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        // 让 DesertBeetle 始终面向玩家
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F - rotationYaw));
    }
    
    @Override
    public ResourceLocation getTextureLocation(DesertBeetle entity) {
        return TEXTURE;
    }
}