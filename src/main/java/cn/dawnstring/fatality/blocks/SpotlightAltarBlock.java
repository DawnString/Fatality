package cn.dawnstring.fatality.blocks;

import cn.dawnstring.fatality.items.normal.ShadowIngot;
import cn.dawnstring.fatality.items.normal.SoulIngot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class SpotlightAltarBlock extends Block {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final int TRANSFORMATION_TIME = 200; // 10秒 (20 ticks/秒 * 10秒)

    public SpotlightAltarBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack itemStack = player.getItemInHand(hand);
        
        // 检查玩家是否持有暗影锭且祭坛未激活
        if (!state.getValue(ACTIVE) && itemStack.getItem() instanceof ShadowIngot) {
            if (!level.isClientSide) {
                // 消耗玩家手中的暗影锭
                if (!player.isCreative()) {
                    itemStack.shrink(1);
                }
                
                // 激活祭坛
                level.setBlock(pos, state.setValue(ACTIVE, true), 3);
                
                // 播放激活音效
                level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.0F);
                
                // 开始转换过程
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.scheduleTick(pos, this, TRANSFORMATION_TIME);
                }
            }
            
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        
        return InteractionResult.PASS;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, net.minecraft.util.RandomSource random) {
        if (state.getValue(ACTIVE)) {
            // 转换完成，生成灵锭
            level.setBlock(pos, state.setValue(ACTIVE, false), 3);
            
            // 播放完成音效
            level.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0F, 1.0F);
            
            // 在祭坛上方生成灵锭
            ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 
                new ItemStack(cn.dawnstring.fatality.registry.ModItems.SOUL_INGOT.get()));
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, net.minecraft.util.RandomSource random) {
        if (state.getValue(ACTIVE)) {
            // 生成白色粒子效果
            for (int i = 0; i < 3; i++) {
                double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
                double y = pos.getY() + 1.0 + random.nextDouble() * 0.5;
                double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
                
                level.addParticle(ParticleTypes.END_ROD, x, y, z, 0, 0.1, 0);
            }
            
            // 生成向上汇聚的粒子效果
            if (random.nextInt(5) == 0) {
                double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
                double y = pos.getY() - 1.0;
                double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
                
                level.addParticle(ParticleTypes.END_ROD, x, y, z, 0, 0.2, 0);
            }
        }
    }
}