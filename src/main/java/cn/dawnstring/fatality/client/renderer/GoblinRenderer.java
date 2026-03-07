package cn.dawnstring.fatality.client.renderer;

import cn.dawnstring.fatality.entity.basemonster.goblin.Goblin;
import cn.dawnstring.fatality.entity.basemonster.goblin.GoblinModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Goblin 渲染器 - 使用自定义模型渲染 Goblin 实体
 */
public class GoblinRenderer extends MobRenderer<Goblin, GoblinModel<Goblin>> {
    
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("fatality", "textures/entity/goblin.png");
    
    public GoblinRenderer(EntityRendererProvider.Context context) {
        super(context, new GoblinModel<>(context.bakeLayer(GoblinModel.LAYER_LOCATION)), 0.5F);
    }
    
    @Override
    public void render(Goblin entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        
        // 如果正在攻击，添加轻微的攻击动画效果
        if (entity.isAttacking()) {
            float attackProgress = (entity.attackAnimationTick + partialTicks) / 10.0f;
            float attackTilt = (float) Math.sin(attackProgress * Math.PI) * 0.1f;
            poseStack.translate(0, -attackTilt * 0.3, attackTilt * 0.5);
        }
        
        // 调用父类渲染方法，使用自定义模型
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        
        poseStack.popPose();
    }
    
    @Override
    protected void setupRotations(Goblin entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        // 让 Goblin 始终面向玩家
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F - rotationYaw));
    }
    
    @Override
    public ResourceLocation getTextureLocation(Goblin entity) {
        return TEXTURE;
    }
}