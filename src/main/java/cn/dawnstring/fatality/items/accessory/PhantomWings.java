package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.BaseWings;
import net.minecraft.world.item.Rarity;

public class PhantomWings extends BaseWings
{
    public PhantomWings() {
        super(
                new Properties()
                        .stacksTo(1),
                6.0f,   // 最大飞行时间：6秒
                1.5f,   // 最大水平速度
                0.8f,   // 最大垂直速度
                0.4f,  // 水平加速度
                0.35f,  // 垂直加速度
                0.4f    // 滑行速度
        );
    }
}
