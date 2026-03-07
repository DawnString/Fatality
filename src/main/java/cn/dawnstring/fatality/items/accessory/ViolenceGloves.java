package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class ViolenceGloves extends AccessoryItem
{
    private static final UUID ATTACK_UUID = UUID.fromString("22345678-1234-1234-1234-123456789abc");

    public ViolenceGloves(Properties properties)
    {
        super(properties);
    }

    /**
     * 提供5点攻击伤害加成
     */
    @Override
    public float getMeleeDamageValueBonus() {
        return 5.0f; // 5点近战伤害加成（数值）
    }
}
