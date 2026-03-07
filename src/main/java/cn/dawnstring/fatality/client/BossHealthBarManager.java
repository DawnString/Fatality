package cn.dawnstring.fatality.client;

import cn.dawnstring.fatality.entity.BaseBoss;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * 统一的Boss血条管理器 - 整合所有Boss血条渲染逻辑
 */
@Mod.EventBusSubscriber(modid = "fatality")
public class BossHealthBarManager {

    // 配置参数
    private static final int MAX_RENDER_DISTANCE = 64; // 最大渲染距离（格）
    private static final int BOSS_HEALTH_BAR_Y_OFFSET = 20; // Boss血条Y轴偏移
    private static final int BOSS_HEALTH_BAR_SPACING = 10; // Boss血条间距
    private static final int COMBAT_BOSS_Y_OFFSET = 40; // 战斗状态Boss血条Y轴偏移

    /**
     * 主渲染方法 - 处理所有Boss血条渲染
     */
    @SubscribeEvent
    public static void onRenderGuiOverlayPost(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = event.getWindow().getGuiScaledWidth();

        // 安全检查
        if (player == null || minecraft.screen != null) {
            return;
        }

        // 渲染附近的所有Boss血条
        renderNearbyBossHealthBars(guiGraphics, player, screenWidth);

        // 渲染战斗状态的Boss血条（如果有）
        renderCombatBossHealthBar(guiGraphics, player, screenWidth);
    }

    /**
     * 渲染玩家附近的所有Boss血条
     */
    private static void renderNearbyBossHealthBars(GuiGraphics guiGraphics, Player player, int screenWidth) {
        // 获取玩家周围的Boss实体
        List<LivingEntity> nearbyBosses = player.level().getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(MAX_RENDER_DISTANCE),
                entity -> entity instanceof BaseBoss && entity.isAlive()
        );

        int yOffset = BOSS_HEALTH_BAR_Y_OFFSET;

        for (LivingEntity boss : nearbyBosses) {
            if (boss instanceof BaseBoss baseBoss && baseBoss.shouldShowCustomHealthBar()) {
                // 使用UniversalBossHealthBarRenderer渲染贴图血条
                UniversalBossHealthBarRenderer.renderBossHealthBar(guiGraphics, baseBoss, screenWidth, yOffset);
                yOffset += BOSS_HEALTH_BAR_SPACING + baseBoss.getScaledDecorationHeight();
            }
        }
    }

    /**
     * 渲染战斗状态的Boss血条（玩家正在攻击或被攻击的Boss）
     */
    private static void renderCombatBossHealthBar(GuiGraphics guiGraphics, Player player, int screenWidth) {
        // 检查玩家是否正在与Boss战斗
        Entity target = player.getLastHurtByMob();
        Entity attackingTarget = player.getLastHurtMob();

        BaseBoss combatBoss = null;

        // 优先显示攻击玩家的Boss
        if (target instanceof BaseBoss boss && boss.isAlive() && boss.shouldShowCustomHealthBar()) {
            combatBoss = boss;
        }
        // 如果没有被攻击，显示玩家正在攻击的Boss
        else if (attackingTarget instanceof BaseBoss boss && boss.isAlive() && boss.shouldShowCustomHealthBar()) {
            combatBoss = boss;
        }

        if (combatBoss != null) {
            // 在固定位置渲染战斗Boss血条
            UniversalBossHealthBarRenderer.renderBossHealthBar(guiGraphics, combatBoss, screenWidth, COMBAT_BOSS_Y_OFFSET);
        }
    }

    /**
     * 检查Boss是否在渲染范围内
     */
    private static boolean isBossInRange(Player player, BaseBoss boss) {
        return player.distanceTo(boss) <= MAX_RENDER_DISTANCE;
    }
}
