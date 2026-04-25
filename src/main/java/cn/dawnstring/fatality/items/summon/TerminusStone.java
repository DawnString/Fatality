package cn.dawnstring.fatality.items.summon;

import cn.dawnstring.fatality.items.NormalItem;
import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

//TODO
public class TerminusStone extends NormalItem {

    public TerminusStone() {
        super(new Item.Properties()
                .stacksTo(1));
        // 设置物品故事
        setStory("召唤终焉之梦魇");
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos().above(); // 在点击位置上方召唤

        if (!level.isClientSide() && player != null) {
            ServerLevel serverLevel = (ServerLevel) level;

        }

        return InteractionResult.PASS;
    }

    /**
     * 检查召唤位置是否有效
     */
    private boolean isValidSummonLocation(ServerLevel level, BlockPos pos) {
        // 检查周围3x3x3区域是否有足够的空间
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 3; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos checkPos = pos.offset(x, y, z);
                    if (!level.getBlockState(checkPos).isAir()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}