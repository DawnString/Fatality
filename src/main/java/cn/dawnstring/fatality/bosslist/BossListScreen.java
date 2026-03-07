package cn.dawnstring.fatality.bosslist;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;
import cn.dawnstring.fatality.entity.boss.BossList;

import java.util.ArrayList;
import java.util.List;

public class BossListScreen extends Screen {
    private static final ResourceLocation BOOK_BACKGROUND = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/book.png");
    private static final int BACKGROUND_WIDTH = 192;
    private static final int BACKGROUND_HEIGHT = 192;
    
    private int leftPos;
    private int topPos;
    private List<BossButton> bossButtons;
    private int scrollOffset = 0;
    private int maxVisibleBosses = 6;
    private boolean isScrolling = false;
    
    public BossListScreen() {
        super(Component.translatable("gui.fatality.boss_list.title"));
    }
    
    @Override
    protected void init() {
        super.init();
        
        this.leftPos = (this.width - BACKGROUND_WIDTH) / 2;
        this.topPos = (this.height - BACKGROUND_HEIGHT) / 2;
        
        this.bossButtons = new ArrayList<>();
        
        rebuildButtons();
        
        // 添加关闭按钮
        this.addRenderableWidget(Button.builder(Component.translatable("gui.fatality.boss_list.close"), button -> {
            this.onClose();
        }).bounds(this.leftPos + BACKGROUND_WIDTH - 60, this.topPos + BACKGROUND_HEIGHT - 25, 50, 20).build());
    }
    
    private void rebuildButtons() {
        // 移除所有boss按钮
        for (BossButton button : bossButtons) {
            this.removeWidget(button);
        }
        bossButtons.clear();
        
        // 重新创建boss按钮
        BossList[] bosses = BossList.values();
        int buttonY = this.topPos + 30;
        int buttonWidth = 140;
        int buttonHeight = 20;
        int buttonSpacing = 5;
        
        for (int i = 0; i < bosses.length; i++) {
            if (i >= scrollOffset && i < scrollOffset + maxVisibleBosses) {
                BossList boss = bosses[i];
                int displayIndex = i - scrollOffset;
                int buttonX = this.leftPos + 26;
                int buttonYPos = buttonY + displayIndex * (buttonHeight + buttonSpacing);
                
                BossButton button = new BossButton(
                    buttonX, buttonYPos, buttonWidth, buttonHeight,
                    Component.translatable("entity.fatality." + boss.name().toLowerCase()),
                    boss,
                    btn -> {
                        this.minecraft.setScreen(new BossDetailScreen(boss));
                    }
                );
                
                this.addRenderableWidget(button);
                bossButtons.add(button);
            }
        }
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 渲染书的背景
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BOOK_BACKGROUND);
        guiGraphics.blit(BOOK_BACKGROUND, this.leftPos, this.topPos, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, 256, 256);
        
        // 渲染标题
        guiGraphics.drawString(this.font, Component.translatable("gui.fatality.boss_list.title").withStyle(ChatFormatting.BOLD), 
            this.leftPos + 50, this.topPos + 10, 0x000000, false);
        
        // 渲染滚动条
        renderScrollBar(guiGraphics);
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
    
