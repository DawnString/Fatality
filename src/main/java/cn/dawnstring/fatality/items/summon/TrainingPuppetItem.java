package cn.dawnstring.fatality.items.summon;

import cn.dawnstring.fatality.entity.TrainingPuppet;
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

public class TrainingPuppetItem extends NormalItem {

    public TrainingPuppetItem() {
        super(new Item.Properties()
                .stacksTo(16)); // 可堆叠到16个
        // 设置物品故事
        setStory("训练人偶召唤物品。\n" +
                "右键放置训练人偶，用于测试武器DPS。\n" +
                "训练人偶会显示实时DPS、总伤害和持续时间。");
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos().above(); // 在点击位置上方召唤

        if (!level.isClientSide() && player != null) {
            ServerLevel serverLevel = (ServerLevel) level;

            // 检查是否在空旷区域（周围没有方块阻挡）
            if (isValidSummonLocation(serverLevel, pos)) {
                // 创建训练人偶实体
                TrainingPuppet trainingPuppet = new TrainingPuppet(ModEntities.TRAINING_PUPPET.get(), serverLevel);
                trainingPuppet.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);

                // 设置训练人偶面向玩家
                trainingPuppet.setYRot(player.getYRot());

                // 添加到世界
                serverLevel.addFreshEntity(trainingPuppet);

                // 消耗物品（如果不是创造模式）
                if (!player.isCreative()) {
                    ItemStack itemStack = context.getItemInHand();
                    itemStack.shrink(1);
                }

                // 播放放置音效
                // level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.BLOCKS, 1.0F, 1.0F);

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    /**
     * 检查召唤位置是否有效
     */
    private boolean isValidSummonLocation(ServerLevel level, BlockPos pos) {
        // 检查周围2x3x2区域是否有足够的空间
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 2; y++) {
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