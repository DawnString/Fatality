package cn.dawnstring.fatality.items.normal;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * 调试物品：重置生命值
 * 用于测试和调试，重置玩家的生命值加成
 */
public class DebugHealthReset extends Item {
    
    public DebugHealthReset() {
        super(new Item.Properties().stacksTo(1));
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        
        if (!level.isClientSide()) {
            // 重置生命值加成
            resetHealthBonus(player);
            
            // 发送成功消息
            player.sendSystemMessage(Component.literal("§a生命值加成已重置！"));
            
            // 消耗物品
            itemStack.shrink(1);
        }
        
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
    
    /**
     * 重置玩家的生命值加成
     */
    private void resetHealthBonus(Player player) {
        // 移除所有生命值加成属性修改器
        var maxHealthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttribute != null) {
            // 移除通过持久化系统添加的生命值加成
            String playerId = player.getUUID().toString();
            UUID modifierUUID = UUID.nameUUIDFromBytes(("persistent_health_bonus_" + playerId).getBytes());
            maxHealthAttribute.removeModifier(modifierUUID);
            
            // 重置持久化存储的生命值加成
            cn.dawnstring.fatality.system.PlayerDataSystem.savePlayerBonusHealth(player, 0.0f);
        }
        
        // 确保当前生命值不超过最大生命值
        float maxHealth = player.getMaxHealth();
        if (player.getHealth() > maxHealth) {
            player.setHealth(maxHealth);
        }
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // 添加闪烁效果，表示特殊物品
    }
}