package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class FragrantScarf extends AccessoryItem
{
    public FragrantScarf(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getDamageReductionBonus() {
        return 0.08f;
    }

    @Override
    public void applyEffects(Player player, ItemStack stack) {
        // 先调用父类的效果应用
        super.applyEffects(player, stack);

        // 添加负面效果免疫
        applyNegativeEffectImmunity(player);
    }

    @Override
    public void removeEffects(Player player, ItemStack stack) {
        // 移除负面效果免疫
        removeNegativeEffectImmunity(player);

        // 调用父类的效果移除
        super.removeEffects(player, stack);
    }

    /**
     * 应用负面效果免疫
     */
    public void applyNegativeEffectImmunity(Player player) {
        // 免疫中毒效果
        if (player.hasEffect(MobEffects.POISON)) {
            player.removeEffect(MobEffects.POISON);
        }

        // 免疫凋零效果
        if (player.hasEffect(MobEffects.WITHER)) {
            player.removeEffect(MobEffects.WITHER);
        }

        // 免疫缓慢效果
        if (player.hasEffect(MobEffects.SLOW_FALLING)) {
            player.removeEffect(MobEffects.SLOW_FALLING);
        }
    }

    /**
     * 移除负面效果免疫（主要是清理工作）
     */
    public void removeNegativeEffectImmunity(Player player)
    {
    }
}
