package cn.dawnstring.fatality.items.summon;

import cn.dawnstring.fatality.entity.boss.endofnightmare.EndOfNightmare;
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

            // 检查Boss是否已经存在
            if (isBossAlreadyExists(serverLevel, pos)) {
                return InteractionResult.FAIL;
            }

            // 检查是否在空旷区域（周围没有方块阻挡）
            if (isValidSummonLocation(serverLevel, pos)) {
                // 使用EntityType正确创建EndOfNightmare Boss
                EndOfNightmare boss = new EndOfNightmare(ModEntities.END_OF_NIGHTMARE.get(), serverLevel);
                boss.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);

                // 设置战斗场地中心为召唤位置
                boss.setArenaCenter(pos);

                // 设置Boss的目标为玩家（但出场动画期间不会攻击）
                boss.setTarget(player);

                // 添加到世界
                serverLevel.addFreshEntity(boss);

                // 播放召唤效果
                // 这里可以添加粒子效果和音效

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    /**
     * 检查Boss是否已经存在
     */
    private boolean isBossAlreadyExists(ServerLevel level, BlockPos pos) {
        // 搜索周围100格范围内的所有EndOfNightmare实体
        AABB searchArea = new AABB(pos).inflate(100.0); // 100格搜索范围
        List<EndOfNightmare> existingBosses = level.getEntitiesOfClass(EndOfNightmare.class, searchArea);

        // 如果有存活的EndOfNightmare实体，则认为Boss已经存在
        for (EndOfNightmare boss : existingBosses) {
            if (boss.isAlive()) {
                return true;
            }
        }
        return false;
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