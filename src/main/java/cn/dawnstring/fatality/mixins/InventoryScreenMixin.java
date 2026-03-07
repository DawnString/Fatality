package cn.dawnstring.fatality.mixins;

import cn.dawnstring.fatality.config.AccessoryConfig;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin
{
    private static final ResourceLocation ACCESSORY_SLOTS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("fatality", "textures/gui/accessory_inventory.png");

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void renderAccessorySlotsBackground(net.minecraft.client.gui.GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        InventoryScreen screen = (InventoryScreen) (Object) this;

        // 渲染饰品槽位背景
        int leftPos = screen.getGuiLeft();
        int topPos = screen.getGuiTop();

        // 在背包左侧绘制饰品槽位背景
        for (int i = 0; i < AccessoryConfig.ACCESSORY_SLOT_COUNT; i++) {
            int x = leftPos + AccessoryConfig.ACCESSORY_SLOT_OFFSET_X;
            int y = topPos + AccessoryConfig.ACCESSORY_SLOT_START_Y + i * AccessoryConfig.ACCESSORY_SLOT_SIZE;

            // 绘制槽位背景（使用半透明白色边框）
            guiGraphics.fill(x - 1, y - 1, x + AccessoryConfig.ACCESSORY_SLOT_SIZE - 1, y + AccessoryConfig.ACCESSORY_SLOT_SIZE - 1, 0x80FFFFFF);
            guiGraphics.fill(x, y, x + AccessoryConfig.ACCESSORY_SLOT_SIZE - 2, y + AccessoryConfig.ACCESSORY_SLOT_SIZE - 2, 0x80000000);
        }

        guiGraphics.blit(ACCESSORY_SLOTS_TEXTURE, 
            leftPos + AccessoryConfig.ACCESSORY_TEXTURE_OFFSET_X, 
            topPos + AccessoryConfig.ACCESSORY_TEXTURE_OFFSET_Y, 
            0, 0, 
            AccessoryConfig.ACCESSORY_TEXTURE_WIDTH, 
            AccessoryConfig.ACCESSORY_TEXTURE_HEIGHT, 
            AccessoryConfig.ACCESSORY_TEXTURE_WIDTH, 
            AccessoryConfig.ACCESSORY_TEXTURE_HEIGHT);
    }
}