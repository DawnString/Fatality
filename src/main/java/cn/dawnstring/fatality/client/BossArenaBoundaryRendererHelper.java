package cn.dawnstring.fatality.client;

import cn.dawnstring.fatality.entity.BossArenaManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Boss战斗场地边界渲染辅助类（客户端专用）
 * 负责渲染Boss战斗场地的边界线段
 */
@OnlyIn(Dist.CLIENT)
public class BossArenaBoundaryRendererHelper {

    /**
     * 渲染边界线段（客户端专用）
     * @param poseStack 姿势堆栈
     * @param buffer 缓冲区
     * @param center 场地中心坐标
     * @param halfSize 场地半边长
     * @param isXAxis 是否为X轴方向的线段
     */
    @OnlyIn(Dist.CLIENT)
    private static void renderBoundaryLine(PoseStack poseStack, MultiBufferSource buffer,
                                           BlockPos center, int halfSize, boolean isXAxis) {
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.LINES);

        for (int i = -halfSize; i <= halfSize; i += 2) { // 每2格一个线段
            double x1, z1, x2, z2;

            if (isXAxis) {
                x1 = center.getX() + i;
                z1 = center.getZ() - halfSize;
                x2 = center.getX() + i;
                z2 = center.getZ() + halfSize;
            } else {
                x1 = center.getX() - halfSize;
                z1 = center.getZ() + i;
                x2 = center.getX() + halfSize;
                z2 = center.getZ() + i;
            }

            // 渲染线段
            LevelRenderer.renderLineBox(poseStack, vertexConsumer,
                    x1, center.getY(), z1, x2, center.getY() + 0.1, z2,
                    1.0F, 0.0F, 0.0F, 1.0F);
        }
    }

    /**
     * 渲染整个战斗场地边界（客户端专用）
     * @param poseStack 姿势堆栈
     * @param buffer 缓冲区
     * @param arenaManager 战斗场地管理器
     */
    @OnlyIn(Dist.CLIENT)
    public static void renderArenaBoundary(PoseStack poseStack, MultiBufferSource buffer, BossArenaManager arenaManager) {
        if (!arenaManager.isArenaActive() || arenaManager.getArenaCenter() == null) return;

        int halfSize = arenaManager.getArenaSize() / 2;
        BlockPos center = arenaManager.getArenaCenter();

        // 渲染X轴方向的线段
        renderBoundaryLine(poseStack, buffer, center, halfSize, true);
        // 渲染Z轴方向的线段
        renderBoundaryLine(poseStack, buffer, center, halfSize, false);
    }
}