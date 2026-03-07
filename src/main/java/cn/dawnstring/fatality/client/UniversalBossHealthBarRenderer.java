package cn.dawnstring.fatality.client;

import cn.dawnstring.fatality.entity.BaseBoss;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * 通用Boss血条渲染器
 */
public class UniversalBossHealthBarRenderer {

    /**
     * 渲染Boss血条（支持自定义Y轴位置）
     */
    public static void renderBossHealthBar(GuiGraphics guiGraphics, BaseBoss boss, int screenWidth, int yPosition) {
        if (!boss.shouldShowCustomHealthBar()) {
            return;
        }

        PoseStack poseStack = guiGraphics.pose();

        // 获取缩放后的尺寸
        int scaledDecorationWidth = boss.getScaledDecorationWidth();
        int scaledDecorationHeight = boss.getScaledDecorationHeight();
        int scaledMainBarWidth = boss.getScaledMainBarWidth();
        int scaledMainBarHeight = boss.getScaledMainBarHeight();

        // 计算血条位置（屏幕顶部居中）
        int x = (screenWidth - scaledDecorationWidth) / 2;
        int y = yPosition; // 使用传入的Y轴位置

        // 保存当前变换状态
        poseStack.pushPose();

        // 应用缩放
        float scale = boss.getHealthBarScale();
        poseStack.scale(scale, scale, 1.0f);

        // 计算缩放后的坐标
        float scaledX = x / scale;
        float scaledY = y / scale;

        // 渲染装饰背景
        RenderSystem.setShaderTexture(0, boss.HEALTH_BAR_DECORATION);
        guiGraphics.blit(boss.HEALTH_BAR_DECORATION,
                (int)scaledX, (int)scaledY,
                0, 0,
                boss.DECORATION_WIDTH, boss.DECORATION_HEIGHT,
                boss.DECORATION_WIDTH, boss.DECORATION_HEIGHT);

        // 计算血量比例
        float healthRatio = boss.getHealth() / boss.getMaxHealth();
        int healthWidth = (int)(boss.MAIN_BAR_WIDTH * healthRatio);

        // 渲染血量填充部分（使用正确的偏移量）
        if (healthWidth > 0) {
            RenderSystem.setShaderTexture(0, boss.HEALTH_BAR_MAIN);
            guiGraphics.blit(boss.HEALTH_BAR_MAIN,
                    (int)(scaledX + boss.MAIN_BAR_OFFSET_X),
                    (int)(scaledY + boss.MAIN_BAR_OFFSET_Y),
                    0, 0,
                    healthWidth, boss.MAIN_BAR_HEIGHT,
                    boss.MAIN_BAR_WIDTH, boss.MAIN_BAR_HEIGHT);
        }

        poseStack.popPose();

        // 渲染血量文本和阶段名称
        renderHealthInfo(guiGraphics, boss, x, y, scaledDecorationWidth, scaledDecorationHeight);
    }

    private static void renderHealthInfo(GuiGraphics guiGraphics, BaseBoss boss, int x, int y, int width, int height) {
        Font font = Minecraft.getInstance().font;

        // 渲染血量文本
        String healthText = (boss.getHealth() + "/" +  boss.getMaxHealth());
        int healthTextWidth = font.width(healthText);
        guiGraphics.drawString(font, healthText,
                x + (width - healthTextWidth) / 2,
                y + height + 5,
                0xFFFFFF, true);

        /**
        // 渲染阶段名称
        String phaseName = boss.getCurrentPhaseName();
        if (phaseName != null && !phaseName.isEmpty()) {
            int phaseTextWidth = font.width(phaseName);
            guiGraphics.drawString(font, phaseName,
                    x + (width - phaseTextWidth) / 2,
                    y - 15,
                    boss.getPhaseHealthBarColor(), true);
        }
         **/

        // 渲染Boss名称
        String bossName = boss.getDisplayName().getString();
        int bossNameWidth = font.width(bossName);
        guiGraphics.drawString(font, bossName,
                x + (width - bossNameWidth) / 2,
                y - 30,
                0xFFFFFF, true);
    }
}