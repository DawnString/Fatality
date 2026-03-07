package cn.dawnstring.fatality.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;
import cn.dawnstring.fatality.system.ManaSystem;

import java.util.function.Supplier;

/**
 * 魔法数据同步包 - 将服务器端的魔法数据同步到客户端
 */
public class ManaSyncPacket {
    private final float currentMana;
    private final float maxMana;
    private final float bonusMana;

    public ManaSyncPacket(float currentMana, float maxMana, float bonusMana) {
        this.currentMana = currentMana;
        this.maxMana = maxMana;
        this.bonusMana = bonusMana;
    }

    public static void encode(ManaSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeFloat(packet.currentMana);
        buffer.writeFloat(packet.maxMana);
        buffer.writeFloat(packet.bonusMana);
    }

    public static ManaSyncPacket decode(FriendlyByteBuf buffer) {
        return new ManaSyncPacket(
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat()
        );
    }

    public static void handle(ManaSyncPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            // 在客户端更新魔法数据
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
    private static void handleClientSide(ManaSyncPacket packet) {
        var minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft.player != null) {
            // 更新客户端的魔法数据缓存
            String playerId = minecraft.player.getUUID().toString();
            ManaSystem.updateClientManaData(playerId, packet.currentMana, packet.maxMana, packet.bonusMana);
            
            // 强制HUD重绘以确保实时显示更新后的魔法值
            // 在Minecraft中，GUI会在每帧自动重绘，我们只需要确保数据已更新
            // 如果需要强制重绘特定GUI元素，可以调用minecraft.gui.setTimes(0, 0, 0)来重置GUI计时器
            if (minecraft.screen == null) { // 只在游戏界面中重绘
                // 重置GUI计时器，强制下一帧重绘所有GUI元素
                minecraft.gui.setTimes(0, 0, 0);
            }
        }
    }
}