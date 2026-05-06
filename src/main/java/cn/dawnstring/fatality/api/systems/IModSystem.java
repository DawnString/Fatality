package cn.dawnstring.fatality.api.systems;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public interface IModSystem {

    String getSystemId();

    void initialize();

    default void shutdown() {}

    default void onPlayerJoin(Player player) {}

    default void onPlayerLeave(Player player) {}

    default void onServerTick() {}

    default void onClientTick() {}

    default void onPlayerTick(Player player) {}

    default void onLivingDeath(LivingEntity killed) {}

    default void onWorldLoad(ServerLevel level) {}

    default void onPlayerRespawn(Player player) {}
}
