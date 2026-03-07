package cn.dawnstring.fatality.inventory;

import cn.dawnstring.fatality.config.AccessoryConfig;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;

public class AccessoryContainer extends AbstractContainerMenu {
    private final Inventory playerInventory;

    public AccessoryContainer(int windowId, Inventory playerInventory) {
        super(null, windowId);
        this.playerInventory = playerInventory;

        // 添加饰品槽位 (7个槽位)
        for (int i = 0; i < AccessoryConfig.ACCESSORY_SLOT_COUNT; i++) {
            this.addSlot(new AccessorySlot(playerInventory, i,
                    AccessoryConfig.ACCESSORY_SLOT_OFFSET_X,
                    AccessoryConfig.ACCESSORY_SLOT_START_Y + i * AccessoryConfig.ACCESSORY_SLOT_SIZE));
        }

        // 添加玩家背包槽位
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // 添加快捷栏槽位
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < AccessoryConfig.ACCESSORY_SLOT_COUNT) {
                if (!this.moveItemStackTo(itemstack1, AccessoryConfig.ACCESSORY_SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else
                return ItemStack.EMPTY;
        }

        if (itemstack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}