package cn.dawnstring.fatality.client;

import cn.dawnstring.fatality.Fatality;
import cn.dawnstring.fatality.items.BaseWings;
import cn.dawnstring.fatality.items.accessory.HeartOfTheElements;
import cn.dawnstring.fatality.items.accessory.MechanicalHeart;
import cn.dawnstring.fatality.system.ManaSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.HashMap;
import java.util.Map;

/**
 * 优化的自定义HUD渲染器
 */
@Mod.EventBusSubscriber(modid = Fatality.MODID)
public class CustomHudRenderer {

    // ==================== 贴图资源路径 ====================
    private static final ResourceLocation HEALTH_BAR_DECORATION = ResourceLocation.fromNamespaceAndPath("fatality", "textures/gui/health_bar_decoration.png");
    private static final ResourceLocation HEALTH_BAR_MAIN = ResourceLocation.fromNamespaceAndPath("fatality", "textures/gui/health_bar_main.png");
    private static final ResourceLocation FOOD_BAR_DECORATION = ResourceLocation.fromNamespaceAndPath("fatality", "textures/gui/food_bar_decoration.png");
    private static final ResourceLocation FOOD_BAR_MAIN = ResourceLocation.fromNamespaceAndPath("fatality", "textures/gui/food_bar_main.png");
    private static final ResourceLocation FOOD_HUNGER_BAR_DECORATION = ResourceLocation.fromNamespaceAndPath("fatality", "textures/gui/food_hunger_bar_decoration.png");
    private static final ResourceLocation FOOD_HUNGER_BAR_MAIN = ResourceLocation.fromNamespaceAndPath("fatality", "textures/gui/food_hunger_bar_main.png");
    private static final ResourceLocation MANA_BAR_DECORATION = ResourceLocation.fromNamespaceAndPath("fatality", "textures/gui/mana_bar_decoration.png");
    private static final ResourceLocation MANA_BAR_MAIN = ResourceLocation.fromNamespaceAndPath("fatality", "textures/gui/mana_bar_main.png");

    // ==================== 配置常量 ====================
    private static final class Config {
        // 贴图尺寸（装饰贴图155*28，主贴图143*11）- 缩小为80%
        static final int HEALTH_DECORATION_WIDTH = 155; // 155
        static final int HEALTH_DECORATION_HEIGHT = 28; // 28
        static final int HEALTH_MAIN_WIDTH = 143; // 143
        static final int HEALTH_MAIN_HEIGHT = 11; // 11
        static final int HEALTH_MAIN_OFFSET_X = 6; // 6
        static final int HEALTH_MAIN_OFFSET_Y = 12; // 12
        
        static final int FOOD_DECORATION_WIDTH = 155; // 155 * 0.8
        static final int FOOD_DECORATION_HEIGHT = 28; // 28
        static final int FOOD_MAIN_WIDTH = 143; // 143
        static final int FOOD_MAIN_HEIGHT = 11; // 11
        static final int FOOD_MAIN_OFFSET_X = 6; // 6
        static final int FOOD_MAIN_OFFSET_Y = 12; // 12
        
        static final int MANA_DECORATION_WIDTH = 155; // 155
        static final int MANA_DECORATION_HEIGHT = 28; // 28
        static final int MANA_MAIN_WIDTH = 143; // 143
        static final int MANA_MAIN_HEIGHT = 11; // 11
        static final int MANA_MAIN_OFFSET_X = 6; // 6
        static final int MANA_MAIN_OFFSET_Y = 12; // 12

        // 条状元素尺寸（保持兼容性）
        static final int HEALTH_BAR_WIDTH = 50;
        static final int HEALTH_BAR_HEIGHT = 6;
        static final int FOOD_BAR_WIDTH = 50;
        static final int FOOD_BAR_HEIGHT = 6;
        static final int ARMOR_BAR_WIDTH = 50;
        static final int ARMOR_BAR_HEIGHT = 6;
        static final int FLIGHT_BAR_WIDTH = 8;
        static final int FLIGHT_BAR_HEIGHT = 30;
        static final int MANA_BAR_WIDTH = 50;
        static final int MANA_BAR_HEIGHT = 6;
        static final int CHARGE_BAR_WIDTH = 60;
        static final int CHARGE_BAR_HEIGHT = 8;

        // 间距和边距
        static final int BOTTOM_MARGIN = 35;
        static final int SIDE_MARGIN = 120;
        static final int CORNER_RADIUS = 2;
        static final int BAR_SPACING = 5;
        static final int TEXT_OFFSET = 12;
        static final int CHARGE_BAR_SPACING = 15;

