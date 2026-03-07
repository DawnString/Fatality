package cn.dawnstring.fatality.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;
import cn.dawnstring.fatality.inventory.AccessoryInventory;

import java.util.function.Supplier;

public class AccessorySyncPacket {
    private final int slot;
    private final ItemStack stack;

    public AccessorySyncPacket(int slot, ItemStack stack) {
        this.slot = slot;
        this.stack = stack;
    }

    public static void encode(AccessorySyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.slot);
        buffer.writeItem(packet.stack);
    }

    public static AccessorySyncPacket decode(FriendlyByteBuf buffer) {
        return new AccessorySyncPacket(buffer.readInt(), buffer.readItem());
    }

    public static void handle(AccessorySyncPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            // 在客户端更新饰品栏
            if (context.get().getDirection().getReceptionSide().isClient()) {
                // 使用环境检查来避免服务端加载客户端代码
                if (FMLEnvironment.dist == Dist.CLIENT) {
                    handleClientSide(packet);
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClientSide(AccessorySyncPacket packet) {
        var minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft.player != null) {
            AccessoryInventory inventory = AccessoryInventory.get(minecraft.player);
            inventory.getItemHandler().setStackInSlot(packet.slot, packet.stack);
            inventory.updatePlayerAttributes();
        }
    }
}