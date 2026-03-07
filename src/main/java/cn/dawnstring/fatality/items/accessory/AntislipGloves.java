package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class AntislipGloves extends AccessoryItem
{
    public AntislipGloves(Properties properties) {
        super(properties);
    }

    /**
     * 提供5%暴击率加成
     */
    @Override
    public float getCriticalChanceBonus()
    {
        return 0.05f; // 5%暴击率加成
    }
}
