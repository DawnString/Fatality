package cn.dawnstring.fatality.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import cn.dawnstring.fatality.Fatality;

public class ModCreativeTabs {
    // CreativeTab注册器
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Fatality.MODID);

    // 饰品CreativeTab
    public static final RegistryObject<CreativeModeTab> FATALITY_ACCESSORY_TAB = CREATIVE_MODE_TABS.register("fatality_accessory_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.NAMELESS_CROWN.get()))
                    .title(Component.translatable("itemGroup.fatality.accessory"))
                    .displayItems((parameters, output) -> {
                        // 使用遍历方法添加所有饰品物品到CreativeTab
                        for (RegistryObject<Item> item : ModItems.getAccessoryItems()) {
                            output.accept(item.get());
                        }
                    })
                    .build());

    // 召唤物CreativeTab
    public static final RegistryObject<CreativeModeTab> FATALITY_SUMMON_TAB = CREATIVE_MODE_TABS.register("fatality_summon_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.TERMINUS_STONE.get()))
                    .title(Component.translatable("itemGroup.fatality.summon"))
                    .displayItems((parameters, output) -> {
                        // 使用遍历方法添加所有召唤物品到CreativeTab
                        for (RegistryObject<Item> item : ModItems.getSummonItems()) {
                            output.accept(item.get());
                        }
                    })
                    .build());

    // 物品CreativeTab
    public static final RegistryObject<CreativeModeTab> FATALITY_NORMAL_ITEM_TAB = CREATIVE_MODE_TABS.register("fatality_normal_item_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.ABYSSAL_DEMON_INGOT.get()))
                    .title(Component.translatable("itemGroup.fatality.normal_item"))
                    .displayItems((parameters, output) -> {
                        // 使用遍历方法添加所有普通物品到CreativeTab
                        for (RegistryObject<Item> item : ModItems.getNormalItems()) {
                            output.accept(item.get());
                        }
                    })
                    .build());

    // 武器CreativeTab
    public static final RegistryObject<CreativeModeTab> FATALITY_WEAPON_TAB = CREATIVE_MODE_TABS.register("fatality_weapon_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.SCYTHE_OF_THE_END.get()))
                    .title(Component.translatable("itemGroup.fatality.weapon"))
                    .displayItems((parameters, output) -> {
                        // 使用遍历方法添加所有武器物品到CreativeTab
                        for (RegistryObject<Item> item : ModItems.getWeaponItems()) {
                            output.accept(item.get());
                        }
                    })
                    .build());

    // 方块CreativeTab
    public static final RegistryObject<CreativeModeTab> FATALITY_BLOCK_TAB = CREATIVE_MODE_TABS.register("fatality_block_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocks.SPOTLIGHT_ALTAR_ITEM.get()))
                    .title(Component.translatable("itemGroup.fatality.block"))
                    .displayItems((parameters, output) -> {
                        // 使用遍历方法添加所有方块物品到CreativeTab
                        for (RegistryObject<Item> item : ModItems.getBlockItems()) {
                            output.accept(item.get());
                        }
                    })
                    .build());


    // 注册方法
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}