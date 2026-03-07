package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.BaseWings;
import net.minecraft.world.item.Rarity;

public class DemonsWings extends BaseWings
{
    public DemonsWings() {
        super(
                new Properties()
                        .stacksTo(1),
                7.0f,   // 最大飞行时间：7秒
                1.6f,   // 最大水平速度
                1.0f,   // 最大垂直速度
                0.6f,  // 水平加速度
                0.5f,  // 垂直加速度
                0.3f    // 滑行速度
        );
    }
}
