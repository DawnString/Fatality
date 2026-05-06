package cn.dawnstring.fatality.events;

import cn.dawnstring.fatality.Fatality;
import cn.dawnstring.fatality.api.systems.IModSystem;
import cn.dawnstring.fatality.gamestage.GameStage;
import cn.dawnstring.fatality.gamestage.GameStageManager;
import cn.dawnstring.fatality.network.GameEventSyncPacket;
import cn.dawnstring.fatality.network.NetworkManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class GameEventManager implements IModSystem {

    private static final Logger LOGGER = Fatality.LOGGER;
    private static final GameEventManager INSTANCE = new GameEventManager();
    private static final Map<Level, ActiveEvent> activeEvents = new HashMap<>();
    private static final Map<Level, ActiveEvent> clientActiveEvents = new HashMap<>();

    public static GameEventManager getInstance() {
        return INSTANCE;
    }

    @Override
    public String getSystemId() {
        return "game_event";
    }

    @Override
    public void initialize() {
        LOGGER.info("GameEvent system initialized");
    }

    public static class ActiveEvent {
        public final GameEvent event;
        public final GameEvent.EventVersion version;
        public final long startDayTime;
        public final int durationInDays;

        public ActiveEvent(GameEvent event, GameEvent.EventVersion version, long startDayTime, int durationInDays) {
            this.event = event;
            this.version = version;
            this.startDayTime = startDayTime;
            this.durationInDays = durationInDays;
        }

        public boolean shouldEnd(ServerLevel level) {
            long currentDayTime = level.getDayTime();
            long eventEndTime = startDayTime + (long) durationInDays * 24000L;
            long currentTimeOfDay = currentDayTime % 24000;

            if (event == GameEvent.BLOOD_MOON) {
                if (currentTimeOfDay >= 0 && currentTimeOfDay < 12000) {
                    return true;
                }
            } else if (event == GameEvent.SOLAR_ECLIPSE) {
                if (currentTimeOfDay >= 12000 || currentTimeOfDay < 0) {
                    return true;
                }
            }

            return currentDayTime >= eventEndTime;
        }

        public long getRemainingTime(ServerLevel level) {
            long currentDayTime = level.getDayTime();
            long eventEndTime = startDayTime + (long) durationInDays * 24000L;
            return Math.max(0, eventEndTime - currentDayTime);
        }
    }

    @Override
    public void onServerTick() {
        for (Map.Entry<Level, ActiveEvent> entry : new HashMap<>(activeEvents).entrySet()) {
            Level level = entry.getKey();
            ActiveEvent activeEvent = entry.getValue();

            if (level instanceof ServerLevel serverLevel) {
                if (activeEvent.shouldEnd(serverLevel)) {
                    endEvent(serverLevel);
                }
            }
        }
    }

    public static boolean hasActiveEvent(Level level) {
        return activeEvents.containsKey(level);
    }

    public static void startEvent(ServerLevel level, GameEvent event, GameEvent.EventVersion version) {
        long currentDayTime = level.getDayTime();
        int duration = event.getDuration();

        ActiveEvent activeEvent = new ActiveEvent(event, version, currentDayTime, duration / 24000);
        activeEvents.put(level, activeEvent);

        String message = "§6事件 " + event.getDisplayName() + " 已开始！";
        level.getServer().getPlayerList().broadcastSystemMessage(
                net.minecraft.network.chat.Component.literal(message), false);

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                    new GameEventSyncPacket(event.getId(), version.getName(), currentDayTime, duration / 24000));
        }

        LOGGER.info("Event triggered: {} in world {}", event.name(), level.dimension().location());
    }

    public static void endEvent(ServerLevel level) {
        ActiveEvent removed = activeEvents.remove(level);
        if (removed != null) {
            String message = "§6事件 " + removed.event.getDisplayName() + " 已结束。";
            level.getServer().getPlayerList().broadcastSystemMessage(
                    net.minecraft.network.chat.Component.literal(message), false);

            for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                        new GameEventSyncPacket("", "", 0, 0));
            }

            LOGGER.info("Event ended: {} in world {}", removed.event.name(), level.dimension().location());
        }
    }

    public static ActiveEvent getActiveEvent(Level level) {
        return activeEvents.get(level);
    }

    public static float getSpawnRateMultiplier(Level level) {
        ActiveEvent active = activeEvents.get(level);
        if (active != null) {
            return active.version.getSpawnRateMultiplier();
        }
        return 1.0f;
    }

    public static float getEntitySpawnMultiplier(Level level, EntityType<?> entityType) {
        ActiveEvent active = activeEvents.get(level);
        if (active == null) return 1.0f;

        if (active.version.getIncreasedSpawnTypes().contains(entityType)) {
            return active.version.getSpawnRateMultiplier();
        }
        if (active.version.getDecreasedSpawnTypes().contains(entityType)) {
            return active.version.getSpawnCapMultiplier();
        }
        return 1.0f;
    }

    public static void setClientActiveEvent(Level level, GameEventManager.ActiveEvent event) {
        if (event != null) {
            clientActiveEvents.put(level, event);
        } else {
            clientActiveEvents.remove(level);
        }
    }

    public static void removeClientEvent(Level level) {
        clientActiveEvents.remove(level);
    }

    public static GameEventManager.ActiveEvent getClientActiveEvent(Level level) {
        return clientActiveEvents.get(level);
    }

    public static boolean isClientEventActive(Level level) {
        return clientActiveEvents.containsKey(level);
    }
}
