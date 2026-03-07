package cn.dawnstring.fatality.items.normal;

import cn.dawnstring.fatality.items.NormalItem;
import cn.dawnstring.fatality.network.ManaSyncHandler;
import cn.dawnstring.fatality.system.ManaSystem;
import cn.dawnstring.fatality.system.PlayerDataSystem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 魔法之星 - 右键使用增加最大魔法值20点
 * 最大魔法值上限：400点（基础100点 + 饰品加成 + 物品加成）
 */
public class MagicStar extends NormalItem {
    private static final float MANA_BONUS = 20.0f; // 每次增加20点魔法值
    private static final float MAX_BONUS_MANA = 200.0f; // 通过物品最多增加200点魔法值

    public MagicStar() {
        super();
        this.setStory("蕴含着纯净魔法能量的星辰碎片，使用后可以永久提升魔法上限20点。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 检查当前已增加的魔法值（从持久化存储加载）
            float currentBonus = PlayerDataSystem.loadPlayerBonusMana(player);

            if (currentBonus >= MAX_BONUS_MANA) {
                // 已达到最大上限
                player.displayClientMessage(Component.literal("§c已达到最大魔法值上限！无法继续使用魔法之星。"), true);
                return InteractionResultHolder.fail(itemstack);
            }

            // 尝试增加魔法值
            if (ManaSystem.addBonusMana(player, MANA_BONUS)) {
                // 成功增加魔法值
                float newMaxMana = ManaSystem.getMaxMana(player);
                player.displayClientMessage(Component.literal("§a使用魔法之星！最大魔法值增加20点，当前最大魔法值：" + newMaxMana + "点"), true);

                // 向客户端同步魔法数据
                if (player instanceof ServerPlayer serverPlayer) {
                    ManaSyncHandler.syncManaDataToClient(serverPlayer);
                }

                // 消耗物品
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }

                return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
            } else {
                // 增加失败（可能已达到上限）
                player.displayClientMessage(Component.literal("§c已达到最大魔法值上限！无法继续使用魔法之星。"), true);
                return InteractionResultHolder.fail(itemstack);
            }
        }

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}