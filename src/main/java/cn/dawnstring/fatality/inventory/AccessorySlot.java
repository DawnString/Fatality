package cn.dawnstring.fatality.inventory;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import cn.dawnstring.fatality.items.AccessoryItem;

public class AccessorySlot extends Slot {
    private final Inventory inventory;
    private final int accessorySlotIndex;

    public AccessorySlot(Inventory inventory, int slotIndex, int x, int y) {
        super(inventory, slotIndex, x, y);
        this.inventory = inventory;
        this.accessorySlotIndex = slotIndex;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem() instanceof AccessoryItem;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (inventory.player != null) {
            // 更新玩家属性
            AccessoryInventory.get(inventory.player).updatePlayerAttributes();
        }
    }

    @Override
    public ItemStack getItem() {
        // 从饰品栏获取物品
        if (inventory.player != null) {
            var accessoryInventory = AccessoryInventory.get(inventory.player);
            return accessoryInventory.getItemHandler().getStackInSlot(accessorySlotIndex);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void set(ItemStack stack) {
        if (inventory.player != null) {
            var accessoryInventory = AccessoryInventory.get(inventory.player);
            accessoryInventory.getItemHandler().setStackInSlot(accessorySlotIndex, stack);
            accessoryInventory.updatePlayerAttributes();
        }
    }

    @Override
    public boolean hasItem() {
        return !getItem().isEmpty();
    }

    @Override
    public ItemStack remove(int amount) {
        if (inventory.player != null) {
            var accessoryInventory = AccessoryInventory.get(inventory.player);
            ItemStack stack = accessoryInventory.getItemHandler().getStackInSlot(accessorySlotIndex);
            if (!stack.isEmpty()) {
                ItemStack result = stack.copy();
                if (amount >= stack.getCount()) {
                    accessoryInventory.getItemHandler().setStackInSlot(accessorySlotIndex, ItemStack.EMPTY);
                } else {
                    stack.shrink(amount);
                    result.setCount(amount);
                }
                accessoryInventory.updatePlayerAttributes();
                return result;
            }
        }
        return ItemStack.EMPTY;
    }
}
