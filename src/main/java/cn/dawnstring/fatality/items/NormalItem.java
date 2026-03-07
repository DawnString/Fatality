package cn.dawnstring.fatality.items;

import cn.dawnstring.fatality.util.TooltipHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class NormalItem extends Item
{
    protected String story;

    public NormalItem()
    {
        super(new Item.Properties());
    }

    // 添加支持自定义属性的构造函数
    public NormalItem(Item.Properties properties) {
        super(properties);
    }

    /**
     * 设置物品故事
     */
    public void setStory(String story) {
        this.story = story;
    }

    /**
     * 获取原版附魔信息
     */
    protected String getEnchantmentsDescription(ItemStack stack) {
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);

        if (enchantments.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();

            // 获取附魔的显示名称
            Component enchantmentName = enchantment.getFullname(level);
            sb.append(enchantmentName.getString()).append("\n");
        }

        return sb.toString().trim();
    }

    /**
     * 重写工具提示方法，实现按键显示不同描述
     */
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        // 使用统一的工具提示辅助类
        TooltipHelper.addDescriptiveTooltip(stack, level, tooltip, flag, story, null);
    }
}
