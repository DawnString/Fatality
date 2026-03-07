package cn.dawnstring.fatality.bosslist.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class OpenBossListPacket {
    public OpenBossListPacket() {}
    
    public OpenBossListPacket(FriendlyByteBuf buf) {}
    
    public void encode(FriendlyByteBuf buf) {}
    
    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                // 服务器端处理，发送打开界面的数据包给客户端
                // 这里需要实现客户端界面的打开逻辑
            }
        });
        context.get().setPacketHandled(true);
    }
    
    public static void sendToClient(ServerPlayer player) {
        // 发送给客户端的数据包
        // NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new OpenBossListPacket());
    }
}