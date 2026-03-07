package cn.dawnstring.fatality.registry;

import cn.dawnstring.fatality.blocks.SpotlightAltarBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    // 聚光台
    public static final RegistryObject<Block> SPOTLIGHT_ALTAR = ModRegistry.BLOCKS.register("spotlight_altar",
            () -> new SpotlightAltarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> state.getValue(SpotlightAltarBlock.ACTIVE) ? 10 : 0)));

    // 聚光台物品
    public static final RegistryObject<Item> SPOTLIGHT_ALTAR_ITEM = ModRegistry.ITEMS.register("spotlight_altar",
            () -> new BlockItem(SPOTLIGHT_ALTAR.get(), new Item.Properties()));

    // 触发静态初始化
    public static void getBlocks() {
        SPOTLIGHT_ALTAR.getClass();
        SPOTLIGHT_ALTAR_ITEM.getClass();
    }
}