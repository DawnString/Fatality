package cn.dawnstring.fatality.registry;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import cn.dawnstring.fatality.Fatality;

public class ModRegistry {
    // 物品注册器
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Fatality.MODID);

    // 方块注册器
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Fatality.MODID);

    // 容器注册器
    public static final DeferredRegister<MenuType<?>> CONTAINERS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Fatality.MODID);
    // 实体注册器
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Fatality.MODID);

    // 药水效果注册器
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Fatality.MODID);


    // 注册方法
    public static void register(IEventBus eventBus)
    {
        // 注册注册器到事件总线
        ITEMS.register(eventBus);
        BLOCKS.register(eventBus);
        CONTAINERS.register(eventBus);
        ENTITIES.register(eventBus);
        EFFECTS.register(eventBus);

        // 静态初始化
        ModItems.getItems();
        ModBlocks.getBlocks();
        ModContainers.ACCESSORY_CONTAINER.getClass(); // 触发静态初始化
        // 初始化药水效果注册
        ModEffects.registerEffects();
        
        // 重要：触发ModEntities的静态初始化，确保实体类型在属性注册前已注册
        try {
            Class.forName("cn.dawnstring.fatality.registry.ModEntities");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        
        // 注册CreativeTab
        ModCreativeTabs.register(eventBus);
        
        // 最后注册实体属性注册器（确保所有DeferredRegister已完成注册）
        EntityAttributeRegistry.register(eventBus);
    }
}