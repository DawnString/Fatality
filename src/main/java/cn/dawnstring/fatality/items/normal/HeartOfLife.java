package cn.dawnstring.fatality.items.normal;

import cn.dawnstring.fatality.items.NormalItem;
import cn.dawnstring.fatality.system.PlayerDataSystem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 生命之心 - 右键使用增加最大生命值20点
 * 最大生命值上限：1000点（基础20点 + 饰品加成 + 物品加成）
 */
public class HeartOfLife extends NormalItem {
    private static final float HEALTH_BONUS = 20.0f; // 每次增加20点生命值
    private static final float MAX_BONUS_HEALTH = 400.0f; // 通过物品最多增加400点生命值
    private static final float TOTAL_MAX_HEALTH = 400.0f; // 总生命值上限400点（基础20点 + 物品加成380点）

    // 存储玩家的属性修改器UUID（临时缓存）
    private static final Map<String, UUID> playerHealthModifierMap = new HashMap<>();

    public HeartOfLife() {
        super();
        this.setStory("蕴含着生命精华的神秘心脏，使用后可以永久提升生命上限20点。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 检查当前已增加的生命值（从持久化存储加载）
            float currentBonus = PlayerDataSystem.loadPlayerBonusHealth(player);

            if (currentBonus >= MAX_BONUS_HEALTH) {
                // 已达到最大上限
                player.displayClientMessage(Component.literal("§c已达到最大生命值上限！无法继续使用生命之心。"), true);
                return InteractionResultHolder.fail(itemstack);
            }

            // 计算新的生命值加成
            float newBonus = currentBonus + HEALTH_BONUS;

            // 获取当前基础最大生命值（不包括物品加成）
            float baseMaxHealth = 20.0f; // 原版基础生命值

            // 计算总生命值（基础20点 + 物品加成）
            float totalHealth = baseMaxHealth + newBonus;

            // 修复：确保总生命值不超过400点上限
            if (totalHealth > TOTAL_MAX_HEALTH) {
                player.displayClientMessage(Component.literal("§c已达到最大生命值上限！无法继续使用生命之心。"), true);
                return InteractionResultHolder.fail(itemstack);
            }

            // 应用生命值加成
            applyHealthBonus(player, newBonus);

            // 保存到持久化存储
            PlayerDataSystem.savePlayerBonusHealth(player, newBonus);

            // 恢复玩家生命值（不超过新的最大生命值）
            float newHealth = Math.min(player.getHealth() + HEALTH_BONUS, totalHealth);
            player.setHealth(newHealth);

            player.displayClientMessage(Component.literal("§a使用生命之心！最大生命值增加20点，当前生命值：" + player.getHealth() + "/" + totalHealth + "点"), true);

            // 消耗物品
            if (!player.getAbilities().instabuild) {
                itemstack.shrink(1);
            }

            return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
        }

        return InteractionResultHolder.fail(itemstack);
    }

    /**
     * 应用生命值加成
     */
    private void applyHealthBonus(Player player, float bonus) {
        // 使用PlayerDataSystem中的持久化方法来应用生命值加成
        PlayerDataSystem.applyHealthBonus(player, bonus);
    }

    /**
     * 获取玩家通过物品增加的生命值
     */
    public static float getBonusHealth(Player player) {
        // 从持久化存储加载
        return PlayerDataSystem.loadPlayerBonusHealth(player);
    }
}