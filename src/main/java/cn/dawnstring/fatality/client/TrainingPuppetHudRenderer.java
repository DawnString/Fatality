package cn.dawnstring.fatality.client;

import cn.dawnstring.fatality.Fatality;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 训练人偶DPS数据HUD渲染器
 * 在屏幕右侧显示训练人偶的伤害数据
 */
@Mod.EventBusSubscriber(modid = Fatality.MODID)
public class TrainingPuppetHudRenderer {
    
    // DPS数据结构
    private static class DpsData {
        double dps;
        double totalDamage;
        double lastHitDamage;
        double highestHit;
        double time;
        double healthPercentage;
        long lastUpdateTime;
        
        DpsData() {
            this.dps = 0;
            this.totalDamage = 0;
            this.lastHitDamage = 0;
            this.highestHit = 0;
            this.time = 0;
            this.healthPercentage = 100;
            this.lastUpdateTime = 0;
        }
        
        void updateFromString(String data) {
            try {
                // 解析格式: DPS:%.1f|总伤害:%.1f|时间:%.1f|当前单次:%.1f|百分比:%.1f|最高伤害:%.1f
                // 注意：数据可能包含颜色代码，需要清理
                String cleanData = data.replaceAll("§[0-9a-fk-or]", "").trim();
                
                Pattern pattern = Pattern.compile("DPS:([0-9.]+)\\|总伤害:([0-9.]+)\\|时间:([0-9.]+)\\|当前单次:([0-9.]+)\\|百分比:([0-9.]+)\\|最高伤害:([0-9.]+)");
                Matcher matcher = pattern.matcher(cleanData);
                
                if (matcher.find()) {
                    this.dps = Double.parseDouble(matcher.group(1));
                    this.totalDamage = Double.parseDouble(matcher.group(2));
                    this.time = Double.parseDouble(matcher.group(3));
                    this.lastHitDamage = Double.parseDouble(matcher.group(4));
                    this.healthPercentage = Double.parseDouble(matcher.group(5));
                    this.highestHit = Double.parseDouble(matcher.group(6));
                    this.lastUpdateTime = System.currentTimeMillis();
                } else {
                    System.err.println("DPS数据格式不匹配: " + cleanData);
                }
            } catch (Exception e) {
                // 解析失败时保持原有数据
                System.err.println("解析DPS数据失败: " + data + " - " + e.getMessage());
            }
        }
        
        boolean isValid() {
            // 数据在5秒内有效
            return System.currentTimeMillis() - lastUpdateTime < 5000;
        }
    }
    
    // 当前显示的DPS数据
    private static final DpsData currentDpsData = new DpsData();
    
    // 配置常量
    private static final class Config {
        static final int PANEL_WIDTH = 180;
        static final int PANEL_HEIGHT = 120;
        static final int MARGIN_RIGHT = 20;
        static final int MARGIN_TOP = 80;
        static final int TEXT_SPACING = 12;
        static final int PANEL_PADDING = 8;
        static final int CORNER_RADIUS = 4;
        
        // 颜色配置
        static final int PANEL_BACKGROUND = 0x80000000; // 半透明黑色
        static final int PANEL_BORDER = 0xFF00FF00;    // 绿色边框
        static final int TEXT_COLOR = 0xFFFFFFFF;      // 白色文字
        static final int HIGHLIGHT_COLOR = 0xFFFFFF00; // 黄色高亮
    }
    
    /**
     * 处理客户端消息，提取DPS数据
     */
    public static void handleDpsMessage(Component message) {
        String text = message.getString();
        if (text.contains("[DPS_DATA]")) {
            // 提取[DPS_DATA]之后的内容
            int startIndex = text.indexOf("[DPS_DATA]") + "[DPS_DATA]".length();
            String data = text.substring(startIndex).trim();
            currentDpsData.updateFromString(data);
        }
    }
    
    /**
     * 监听客户端聊天消息事件
     */
    @SubscribeEvent
    public static void onClientChatReceived(ClientChatReceivedEvent event) {
        Component message = event.getMessage();
        handleDpsMessage(message);
    }
    
