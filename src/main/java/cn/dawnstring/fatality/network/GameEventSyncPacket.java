package cn.dawnstring.fatality.network;

import cn.dawnstring.fatality.events.GameEvent;
import cn.dawnstring.fatality.events.GameEventManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class GameEventSyncPacket {
    private final String eventId;
    private final String eventVersion;
    private final long startDayTime;
    private final int durationInDays;

    public GameEventSyncPacket(String eventId, String eventVersion, long startDayTime, int durationInDays) {
        this.eventId = eventId;
        this.eventVersion = eventVersion;
        this.startDayTime = startDayTime;
        this.durationInDays = durationInDays;
    }

    public static void encode(GameEventSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.eventId);
        buffer.writeUtf(packet.eventVersion);
        buffer.writeLong(packet.startDayTime);
        buffer.writeInt(packet.durationInDays);
    }

    public static GameEventSyncPacket decode(FriendlyByteBuf buffer) {
        return new GameEventSyncPacket(
                buffer.readUtf(),
                buffer.readUtf(),
                buffer.readLong(),
                buffer.readInt()
        );
    }

    public static void handle(GameEventSyncPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (context.get().getDirection().getReceptionSide().isClient()) {
                if (FMLEnvironment.dist == Dist.CLIENT) {
                    handleClientSide(packet);
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClientSide(GameEventSyncPacket packet) {
        var minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft.level != null) {
            // 如果事件ID为空，表示事件结束
            if (packet.eventId.isEmpty()) {
                GameEventManager.removeClientActiveEvent(minecraft.level);
                return;
            }

            GameEvent gameEvent = GameEvent.getById(packet.eventId);
            if (gameEvent != null) {
                GameEvent.EventVersion version = findEventVersion(gameEvent, packet.eventVersion);
                if (version != null) {
                    GameEventManager.ActiveEvent activeEvent = new GameEventManager.ActiveEvent(
                            gameEvent, version, packet.startDayTime, packet.durationInDays
                    );
                    GameEventManager.setClientActiveEvent(minecraft.level, activeEvent);
                }
            }
        }
    }

    private static GameEvent.EventVersion findEventVersion(GameEvent gameEvent, String versionName) {
        for (GameEvent.EventVersion version : gameEvent.getVersions()) {
            if (version.getName().equals(versionName)) {
                return version;
            }
        }
        return null;
    }
}