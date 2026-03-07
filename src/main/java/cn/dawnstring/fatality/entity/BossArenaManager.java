package cn.dawnstring.fatality.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Boss战斗场地管理器
 * 负责管理战斗场地的创建、渲染、玩家限制等功能
 */
public class BossArenaManager {
    private final BaseBoss boss;
    private boolean arenaActive = false;
    private BlockPos arenaCenter;
    private int arenaSize;
    private Integer arenaHeight; // 可为null，表示无高度限制
    private List<Player> playersInArena = new ArrayList<>();
    private int boundaryParticleTimer = 0;
    private static final int BOUNDARY_PARTICLE_INTERVAL = 5; // 每5tick显示一次边界粒子

    public BossArenaManager(BaseBoss boss) {
        this.boss = boss;
    }

    /**
     * 激活战斗场地
     * @param center 场地中心坐标
     * @param size 场地大小（正方形边长）
     * @param height 场地高度限制（可为null）
     */
    public void activateArena(BlockPos center, int size, Integer height) {
        this.arenaCenter = center;
        this.arenaSize = size;
        this.arenaHeight = height;
        this.arenaActive = true;
        this.playersInArena.clear();

        // 通知玩家战斗开始
        broadcastMessage(Component.literal("§c" + boss.getDisplayName().getString() + "的战斗已经开始！"));
        broadcastMessage(Component.literal("§6战斗场地已激活，请不要离开场地范围。"));
    }

    /**
     * 停用战斗场地
     */
    public void deactivateArena()
    {
        this.arenaActive = false;
        this.playersInArena.clear();
    }

    /**
     * 检查玩家是否在战斗场地内
     */
    public boolean isPlayerInArena(Player player) {
        if (!arenaActive || arenaCenter == null) return true;

        double dx = Math.abs(player.getX() - arenaCenter.getX());
        double dz = Math.abs(player.getZ() - arenaCenter.getZ());
        boolean inHorizontalBounds = dx <= arenaSize / 2.0 && dz <= arenaSize / 2.0;

        // 检查高度限制（如果有）
        boolean inVerticalBounds = true;
        if (arenaHeight != null) {
            double dy = Math.abs(player.getY() - arenaCenter.getY());
            inVerticalBounds = dy <= arenaHeight / 2.0;
        }

        return inHorizontalBounds && inVerticalBounds;
    }

    /**
     * 检查所有玩家是否在场地内，并将离开的玩家传送回场地
     */
    public void checkPlayersInArena() {
        if (!arenaActive || arenaCenter == null || boss.level().isClientSide()) return;

        ServerLevel serverLevel = (ServerLevel) boss.level();
        List<ServerPlayer> players = serverLevel.players();

        for (ServerPlayer player : players) {
            if (player.isAlive()) {
                if (!isPlayerInArena(player)) {
                    teleportPlayerToArena(player);
                } else {
                    // 记录在场地内的玩家
                    if (!playersInArena.contains(player)) {
                        playersInArena.add(player);
                    }
                }
            }
        }

        // 清理已死亡或离线的玩家
        playersInArena.removeIf(player -> !player.isAlive() || player.level() != boss.level());
    }

