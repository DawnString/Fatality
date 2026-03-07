package cn.dawnstring.fatality.bosslist;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import cn.dawnstring.fatality.entity.boss.BossList;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BossProgressManager {
    private static final String BOSS_PROGRESS_TAG = "fatality_boss_progress";
    
    // 存储玩家boss进度的缓存
    private static final Map<UUID, Map<BossList, BossProgress>> playerProgressCache = new HashMap<>();
    
    public static class BossProgress {
        public boolean defeated;
        public int attempts;
        public long bestTime; // 毫秒
        
        public BossProgress() {
            this.defeated = false;
            this.attempts = 0;
            this.bestTime = 0;
        }
        
        public void saveToNBT(CompoundTag tag) {
            tag.putBoolean("defeated", defeated);
            tag.putInt("attempts", attempts);
            tag.putLong("bestTime", bestTime);
        }
        
        public void loadFromNBT(CompoundTag tag) {
            defeated = tag.getBoolean("defeated");
            attempts = tag.getInt("attempts");
            bestTime = tag.getLong("bestTime");
        }
    }
    
    public static boolean isBossDefeated(Player player, BossList boss) {
        if (player == null) return false;
        
        BossProgress progress = getBossProgress(player, boss);
        return progress.defeated;
    }
    
    public static int getAttempts(Player player, BossList boss) {
        if (player == null) return 0;
        
        BossProgress progress = getBossProgress(player, boss);
        return progress.attempts;
    }
    
    public static long getBestTime(Player player, BossList boss) {
        if (player == null) return 0;
        
        BossProgress progress = getBossProgress(player, boss);
        return progress.bestTime;
    }
    
    public static void recordBossAttempt(Player player, BossList boss) {
        if (player == null) return;
        
        BossProgress progress = getBossProgress(player, boss);
        progress.attempts++;
        savePlayerProgress(player);
    }
    
    public static void recordBossDefeat(Player player, BossList boss, long timeTaken) {
        if (player == null) return;
        
        BossProgress progress = getBossProgress(player, boss);
        progress.defeated = true;
        if (progress.bestTime == 0 || timeTaken < progress.bestTime) {
            progress.bestTime = timeTaken;
        }
        savePlayerProgress(player);
    }
    
    private static BossProgress getBossProgress(Player player, BossList boss) {
        UUID playerUUID = player.getUUID();
        
        // 从缓存获取
        Map<BossList, BossProgress> playerMap = playerProgressCache.computeIfAbsent(playerUUID, k -> new HashMap<>());
        
        if (!playerMap.containsKey(boss)) {
            // 从NBT加载
            BossProgress progress = new BossProgress();
            CompoundTag playerData = player.getPersistentData();
            if (playerData.contains(BOSS_PROGRESS_TAG)) {
                CompoundTag progressTag = playerData.getCompound(BOSS_PROGRESS_TAG);
                if (progressTag.contains(boss.name())) {
                    CompoundTag bossTag = progressTag.getCompound(boss.name());
                    progress.loadFromNBT(bossTag);
                }
            }
            playerMap.put(boss, progress);
        }
        
        return playerMap.get(boss);
    }
    
    private static void savePlayerProgress(Player player) {
        UUID playerUUID = player.getUUID();
        Map<BossList, BossProgress> playerMap = playerProgressCache.get(playerUUID);
        
        if (playerMap != null) {
            CompoundTag playerData = player.getPersistentData();
            CompoundTag progressTag = new CompoundTag();
            
            for (Map.Entry<BossList, BossProgress> entry : playerMap.entrySet()) {
                CompoundTag bossTag = new CompoundTag();
                entry.getValue().saveToNBT(bossTag);
                progressTag.put(entry.getKey().name(), bossTag);
            }
            
            playerData.put(BOSS_PROGRESS_TAG, progressTag);
        }
    }
    
    // 当玩家退出游戏时清理缓存
    public static void onPlayerLogout(Player player) {
        if (player != null) {
            playerProgressCache.remove(player.getUUID());
        }
    }
    
    // 重置玩家boss进度（用于调试）
    public static void resetPlayerProgress(Player player) {
        if (player != null) {
            UUID playerUUID = player.getUUID();
            playerProgressCache.remove(playerUUID);
            
            CompoundTag playerData = player.getPersistentData();
            if (playerData.contains(BOSS_PROGRESS_TAG)) {
                playerData.remove(BOSS_PROGRESS_TAG);
            }
        }
    }
}