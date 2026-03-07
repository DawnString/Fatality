package cn.dawnstring.fatality.events;

import cn.dawnstring.fatality.gamestage.GameStage;
import cn.dawnstring.fatality.gamestage.GameStageManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 游戏事件管理器 - 集成游戏阶段系统
 */
@Mod.EventBusSubscriber
public class GameEventManager {
    private static final Map<Level, ActiveEvent> activeEvents = new HashMap<>();
    private static final Random random = new Random();

    /**
     * 活跃事件信息
     */
    public static class ActiveEvent {
        public final GameEvent event;
        public final GameEvent.EventVersion version;
        public final long startDayTime; // 改为存储开始时的世界时间
        public final int durationInDays; // 持续时间以天为单位

        public ActiveEvent(GameEvent event, GameEvent.EventVersion version, long startDayTime, int durationInDays) {
            this.event = event;
            this.version = version;
            this.startDayTime = startDayTime;
            this.durationInDays = durationInDays;
        }

        /**
          * 检查事件是否应该结束（基于世界时间和事件类型）
         */
        public boolean shouldEnd(ServerLevel level) {
            long currentDayTime = level.getDayTime();
            long eventEndTime = startDayTime + (long) durationInDays * 24000L;
            
            // 根据事件类型检查时间限制
            long currentTimeOfDay = currentDayTime % 24000;
            
            if (event == GameEvent.BLOOD_MOON) {
                // 血月事件：只在18:00到6:00有效（夜晚）
                // 18:00对应18000时间刻，6:00对应0时间刻（日出）
                // 如果当前时间不在夜晚（6:00到18:00之间），则结束事件
                if (currentTimeOfDay >= 0 && currentTimeOfDay < 12000) {
                    return true; // 进入白天，结束血月
                }
            } else if (event == GameEvent.SOLAR_ECLIPSE) {
                // 日食事件：只在6:00到18:00有效（白天）
                // 6:00对应0时间刻（日出），18:00对应18000时间刻
                // 如果当前时间不在白天（18:00到6:00之间），则结束事件
                if (currentTimeOfDay >= 12000 || currentTimeOfDay < 0) {
                    return true; // 进入夜晚，结束日食
                }
            }
            
            // 如果当前时间已经超过了事件结束时间
            return currentDayTime >= eventEndTime;
        }

        /**
         * 获取事件剩余时间（以ticks为单位）
         */
        public long getRemainingTime(ServerLevel level) {
            long currentDayTime = level.getDayTime();
            long eventEndTime = startDayTime + (long) durationInDays * 24000L;
            return Math.max(0, eventEndTime - currentDayTime);
        }
    }

