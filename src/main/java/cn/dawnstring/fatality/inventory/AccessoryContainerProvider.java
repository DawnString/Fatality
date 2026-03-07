package cn.dawnstring.fatality.inventory;

import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.chat.Component;

public class AccessoryContainerProvider implements MenuProvider {
    @Override
    public Component getDisplayName() {
        return Component.translatable("container.fatality.accessory");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new AccessoryContainer(windowId, playerInventory);
    }
}