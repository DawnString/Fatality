package cn.dawnstring.fatality.client;

import cn.dawnstring.fatality.entity.BaseBoss;
import cn.dawnstring.fatality.entity.BossArenaManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Boss战斗场地边界渲染器
 * 在AFTER_SOLID_BLOCKS阶段渲染Boss战斗场地的边界线段
 */
@OnlyIn(Dist.CLIENT)
public class BossArenaBoundaryRenderer {

    private static final Minecraft minecraft = Minecraft.getInstance();

    @SubscribeEvent
    public void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS) {
            return;
        }

        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();

        // 获取玩家周围的Boss实体（100格范围内）
        AABB searchArea = minecraft.player.getBoundingBox().inflate(100.0);
        var bosses = minecraft.level.getEntitiesOfClass(
                BaseBoss.class,
                searchArea,
                Entity::isAlive
        );

        if (!bosses.isEmpty()) {
            poseStack.pushPose();

            try {
                // 为每个Boss渲染战斗场地边界
                for (BaseBoss boss : bosses) {
                    BossArenaManager arenaManager = boss.getArenaManager();
                    if (arenaManager != null && arenaManager.isArenaActive()) {
                        BossArenaBoundaryRendererHelper.renderArenaBoundary(poseStack, bufferSource, arenaManager);
                    }
                }

                bufferSource.endBatch();
            } finally {
                poseStack.popPose();
            }
        }
    }
}