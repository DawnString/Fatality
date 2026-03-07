package cn.dawnstring.fatality.items.normal;

import cn.dawnstring.fatality.items.NormalItem;
import cn.dawnstring.fatality.registry.ModEffects;
import cn.dawnstring.fatality.system.ManaSystem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 中级魔力药水 - 恢复200点魔法值
 * 使用后获得15秒的魔力衰减效果（魔法伤害减少20%，魔法恢复减少20%）
 */
public class ManaPotionIntermediate extends NormalItem {
    private static final int MANA_RESTORE_AMOUNT = 200; // 恢复200点魔法值
    private static final int MAGIC_FADE_DURATION = 300; // 15秒 * 20tick/秒 = 300tick

    public ManaPotionIntermediate() {
        super();
        this.setStory("中级魔力药水，可以立即恢复200点魔法值，但会暂时降低魔法伤害和恢复效果。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 恢复魔法值
            float currentMana = ManaSystem.getCurrentMana(player);
            float maxMana = ManaSystem.getMaxMana(player);
            float newMana = Math.min(maxMana, currentMana + MANA_RESTORE_AMOUNT);
            ManaSystem.setCurrentMana(player, newMana);

            // 立即同步魔法数据到客户端
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                cn.dawnstring.fatality.network.ManaSyncHandler.syncManaDataToClient(serverPlayer);
            }

            // 添加魔力衰减效果（15秒）
            player.addEffect(new MobEffectInstance(ModEffects.MAGIC_FADE.get(), 
                MAGIC_FADE_DURATION, 0, false, true));

            player.displayClientMessage(Component.literal("§a使用中级魔力药水！恢复200点魔法值，获得魔力衰减效果（15秒）"), true);

            // 消耗物品
            if (!player.getAbilities().instabuild) {
                itemstack.shrink(1);
            }

            return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
        }

        return InteractionResultHolder.fail(itemstack);
    }
}