package cn.dawnstring.fatality.gamestage;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

/**
 * 游戏阶段命令 - 基于世界阶段
 */
public class GameStageCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // 主要命令：/gamestage
        dispatcher.register(Commands.literal("gamestage")
                .requires(source -> source.hasPermission(2)) // 需要OP权限

                // 设置阶段：/gamestage set <阶段ID>
                .then(Commands.literal("set")
                        .then(Commands.argument("stage", StringArgumentType.string())
                                .executes(context -> {
                                    ServerLevel level = context.getSource().getLevel();
                                    String stageId = StringArgumentType.getString(context, "stage").toLowerCase();

                                    GameStage stage = GameStage.getById(stageId);
                                    if (stage != null) {
                                        GameStageManager.setWorldStage(level, stage);
                                        context.getSource().sendSuccess(() ->
                                                        Component.literal("§a已将游戏阶段设置为: §6" + stage.getDisplayName()),
                                                true);
                                        return 1;
                                    } else {
                                        context.getSource().sendFailure(Component.literal("§c无效的游戏阶段: " + stageId));
                                        return 0;
                                    }
                                }))
                )

                // 查看阶段：/gamestage get
                .then(Commands.literal("get")
                        .executes(context -> {
                            ServerLevel level = context.getSource().getLevel();
                            GameStage stage = GameStageManager.getWorldStage(level);

                            context.getSource().sendSuccess(() ->
                                            Component.literal("§a当前游戏阶段: §6" + stage.getDisplayName() +
                                                    " §7(生命增益: " + stage.getHealthMultiplier() + "x, " +
                                                    "伤害增益: " + stage.getDamageMultiplier() + "x)"),
                                    false);
                            return 1;
                        })
                )

                // 列出阶段：/gamestage list
                .then(Commands.literal("list")
                        .executes(context -> {
                            StringBuilder stages = new StringBuilder("§6=== 所有游戏阶段 ===\n");
                            for (GameStage stage : GameStage.values()) {
                                stages.append("§a- §e").append(stage.getId()).append(": §b").append(stage.getDisplayName())
                                        .append(" §7(Boss: ").append(stage.getBossEntityType().getDescription().getString())
                                        .append(")\n");
                            }
                            stages.append("§7使用命令: §f/gamestage set <阶段ID>");

                            context.getSource().sendSuccess(() -> Component.literal(stages.toString()), false);
                            return 1;
                        })
                )

                // 重置阶段：/gamestage reset
                .then(Commands.literal("reset")
                        .executes(context -> {
                            ServerLevel level = context.getSource().getLevel();
                            GameStageManager.resetWorldStage(level);
                            context.getSource().sendSuccess(() ->
                                            Component.literal("§a已重置游戏阶段为第一阶段"),
                                    true);
                            return 1;
                        })
                )

                // 直接命令：/gamestage
                .executes(context -> {
                    ServerLevel level = context.getSource().getLevel();
                    GameStage stage = GameStageManager.getWorldStage(level);

                    context.getSource().sendSuccess(() ->
                                    Component.literal("§a当前游戏阶段: §6" + stage.getDisplayName() +
                                            " §7(使用 §f/gamestage list §7查看所有阶段)"),
                            false);
                    return 1;
                })
        );
    }
}