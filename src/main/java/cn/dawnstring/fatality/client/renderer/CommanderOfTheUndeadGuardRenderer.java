package cn.dawnstring.fatality.client.renderer;

import cn.dawnstring.fatality.Fatality;
import cn.dawnstring.fatality.client.model.ModModelLayers;
import cn.dawnstring.fatality.entity.boss.commanderoftheundeadguard.CommanderOfTheUndeadGuard;
import cn.dawnstring.fatality.entity.boss.commanderoftheundeadguard.CommanderOfTheUndeadGuardAnimation;
import cn.dawnstring.fatality.entity.boss.commanderoftheundeadguard.CommanderOfTheUndeadGuardModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

public class CommanderOfTheUndeadGuardRenderer extends MobRenderer<CommanderOfTheUndeadGuard, CommanderOfTheUndeadGuardModel<CommanderOfTheUndeadGuard>> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Fatality.MODID, "textures/entity/commander_of_the_undead_guard.png");

    public CommanderOfTheUndeadGuardRenderer(EntityRendererProvider.Context context) {
        super(context, new CommanderOfTheUndeadGuardModel<CommanderOfTheUndeadGuard>(context.bakeLayer(ModModelLayers.COMMANDER_OF_THE_UNDEAD_GUARD_LAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(CommanderOfTheUndeadGuard entity) {
        return TEXTURE;
    }

    @Override
    public void render(CommanderOfTheUndeadGuard entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        CommanderOfTheUndeadGuardModel<CommanderOfTheUndeadGuard> model = this.getModel();
        
        // ===== 动画状态机 =====
        // 根据实体当前状态决定播放哪个动画

        // 1. 攻击动画优先级最高
        if (entity.isAttacking()) {
            KeyframeAnimations.animate(
                    model,
                    CommanderOfTheUndeadGuardAnimation.ATTACK,
                    entity.tickCount, 
                    partialTick,
                    new Vector3f());
        }
        // 2. 跳跃相关动画
        else if (!entity.onGround()) {
            // 判断是在空中还是落地过程
            if (entity.isJumping) {
                // 跳跃上升阶段
                KeyframeAnimations.animate(
                        model,
                        CommanderOfTheUndeadGuardAnimation.JUMP,
                        entity.tickCount,
                        partialTick,
                        new Vector3f());
            } else {
                // 下落阶段
                KeyframeAnimations.animate(
                        model,
                        CommanderOfTheUndeadGuardAnimation.JUMP_END,
                        entity.tickCount,
                        partialTick,
                        new Vector3f());
            }
        }
        // 3. 冲刺相关动画（在地面上且冲刺）
        else if (entity.isRushReady()) {
            KeyframeAnimations.animate(
                    model,
                    CommanderOfTheUndeadGuardAnimation.RUSH_READY,
                    entity.tickCount,
                    partialTick,
                    new Vector3f());
        } else if (entity.isRushing()) {
            KeyframeAnimations.animate(
                    model,
                    CommanderOfTheUndeadGuardAnimation.RUSH,
                    entity.tickCount,
                    partialTick,
                    new Vector3f());
        } else if (entity.isRushEnding()) {
            KeyframeAnimations.animate(
                    model,
                    CommanderOfTheUndeadGuardAnimation.RUSH_END,
                    entity.tickCount,
                    partialTick,
                    new Vector3f());
        }
        // 4. 移动动画（在地面上，不攻击，不冲刺，但有移动速度）
        else if (entity.getDeltaMovement().horizontalDistanceSqr() > 0.001) {
            KeyframeAnimations.animate(
                    model,
                    CommanderOfTheUndeadGuardAnimation.MOVE,
                    entity.tickCount,
                    partialTick,
                    new Vector3f());
        }
        // 5. 默认站立动画
        else {
            KeyframeAnimations.animate(
                    model,
                    CommanderOfTheUndeadGuardAnimation.STAND,
                    entity.tickCount,
                    partialTick,
                    new Vector3f());
        }

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}