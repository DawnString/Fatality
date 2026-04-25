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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import cn.dawnstring.fatality.entity.boss.BossList;

import java.util.ArrayList;
import java.util.List;

public class BossDetailScreen extends Screen {
    private static final ResourceLocation BOOK_BACKGROUND = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/book.png");
    private static final int BACKGROUND_WIDTH = 192;
    private static final int BACKGROUND_HEIGHT = 192;
    
    private final BossList boss;
    private int leftPos;
    private int topPos;
    
    public BossDetailScreen(BossList boss) {
        super(Component.translatable("gui.fatality.boss_detail.title"));
        this.boss = boss;
    }
    
    @Override
    protected void init() {
        super.init();
        
        this.leftPos = (this.width - BACKGROUND_WIDTH) / 2;
        this.topPos = (this.height - BACKGROUND_HEIGHT) / 2;
        
        // 添加返回按钮
        this.addRenderableWidget(Button.builder(Component.translatable("gui.fatality.boss_detail.back"), button -> {
            this.minecraft.setScreen(new BossListScreen());
        }).bounds(this.leftPos + 20, this.topPos + BACKGROUND_HEIGHT - 25, 50, 20).build());
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 渲染书的背景
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BOOK_BACKGROUND);
        guiGraphics.blit(BOOK_BACKGROUND, this.leftPos, this.topPos, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, 256, 256);
        
        // 渲染boss名称（使用语言文件）
        Component bossName = Component.translatable("entity.fatality." + boss.name().toLowerCase()).withStyle(ChatFormatting.BOLD);
        int bossNameWidth = this.font.width(bossName);
        int bossNameX = this.leftPos + (BACKGROUND_WIDTH - bossNameWidth) / 2;
        guiGraphics.drawString(this.font, bossName, bossNameX, this.topPos + 15, 0x000000, false);
        
        Minecraft minecraft = Minecraft.getInstance();
        
        // 渲染boss状态
        boolean isDefeated = minecraft.player != null && BossProgressManager.isBossDefeated(minecraft.player, boss);
        Component statusText = isDefeated ? 
            Component.translatable("gui.fatality.boss_detail.status.defeated") : 
            Component.translatable("gui.fatality.boss_detail.status.not_defeated");
        int statusColor = isDefeated ? 0x00AA00 : 0xAA0000;
        
        // 状态文本可能需要换行处理
        Component statusLabel = Component.translatable("gui.fatality.boss_detail.status", statusText);
        if (this.font.width(statusLabel) > BACKGROUND_WIDTH - 40) {
            // 如果状态文本太长，分两行显示
            guiGraphics.drawString(this.font, Component.translatable("gui.fatality.boss_detail.status"), 
                this.leftPos + 20, this.topPos + 35, 0x000000, false);
            guiGraphics.drawString(this.font, statusText, 
                this.leftPos + 20, this.topPos + 45, statusColor, false);
        } else {
            guiGraphics.drawString(this.font, statusLabel, 
                this.leftPos + 20, this.topPos + 35, statusColor, false);
        }
        
        // 渲染尝试次数
        int attempts = minecraft.player != null ? BossProgressManager.getAttempts(minecraft.player, boss) : 0;
        Component attemptsText = Component.translatable("gui.fatality.boss_detail.attempts", attempts);
        guiGraphics.drawString(this.font, attemptsText, 
            this.leftPos + 20, this.topPos + 55, 0x000000, false);
        
        // 渲染最短时间
        long bestTime = minecraft.player != null ? BossProgressManager.getBestTime(minecraft.player, boss) : 0;
        String timeText = bestTime > 0 ? formatTime(bestTime) : Component.translatable("gui.fatality.boss_detail.time.na").getString();
        Component bestTimeText = Component.translatable("gui.fatality.boss_detail.best_time", timeText);
        guiGraphics.drawString(this.font, bestTimeText, 
            this.leftPos + 20, this.topPos + 70, 0x000000, false);
        
        // 渲染掉落物标题
        guiGraphics.drawString(this.font, Component.translatable("gui.fatality.boss_detail.drops").withStyle(ChatFormatting.BOLD), 
            this.leftPos + 20, this.topPos + 90, 0x000000, false);
        
        // 渲染掉落物列表（只显示物品贴图，鼠标悬停显示名称）
        List<ItemStack> drops = getBossDrops(boss);
        int dropY = this.topPos + 105;
        int maxDrops = 4; // 现在可以显示更多物品，因为不需要显示文字
        int itemSpacing = 25; // 物品间距
        int itemsPerRow = 4; // 每行显示4个物品
        
        for (int i = 0; i < drops.size() && i < maxDrops; i++) {
            ItemStack drop = drops.get(i);
            
            // 计算物品位置
            int row = i / itemsPerRow;
            int col = i % itemsPerRow;
            int itemX = this.leftPos + 20 + col * 40;
            int itemY = dropY + row * itemSpacing;
            
            // 渲染物品图标
            guiGraphics.renderItem(drop, itemX, itemY);
            
            // 渲染物品数量（如果大于1）
            if (drop.getCount() > 1) {
                guiGraphics.renderItemDecorations(this.font, drop, itemX, itemY);
            }
        }
        