    /**
     * 服务器tick事件处理
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // 处理所有活跃世界的事件
        for (Map.Entry<Level, ActiveEvent> entry : new HashMap<>(activeEvents).entrySet()) {
            Level level = entry.getKey();
            ActiveEvent activeEvent = entry.getValue();

            if (level instanceof ServerLevel serverLevel) {
                // 检查事件是否结束（基于世界时间）
                if (activeEvent.shouldEnd(serverLevel)) {
                    endEvent(serverLevel);
                    continue;
                }

                // 应用事件效果
                applyEventEffects(serverLevel, activeEvent);
            }
        }
    }

    /**
     * 世界tick事件处理 - 检查事件触发
     */
    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ServerLevel serverLevel)) return;

        // 检查是否已经有活跃事件
        if (activeEvents.containsKey(serverLevel)) {
            return;
        }

        // 检查当前时间，根据时间决定触发哪种事件
        long currentTimeOfDay = serverLevel.getDayTime() % 24000;
        
        // 血月触发时间：18:00到6:00（夜晚）
        // 18:00对应18000时间刻，6:00对应0时间刻（日出）
        boolean isBloodMoonTime = currentTimeOfDay >= 18000 || currentTimeOfDay < 0;
        
        // 日食触发时间：6:00到18:00（白天）
        // 6:00对应0时间刻（日出），18:00对应18000时间刻
        boolean isSolarEclipseTime = currentTimeOfDay >= 0 && currentTimeOfDay < 18000;
        
        // 只在特定时间窗口检查触发
        // 血月：在18:00-18:10（18000-18100时间刻）触发检查
        // 日食：在6:00-6:10（0-100时间刻）触发检查
        if ((isBloodMoonTime && currentTimeOfDay >= 18000 && currentTimeOfDay <= 18100) ||
            (isSolarEclipseTime && currentTimeOfDay >= 0 && currentTimeOfDay <= 100)) {
            checkEventTrigger(serverLevel, isBloodMoonTime ? GameEvent.BLOOD_MOON : GameEvent.SOLAR_ECLIPSE);
        }
    }

    /**
     * 检查事件触发（指定事件类型）
     */
    private static void checkEventTrigger(ServerLevel level, GameEvent specificEvent) {
        // 获取当前游戏阶段
        GameStage currentStage = GameStageManager.getWorldStage(level);
        if (currentStage == null) {
            currentStage = GameStage.STAGE_1; // 默认第一阶段
        }

        // 只检查指定的事件类型
        if (specificEvent.shouldTrigger(level, random)) {
            // 根据当前游戏阶段获取对应的事件版本
            GameEvent.EventVersion version = specificEvent.getVersionForStage(currentStage);
            startEvent(level, specificEvent, version);
        }
    }

    /**
     * 检查事件触发
     */
    private static void checkEventTrigger(ServerLevel level) {
        // 获取当前游戏阶段
        GameStage currentStage = GameStageManager.getWorldStage(level);
        if (currentStage == null) {
            currentStage = GameStage.STAGE_0; // 默认第一阶段
        }

        for (GameEvent gameEvent : GameEvent.values()) {
            if (gameEvent.shouldTrigger(level, random)) {
                // 根据当前游戏阶段获取对应的事件版本
                GameEvent.EventVersion version = gameEvent.getVersionForStage(currentStage);
                startEvent(level, gameEvent, version);
                break; // 一次只触发一个事件
            }
        }
    }

    /**
     * 开始事件
     */
    public static void startEvent(ServerLevel level, GameEvent event, GameEvent.EventVersion version) {
        // 计算持续时间（将tick转换为天数）
        int durationInDays = (int) Math.ceil(event.getDuration() / 24000.0);
        ActiveEvent activeEvent = new ActiveEvent(event, version, level.getDayTime(), durationInDays);
        activeEvents.put(level, activeEvent);

        // 应用初始效果
        applyEventEffects(level, activeEvent);

        // 广播事件开始消息（包含版本信息）
        String message = String.format("§c%s (%s) 开始了！将持续%d天",
                event.getDisplayName(), version.getName(), durationInDays);
        level.players().forEach(player ->
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(message)));
    }

    /**
     * 应用事件效果
     */
    private static void applyEventEffects(ServerLevel level, ActiveEvent activeEvent) {
        GameEvent event = activeEvent.event;
        GameEvent.EventVersion version = activeEvent.version;

        // 应用天气效果
        applyWeatherEffects(level, version);

        // 应用刷怪效果
        applySpawnEffects(level, version);
    }

    /**
     * 结束事件
     */
    public static void endEvent(ServerLevel level) {
        ActiveEvent activeEvent = activeEvents.remove(level);
        if (activeEvent != null) {
            // 广播事件结束消息
            String message = String.format("§a%s (%s) 结束了。",
                    activeEvent.event.getDisplayName(), activeEvent.version.getName());
            level.players().forEach(player ->
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(message)));

            // 重置天气效果
            resetWeatherEffects(level);
        }
    }

    /**
     * 应用天气效果
     */
    private static void applyWeatherEffects(ServerLevel level, GameEvent.EventVersion version) {
        ServerLevelData worldInfo = (ServerLevelData) level.getLevelData();

        // 设置天气持续时间（24000 ticks = 1天）
        level.setWeatherParameters(0, 24000, true, true);

        switch (version.getWeatherType()) {
            case "solar_eclipse_normal":
                // 普通日食：设置黑暗天气
                level.setWeatherParameters(24000, 0, true, false);
                break;

            case "solar_eclipse_enhanced":
                // 增强日食：设置更暗的天气
                level.setWeatherParameters(24000, 24000, true, true);
                break;

            case "solar_eclipse_ultimate":
                // 终极日食：重度黑暗，雷暴
                level.setWeatherParameters(24000, 24000, true, true);
                break;

            case "blood_moon_normal":
                // 普通血月：红色月光效果
                level.setWeatherParameters(0, 24000, false, true);
                break;

            case "blood_moon_enhanced":
                // 增强血月：中度红色月光
                level.setWeatherParameters(24000, 0, true, false);
                break;

            case "blood_moon_ultimate":
                // 终极血月：重度红色月光，雷暴
                level.setWeatherParameters(24000, 24000, true, true);
                break;
        }
    }

    /**
     * 重置天气效果
     */
    private static void resetWeatherEffects(ServerLevel level) {
        ServerLevelData worldInfo = (ServerLevelData) level.getLevelData();
        // 恢复默认天气
        worldInfo.setRaining(false);
        worldInfo.setThundering(false);
    }

    /**
     * 应用刷怪效果
     */
    private static void applySpawnEffects(ServerLevel level, GameEvent.EventVersion version) {
        // 这里可以通过修改刷怪规则或直接修改实体生成来实现
        // 实际实现可能需要更复杂的逻辑，这里提供框架
    }

    /**
     * 获取当前活跃事件
     */
    public static ActiveEvent getActiveEvent(Level level) {
        return activeEvents.get(level);
    }

    /**
     * 检查是否有活跃事件
     */
    public static boolean hasActiveEvent(Level level) {
        return activeEvents.containsKey(level);
    }

    /**
     * 获取事件刷怪乘数
     */
    public static float getSpawnRateMultiplier(Level level) {
        ActiveEvent activeEvent = getActiveEvent(level);
        if (activeEvent != null) {
            return activeEvent.version.getSpawnRateMultiplier();
        }
        return 1.0f;
    }

    /**
     * 检查实体类型是否受事件影响
     */
    public static boolean isEntityAffected(Level level, net.minecraft.world.entity.EntityType<?> entityType) {
        ActiveEvent activeEvent = getActiveEvent(level);
        if (activeEvent != null) {
            GameEvent.EventVersion version = activeEvent.version;
            return version.getIncreasedSpawnTypes().contains(entityType) ||
                    version.getDecreasedSpawnTypes().contains(entityType);
        }
        return false;
    }

    /**
     * 获取事件剩余时间（以天为单位）
     */
    public static double getRemainingTimeInDays(ServerLevel level) {
        ActiveEvent activeEvent = getActiveEvent(level);
        if (activeEvent != null) {
            long remainingTicks = activeEvent.getRemainingTime(level);
            return remainingTicks / 24000.0;
        }
        return 0.0;
    }

    /**
     * 获取特定实体类型的刷怪乘数
     */
    public static float getEntitySpawnMultiplier(Level level, net.minecraft.world.entity.EntityType<?> entityType) {
        ActiveEvent activeEvent = getActiveEvent(level);
        if (activeEvent != null) {
            GameEvent.EventVersion version = activeEvent.version;
            
            // 检查是否在增加刷怪的实体类型列表中
            if (version.getIncreasedSpawnTypes().contains(entityType)) {
                return version.getSpawnRateMultiplier();
            }
            
            // 检查是否在减少刷怪的实体类型列表中
            if (version.getDecreasedSpawnTypes().contains(entityType)) {
                return 1.0f / version.getSpawnRateMultiplier();
            }
        }
        
        // 默认乘数为1.0（无影响）
        return 1.0f;
    }
}