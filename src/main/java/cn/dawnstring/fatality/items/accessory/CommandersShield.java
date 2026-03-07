package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.inventory.AccessoryInventory;
import cn.dawnstring.fatality.items.BaseShield;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

/**
 * 指挥官之盾 - 继承自BaseShield，提供冲刺功能
 */
public class CommandersShield extends BaseShield {

    public CommandersShield() {
        super(new Properties()
                .stacksTo(1));
    }

    @Override
    public boolean hasShieldEquipped(Player player)
    {
        // 检查饰品栏
        var accessoryInventory = cn.dawnstring.fatality.inventory.AccessoryInventory.get(player);
        if (accessoryInventory != null) {
            for (int i = 0; i < accessoryInventory.getItemHandler().getSlots(); i++) {
                ItemStack accessory = accessoryInventory.getItemHandler().getStackInSlot(i);
                if (accessory.getItem() instanceof CommandersShield) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public float getDashSpeed() {
        return 3.0f; // 冲刺速度
    }

    @Override
    public float getDashDuration() {
        return 1.0f; // 冲刺持续时间（秒）
    }

    @Override
    public float getDashCooldown() {
        return 2.0f; // 冲刺冷却时间（秒）
    }

    @Override
    public float getDashDamage() {
        return 20.0f; // 冲刺碰撞伤害
    }

    @Override
    public float getDefenseBonus() {
        return 5.0f; // 防御加成
    }
}