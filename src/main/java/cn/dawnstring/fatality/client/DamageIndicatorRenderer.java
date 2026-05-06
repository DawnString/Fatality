package cn.dawnstring.fatality.client;

import cn.dawnstring.fatality.Fatality;
import cn.dawnstring.fatality.client.DamageIndicatorManager.DamageIndicator;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.Logger;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 * 伤害数值指示器渲染器（修复字体倒置和大小）
 */
@OnlyIn(Dist.CLIENT)
public class DamageIndicatorRenderer {

    private static final Logger LOGGER = Fatality.LOGGER;
    private static final Minecraft minecraft = Minecraft.getInstance();

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && minecraft.level != null) {
            DamageIndicatorManager.updateIndicators();
        }
    }

    @SubscribeEvent
    public void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        Camera camera = event.getCamera();

        List<DamageIndicator> activeIndicators = DamageIndicatorManager.getActiveIndicators();

        if (!activeIndicators.isEmpty()) {
            LOGGER.debug("DamageIndicatorRenderer: 开始渲染 {} 个指示器", activeIndicators.size());

            for (DamageIndicator indicator : activeIndicators) {
                renderDamageIndicator3D(poseStack, bufferSource, camera, indicator);
            }

            bufferSource.endBatch();
        }
    }

    /**
     * 渲染3D伤害数值指示器（彻底修复字体倒置和重叠问题）
     */
    private void renderDamageIndicator3D(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                         Camera camera, DamageIndicator indicator) {
        Vec3 indicatorPos = indicator.getCurrentPosition();
        Vec3 cameraPos = camera.getPosition();

        // 计算距离
        double distance = indicatorPos.distanceTo(cameraPos);
        if (distance > 20.0) {
            return;
        }

        // 计算相对位置
        double dx = indicatorPos.x - cameraPos.x;
        double dy = indicatorPos.y - cameraPos.y;
        double dz = indicatorPos.z - cameraPos.z;

        poseStack.pushPose();

        try {
            // 移动到指示器位置
            poseStack.translate(dx, dy, dz);

            // 正确的广告牌效果：始终面向摄像机
            // 使用更简单的旋转方法
            poseStack.mulPose(camera.rotation());
            
            // 额外旋转180度来修正文字方向
            poseStack.mulPose(new Quaternionf().rotationY((float)Math.PI));

            // 使用合适的文本大小
            float textScale = getTextScale(distance);

            // 传奇风格：暴击时添加轻微缩放效果
            if (indicator.isCritical) {
                long elapsed = System.currentTimeMillis() - indicator.spawnTime;
                float pulse = (float)Math.sin(elapsed * 0.01f) * 0.1f + 1.0f;
                textScale *= pulse;
            }

            // 应用缩放（使用正值）
            poseStack.scale(textScale, textScale, textScale);

            // 格式化伤害文本
            String damageText = formatDamageText(indicator.damage);

            // 获取伤害颜色
            int color = getDamageColor(indicator.damage, indicator.isCritical);

            // 获取字体
            Font font = minecraft.font;

            // 计算文本宽度用于居中
            int textWidth = font.width(damageText);
            float xOffset = -textWidth / 2.0f;
            float yOffset = 0; // 不使用Y偏移

            // 渲染文本（使用透明度）
            int alpha = (int)(indicator.alpha * 255);
            int finalColor = (alpha << 24) | (color & 0x00FFFFFF);

            // 使用正确的文本渲染方法
            font.drawInBatch(
                    Component.literal(damageText),
                    xOffset, yOffset,
                    finalColor,
                    false,
                    poseStack.last().pose(),
                    bufferSource,
                    Font.DisplayMode.NORMAL,
                    0,
                    15728880
            );

        } finally {
            poseStack.popPose();
        }
    }

    /**
     * 根据距离获取文本缩放比例（传奇风格：大字体）
     */
    private float getTextScale(double distance) {
        if (distance < 5.0) {
            return 0.12f; // 传奇风格：更大的字体
        } else if (distance < 10.0) {
            return 0.10f;
        } else if (distance < 15.0) {
            return 0.08f;
        } else {
            return 0.06f;
        }
    }

    /**
     * 格式化伤害文本
     */
    private String formatDamageText(float damage) {
        if (damage == (int) damage) {
            return String.valueOf((int) damage);
        } else {
            return String.format("%.1f", damage);
        }
    }

    /**
     * 根据伤害值获取颜色（传奇风格：鲜艳颜色）
     */
    private int getDamageColor(float damage, boolean isCritical) {
        if (isCritical) {
            return 0xFFFF00; // 暴击：亮黄色（传奇经典暴击色）
        } else if (damage >= 20.0f) {
            return 0xFF0000; // 极高伤害：纯红色
        } else if (damage >= 10.0f) {
            return 0xFF5500; // 高伤害：橙红色
        } else if (damage >= 5.0f) {
            return 0xFFAA00; // 中伤害：橙色
        } else {
            return 0xFFFFFF; // 低伤害：白色（传奇经典普通伤害色）
        }
    }
}