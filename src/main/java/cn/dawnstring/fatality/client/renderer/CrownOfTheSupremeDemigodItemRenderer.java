package cn.dawnstring.fatality.client.renderer;

import cn.dawnstring.fatality.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class CrownOfTheSupremeDemigodItemRenderer
{
    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();

        // 检查玩家是否佩戴至高半神王冠
        if (hasCrownOfTheSupremeDemigodEquipped(player)) {
            renderCrownOfTheSupremeDemigodHalo(player, event.getPoseStack(),
                    event.getMultiBufferSource(), event.getPackedLight(),
                    event.getPartialTick());
        }
    }

    private static void renderCrownOfTheSupremeDemigodHalo(Player player, PoseStack poseStack,
                                                           MultiBufferSource bufferSource, int packedLight,
                                                           float partialTick) {
        poseStack.pushPose();

        // 获取玩家位置和旋转
        float playerHeight = player.getBbHeight();

        // 将王冠定位在玩家头顶上方，像天使光环一样水平悬浮
        poseStack.translate(0, playerHeight + 0.4, 0); // 头顶上方0.4格

        // 使王冠始终保持水平（不随玩家头部俯仰旋转）
        // 只跟随玩家水平旋转，不跟随俯仰
        float yaw = player.getViewYRot(partialTick);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-yaw));

        // 添加轻微的旋转动画，使光环更有活力
        float rotation = (player.tickCount + partialTick) * 2.0f; // 缓慢旋转
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));

        // 设置光环大小 - 调整为适合光环的尺寸
        float scale = 1.0f; // 较小的缩放比例，使光环更精致
        poseStack.scale(scale, scale, scale);

        // 渲染王冠物品模型
        renderCrownHaloModel(poseStack, bufferSource, packedLight, partialTick);

        poseStack.popPose();
    }

    private static void renderCrownHaloModel(PoseStack poseStack, MultiBufferSource bufferSource,
                                             int packedLight, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();

        // 获取至高半神王冠的物品实例
        var crownOfTheSupremeDemigodItem = ModItems.CROWN_OF_THE_SUPREME_DEMIGOD.get();
        ItemStack crownStack = new ItemStack(crownOfTheSupremeDemigodItem);

        // 获取当前世界
        Level level = minecraft.level;

        // 渲染物品模型 - 使用FIXED显示上下文，更适合光环效果
        minecraft.getItemRenderer().renderStatic(
                crownStack,
                ItemDisplayContext.FIXED, // 使用FIXED而不是GROUND，更适合光环显示
                packedLight,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                level,
                0
        );
    }

    private static boolean hasCrownOfTheSupremeDemigodEquipped(Player player) {
        return cn.dawnstring.fatality.items.accessory.CrownOfTheSupremeDemigod.isPlayerWearingCrown(player);
    }
}