    /**
     * 将玩家传送回战斗场地
     */
    private void teleportPlayerToArena(Player player) {
        if (!arenaActive || arenaCenter == null || boss.level().isClientSide()) return;

        // 计算安全位置（场地边界内2格）
        double halfSize = arenaSize / 2.0;
        double safeX = Math.max(arenaCenter.getX() - halfSize + 2,
                Math.min(arenaCenter.getX() + halfSize - 2, player.getX()));
        double safeZ = Math.max(arenaCenter.getZ() - halfSize + 2,
                Math.min(arenaCenter.getZ() + halfSize - 2, player.getZ()));
        double safeY = findSafeYPosition(safeX, safeZ);

        player.teleportTo(safeX, safeY, safeZ);

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.resetPosition();
            player.sendSystemMessage(Component.literal("§e你被传送回了战斗场地！"));
        }
    }

    /**
     * 寻找安全的高度位置
     */
    private double findSafeYPosition(double x, double z) {
        if (boss.level().isClientSide()) {
            return arenaCenter != null ? arenaCenter.getY() : boss.getY();
        }

        ServerLevel serverLevel = (ServerLevel) boss.level();
        for (int y = (int)boss.getY(); y < boss.getY() + 10; y++) {
            BlockPos checkPos = new BlockPos((int)x, y, (int)z);
            if (serverLevel.getBlockState(checkPos).isAir() &&
                    serverLevel.getBlockState(checkPos.above()).isAir()) {
                return y;
            }
        }
        return arenaCenter.getY();
    }

    /**
     * 显示战斗场地边界粒子效果
     */
    public void showBoundaryParticles() {
        if (!arenaActive || arenaCenter == null || boss.level().isClientSide()) return;

        boundaryParticleTimer++;
        if (boundaryParticleTimer < BOUNDARY_PARTICLE_INTERVAL) return;
        boundaryParticleTimer = 0;

        ServerLevel serverLevel = (ServerLevel) boss.level();
        double halfSize = arenaSize / 2.0;
        Vector3f color = new Vector3f(1.0f, 0.0f, 0.0f); // 红色粒子
        DustParticleOptions particleOptions = new DustParticleOptions(color, 1.0f);

        // 显示边界粒子效果
        for (int i = 0; i < 4; i++) {
            double cornerX = (i % 2 == 0) ? arenaCenter.getX() - halfSize : arenaCenter.getX() + halfSize;
            double cornerZ = (i < 2) ? arenaCenter.getZ() - halfSize : arenaCenter.getZ() + halfSize;

            // 显示垂直边界
            double heightLimit = arenaHeight != null ? arenaHeight : 50; // 默认50格高度
            for (int y = arenaCenter.getY(); y < arenaCenter.getY() + heightLimit; y += 3) {
                serverLevel.sendParticles(particleOptions, cornerX, y, cornerZ, 1, 0.1, 0.1, 0.1, 0);
            }
        }
    }

    /**
     * 检查是否有玩家在战斗场地内
     */
    public boolean hasPlayersInArena() {
        if (!arenaActive) return false;

        if (boss.level().isClientSide()) return true; // 客户端总是返回true

        ServerLevel serverLevel = (ServerLevel) boss.level();
        List<ServerPlayer> players = serverLevel.players();

        return players.stream().anyMatch(player ->
                player.isAlive() && isPlayerInArena(player));
    }

    /**
     * 处理玩家死亡事件
     */
    public void onPlayerDeath(Player player) {
        if (!arenaActive) return;

        playersInArena.remove(player);

        // 检查是否所有玩家都死亡
        if (!hasPlayersInArena()) {
            deactivateArena();
            boss.discard(); // Boss消失
        }
    }

    /**
     * 处理Boss死亡事件
     */
    public void onBossDeath() {
        if (arenaActive)
        {
            deactivateArena();
        }
    }

    /**
     * 广播消息给所有在战斗场地内的玩家
     */
    private void broadcastMessage(Component message) {
        if (boss.level().isClientSide()) return;

        ServerLevel serverLevel = (ServerLevel) boss.level();
        serverLevel.players().forEach(player -> {
            if (isPlayerInArena(player)) {
                player.sendSystemMessage(message);
            }
        });
    }

    // Getter方法
    public boolean isArenaActive() {
        return arenaActive;
    }

    public BlockPos getArenaCenter() {
        return arenaCenter;
    }

    public int getArenaSize() {
        return arenaSize;
    }

    public Integer getArenaHeight() {
        return arenaHeight;
    }

    public List<Player> getPlayersInArena() {
        return new ArrayList<>(playersInArena);
    }
}