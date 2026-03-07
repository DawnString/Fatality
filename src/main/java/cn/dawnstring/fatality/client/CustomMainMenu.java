package cn.dawnstring.fatality.client;

import cn.dawnstring.fatality.Fatality;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class CustomMainMenu extends Screen {

    // 自定义资源路径
    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath("fatality", "textures/gui/main_menu_background.png");
    private static final ResourceLocation BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath("fatality", "textures/gui/main_menu_button.png");
    private static final ResourceLocation TITLE_TEXTURE = ResourceLocation.fromNamespaceAndPath("fatality", "textures/gui/main_menu_title.png");

    // 背景音乐资源
    private static final ResourceLocation MAIN_MENU_MUSIC = ResourceLocation.fromNamespaceAndPath("fatality", "main_menu_music");

    // 调整按钮尺寸（更小更合适）
    private static final int BUTTON_WIDTH = 80;  // 从200减小到120
    private static final int BUTTON_HEIGHT = 20;  // 从40减小到24
    private static final int BUTTON_SPACING = 8;  // 从10减小到8

    // 标题图片尺寸（根据实际图片调整，不再拉伸）
    private static final int TITLE_WIDTH = 256;   // 从400减小到256
    private static final int TITLE_HEIGHT = 64;   // 从100减小到64

    // 背景音乐实例
    @Nullable
    private SimpleSoundInstance backgroundMusic;

    public CustomMainMenu() {
        super(Component.literal("Fatality Main Menu"));
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2 - 150;
        int startY = this.height / 2 - 20;  // 从-50调整到-40，因为按钮变小了

        // 创建自定义图片按钮
        this.addRenderableWidget(new ImageButton(centerX - BUTTON_WIDTH / 2, startY, BUTTON_WIDTH, BUTTON_HEIGHT,
                Component.literal("单人游戏"), button -> {
            Minecraft.getInstance().setScreen(new net.minecraft.client.gui.screens.worldselection.SelectWorldScreen(this));
        }));

        this.addRenderableWidget(new ImageButton(centerX - BUTTON_WIDTH / 2, startY + BUTTON_HEIGHT + BUTTON_SPACING,
                BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal("多人游戏"), button -> {
            Minecraft.getInstance().setScreen(new net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen(this));
        }));

        this.addRenderableWidget(new ImageButton(centerX - BUTTON_WIDTH / 2, startY + (BUTTON_HEIGHT + BUTTON_SPACING) * 2,
                BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal("选项"), button -> {
            Minecraft.getInstance().setScreen(new net.minecraft.client.gui.screens.OptionsScreen(this, Minecraft.getInstance().options));
        }));

        this.addRenderableWidget(new ImageButton(centerX - BUTTON_WIDTH / 2, startY + (BUTTON_HEIGHT + BUTTON_SPACING) * 3,
                BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal("退出游戏"), button -> {
            Minecraft.getInstance().stop();
        }));

        // 播放背景音乐
        playBackgroundMusic();
    }

    @Override
    public void onClose() {
        super.onClose();
        stopBackgroundMusic();
    }

    @Override
    public void removed() {
        super.removed();
        stopBackgroundMusic();
    }

    /**
     * 播放背景音乐
     */
    private void playBackgroundMusic() {
        if (backgroundMusic == null) {
            SoundManager soundManager = Minecraft.getInstance().getSoundManager();

            // 只停止原版音乐，不停止其他音效
            soundManager.stop(null, SoundSource.MUSIC);

            try {
                // 使用已注册的声音事件
                backgroundMusic = SimpleSoundInstance.forMusic(
                        Fatality.MAIN_MENU_MUSIC.get() // 使用注册的声音事件
                );

                // 播放音乐（循环播放）
                soundManager.play(backgroundMusic);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 停止背景音乐
     */
    private void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            SoundManager soundManager = Minecraft.getInstance().getSoundManager();
            soundManager.stop(backgroundMusic);
            backgroundMusic = null;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 渲染自定义背景
        this.renderBackground(guiGraphics);

        // 渲染版本信息（使用项目中的实际版本号）
        String version = "v" + cn.dawnstring.fatality.Fatality.VERSION;
        guiGraphics.drawString(this.font, Component.literal(version),
                this.width - 30, this.height - 20, 0x888888, true);

        String author = "DawnString";
        guiGraphics.drawString(this.font, Component.literal(author),
                10, this.height - 20, 0x888888, true);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics) {
        // 设置渲染状态
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // 尝试渲染自定义背景，如果不存在则使用默认背景
        if (Minecraft.getInstance().getResourceManager().getResource(BACKGROUND_TEXTURE).isPresent()) {
            RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
            // 修复背景拉伸：使用正确的纹理尺寸
            guiGraphics.blit(BACKGROUND_TEXTURE, 0, 0, 0, 0, this.width, this.height, this.width, this.height);
        } else {
            // 使用默认的渐变背景
            super.renderBackground(guiGraphics);
        }
    }

    /**
     * 渲染标题图片（修复拉伸问题）
     */
    /**
    private void renderTitle(GuiGraphics guiGraphics) {
        // 设置渲染状态
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // 检查标题图片是否存在
        if (Minecraft.getInstance().getResourceManager().getResource(TITLE_TEXTURE).isPresent()) {
            RenderSystem.setShaderTexture(0, TITLE_TEXTURE);

            // 计算标题位置（屏幕顶部居中）
            int titleX = (this.width - TITLE_WIDTH) / 2;
            int titleY = 20; // 从30调整到20，因为标题变小了

            // 修复标题图片拉伸：使用原始尺寸渲染，不拉伸
            guiGraphics.blit(TITLE_TEXTURE, titleX, titleY, 0, 0, TITLE_WIDTH, TITLE_HEIGHT, TITLE_WIDTH, TITLE_HEIGHT);
        } else {
            // 如果标题图片不存在，回退到文字标题
            guiGraphics.drawString(this.font, Component.literal("Fatality"),
                    this.width / 2 - this.font.width("Fatality") / 2, 40, 0xFFFFFF, true); // 从50调整到40
        }
    }
     **/

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /**
     * 自定义图片按钮类（优化渲染）
     */
    private static class ImageButton extends AbstractWidget {
        private final OnPress onPress;

        public ImageButton(int x, int y, int width, int height, Component message, OnPress onPress) {
            super(x, y, width, height, message);
            this.onPress = onPress;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // 设置渲染状态
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            // 检查按钮图片是否存在
            if (Minecraft.getInstance().getResourceManager().getResource(BUTTON_TEXTURE).isPresent()) {
                RenderSystem.setShaderTexture(0, BUTTON_TEXTURE);

                // 移除悬停状态判断，始终使用第一行纹理
                int textureY = 0;

                // 修复按钮图片拉伸：使用原始尺寸渲染
                guiGraphics.blit(BUTTON_TEXTURE,
                        this.getX(), this.getY(),
                        0, textureY,
                        this.getWidth(), this.getHeight(),
                        this.getWidth(), this.getHeight()); // 移除*2，因为不再需要悬停状态纹理

                // 渲染按钮文字（居中显示，移除颜色变化）
                int textColor = 0xFFFFFF; // 固定白色，移除悬停颜色变化
                guiGraphics.drawCenteredString(Minecraft.getInstance().font, this.getMessage(),
                        this.getX() + this.getWidth() / 2,
                        this.getY() + (this.getHeight() - Minecraft.getInstance().font.lineHeight) / 2,
                        textColor);
            } else {
                // 如果按钮图片不存在，回退到默认按钮
                Button.builder(this.getMessage(), (Button.OnPress) this.onPress)
                        .bounds(this.getX(), this.getY(), this.getWidth(), this.getHeight())
                        .build()
                        .render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            this.onPress.onPress(this);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }

        @FunctionalInterface
        public interface OnPress {
            void onPress(ImageButton button);
        }
    }
}