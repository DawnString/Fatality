package cn.dawnstring.fatality.mixins.bossbar;

import cn.dawnstring.fatality.Fatality;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.UUID;

@Mixin(net.minecraft.client.gui.components.BossHealthOverlay.class)
public class BossBarMixin {

    private static final ResourceLocation BAR_MAIN = new ResourceLocation(Fatality.MODID, "textures/gui/health_bossbar_main.png");
    private static final ResourceLocation BAR_DECO = new ResourceLocation(Fatality.MODID, "textures/gui/health_bossbar_decoration.png");

    private static final int DECO_W = 260;
    private static final int DECO_H = 52;
    private static final int BAR_W = 240;
    private static final int BAR_H = 4;
    private static final int BAR_RENDER_H = 4;

    @Shadow @Final
    private Map<UUID, net.minecraft.client.gui.components.LerpingBossEvent> events;

    @Shadow @Final
    private Minecraft minecraft;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRenderBossBars(GuiGraphics graphics, CallbackInfo ci) {
        if (events.isEmpty()) return;

        ci.cancel();

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int y = 10;

        for (net.minecraft.client.gui.components.LerpingBossEvent event : events.values()) {
            float progress = event.getProgress();
            String name = event.getName().getString();

            int cx = screenWidth / 2;

            graphics.blit(BAR_DECO, cx - DECO_W / 2, y, DECO_W, DECO_H, 0, 0, DECO_W, DECO_H, DECO_W, DECO_H);

            int barX = cx - BAR_W / 2;
            int barY = y + 23;
            int filled = (int) (progress * BAR_W);
            graphics.blit(BAR_MAIN, barX, barY, filled, BAR_RENDER_H, 0, 0, filled, BAR_H, BAR_W, BAR_H);

            String display = name + " §7" + String.format("%.1f", progress * 100) + "%";
            int textX = cx - minecraft.font.width(display) / 2;
            graphics.drawString(minecraft.font, display, textX, barY - 10, 0xFFFFFFFF);

            y += DECO_H + 4;
        }
    }
}
