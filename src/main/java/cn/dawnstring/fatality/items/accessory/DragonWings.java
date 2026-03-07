package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.BaseWings;
import net.minecraft.world.item.Rarity;

public class DragonWings extends BaseWings
{
    public DragonWings() {
        super(
                new Properties()
                        .stacksTo(1),
                8.0f,   // 最大飞行时间：8秒
                1.8f,   // 最大水平速度
                1.2f,   // 最大垂直速度
                0.8f,  // 水平加速度
                0.6f,  // 垂直加速度
                0.4f    // 滑行速度
        );
    }
}