    /**
     * 渲染GUI事件处理
     */
    @SubscribeEvent
    public static void onRenderGuiOverlayPost(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() == VanillaGuiOverlay.CHAT_PANEL.type() || 
            event.getOverlay() == VanillaGuiOverlay.PLAYER_LIST.type()) {
            return;
        }
        
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }
        
        // 只在有有效数据时显示
        if (!currentDpsData.isValid()) {
            return;
        }
        
        renderDpsPanel(event.getGuiGraphics(), minecraft);
    }
    
    /**
     * 渲染DPS数据面板
     */
    private static void renderDpsPanel(GuiGraphics guiGraphics, Minecraft minecraft) {
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        
        // 计算面板位置（右上角）
        int panelX = screenWidth - Config.PANEL_WIDTH - Config.MARGIN_RIGHT;
        int panelY = Config.MARGIN_TOP;
        
        // 绘制背景面板
        drawRoundedPanel(guiGraphics, panelX, panelY, Config.PANEL_WIDTH, Config.PANEL_HEIGHT);
        
        // 绘制标题
        int textX = panelX + Config.PANEL_PADDING;
        int textY = panelY + Config.PANEL_PADDING;
        
        guiGraphics.drawString(minecraft.font, 
                Component.literal("训练人偶数据").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), 
                textX, textY, Config.TEXT_COLOR, true);
        
        textY += Config.TEXT_SPACING + 2;
        
        // 绘制分隔线
        guiGraphics.fill(panelX + Config.PANEL_PADDING, textY, 
                        panelX + Config.PANEL_WIDTH - Config.PANEL_PADDING, 
                        textY + 1, Config.PANEL_BORDER);
        
        textY += Config.TEXT_SPACING;
        
        // 绘制数据行
        renderDataLine(guiGraphics, minecraft, "总伤害", 
                String.format("%.1f", currentDpsData.totalDamage), textX, textY);
        textY += Config.TEXT_SPACING;
        
        renderDataLine(guiGraphics, minecraft, "时间", 
                formatTime(currentDpsData.time), textX, textY);
        textY += Config.TEXT_SPACING;
        
        renderDataLine(guiGraphics, minecraft, "DPS", 
                String.format("%.1f", currentDpsData.dps), textX, textY);
        textY += Config.TEXT_SPACING;
        
        renderDataLine(guiGraphics, minecraft, "当前单次", 
                String.format("%.1f", currentDpsData.lastHitDamage), textX, textY);
        textY += Config.TEXT_SPACING;
        
        renderDataLine(guiGraphics, minecraft, "最高伤害", 
                String.format("%.1f", currentDpsData.highestHit), textX, textY);
        textY += Config.TEXT_SPACING;
        
        renderDataLine(guiGraphics, minecraft, "血量", 
                String.format("%.1f%%", currentDpsData.healthPercentage), textX, textY);
    }
    
    /**
     * 绘制圆角面板
     */
    private static void drawRoundedPanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // 绘制背景
        guiGraphics.fill(x, y, x + width, y + height, Config.PANEL_BACKGROUND);
        
        // 绘制边框
        // 上边框
        guiGraphics.fill(x, y, x + width, y + 1, Config.PANEL_BORDER);
        // 下边框
        guiGraphics.fill(x, y + height - 1, x + width, y + height, Config.PANEL_BORDER);
        // 左边框
        guiGraphics.fill(x, y, x + 1, y + height, Config.PANEL_BORDER);
        // 右边框
        guiGraphics.fill(x + width - 1, y, x + width, y + height, Config.PANEL_BORDER);
        
        // 绘制圆角（简化版，使用小方块模拟圆角）
        int cornerSize = Config.CORNER_RADIUS;
        // 左上角
        guiGraphics.fill(x, y, x + cornerSize, y + 1, Config.PANEL_BORDER);
        guiGraphics.fill(x, y, x + 1, y + cornerSize, Config.PANEL_BORDER);
        // 右上角
        guiGraphics.fill(x + width - cornerSize, y, x + width, y + 1, Config.PANEL_BORDER);
        guiGraphics.fill(x + width - 1, y, x + width, y + cornerSize, Config.PANEL_BORDER);
        // 左下角
        guiGraphics.fill(x, y + height - 1, x + cornerSize, y + height, Config.PANEL_BORDER);
        guiGraphics.fill(x, y + height - cornerSize, x + 1, y + height, Config.PANEL_BORDER);
        // 右下角
        guiGraphics.fill(x + width - cornerSize, y + height - 1, x + width, y + height, Config.PANEL_BORDER);
        guiGraphics.fill(x + width - 1, y + height - cornerSize, x + width, y + height, Config.PANEL_BORDER);
    }
    
    /**
     * 渲染数据行
     */
    private static void renderDataLine(GuiGraphics guiGraphics, Minecraft minecraft, 
                                      String label, String value, int x, int y) {
        // 绘制标签
        guiGraphics.drawString(minecraft.font, 
                Component.literal(label + ": ").withStyle(ChatFormatting.WHITE), 
                x, y, Config.TEXT_COLOR, true);
        
        // 绘制值（右对齐）
        int valueWidth = minecraft.font.width(value);
        int valueX = x + Config.PANEL_WIDTH - Config.PANEL_PADDING * 2 - valueWidth;
        
        guiGraphics.drawString(minecraft.font, 
                Component.literal(value).withStyle(ChatFormatting.YELLOW), 
                valueX, y, Config.HIGHLIGHT_COLOR, true);
    }
    
    /**
     * 格式化时间（秒转分:秒）
     */
    private static String formatTime(double seconds) {
        int minutes = (int)(seconds / 60);
        int secs = (int)(seconds % 60);
        return String.format("%d:%02d", minutes, secs);
    }
}