        // 颜色配置
        static final class Colors {
            // 边框颜色
            static final int HEALTH_BORDER = 0xFFFF0000;
            static final int FOOD_BORDER = 0xFFFFD700;
            static final int ARMOR_BORDER = 0xFF87CEEB;
            static final int FLIGHT_BORDER = 0xFF9370DB;
            static final int MANA_BORDER = 0xFF4169E1;
            static final int CHARGE_BORDER = 0xFFFFA500;

            // 背景颜色
            static final int HEALTH_BG = 0xFF4A0000;
            static final int FOOD_BG = 0xFF4A2F00;
            static final int ARMOR_BG = 0xFF1A2F4A;
            static final int FLIGHT_BG = 0xFF2A1F4A;
            static final int MANA_BG = 0xFF1A1F4A;
            static final int CHARGE_BG = 0xFF4A2F00;

            // 机械之心颜色
            static final int MECHANICAL_FULL = 0xFFFFFF00;
            static final int MECHANICAL_CHARGING = 0xFFFFA500;
            static final int MECHANICAL_ACTIVE = 0xFF00FF00;

            // 元素之心颜色
            static final int ELEMENTAL_FULL = 0xFFFF69B4;
            static final int ELEMENTAL_CHARGING = 0xFFFF1493;
            static final int ELEMENTAL_ACTIVE = 0xFFFF00FF;

            // 魔法条颜色梯度
            static final int MANA_HIGH = 0xFF4169E1;
            static final int MANA_MEDIUM = 0xFF1E90FF;
            static final int MANA_LOW = 0xFF87CEEB;
            static final int MANA_CRITICAL = 0xFFB0C4DE;

            // 血条颜色梯度
            static final int HEALTH_HIGH = 0xFF00FF00;
            static final int HEALTH_MEDIUM = 0xFFFFFF00;
            static final int HEALTH_LOW = 0xFFFF0000;
            static final int HEALTH_CRITICAL = 0xFF8B0000;

            // 饱食度颜色梯度
            static final int FOOD_HIGH = 0xFFFFFF00;
            static final int FOOD_MEDIUM = 0xFFFFA500;
            static final int FOOD_LOW = 0xFFFF0000;

            // 盔甲颜色梯度
            static final int ARMOR_HIGH = 0xFF87CEEB;
            static final int ARMOR_MEDIUM = 0xFF4682B4;
            static final int ARMOR_LOW = 0xFF0000FF;

            // 飞行时间颜色梯度
            static final int FLIGHT_HIGH = 0xFF9370DB;
            static final int FLIGHT_MEDIUM = 0xFF8A2BE2;
            static final int FLIGHT_LOW = 0xFF4B0082;
        }
    }

    // ==================== 缓存变量 ====================
    private Minecraft cachedMinecraft;
    private Player cachedPlayer;
    private GuiGraphics cachedGuiGraphics;
    private int cachedScreenWidth;
    private int cachedScreenHeight;
    
    // ==================== 动画状态管理 ====================
    private static class AnimationState {
        float currentValue;
        float targetValue;
        float animationSpeed;
        boolean isAnimating;
        long lastUpdateTime;
        
        AnimationState(float initialValue) {
            this.currentValue = initialValue;
            this.targetValue = initialValue;
            this.animationSpeed = 0.1f; // 默认动画速度
            this.isAnimating = false;
            this.lastUpdateTime = System.currentTimeMillis();
        }
        
        void update(float newValue) {
            long currentTime = System.currentTimeMillis();
            long deltaTime = currentTime - lastUpdateTime;
            lastUpdateTime = currentTime;
            
            if (Math.abs(newValue - targetValue) > 0.01f) {
                targetValue = newValue;
                isAnimating = true;
            }
            
            if (isAnimating) {
                float delta = targetValue - currentValue;
                float maxChange = animationSpeed * (deltaTime / 16.0f); // 基于时间的平滑动画
                
                if (Math.abs(delta) <= maxChange) {
                    currentValue = targetValue;
                    isAnimating = false;
                } else {
                    currentValue += Math.signum(delta) * maxChange;
                }
            }
        }
        
        float getAnimatedValue() {
            return currentValue;
        }
        
        boolean isAnimating() {
            return isAnimating;
        }
    }
    
    // 动画状态映射
    private final Map<String, AnimationState> animationStates = new HashMap<>();

    // ==================== 事件处理 ====================

