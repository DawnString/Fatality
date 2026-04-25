package cn.dawnstring.fatality.api.events;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 饰品事件基类
 */
public abstract class AccessoryEvent extends FatalityEvent {
    
    private final ItemStack accessory;
    private final int slot;
    
    public AccessoryEvent(Player player, ItemStack accessory, int slot) {
        super(player);
        this.accessory = accessory;
        this.slot = slot;
    }
    
    public ItemStack getAccessory() {
        return accessory;
    }
    
    public int getSlot() {
        return slot;
    }
}

