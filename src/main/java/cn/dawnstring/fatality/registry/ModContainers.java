package cn.dawnstring.fatality.registry;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.RegistryObject;
import cn.dawnstring.fatality.inventory.AccessoryContainer;

public class ModContainers {
    // 饰品容器
    public static final RegistryObject<MenuType<AccessoryContainer>> ACCESSORY_CONTAINER =
            ModRegistry.CONTAINERS.register("accessory_container",
                    () -> IForgeMenuType.create((windowId, inv, data) ->
                            new AccessoryContainer(windowId, inv)));
}