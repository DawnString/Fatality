package cn.dawnstring.fatality.events;

import cn.dawnstring.fatality.gamestage.GameStage;
import cn.dawnstring.fatality.gamestage.GameStageManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;

/**
 * 游戏事件命令 - 支持版本信息显示
 */
public class GameEventCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("fatalityevent")
                .requires(source -> source.hasPermission(2)) // 需要操作员权限
                .then(Commands.literal("start")
                        .then(Commands.argument("event", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    for (GameEvent event : GameEvent.values()) {
                                        builder.suggest(event.getId());
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String eventId = StringArgumentType.getString(context, "event");
                                    GameEvent event = GameEvent.getById(eventId);
                                    ServerLevel level = context.getSource().getLevel();

                                    if (event != null) {
                                        // 获取当前游戏阶段对应的版本
                                        GameStage currentStage = GameStageManager.getWorldStage(level);
                                        if (currentStage == null) {
                                            currentStage = GameStage.STAGE_1;
                                        }
                                        GameEvent.EventVersion version = event.getVersionForStage(currentStage);

                                        GameEventManager.startEvent(level, event, version);
                                        context.getSource().sendSuccess(() ->
                                                net.minecraft.network.chat.Component.literal(
                                                        String.format("成功触发事件: %s (%s)",
                                                                event.getDisplayName(), version.getName())), true);
                                        return 1;
                                    } else {
                                        context.getSource().sendFailure(net.minecraft.network.chat.Component.literal("未知的事件ID: " + eventId));
                                        return 0;
                                    }
                                })
                        )
                )
                .then(Commands.literal("stop")
                        .executes(context -> {
                            ServerLevel level = context.getSource().getLevel();
                            if (GameEventManager.hasActiveEvent(level)) {
                                GameEventManager.endEvent(level);
                                context.getSource().sendSuccess(() ->
                                        net.minecraft.network.chat.Component.literal("已结束当前事件"), true);
                                return 1;
                            } else {
                                context.getSource().sendFailure(net.minecraft.network.chat.Component.literal("当前没有活跃的事件"));
                                return 0;
                            }
                        })
                )
                .then(Commands.literal("info")
                        .executes(context -> {
                            ServerLevel level = context.getSource().getLevel();
                            GameEventManager.ActiveEvent activeEvent = GameEventManager.getActiveEvent(level);

                            if (activeEvent != null) {
                                GameEvent event = activeEvent.event;
                                GameEvent.EventVersion version = activeEvent.version;

                                // 计算剩余时间
                                long remainingTicks = activeEvent.getRemainingTime(level);
                                double remainingDays = remainingTicks / 24000.0;

                                String message = String.format(
                                        "当前事件: %s (%s)\n" +
                                                "持续时间: %.2f天 (剩余%.2f天)\n" +
                                                "刷怪率乘数: %.1fx\n" +
                                                "增强刷怪类型: %d种\n" +
                                                "减少刷怪类型: %d种",
                                        event.getDisplayName(), version.getName(),
                                        activeEvent.durationInDays, remainingDays,
                                        version.getSpawnRateMultiplier(),
                                        version.getIncreasedSpawnTypes().size(),
                                        version.getDecreasedSpawnTypes().size()
                                );

                                context.getSource().sendSuccess(() ->
                                        net.minecraft.network.chat.Component.literal(message), false);
                                return 1;
                            } else {
                                context.getSource().sendSuccess(() ->
                                        net.minecraft.network.chat.Component.literal("当前没有活跃的事件"), false);
                                return 0;
                            }
                        })
                )
                .then(Commands.literal("list")
                        .executes(context -> {
                            StringBuilder message = new StringBuilder("可用事件（基于游戏阶段）:\n");
                            for (GameEvent event : GameEvent.values()) {
                                message.append(String.format("- %s (%s) - 触发概率: %.1f%%\n",
                                        event.getDisplayName(), event.getId(),
                                        event.getTriggerProbability() * 100));

                                // 显示每个事件的版本信息
                                for (GameEvent.EventVersion version : event.getVersions()) {
                                    message.append(String.format("  └ %s (阶段%d-%d): %.1fx刷怪率, %d种增强怪物\n",
                                            version.getName(),
                                            version.getMinStage().ordinal() + 1,
                                            version.getMaxStage().ordinal() + 1,
                                            version.getSpawnRateMultiplier(),
                                            version.getIncreasedSpawnTypes().size()));
                                }
                            }
                            context.getSource().sendSuccess(() ->
                                    net.minecraft.network.chat.Component.literal(message.toString()), false);
                            return 1;
                        })
                )
                .then(Commands.literal("spawninfo")
                        .executes(context -> {
                            ServerLevel level = context.getSource().getLevel();
                            GameEventManager.ActiveEvent activeEvent = GameEventManager.getActiveEvent(level);

                            if (activeEvent != null) {
                                GameEvent.EventVersion version = activeEvent.version;
                                StringBuilder message = new StringBuilder("刷怪信息:\n");

                                message.append("增强刷怪类型:\n");
                                for (net.minecraft.world.entity.EntityType<?> entityType : version.getIncreasedSpawnTypes()) {
                                    message.append("  - ").append(entityType.getDescription().getString()).append("\n");
                                }

                                message.append("减少刷怪类型:\n");
                                for (net.minecraft.world.entity.EntityType<?> entityType : version.getDecreasedSpawnTypes()) {
                                    message.append("  - ").append(entityType.getDescription().getString()).append("\n");
                                }

                                context.getSource().sendSuccess(() ->
                                        net.minecraft.network.chat.Component.literal(message.toString()), false);
                                return 1;
                            } else {
                                context.getSource().sendFailure(net.minecraft.network.chat.Component.literal("当前没有活跃的事件"));
                                return 0;
                            }
                        })
                )
                .then(Commands.literal("debug")
                        .executes(context -> {
                            ServerLevel level = context.getSource().getLevel();

                            // 强制触发日食事件进行测试
                            GameStage currentStage = GameStageManager.getWorldStage(level);
                            if (currentStage == null) {
                                currentStage = GameStage.STAGE_1;
                            }

                            GameEvent.EventVersion version = GameEvent.SOLAR_ECLIPSE.getVersionForStage(currentStage);
                            GameEventManager.startEvent(level, GameEvent.SOLAR_ECLIPSE, version);

                            context.getSource().sendSuccess(() ->
                                    net.minecraft.network.chat.Component.literal("强制触发了日食事件进行测试"), true);
                            return 1;
                        })
                )
        );
    }
}