        // 如果掉落物超过显示数量，显示提示信息
        if (drops.size() > maxDrops) {
            Component moreText = Component.translatable("gui.fatality.boss_detail.more_drops", drops.size() - maxDrops);
            int lastRow = (maxDrops - 1) / itemsPerRow;
            int moreY = dropY + (lastRow + 1) * itemSpacing;
            guiGraphics.drawString(this.font, moreText, 
                this.leftPos + 20, moreY, 0x666666, false);
        }
        
        // 检查鼠标是否悬停在掉落物上

        for (int i = 0; i < drops.size() && i < maxDrops; i++)
        {
            ItemStack drop = drops.get(i);

            // 计算物品位置和尺寸
            int row = i / itemsPerRow;
            int col = i % itemsPerRow;
            int itemX = this.leftPos + 20 + col * 40;
            int itemY = dropY + row * itemSpacing;

            // 检查鼠标是否在物品区域内
            if (mouseX >= itemX && mouseX <= itemX + 16 &&
                mouseY >= itemY && mouseY <= itemY + 16) {
                // 显示物品提示
                guiGraphics.renderTooltip(this.font, drop, mouseX, mouseY);
                break; // 只显示一个提示
            }
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
    
    private String formatBossName(String enumName) {
        return enumName.replace("_", " ");
    }
    
    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    private String truncateText(String text, int maxWidth) {
        if (this.font.width(text) <= maxWidth) {
            return text;
        }
        
        // 添加省略号
        String ellipsis = "...";
        int ellipsisWidth = this.font.width(ellipsis);
        
        // 逐步截断文本直到适合宽度
        for (int i = text.length() - 1; i > 0; i--) {
            String truncated = text.substring(0, i) + ellipsis;
            if (this.font.width(truncated) <= maxWidth) {
                return truncated;
            }
        }
        
        return ellipsis; // 如果连省略号都放不下，只显示省略号
    }
    
    private List<ItemStack> getBossDrops(BossList boss) {
        List<ItemStack> drops = new ArrayList<>();
        
        switch (boss) {
            case commander_of_the_undead_guard:
                drops.add(new ItemStack(Items.IRON_SWORD));
                drops.add(new ItemStack(Items.SHIELD));
                drops.add(new ItemStack(Items.IRON_INGOT, 5));
                break;
            case Calamity_Mage:
                drops.add(new ItemStack(Items.ENCHANTED_BOOK));
                drops.add(new ItemStack(Items.BLAZE_ROD, 3));
                drops.add(new ItemStack(Items.ENDER_PEARL, 2));
                break;
            case Acid_eroding_parasite:
                drops.add(new ItemStack(Items.SLIME_BALL, 5));
                drops.add(new ItemStack(Items.POISONOUS_POTATO));
                drops.add(new ItemStack(Items.GREEN_DYE, 3));
                break;
            case Jungle_turtle:
                drops.add(new ItemStack(Items.SEAGRASS, 5));
                drops.add(new ItemStack(Items.KELP, 3));
                break;
            case Blood_red_slime:
                drops.add(new ItemStack(Items.SLIME_BALL, 8));
                drops.add(new ItemStack(Items.RED_DYE, 3));
                drops.add(new ItemStack(Items.MAGMA_CREAM));
                break;
            case Stone_Giant:
                drops.add(new ItemStack(Items.STONE, 10));
                drops.add(new ItemStack(Items.IRON_INGOT, 3));
                drops.add(new ItemStack(Items.COAL, 5));
                break;
            case Flesh_and_blood_aggregation:
                drops.add(new ItemStack(Items.ROTTEN_FLESH, 8));
                drops.add(new ItemStack(Items.BONE, 5));
                drops.add(new ItemStack(Items.STRING, 3));
                break;
            case Gatekeeper_of_Darkness:
                drops.add(new ItemStack(Items.OBSIDIAN, 3));
                drops.add(new ItemStack(Items.ENDER_EYE));
                drops.add(new ItemStack(Items.BLACK_DYE, 2));
                break;
            case Reconnaissance_mechanical_bird:
                drops.add(new ItemStack(Items.FEATHER, 5));
                drops.add(new ItemStack(Items.IRON_INGOT, 3));
                drops.add(new ItemStack(Items.REDSTONE, 4));
                break;
            case Thousand_faced_Spectre:
                drops.add(new ItemStack(Items.GHAST_TEAR));
                drops.add(new ItemStack(Items.PHANTOM_MEMBRANE, 2));
                drops.add(new ItemStack(Items.GLOWSTONE_DUST, 3));
                break;
            case Holy_Knight:
                drops.add(new ItemStack(Items.GOLDEN_SWORD));
                drops.add(new ItemStack(Items.GOLDEN_APPLE));
                drops.add(new ItemStack(Items.EXPERIENCE_BOTTLE, 3));
                break;
            case Residual_soul_of_a_deity:
                drops.add(new ItemStack(Items.NETHER_STAR));
                drops.add(new ItemStack(Items.ECHO_SHARD));
                drops.add(new ItemStack(Items.AMETHYST_SHARD, 3));
                break;
            case Corrosion_infesting_Insect:
                drops.add(new ItemStack(Items.SPIDER_EYE, 3));
                drops.add(new ItemStack(Items.FERMENTED_SPIDER_EYE));
                drops.add(new ItemStack(Items.STRING, 5));
                break;
            case Red_Flame_Demon:
                drops.add(new ItemStack(Items.BLAZE_ROD, 3));
                drops.add(new ItemStack(Items.MAGMA_CREAM, 2));
                drops.add(new ItemStack(Items.FIRE_CHARGE, 4));
                break;
            case Lord_of_Hell:
                drops.add(new ItemStack(Items.NETHERITE_SCRAP));
                drops.add(new ItemStack(Items.BLAZE_POWDER, 5));
                drops.add(new ItemStack(Items.GOLD_INGOT, 8));
                break;
            case End_Dragon:
                drops.add(new ItemStack(Items.DRAGON_EGG));
                drops.add(new ItemStack(Items.DRAGON_HEAD));
                drops.add(new ItemStack(Items.ELYTRA));
                break;
            case Ender_servant:
                drops.add(new ItemStack(Items.ENDER_PEARL, 3));
                drops.add(new ItemStack(Items.CHORUS_FRUIT, 5));
                drops.add(new ItemStack(Items.PURPUR_BLOCK, 2));
                break;
            case Lord_of_Ender:
                drops.add(new ItemStack(Items.ENDER_EYE, 3));
                drops.add(new ItemStack(Items.SHULKER_SHELL, 2));
                drops.add(new ItemStack(Items.OBSIDIAN, 5));
                break;
            case wither:
                drops.add(new ItemStack(Items.NETHER_STAR));
                drops.add(new ItemStack(Items.WITHER_SKELETON_SKULL));
                drops.add(new ItemStack(Items.COAL, 10));
                break;
            case Spirit_Fire_Elf:
                drops.add(new ItemStack(Items.BLAZE_POWDER, 4));
                drops.add(new ItemStack(Items.GLOWSTONE_DUST, 3));
                drops.add(new ItemStack(Items.FIREWORK_ROCKET, 2));
                break;
            case Form_of_Darkness:
                drops.add(new ItemStack(Items.BLACK_DYE, 3));
                drops.add(new ItemStack(Items.OBSIDIAN, 2));
                drops.add(new ItemStack(Items.ENDER_PEARL));
                break;
            case Mechanical_End_Dragon:
                drops.add(new ItemStack(Items.IRON_INGOT, 8));
                drops.add(new ItemStack(Items.REDSTONE, 5));
                drops.add(new ItemStack(Items.ELYTRA));
                break;
            case Tsunami_Dragon:
                drops.add(new ItemStack(Items.PRISMARINE_SHARD, 5));
                drops.add(new ItemStack(Items.HEART_OF_THE_SEA));
                drops.add(new ItemStack(Items.TRIDENT));
                break;
            case Abyssal_Dragon:
                drops.add(new ItemStack(Items.DIAMOND, 3));
                drops.add(new ItemStack(Items.DEEPSLATE, 8));
                drops.add(new ItemStack(Items.AMETHYST_SHARD, 4));
                break;
            case Lord_of_the_Dead:
                drops.add(new ItemStack(Items.BONE, 10));
                drops.add(new ItemStack(Items.ROTTEN_FLESH, 8));
                drops.add(new ItemStack(Items.WITHER_ROSE));
                break;
            case Holy_Flame_Calamity:
                drops.add(new ItemStack(Items.BLAZE_ROD, 4));
                drops.add(new ItemStack(Items.GOLDEN_CARROT, 3));
                drops.add(new ItemStack(Items.EXPERIENCE_BOTTLE, 5));
                break;
            case God_of_Calamity:
                drops.add(new ItemStack(Items.NETHER_STAR));
                drops.add(new ItemStack(Items.DIAMOND_BLOCK));
                drops.add(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));
                break;
            case Hunting_Dragon:
                drops.add(new ItemStack(Items.DRAGON_BREATH));
                drops.add(new ItemStack(Items.ARROW, 10));
                drops.add(new ItemStack(Items.BOW));
                break;
            case Necromancer_Witch:
                drops.add(new ItemStack(Items.POTION));
                drops.add(new ItemStack(Items.CAULDRON));
                drops.add(new ItemStack(Items.SPIDER_EYE, 3));
                break;
            case Mage_portrait:
                drops.add(new ItemStack(Items.PAINTING));
                drops.add(new ItemStack(Items.BOOK, 3));
                drops.add(new ItemStack(Items.INK_SAC, 2));
                break;
            case End_of_Nightmare:
                drops.add(new ItemStack(Items.NETHER_STAR));
                drops.add(new ItemStack(Items.TOTEM_OF_UNDYING));
                drops.add(new ItemStack(Items.ENCHANTED_BOOK));
                break;
            default:
                // 默认掉落物
                drops.add(new ItemStack(Items.DIAMOND));
                drops.add(new ItemStack(Items.EMERALD));
                drops.add(new ItemStack(Items.GOLD_INGOT));
                break;
        }
        
        return drops;
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}