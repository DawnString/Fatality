package cn.dawnstring.fatality.api.events;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack; /**
 * 饰品装备事件
 */
public class AccessoryEquipEvent extends AccessoryEvent {
    
    public AccessoryEquipEvent(Player player, ItemStack accessory, int slot) {
        super(player, accessory, slot);
    }
}
