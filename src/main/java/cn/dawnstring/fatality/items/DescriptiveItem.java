package cn.dawnstring.fatality.items;

import cn.dawnstring.fatality.util.TooltipHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 描述性物品基类 - 支持故事和附魔/属性描述
 * 按住Shift显示附魔与属性，按住Alt显示故事
 */
public class DescriptiveItem extends Item {

    // 物品描述信息
    protected final String story;           // 物品故事
    protected final String enchantments;    // 附魔与属性描述

    public DescriptiveItem(Properties properties, String story, String enchantments) {
        super(properties);
        this.story = story;
        this.enchantments = enchantments;
    }

    /**
     * 重写工具提示方法，实现按键显示不同描述
     */
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        // 使用统一的工具提示辅助类
        TooltipHelper.addDescriptiveTooltip(stack, level, tooltip, flag, story, enchantments);
    }
}