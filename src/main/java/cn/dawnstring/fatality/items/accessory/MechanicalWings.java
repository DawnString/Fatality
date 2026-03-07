package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.BaseWings;
import net.minecraft.world.item.Rarity;

public class MechanicalWings extends BaseWings
{
    public MechanicalWings() {
        super(
                new Properties()
                        .stacksTo(1),
                4.0f,   // 最大飞行时间：4秒
                0.8f,   // 最大水平速度
                0.6f,   // 最大垂直速度
                0.4f,  // 水平加速度
                0.3f,  // 垂直加速度
                0.3f    // 滑行速度
        );
    }
}
