package cn.dawnstring.fatality.items.normal;

import cn.dawnstring.fatality.utils.GameConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 调试物品：重置魔法值
 * 用于测试和调试，重置玩家的魔法值加成
 */
public class DebugManaReset extends Item {
    
    public DebugManaReset() {
        super(new Item.Properties().stacksTo(1));
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        
        if (!level.isClientSide()) {
            // 重置魔法值加成
            resetManaBonus(player);
            
            // 发送成功消息
            player.sendSystemMessage(Component.literal("§a魔法值加成已重置！"));
            
            // 消耗物品
            itemStack.shrink(1);
        }
        
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
    
    /**
     * 重置玩家的魔法值加成
     */
    private void resetManaBonus(Player player) {
        // 重置通过物品增加的魔法值加成
        cn.dawnstring.fatality.system.PlayerDataSystem.savePlayerBonusMana(player, 0.0f);
        
        // 重置当前魔法值为基础值的一半
        float baseMana = GameConstants.BASE_MAX_MANA * 0.5f;
        cn.dawnstring.fatality.system.ManaSystem.setCurrentMana(player, baseMana);
        
        // 清除内存中的缓存
        String playerId = player.getUUID().toString();
        cn.dawnstring.fatality.system.ManaSystem.getBonusMana(player); // 这会重新加载数据
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        
        tooltip.add(Component.literal("§7调试物品 - 重置魔法值加成"));
        tooltip.add(Component.literal("§e右键使用：重置所有魔法值加成"));
        tooltip.add(Component.literal("§6包括：物品加成、饰品加成等"));
        tooltip.add(Component.literal("§6当前魔法值将重置为基础值的一半"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§c警告：此操作不可逆！"));
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // 添加闪烁效果，表示特殊物品
    }
}