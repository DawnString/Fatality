package cn.dawnstring.fatality.items.normal;

import cn.dawnstring.fatality.items.NormalItem;
import cn.dawnstring.fatality.registry.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 初级治疗药水 - 恢复50点生命值
 * 使用后获得1分钟的治疗饱和效果（生命恢复减少50%）
 */
public class HealingPotionBasic extends NormalItem {
    private static final int HEAL_AMOUNT = 50; // 恢复50点生命值
    private static final int TREATMENT_SATURATION_DURATION = 1200; // 60秒 * 20tick/秒 = 1200tick

    public HealingPotionBasic() {
        super();
        this.setStory("初级治疗药水，可以立即恢复50点生命值，但会暂时降低生命恢复效果。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 检查玩家是否有治疗饱和效果
            if (player.hasEffect(ModEffects.TREATMENT_SATURATION.get())) {
                player.displayClientMessage(Component.literal("§c你正处于治疗饱和状态，无法使用治疗药水！"), true);
                return InteractionResultHolder.fail(itemstack);
            }

            // 恢复生命值
            float currentHealth = player.getHealth();
            float maxHealth = player.getMaxHealth();
            float newHealth = Math.min(maxHealth, currentHealth + HEAL_AMOUNT);
            player.setHealth(newHealth);

            // 添加治疗饱和效果（1分钟）
            player.addEffect(new MobEffectInstance(ModEffects.TREATMENT_SATURATION.get(), 
                TREATMENT_SATURATION_DURATION, 0, false, true));

            player.displayClientMessage(Component.literal("§a使用初级治疗药水！恢复50点生命值，获得治疗饱和效果（60秒）"), true);

            // 消耗物品
            if (!player.getAbilities().instabuild) {
                itemstack.shrink(1);
            }

            return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
        }

        return InteractionResultHolder.fail(itemstack);
    }
}