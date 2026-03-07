package cn.dawnstring.fatality.inventory;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import cn.dawnstring.fatality.items.AccessoryItem;
import cn.dawnstring.fatality.network.NetworkManager;
import cn.dawnstring.fatality.network.AccessorySyncPacket;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class AccessoryInventory implements ICapabilityProvider {
    public static final Capability<AccessoryInventory> CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() {});

    private final ItemStackHandler itemHandler = new ItemStackHandler(7) {
        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return stack.getItem() instanceof AccessoryItem;
        }

        @Override
        protected void onContentsChanged(int slot) {
            updatePlayerAttributes();
            // 同步到客户端
            if (player != null && !player.level().isClientSide()) {
                // 使用正确的网络发送方法
                NetworkManager.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                        new AccessorySyncPacket(slot, getStackInSlot(slot)));
            }
        }
    };

    private final LazyOptional<AccessoryInventory> holder = LazyOptional.of(() -> this);
    private Player player;

    // 存储每个饰品槽位对应的UUID映射，用于正确移除属性修改器
    private final Map<Integer, List<UUID>> accessoryModifierUUIDs = new HashMap<>();

    public AccessoryInventory(Player player) {
        this.player = player;
        // 初始化所有槽位的UUID列表
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            accessoryModifierUUIDs.put(i, new ArrayList<>());
        }
    }

    public static AccessoryInventory get(Player player) {
        return player.getCapability(CAPABILITY).orElse(new AccessoryInventory(player));
    }

    public void updatePlayerAttributes() {
        if (player == null) return;

        // 重置所有饰品效果
        resetPlayerAttributes();

        // 应用所有饰品效果
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (stack.getItem() instanceof AccessoryItem accessory) {
                // 先移除该槽位之前的所有修改器
                removeSlotModifiers(i);

                // 应用饰品效果
                accessory.applyEffects(player, stack);

                // 记录饰品应用的效果UUID（需要在饰品类中实现）
                recordAppliedModifiers(i, accessory, stack);
            }
        }
    }

    private void resetPlayerAttributes() {
        if (player == null) return;

        // 先调用每个饰品的removeEffects方法（这会移除饰品自己记录的UUID）
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (stack.getItem() instanceof AccessoryItem accessory) {
                accessory.removeEffects(player, stack);
            }
        }

        // 然后移除所有槽位的属性修改器（确保完全清理）
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            removeSlotModifiers(i);
        }

        // 重置飞行能力（只在非创造模式下）
        if (!player.isCreative()) {
            player.getAbilities().mayfly = false;
            if (player.getAbilities().flying) {
                player.getAbilities().flying = false;
            }
            player.onUpdateAbilities();
        }
    }

    /**
     * 移除指定槽位的所有属性修改器
     */
    private void removeSlotModifiers(int slot) {
        List<UUID> uuids = accessoryModifierUUIDs.get(slot);
        if (uuids != null) {
            for (UUID uuid : uuids) {
                removeAttributeModifierByUUID(uuid);
            }
            uuids.clear();
        }
    }

    /**
     * 根据UUID移除属性修改器
     */
    private void removeAttributeModifierByUUID(UUID uuid) {
        // 移除所有主要属性的修改器
        AttributeInstance[] attributes = {
                player.getAttribute(Attributes.MAX_HEALTH),
                player.getAttribute(Attributes.ATTACK_DAMAGE),
                player.getAttribute(Attributes.MOVEMENT_SPEED),
                player.getAttribute(Attributes.ARMOR),
                player.getAttribute(Attributes.ARMOR_TOUGHNESS),
                player.getAttribute(Attributes.ATTACK_SPEED)
        };

        for (AttributeInstance attribute : attributes) {
            if (attribute != null) {
                attribute.removeModifier(uuid);
            }
        }
    }

    /**
     * 记录饰品应用的修改器UUID（需要饰品类配合实现）
     */
    private void recordAppliedModifiers(int slot, AccessoryItem accessory, ItemStack stack) {
        // 获取饰品实际应用的UUID列表
        List<UUID> appliedUUIDs = accessory.getCurrentModifierUUIDs();
        List<UUID> slotUUIDs = accessoryModifierUUIDs.get(slot);

        if (slotUUIDs != null && appliedUUIDs != null) {
            // 清空当前槽位的UUID记录
            slotUUIDs.clear();
            // 记录饰品实际应用的所有UUID
            slotUUIDs.addAll(appliedUUIDs);
        }
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CAPABILITY) {
            return holder.cast();
        }
        return LazyOptional.empty();
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("Items", itemHandler.serializeNBT());
        return tag;
    }

    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("Items")) {
            itemHandler.deserializeNBT(nbt.getCompound("Items"));
        }
    }
}