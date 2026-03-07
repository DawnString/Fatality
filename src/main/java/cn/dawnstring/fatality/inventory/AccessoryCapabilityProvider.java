package cn.dawnstring.fatality.inventory;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import cn.dawnstring.fatality.Fatality;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = Fatality.MODID)
public class AccessoryCapabilityProvider implements ICapabilitySerializable<CompoundTag> {
    private final AccessoryInventory inventory;
    private final LazyOptional<AccessoryInventory> optional;

    public AccessoryCapabilityProvider(Player player) {
        this.inventory = new AccessoryInventory(player);
        this.optional = LazyOptional.of(() -> inventory);
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(ResourceLocation.fromNamespaceAndPath(Fatality.MODID, "accessory_inventory"),
                    new AccessoryCapabilityProvider((Player) event.getObject()));
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            // 玩家死亡时复制饰品数据
            event.getOriginal().reviveCaps();
            event.getOriginal().getCapability(AccessoryInventory.CAPABILITY).ifPresent(oldStore -> {
                event.getEntity().getCapability(AccessoryInventory.CAPABILITY).ifPresent(newStore -> {
                    newStore.deserializeNBT(oldStore.serializeNBT());
                });
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        // 玩家登录时更新属性
        AccessoryInventory.get(event.getEntity()).updatePlayerAttributes();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == AccessoryInventory.CAPABILITY) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return inventory.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        inventory.deserializeNBT(nbt);
    }
}