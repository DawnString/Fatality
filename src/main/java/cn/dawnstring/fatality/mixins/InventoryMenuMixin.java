package cn.dawnstring.fatality.mixins;

import cn.dawnstring.fatality.config.AccessoryConfig;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import cn.dawnstring.fatality.inventory.AccessorySlot;

@Mixin(InventoryMenu.class)
public class InventoryMenuMixin {

    @Inject(method = "<init>(Lnet/minecraft/world/entity/player/Inventory;ZLnet/minecraft/world/entity/player/Player;)V", at = @At("TAIL"))
    private void addAccessorySlots(Inventory inventory, boolean isLocalWorld, net.minecraft.world.entity.player.Player player, CallbackInfo ci) {
        InventoryMenu menu = (InventoryMenu) (Object) this;

        // 在背包左侧添加饰品槽位
        int startX = AccessoryConfig.ACCESSORY_SLOT_OFFSET_X; // 背包左侧
        int startY = AccessoryConfig.ACCESSORY_SLOT_START_Y;   // 顶部对齐

        for (int i = 0; i < AccessoryConfig.ACCESSORY_SLOT_COUNT; i++) {
            int x = startX;
            int y = startY + i * AccessoryConfig.ACCESSORY_SLOT_SIZE;

            // 使用AccessoryInventory来管理饰品槽位
            addAccessorySlotSafely(menu, inventory, i, x, y);
        }
    }

    // 安全地添加饰品槽位
    private void addAccessorySlotSafely(InventoryMenu menu, Inventory inventory, int slotIndex, int x, int y) {
        try {
            // 创建饰品槽位
            AccessorySlot accessorySlot = new AccessorySlot(inventory, slotIndex, x, y);

            // 使用Mixin的访问器模式来添加槽位
            addSlotToContainer(menu, accessorySlot);
        } catch (Exception e) {
            System.err.println("Failed to add accessory slot: " + e.getMessage());
        }
    }

    // 使用反射来调用protected方法（作为最后的手段）
    private void addSlotToContainer(InventoryMenu menu, Slot slot) {
        try {
            java.lang.reflect.Method addSlotMethod = net.minecraft.world.inventory.AbstractContainerMenu.class.getDeclaredMethod("addSlot", Slot.class);
            addSlotMethod.setAccessible(true);
            addSlotMethod.invoke(menu, slot);
        } catch (Exception e) {
            System.err.println("Failed to add slot via reflection: " + e.getMessage());
        }
    }
}