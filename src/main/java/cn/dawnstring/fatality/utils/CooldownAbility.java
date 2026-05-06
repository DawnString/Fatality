package cn.dawnstring.fatality.utils;

import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownAbility {

    private static final Map<String, Map<UUID, Long>> cooldownMaps = new HashMap<>();

    public static boolean canTrigger(String abilityId, Player player, long cooldownMs) {
        Map<UUID, Long> map = cooldownMaps.computeIfAbsent(abilityId, k -> new HashMap<>());
        UUID playerId = player.getUUID();
        long currentTime = System.currentTimeMillis();
        Long lastTrigger = map.get(playerId);
        if (lastTrigger != null && currentTime - lastTrigger < cooldownMs) {
            return false;
        }
        map.put(playerId, currentTime);
        return true;
    }

    public static void resetCooldown(String abilityId, Player player) {
        Map<UUID, Long> map = cooldownMaps.get(abilityId);
        if (map != null) {
            map.remove(player.getUUID());
        }
    }

    public static void cleanupPlayer(Player player) {
        UUID playerId = player.getUUID();
        for (Map<UUID, Long> map : cooldownMaps.values()) {
            map.remove(playerId);
        }
    }
}