    private void renderScrollBar(GuiGraphics guiGraphics) {
        BossList[] bosses = BossList.values();
        if (bosses.length <= maxVisibleBosses) {
            return; // 不需要滚动条
        }
        
        int scrollBarX = this.leftPos + BACKGROUND_WIDTH - 10;
        int scrollBarY = this.topPos + 30;
        int scrollBarWidth = 6;
        int scrollBarHeight = BACKGROUND_HEIGHT - 60;
        
        // 滚动条背景
        guiGraphics.fill(scrollBarX, scrollBarY, scrollBarX + scrollBarWidth, scrollBarY + scrollBarHeight, 0x80000000);
        
        // 滚动条滑块
        float scrollPercentage = (float) scrollOffset / (bosses.length - maxVisibleBosses);
        int sliderHeight = Math.max(20, (int) (scrollBarHeight * (float) maxVisibleBosses / bosses.length));
        int sliderY = scrollBarY + (int) (scrollPercentage * (scrollBarHeight - sliderHeight));
        
        guiGraphics.fill(scrollBarX, sliderY, scrollBarX + scrollBarWidth, sliderY + sliderHeight, 0xFF808080);
        
        // 滚动条边框
        guiGraphics.fill(scrollBarX, sliderY, scrollBarX + scrollBarWidth, sliderY + 1, 0xFFFFFFFF);
        guiGraphics.fill(scrollBarX, sliderY + sliderHeight - 1, scrollBarX + scrollBarWidth, sliderY + sliderHeight, 0xFFFFFFFF);
        guiGraphics.fill(scrollBarX, sliderY, scrollBarX + 1, sliderY + sliderHeight, 0xFFFFFFFF);
        guiGraphics.fill(scrollBarX + scrollBarWidth - 1, sliderY, scrollBarX + scrollBarWidth, sliderY + sliderHeight, 0xFFFFFFFF);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        BossList[] bosses = BossList.values();
        if (bosses.length <= maxVisibleBosses) {
            return false; // 不需要滚动
        }
        
        if (scrollDelta > 0) {
            // 向上滚动
            if (scrollOffset > 0) {
                scrollOffset--;
                rebuildButtons();
                return true;
            }
        } else if (scrollDelta < 0) {
            // 向下滚动
            if (scrollOffset < bosses.length - maxVisibleBosses) {
                scrollOffset++;
                rebuildButtons();
                return true;
            }
        }
        
        return super.mouseScrolled(mouseX, mouseY, scrollDelta);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        BossList[] bosses = BossList.values();
        if (bosses.length <= maxVisibleBosses) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        
        int scrollBarX = this.leftPos + BACKGROUND_WIDTH - 10;
        int scrollBarY = this.topPos + 30;
        int scrollBarWidth = 6;
        int scrollBarHeight = BACKGROUND_HEIGHT - 60;
        
        // 检查是否点击了滚动条区域
        if (mouseX >= scrollBarX && mouseX <= scrollBarX + scrollBarWidth &&
            mouseY >= scrollBarY && mouseY <= scrollBarY + scrollBarHeight) {
            isScrolling = true;
            updateScrollFromMouse(mouseY);
            return true;
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isScrolling) {
            updateScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isScrolling) {
            isScrolling = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    private void updateScrollFromMouse(double mouseY) {
        BossList[] bosses = BossList.values();
        if (bosses.length <= maxVisibleBosses) {
            return;
        }
        
        int scrollBarY = this.topPos + 30;
        int scrollBarHeight = BACKGROUND_HEIGHT - 60;
        
        float scrollPercentage = (float) Math.max(0, Math.min(1, (mouseY - scrollBarY) / scrollBarHeight));
        int newScrollOffset = (int) (scrollPercentage * (bosses.length - maxVisibleBosses));
        
        if (newScrollOffset != scrollOffset) {
            scrollOffset = newScrollOffset;
            rebuildButtons();
        }
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    private static class BossButton extends Button {
        private final BossList boss;
        
        public BossButton(int x, int y, int width, int height, Component message, BossList boss, OnPress onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
            this.boss = boss;
        }
        
        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            Minecraft minecraft = Minecraft.getInstance();
            
            // 根据boss是否被击败来设置按钮颜色
            boolean isDefeated = minecraft.player != null && BossProgressManager.isBossDefeated(minecraft.player, boss);
            int color = isDefeated ? 0x00FF00 : 0xFF0000;
            
            // 渲染按钮背景
            guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 
                isDefeated ? 0x3300FF00 : 0x33FF0000);
            
            // 渲染按钮边框
            guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + 1, color);
            guiGraphics.fill(this.getX(), this.getY() + this.height - 1, this.getX() + this.width, this.getY() + this.height, color);
            guiGraphics.fill(this.getX(), this.getY(), this.getX() + 1, this.getY() + this.height, color);
            guiGraphics.fill(this.getX() + this.width - 1, this.getY(), this.getX() + this.width, this.getY() + this.height, color);
            
            // 渲染文本
            int textColor = isDefeated ? 0x00AA00 : 0xAA0000;
            guiGraphics.drawString(minecraft.font, this.getMessage(), 
                this.getX() + 5, this.getY() + (this.height - 8) / 2, textColor, false);
        }
    }
}