package cn.dawnstring.fatality.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.ChatFormatting;
import cn.dawnstring.fatality.system.AttributeSystem;
import cn.dawnstring.fatality.system.ManaSystem;

public class AttributePanelScreen extends Screen {
    private final Player player;
    private int backgroundWidth = 280;
    private int backgroundHeight = 300;
    private int leftPos;
    private int topPos;

    public AttributePanelScreen() {
        super(Component.literal("属性面板"));
        this.player = Minecraft.getInstance().player;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.backgroundWidth) / 2;
        this.topPos = (this.height - this.backgroundHeight) / 2;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        // 手动绘制圆角黑边背景
        drawRoundedBackground(guiGraphics);

        // 绘制标题（向上移动）
        Component title = Component.literal("玩家属性").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD);
        int titleWidth = this.font.width(title);
        guiGraphics.drawString(this.font, title, leftPos + (backgroundWidth - titleWidth) / 2, topPos + 5, 0xFFFFFF, true);

        // 绘制属性信息
        drawAttributes(guiGraphics);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    /**
     * 绘制圆角黑边背景
     */
    private void drawRoundedBackground(GuiGraphics guiGraphics) {
        int backgroundColor = 0x80000000; // 半透明黑色背景
        int borderColor = 0xFF000000;     // 黑色边框
        int cornerRadius = 8;              // 圆角半径

        // 绘制背景
        guiGraphics.fill(leftPos, topPos, leftPos + backgroundWidth, topPos + backgroundHeight, backgroundColor);

        // 绘制圆角边框
        // 上边框
        guiGraphics.fill(leftPos + cornerRadius, topPos, leftPos + backgroundWidth - cornerRadius, topPos + 1, borderColor);
        // 下边框
        guiGraphics.fill(leftPos + cornerRadius, topPos + backgroundHeight - 1, leftPos + backgroundWidth - cornerRadius, topPos + backgroundHeight, borderColor);
        // 左边框
        guiGraphics.fill(leftPos, topPos + cornerRadius, leftPos + 1, topPos + backgroundHeight - cornerRadius, borderColor);
        // 右边框
        guiGraphics.fill(leftPos + backgroundWidth - 1, topPos + cornerRadius, leftPos + backgroundWidth, topPos + backgroundHeight - cornerRadius, borderColor);

        // 绘制四个圆角
        drawRoundedCorner(guiGraphics, leftPos, topPos, cornerRadius, borderColor, true, true);   // 左上角
        drawRoundedCorner(guiGraphics, leftPos + backgroundWidth - cornerRadius, topPos, cornerRadius, borderColor, false, true);  // 右上角
        drawRoundedCorner(guiGraphics, leftPos, topPos + backgroundHeight - cornerRadius, cornerRadius, borderColor, true, false); // 左下角
        drawRoundedCorner(guiGraphics, leftPos + backgroundWidth - cornerRadius, topPos + backgroundHeight - cornerRadius, cornerRadius, borderColor, false, false); // 右下角
    }

    /**
     * 绘制圆角
     */
    private void drawRoundedCorner(GuiGraphics guiGraphics, int x, int y, int radius, int color, boolean left, boolean top) {
        for (int i = 0; i < radius; i++) {
            for (int j = 0; j < radius; j++) {
                if (i + j < radius) {
                    int drawX = left ? x + i : x + radius - 1 - i;
                    int drawY = top ? y + j : y + radius - 1 - j;
                    guiGraphics.fill(drawX, drawY, drawX + 1, drawY + 1, color);
                }
            }
        }
    }

    private void drawAttributes(GuiGraphics guiGraphics)
    {
        // 整体向上移动10像素
        int startX = leftPos + 15;
        int startY = topPos + 15;  // 从25改为15，向上移动10像素
        int lineHeight = 12;
        int sectionSpacing = 3;

        // 基础属性
        drawScaledText(guiGraphics, "基础属性:", startX, startY, ChatFormatting.BOLD, ChatFormatting.YELLOW);

        // 生命值
        float currentHealth = player.getHealth();
        float maxHealth = player.getMaxHealth();
        String healthText = String.format("❤ 生命值: %.0f/%.0f", currentHealth, maxHealth);
        drawScaledText(guiGraphics, healthText, startX + 8, startY + lineHeight, getHealthColor(currentHealth, maxHealth));

        // 回血速度
        float healthRegen = AttributeSystem.getHealthRegenerationRate(player);
        String healthRegenText = String.format("💚 回血速度: %.1f/秒", healthRegen);
        drawScaledText(guiGraphics, healthRegenText, startX + 8, startY + lineHeight * 2, ChatFormatting.GREEN);

        // 魔法值
        float currentMana = ManaSystem.getCurrentMana(player);
        float maxMana = ManaSystem.getMaxMana(player);
        String manaText = String.format("✨ 魔法值: %.0f/%.0f", currentMana, maxMana);
        drawScaledText(guiGraphics, manaText, startX + 8, startY + lineHeight * 3, getManaColor(currentMana, maxMana));

        // 回蓝速度
        float manaRegen = AttributeSystem.getManaRegenerationRate(player);
        String manaRegenText = String.format("💙 回蓝速度: %.1f/秒", manaRegen);
        drawScaledText(guiGraphics, manaRegenText, startX + 8, startY + lineHeight * 4, ChatFormatting.BLUE);

        // 攻击属性
        int attackSectionY = startY + lineHeight * 5 + sectionSpacing;
        drawScaledText(guiGraphics, "攻击属性:", startX, attackSectionY, ChatFormatting.BOLD, ChatFormatting.RED);

        // 攻击力
        float attackDamage = AttributeSystem.getAttackDamage(player);
        String attackText = String.format("⚔ 攻击力: %.1f", attackDamage);
        drawScaledText(guiGraphics, attackText, startX + 8, attackSectionY + lineHeight, ChatFormatting.WHITE);

        // 攻击速度
        float attackSpeed = AttributeSystem.getAttackSpeed(player);
        String speedText = String.format("⚡ 攻击速度: %.1f", attackSpeed);
        drawScaledText(guiGraphics, speedText, startX + 8, attackSectionY + lineHeight * 2, ChatFormatting.WHITE);

        // 暴击率
        float critChance = AttributeSystem.getCritChance(player);
        String critText = String.format("💥 暴击率: %.1f%%", critChance * 100);
        drawScaledText(guiGraphics, critText, startX + 8, attackSectionY + lineHeight * 3, ChatFormatting.WHITE);

        // 暴击伤害（分开显示三种类型）
        float meleeCritDamage = AttributeSystem.getMeleeCritDamage(player);
        String meleeCritText = String.format("🗡 近战暴伤: %.1f%%", meleeCritDamage * 100);
        drawScaledText(guiGraphics, meleeCritText, startX + 8, attackSectionY + lineHeight * 4, ChatFormatting.RED);

        float rangedCritDamage = AttributeSystem.getRangedCritDamage(player);
        String rangedCritText = String.format("🏹 远程暴伤: %.1f%%", rangedCritDamage * 100);
        drawScaledText(guiGraphics, rangedCritText, startX + 8, attackSectionY + lineHeight * 5, ChatFormatting.GREEN);

        float magicCritDamage = AttributeSystem.getMagicCritDamage(player);
        String magicCritText = String.format("🔮 魔法暴伤: %.1f%%", magicCritDamage * 100);
        drawScaledText(guiGraphics, magicCritText, startX + 8, attackSectionY + lineHeight * 6, ChatFormatting.BLUE);

        // 防御属性
        int defenseSectionY = attackSectionY + lineHeight * 7 + sectionSpacing;
        drawScaledText(guiGraphics, "防御属性:", startX, defenseSectionY, ChatFormatting.BOLD, ChatFormatting.BLUE);

        // 减伤百分比（替代原来的防御力显示）
        float damageReduction = AttributeSystem.getDamageReductionPercentage(player);
        String defenseText = String.format("🛡 减伤: %.1f%%", damageReduction * 100);
        drawScaledText(guiGraphics, defenseText, startX + 8, defenseSectionY + lineHeight, ChatFormatting.WHITE);

        // 移动速度
        float movementSpeed = AttributeSystem.getMovementSpeed(player);
        String speedText2 = String.format("🏃 移动速度: %.1f", movementSpeed);
        drawScaledText(guiGraphics, speedText2, startX + 8, defenseSectionY + lineHeight * 2, ChatFormatting.WHITE);

        // 其他属性
        int otherSectionY = defenseSectionY + lineHeight * 3 + sectionSpacing;
        drawScaledText(guiGraphics, "其他属性:", startX, otherSectionY, ChatFormatting.BOLD, ChatFormatting.GREEN);

        /**
        // 幸运值
        float luck = AttributeSystem.getLuck(player);
        String luckText = String.format("🍀 幸运值: %.1f", luck);
        drawScaledText(guiGraphics, luckText, startX + 8, otherSectionY + lineHeight, ChatFormatting.WHITE);
         */

        // 经验等级
        int experienceLevel = player.experienceLevel;
        String expText = String.format("⭐ 经验等级: %d", experienceLevel);
        drawScaledText(guiGraphics, expText, startX + 8, otherSectionY + lineHeight * 2, ChatFormatting.WHITE);

        // 状态属性（去除饥饿值，只保留盔甲值）
        int extraSectionY = otherSectionY + lineHeight * 3 + sectionSpacing;
        drawScaledText(guiGraphics, "状态属性:", startX, extraSectionY, ChatFormatting.BOLD, ChatFormatting.LIGHT_PURPLE);

        // 盔甲值
        int armorValue = player.getArmorValue();
        String armorText = String.format("🛡 盔甲值: %d", armorValue);
        drawScaledText(guiGraphics, armorText, startX + 8, extraSectionY + lineHeight, ChatFormatting.WHITE);

        // 右侧属性说明（更新说明列表）
        int rightColumnX = leftPos + backgroundWidth / 2 + 10;
        int descriptionY = startY;
        drawScaledText(guiGraphics, "属性说明:", rightColumnX, descriptionY, ChatFormatting.BOLD, ChatFormatting.GRAY);

        // 简化的属性描述列表（更新为新的属性）
        String[] descriptions = {
                "❤ 生命值: 玩家生命",
                "💚 回血速度: 生命恢复",
                "✨ 魔法值: 技能消耗",
                "💙 回蓝速度: 魔法恢复",
                "⚔ 攻击力: 基础伤害",
                "⚡ 攻速: 攻击频率",
                "💥 暴击率: 暴击概率",
                "🗡 近战暴伤: 近战暴击",
                "🏹 远程暴伤: 远程暴击",
                "🔮 魔法暴伤: 魔法暴击",
                "🛡 减伤: 伤害减免",
                "🏃 移速: 移动速度",
                "⭐ 等级: 经验等级"
        };

        for (int i = 0; i < descriptions.length; i++) {
            drawScaledText(guiGraphics, descriptions[i], rightColumnX + 8, descriptionY + lineHeight * (i + 1), ChatFormatting.GRAY);
        }
    }

    /**
     * 绘制缩放文本 - 使用PoseStack来实现字体缩放效果
     */
    private void drawScaledText(GuiGraphics guiGraphics, String text, int x, int y, ChatFormatting... formats) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        // 缩放因子 - 使用较小的字体
        float scale = 0.8f;
        poseStack.scale(scale, scale, scale);

        // 计算缩放后的位置
        int scaledX = (int)(x / scale);
        int scaledY = (int)(y / scale);

        // 直接绘制文本，不使用样式
        guiGraphics.drawString(this.font, text, scaledX, scaledY, 0xFFFFFF, true);

        poseStack.popPose();
    }

    /**
     * 绘制带格式的缩放文本（重载方法）
     */
    private void drawScaledText(GuiGraphics guiGraphics, String text, int x, int y, ChatFormatting format) {
        drawScaledText(guiGraphics, text, x, y);
    }

    private ChatFormatting getHealthColor(float currentHealth, float maxHealth) {
        float percentage = currentHealth / maxHealth;
        if (percentage > 0.75f) return ChatFormatting.GREEN;
        if (percentage > 0.25f) return ChatFormatting.YELLOW;
        return ChatFormatting.RED;
    }

    private ChatFormatting getManaColor(float currentMana, float maxMana) {
        float percentage = currentMana / maxMana;
        if (percentage > 0.75f) return ChatFormatting.DARK_BLUE;
        if (percentage > 0.25f) return ChatFormatting.BLUE;
        return ChatFormatting.AQUA;
    }

    private ChatFormatting getFoodColor(int foodLevel) {
        if (foodLevel > 15) return ChatFormatting.GREEN;
        if (foodLevel > 5) return ChatFormatting.YELLOW;
        return ChatFormatting.RED;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC键
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}