    @SubscribeEvent
    public void onRenderGuiOverlayPre(RenderGuiOverlayEvent.Pre event) {
        if (shouldCancelVanillaOverlay(event)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderGuiOverlayPost(RenderGuiOverlayEvent.Post event) {
        if (!initializeRenderContext(event)) {
            return;
        }

        try {
            renderAllHudElements();
        } catch (Exception e) {
            // 防止HUD渲染异常导致游戏崩溃
            System.err.println("HUD渲染异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== 核心渲染逻辑 ====================

    /**
     * 初始化渲染上下文
     */
    private boolean initializeRenderContext(RenderGuiOverlayEvent.Post event) {
        cachedMinecraft = Minecraft.getInstance();
        cachedPlayer = cachedMinecraft.player;

        if (cachedPlayer == null || cachedMinecraft.screen != null) {
            return false;
        }

        cachedGuiGraphics = event.getGuiGraphics();
        cachedScreenWidth = event.getWindow().getGuiScaledWidth();
        cachedScreenHeight = event.getWindow().getGuiScaledHeight();

        return true;
    }

    /**
     * 渲染所有HUD元素
     */
    private void renderAllHudElements() {
        HudPositions positions = calculateHudPositions();

        // 渲染基础状态条
        renderHealthBar(positions.healthBarX, positions.healthBarY);
        renderFoodBar(positions.foodBarX, positions.foodBarY);
        renderArmorValue(positions.armorBarX, positions.armorBarY); // 修改为数字显示
        renderManaBar(positions.manaBarX, positions.manaBarY);

        // 渲染特殊状态条
        renderFlightBar(positions.flightBarX, positions.flightBarY);
        renderChargeBars(positions.manaBarX, positions.manaBarY);
    }

    /**
     * 计算HUD元素位置
     */
    private HudPositions calculateHudPositions() {
        HudPositions positions = new HudPositions();

        int topY = 20; // 距离顶部的边距
        int leftX = 20; // 距离左侧的边距

        // 左上角竖着排列三个状态条
        positions.healthBarX = leftX;
        positions.healthBarY = topY;
        
        positions.foodBarX = leftX;
        positions.foodBarY = topY + Config.FOOD_DECORATION_HEIGHT + Config.BAR_SPACING;
        
        positions.manaBarX = leftX;
        positions.manaBarY = positions.foodBarY + Config.MANA_DECORATION_HEIGHT + Config.BAR_SPACING;

        // 其他元素保持原有位置
        int bottomY = cachedScreenHeight - Config.BOTTOM_MARGIN;
        positions.armorBarX = (cachedScreenWidth - Config.ARMOR_BAR_WIDTH) / 2;
        positions.armorBarY = bottomY - Config.ARMOR_BAR_HEIGHT;
        positions.flightBarX = cachedScreenWidth - Config.FLIGHT_BAR_WIDTH - 100;
        positions.flightBarY = bottomY - Config.FLIGHT_BAR_HEIGHT + 30;

        return positions;
    }

    // ==================== 具体元素渲染方法 ====================

    /**
     * 渲染血条（使用贴图）
     */
    private void renderHealthBar(int x, int y) {
        float currentHealth = cachedPlayer.getHealth();
        float maxHealth = cachedPlayer.getMaxHealth();
        float actualPercentage = Math.min(currentHealth / maxHealth, 1.0f);
        
        // 获取动画百分比
        float animatedPercentage = getAnimatedPercentage("health", actualPercentage);
        
        // 计算动画颜色（根据动画状态调整颜色）
        ChatFormatting textColor = getAnimatedTextColor("health", actualPercentage, 
                getHealthTextColor(currentHealth, maxHealth));
        
        renderTextureBar(HEALTH_BAR_DECORATION, HEALTH_BAR_MAIN, 
                x, y, 
                Config.HEALTH_DECORATION_WIDTH, Config.HEALTH_DECORATION_HEIGHT,
                Config.HEALTH_MAIN_WIDTH, Config.HEALTH_MAIN_HEIGHT,
                Config.HEALTH_MAIN_OFFSET_X, Config.HEALTH_MAIN_OFFSET_Y,
                animatedPercentage,
                String.format("%.0f/%.0f", currentHealth, maxHealth),
                textColor);
    }

    /**
     * 渲染饱食度条（使用贴图）
     */
    private void renderFoodBar(int x, int y) {
        int foodLevel = cachedPlayer.getFoodData().getFoodLevel();
        float actualPercentage = Math.min(foodLevel / 20.0f, 1.0f);
        
        // 获取动画百分比
        float animatedPercentage = getAnimatedPercentage("food", actualPercentage);
        
        // 计算动画颜色
        ChatFormatting textColor = getAnimatedTextColor("food", actualPercentage, 
                getFoodTextColor(foodLevel));

        // 根据饥饿状态选择贴图资源
        ResourceLocation decorationTexture = (foodLevel <= 6) ? FOOD_HUNGER_BAR_DECORATION : FOOD_BAR_DECORATION;
        ResourceLocation mainTexture = (foodLevel <= 6) ? FOOD_HUNGER_BAR_MAIN : FOOD_BAR_MAIN;

        renderTextureBar(decorationTexture, mainTexture, 
                x, y, 
                Config.FOOD_DECORATION_WIDTH, Config.FOOD_DECORATION_HEIGHT,
                Config.FOOD_MAIN_WIDTH, Config.FOOD_MAIN_HEIGHT,
                Config.FOOD_MAIN_OFFSET_X, Config.FOOD_MAIN_OFFSET_Y,
                animatedPercentage,
                String.format("%d/20", foodLevel),
                textColor);
    }

    /**
     * 渲染盔甲值（数字显示）
     */
    private void renderArmorValue(int x, int y) {
        int armorValue = cachedPlayer.getArmorValue();

        // 绘制盔甲值背景框
        //drawRoundedRectWithBorder(cachedGuiGraphics, x, y, Config.ARMOR_BAR_WIDTH, Config.ARMOR_BAR_HEIGHT, Config.Colors.ARMOR_BG, Config.Colors.ARMOR_BORDER);

        // 在框内居中显示盔甲值
        String armorText = "🛡️ " + armorValue;
        int textWidth = cachedMinecraft.font.width(armorText);
        int textX = x + (Config.ARMOR_BAR_WIDTH - textWidth) / 2;
        int textY = y + (Config.ARMOR_BAR_HEIGHT - cachedMinecraft.font.lineHeight) / 2;

        cachedGuiGraphics.drawString(cachedMinecraft.font,
                Component.literal(armorText).withStyle(getArmorTextColor(armorValue)),
                textX, textY, 0xFFFFFF, true);
    }

    /**
     * 渲染魔法条（使用贴图）
     */
    private void renderManaBar(int x, int y) {
        // 使用客户端同步的魔法数据，确保HUD显示正确
        float currentMana = ManaSystem.getClientCurrentMana(cachedPlayer);
        float maxMana = ManaSystem.getClientMaxMana(cachedPlayer);

        // 修复：确保魔法值不会超过上限
        float actualPercentage = maxMana > 0 ? Math.min(currentMana / maxMana, 1.0f) : 0.0f;
        
        // 获取动画百分比
        float animatedPercentage = getAnimatedPercentage("mana", actualPercentage);
        
        // 计算动画颜色
        ChatFormatting textColor = getAnimatedTextColor("mana", actualPercentage, 
                getManaTextColor(currentMana, maxMana));

        renderTextureBar(MANA_BAR_DECORATION, MANA_BAR_MAIN, 
                x, y, 
                Config.MANA_DECORATION_WIDTH, Config.MANA_DECORATION_HEIGHT,
                Config.MANA_MAIN_WIDTH, Config.MANA_MAIN_HEIGHT,
                Config.MANA_MAIN_OFFSET_X, Config.MANA_MAIN_OFFSET_Y,
                animatedPercentage,
                String.format("%.0f/%.0f", currentMana, maxMana),
                textColor);
    }

    /**
     * 获取动画百分比
     */
    private float getAnimatedPercentage(String barType, float actualPercentage) {
        AnimationState state = animationStates.computeIfAbsent(barType, 
                k -> new AnimationState(actualPercentage));
        state.update(actualPercentage);
        return state.getAnimatedValue();
    }
    
    /**
     * 获取动画文字颜色
     */
    private ChatFormatting getAnimatedTextColor(String barType, float actualPercentage, ChatFormatting baseColor) {
        AnimationState state = animationStates.get(barType);
        if (state != null && state.isAnimating()) {
            // 动画期间使用闪烁效果
            long currentTime = System.currentTimeMillis();
            boolean shouldFlash = (currentTime / 200) % 2 == 0; // 每200ms闪烁一次
            
            if (shouldFlash) {
                // 根据数值变化方向选择闪烁颜色
                float currentValue = state.getAnimatedValue();
                if (currentValue < actualPercentage) {
                    // 增加时使用绿色闪烁
                    return ChatFormatting.GREEN;
                } else if (currentValue > actualPercentage) {
                    // 减少时使用红色闪烁
                    return ChatFormatting.RED;
                }
            }
        }
        return baseColor;
    }
    
    /**
     * 检查特定条是否在动画中
     */
    private boolean isBarAnimating(String barType) {
        AnimationState state = animationStates.get(barType);
        return state != null && state.isAnimating();
    }
    
    /**
     * 根据文本内容检测条类型
     */
    private String detectBarType(String text) {
        if (text.contains("/")) {
            if (text.contains("/20")) {
                return "food";
            } else if (text.contains("🛡️")) {
                return "armor";
            } else if (text.contains("🪽") || text.contains("🪶")) {
                return "flight";
            } else if (text.contains("魔法")) {
                return "mana";
            } else {
                // 通过数值范围判断
                String[] parts = text.split("/");
                if (parts.length == 2) {
                    try {
                        float current = Float.parseFloat(parts[0].trim());
                        float max = Float.parseFloat(parts[1].trim());
                        
                        if (max == 20) {
                            return "food";
                        } else if (max <= 100) {
                            return "mana";
                        } else {
                            return "health";
                        }
                    } catch (NumberFormatException e) {
                        return "health";
                    }
                }
            }
        }
        return "health";
    }

    /**
     * 渲染飞行时间条
     */
    private void renderFlightBar(int x, int y) {
        if (!BaseWings.hasWingsEquipped(cachedPlayer)) {
            return;
        }

        float remainingTime = BaseWings.getRemainingFlightTime(cachedPlayer);
        float maxTime = BaseWings.getMaxFlightTime(cachedPlayer);
        float percentage = maxTime > 0 ? Math.min(remainingTime / maxTime, 1.0f) : 0.0f;

        boolean isFlying = BaseWings.isFlying(cachedPlayer);
        boolean isGliding = BaseWings.isGliding(cachedPlayer);

        drawVerticalBarWithText(x, y, Config.FLIGHT_BAR_WIDTH, Config.FLIGHT_BAR_HEIGHT,
                percentage, Config.Colors.FLIGHT_BG, Config.Colors.FLIGHT_BORDER,
                getFlightColor(remainingTime, maxTime, isFlying, isGliding),
                getFlightText(remainingTime, isFlying, isGliding),
                getFlightTextColor(remainingTime, maxTime, isFlying, isGliding));
    }

    /**
     * 渲染充能条
     */
    private void renderChargeBars(int manaBarX, int manaBarY) {
        int offset = 10;

        // 机械之心充能条
        if (MechanicalHeart.hasMechanicalHeartEquipped(cachedPlayer)) {
            int y = manaBarY - Config.CHARGE_BAR_HEIGHT - Config.BAR_SPACING - offset;
            renderSingleChargeBar(manaBarX, y,
                    MechanicalHeart.getChargeProgress(cachedPlayer),
                    MechanicalHeart.isFullyCharged(cachedPlayer),
                    MechanicalHeart.isActive(cachedPlayer),
                    MechanicalHeart.getActiveRemainingTime(cachedPlayer),
                    "机械之心", "⚡", "💥", "🔋",
                    Config.Colors.MECHANICAL_FULL,
                    Config.Colors.MECHANICAL_CHARGING,
                    Config.Colors.MECHANICAL_ACTIVE);
            offset += Config.CHARGE_BAR_HEIGHT + Config.CHARGE_BAR_SPACING;
        }

        // 元素之心充能条
        if (HeartOfTheElements.hasHeartOfTheElementsEquipped(cachedPlayer)) {
            int y = manaBarY - Config.CHARGE_BAR_HEIGHT - Config.BAR_SPACING - offset;
            renderSingleChargeBar(manaBarX, y,
                    HeartOfTheElements.getChargeProgress(cachedPlayer),
                    HeartOfTheElements.isFullyCharged(cachedPlayer),
                    HeartOfTheElements.isActive(cachedPlayer),
                    HeartOfTheElements.getActiveRemainingTime(cachedPlayer),
                    "元素之心", "🌪️", "🔥", "💧",
                    Config.Colors.ELEMENTAL_FULL,
                    Config.Colors.ELEMENTAL_CHARGING,
                    Config.Colors.ELEMENTAL_ACTIVE);
        }
    }

    /**
     * 渲染单个充能条
     */
    private void renderSingleChargeBar(int x, int y, float progress, boolean isFullyCharged,
                                       boolean isActive, float remainingTime, String itemName,
                                       String activeIcon, String readyIcon, String chargingIcon,
                                       int fullColor, int chargingColor, int activeColor) {
        // 绘制背景
        drawRoundedRectWithBorder(cachedGuiGraphics, x, y, Config.CHARGE_BAR_WIDTH,
                Config.CHARGE_BAR_HEIGHT, Config.Colors.CHARGE_BG, Config.Colors.CHARGE_BORDER);

        // 绘制前景
        int width = (int) (Config.CHARGE_BAR_WIDTH * progress);
        if (width > 0) {
            int color = isActive ? activeColor : (isFullyCharged ? fullColor : chargingColor);
            drawRoundedRect(cachedGuiGraphics, x, y, width, Config.CHARGE_BAR_HEIGHT, color);
        }

        // 绘制文字（在条内居中显示）
        String text = isActive ? activeIcon + " " + String.format("%.1fs", remainingTime) :
                isFullyCharged ? readyIcon + " 就绪" :
                        chargingIcon + " " + String.format("%.0f%%", progress * 100);

        drawTextInBar(x, y, Config.CHARGE_BAR_WIDTH, Config.CHARGE_BAR_HEIGHT,
                text, getChargeTextColor(isActive, isFullyCharged));
    }

    // ==================== 通用绘制方法 ====================

    /**
     * 贴图条状元素绘制方法（带动画效果）
     */
    private void renderTextureBar(ResourceLocation decorationTexture, ResourceLocation mainTexture,
                                 int x, int y, int decorationWidth, int decorationHeight,
                                 int mainWidth, int mainHeight, int mainOffsetX, int mainOffsetY,
                                 float percentage, String text, ChatFormatting textColor) {
        // 渲染装饰背景
        RenderSystem.setShaderTexture(0, decorationTexture);
        cachedGuiGraphics.blit(decorationTexture, x, y, 0, 0, decorationWidth, decorationHeight, decorationWidth, decorationHeight);

        // 计算填充宽度
        int fillWidth = (int)(mainWidth * percentage);
        
        // 检测当前条是否在动画中（通过text参数推断条类型）
        boolean isAnimating = false;
        String barType = detectBarType(text);
        isAnimating = isBarAnimating(barType);
        
        if (fillWidth > 0) {
            RenderSystem.setShaderTexture(0, mainTexture);
            cachedGuiGraphics.blit(mainTexture, 
                    x + mainOffsetX, y + mainOffsetY, 
                    0, 0, 
                    fillWidth, mainHeight, 
                    mainWidth, mainHeight);
            
            // 动画期间添加高亮效果
            if (isAnimating) {
                long currentTime = System.currentTimeMillis();
                
                // 脉动效果：根据时间计算透明度
                float pulse = (float) (Math.sin(currentTime / 100.0) * 0.3 + 0.7); // 0.4到1.0的脉动
                int baseAlpha = (int)(pulse * 128); // 脉动透明度
                
                // 闪烁效果：每150ms切换一次
                boolean shouldHighlight = (currentTime / 150) % 2 == 0;
                
                if (shouldHighlight) {
                    // 根据条类型选择高亮颜色
                    int highlightColor = 0x80FFFFFF; // 默认白色高亮
                    
                    // 根据条类型调整高亮颜色
                    switch (barType) {
                        case "health":
                            highlightColor = (baseAlpha << 24) | 0xFF0000; // 脉动红色高亮
                            break;
                        case "food":
                            highlightColor = (baseAlpha << 24) | 0xFFFF00; // 脉动黄色高亮
                            break;
                        case "mana":
                            highlightColor = (baseAlpha << 24) | 0x0000FF; // 脉动蓝色高亮
                            break;
                    }
                    
                    // 添加半透明高亮层
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    cachedGuiGraphics.fill(x + mainOffsetX, y + mainOffsetY, 
                            x + mainOffsetX + fillWidth, y + mainOffsetY + mainHeight, 
                            highlightColor);
                    
                    // 添加边框高亮效果
                    int borderHighlight = (baseAlpha << 24) | 0xFFFFFF;
                    cachedGuiGraphics.fill(x + mainOffsetX, y + mainOffsetY, 
                            x + mainOffsetX + fillWidth, y + mainOffsetY + 1, borderHighlight); // 上边框
                    cachedGuiGraphics.fill(x + mainOffsetX, y + mainOffsetY + mainHeight - 1, 
                            x + mainOffsetX + fillWidth, y + mainOffsetY + mainHeight, borderHighlight); // 下边框
                    cachedGuiGraphics.fill(x + mainOffsetX, y + mainOffsetY, 
                            x + mainOffsetX + 1, y + mainOffsetY + mainHeight, borderHighlight); // 左边框
                    cachedGuiGraphics.fill(x + mainOffsetX + fillWidth - 1, y + mainOffsetY, 
                            x + mainOffsetX + fillWidth, y + mainOffsetY + mainHeight, borderHighlight); // 右边框
                    
                    RenderSystem.disableBlend();
                }
            }
        }

        // 绘制文字（在条内居中显示）
        drawTextInBar(x, y, decorationWidth, decorationHeight, text, textColor);
    }

    /**
     * 通用条状元素绘制方法
     */
    private void drawBarWithText(int x, int y, int width, int height, float percentage,
                                 int bgColor, int borderColor, int fillColor,
                                 String text, ChatFormatting textColor) {
        // 绘制背景和边框
        drawRoundedRectWithBorder(cachedGuiGraphics, x, y, width, height, bgColor, borderColor);

        // 绘制填充
        int fillWidth = (int) (width * percentage);
        if (fillWidth > 0) {
            drawRoundedRect(cachedGuiGraphics, x, y, fillWidth, height, fillColor);
        }

        // 绘制文字（在条内居中显示）
        drawTextInBar(x, y, width, height, text, textColor);
    }

    /**
     * 在条内居中绘制文字
     */
    private void drawTextInBar(int x, int y, int width, int height, String text, ChatFormatting color) {
        int textWidth = cachedMinecraft.font.width(text);
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height - cachedMinecraft.font.lineHeight) / 2 + 4; // 向下移动4像素

        cachedGuiGraphics.drawString(cachedMinecraft.font,
                Component.literal(text).withStyle(color), textX, textY, 0xFFFFFF, true);
    }

    /**
     * 绘制竖式条状元素
     */
    private void drawVerticalBarWithText(int x, int y, int width, int height, float percentage,
                                         int bgColor, int borderColor, int fillColor,
                                         String text, ChatFormatting textColor) {
        // 绘制竖式背景和边框
        drawVerticalRoundedRectWithBorder(cachedGuiGraphics, x, y, width, height, bgColor, borderColor);

        // 绘制竖式填充（从底部向上）
        int fillHeight = (int) (height * percentage);
        if (fillHeight > 0) {
            drawVerticalRoundedRect(cachedGuiGraphics, x, y + height - fillHeight, width, fillHeight, fillColor);
        }

        // 绘制文字（右侧）
        int textX = x + width + 5;
        int textY = y + (height - cachedMinecraft.font.lineHeight) / 2;

        cachedGuiGraphics.drawString(cachedMinecraft.font,
                Component.literal(text).withStyle(textColor), textX, textY, 0xFFFFFF, true);
    }

    // ==================== 颜色获取方法 ====================

    private int getHealthColor(float currentHealth, float maxHealth) {
        float percentage = currentHealth / maxHealth;
        if (percentage > 0.75f) return Config.Colors.HEALTH_HIGH;
        if (percentage > 0.5f) return Config.Colors.HEALTH_MEDIUM;
        if (percentage > 0.25f) return Config.Colors.HEALTH_LOW;
        return Config.Colors.HEALTH_CRITICAL;
    }

    private ChatFormatting getHealthTextColor(float currentHealth, float maxHealth) {
        float percentage = currentHealth / maxHealth;
        if (percentage > 0.75f) return ChatFormatting.GREEN;
        if (percentage > 0.5f) return ChatFormatting.YELLOW;
        if (percentage > 0.25f) return ChatFormatting.RED;
        return ChatFormatting.DARK_RED;
    }

    private int getFoodColor(int foodLevel) {
        float percentage = foodLevel / 20.0f;
        if (percentage > 0.75f) return Config.Colors.FOOD_HIGH;
        if (percentage > 0.5f) return Config.Colors.FOOD_MEDIUM;
        return Config.Colors.FOOD_LOW;
    }

    private ChatFormatting getFoodTextColor(int foodLevel) {
        float percentage = foodLevel / 20.0f;
        if (percentage > 0.75f) return ChatFormatting.YELLOW;
        if (percentage > 0.5f) return ChatFormatting.GOLD;
        return ChatFormatting.RED;
    }

    private int getArmorColor(int armorValue) {
        float percentage = armorValue / 20.0f;
        if (percentage > 0.75f) return Config.Colors.ARMOR_HIGH;
        if (percentage > 0.5f) return Config.Colors.ARMOR_MEDIUM;
        return Config.Colors.ARMOR_LOW;
    }

    private ChatFormatting getArmorTextColor(int armorValue) {
        float percentage = armorValue / 20.0f;
        if (percentage > 0.75f) return ChatFormatting.AQUA;
        if (percentage > 0.5f) return ChatFormatting.BLUE;
        return ChatFormatting.DARK_BLUE;
    }

    private int getManaColor(float currentMana, float maxMana) {
        float percentage = currentMana / maxMana;
        if (percentage > 0.75f) return Config.Colors.MANA_HIGH;
        if (percentage > 0.5f) return Config.Colors.MANA_MEDIUM;
        if (percentage > 0.25f) return Config.Colors.MANA_LOW;
        return Config.Colors.MANA_CRITICAL;
    }

    private ChatFormatting getManaTextColor(float currentMana, float maxMana) {
        float percentage = currentMana / maxMana;
        if (percentage > 0.75f) return ChatFormatting.DARK_BLUE;
        if (percentage > 0.5f) return ChatFormatting.BLUE;
        if (percentage > 0.25f) return ChatFormatting.AQUA;
        return ChatFormatting.GRAY;
    }

    private int getFlightColor(float remainingTime, float maxTime, boolean isFlying, boolean isGliding) {
        float percentage = maxTime > 0 ? remainingTime / maxTime : 0;
        if (percentage > 0.75f) return Config.Colors.FLIGHT_HIGH;
        if (percentage > 0.5f) return Config.Colors.FLIGHT_MEDIUM;
        return Config.Colors.FLIGHT_LOW;
    }

    private String getFlightText(float remainingTime, boolean isFlying, boolean isGliding) {
        String icon = isFlying ? "🪽" : (isGliding ? "🪶" : "🪽");
        return icon + " " + String.format("%.1fs", remainingTime);
    }

    private ChatFormatting getFlightTextColor(float remainingTime, float maxTime, boolean isFlying, boolean isGliding) {
        float percentage = maxTime > 0 ? remainingTime / maxTime : 0;
        if (percentage > 0.75f) return ChatFormatting.LIGHT_PURPLE;
        if (percentage > 0.5f) return ChatFormatting.DARK_PURPLE;
        return ChatFormatting.DARK_GRAY;
    }

    private ChatFormatting getChargeTextColor(boolean isActive, boolean isFullyCharged) {
        if (isActive) return ChatFormatting.GREEN;
        if (isFullyCharged) return ChatFormatting.YELLOW;
        return ChatFormatting.GOLD;
    }

    // ==================== 辅助方法 ====================

    /**
     * 检查是否需要取消原版HUD渲染
     */
    private boolean shouldCancelVanillaOverlay(RenderGuiOverlayEvent.Pre event) {
        return event.getOverlay() == VanillaGuiOverlay.PLAYER_HEALTH.type() ||
                event.getOverlay() == VanillaGuiOverlay.FOOD_LEVEL.type() ||
                event.getOverlay() == VanillaGuiOverlay.ARMOR_LEVEL.type();
    }

    /**
     * 绘制圆角矩形
     */
    private void drawRoundedRect(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        guiGraphics.fill(x, y, x + width, y + height, color);
    }

    /**
     * 绘制带边框的圆角矩形
     */
    private void drawRoundedRectWithBorder(GuiGraphics guiGraphics, int x, int y, int width, int height,
                                           int bgColor, int borderColor) {
        drawRoundedRect(guiGraphics, x, y, width, height, bgColor);
        drawRoundedRect(guiGraphics, x, y, width, 1, borderColor);
        drawRoundedRect(guiGraphics, x, y + height - 1, width, 1, borderColor);
        drawRoundedRect(guiGraphics, x, y, 1, height, borderColor);
        drawRoundedRect(guiGraphics, x + width - 1, y, 1, height, borderColor);
    }

    /**
     * 绘制竖式圆角矩形
     */
    private void drawVerticalRoundedRect(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        drawRoundedRect(guiGraphics, x, y, width, height, color);
    }

    /**
     * 绘制带边框的竖式圆角矩形
     */
    private void drawVerticalRoundedRectWithBorder(GuiGraphics guiGraphics, int x, int y, int width, int height,
                                                   int bgColor, int borderColor) {
        drawVerticalRoundedRect(guiGraphics, x, y, width, height, bgColor);
        drawVerticalRoundedRect(guiGraphics, x, y, width, 1, borderColor);
        drawVerticalRoundedRect(guiGraphics, x, y + height - 1, width, 1, borderColor);
        drawVerticalRoundedRect(guiGraphics, x, y, 1, height, borderColor);
        drawVerticalRoundedRect(guiGraphics, x + width - 1, y, 1, height, borderColor);
    }

    /**
     * HUD位置数据类
     */
    private static class HudPositions {
        int healthBarX, healthBarY;
        int foodBarX, foodBarY;
        int armorBarX, armorBarY;
        int manaBarX, manaBarY;
        int flightBarX, flightBarY;
    }
}