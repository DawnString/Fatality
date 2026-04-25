package cn.dawnstring.fatality.api.events;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack; /**
 * 饰品卸下事件
 */
public class AccessoryUnequipEvent extends AccessoryEvent {
    
    public AccessoryUnequipEvent(Player player, ItemStack accessory, int slot) {
        super(player, accessory, slot);
    }
}
