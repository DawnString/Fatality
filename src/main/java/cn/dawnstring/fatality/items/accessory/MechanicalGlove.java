package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class MechanicalGlove extends AccessoryItem
{
    private static final UUID ATTACK_SPEED_UUID = UUID.fromString("62345678-1234-1234-1234-123456789abc");

    public MechanicalGlove(Properties properties) {
        super(properties);
    }

    /**
     * 提供10%攻击速度加成
     */
    @Override
    public float getAttackSpeedBonus() {
        return 0.10f; // 10%攻击速度加成
    }
}
