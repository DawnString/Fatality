package cn.dawnstring.fatality.utils;

import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 属性缓存系统
 * 缓存玩家属性计算结果，避免重复计算
 */
public class AttributeCache {
    
    private static final Map<Player, Map<String, CachedAttribute>> playerAttributeCache = new ConcurrentHashMap<>();
    private static final Map<Player, Long> playerCacheTimestamps = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION_MS = 1000; // 缓存有效期1秒
    
    /**
     * 获取缓存的属性值
     */
    public static float getCachedAttribute(Player player, String attributeName) {
        Map<String, CachedAttribute> playerCache = playerAttributeCache.get(player);
        if (playerCache == null) {
            return Float.NaN; // 缓存不存在
        }
        
        CachedAttribute cached = playerCache.get(attributeName);
        if (cached == null) {
            return Float.NaN; // 属性未缓存
        }
        
        // 检查缓存是否过期
        if (isCacheExpired(player)) {
            return Float.NaN; // 缓存已过期
        }
        
        return cached.value;
    }
    
    /**
     * 缓存属性值
     */
    public static void cacheAttribute(Player player, String attributeName, float value) {
        Map<String, CachedAttribute> playerCache = playerAttributeCache.computeIfAbsent(
            player, k -> new ConcurrentHashMap<>()
        );
        
        playerCache.put(attributeName, new CachedAttribute(value, System.currentTimeMillis()));
        playerCacheTimestamps.put(player, System.currentTimeMillis());
    }
    
    /**
     * 清除玩家缓存
     */
    public static void clearPlayerCache(Player player) {
        playerAttributeCache.remove(player);
        playerCacheTimestamps.remove(player);
    }
    
    /**
     * 清除所有缓存
     */
    public static void clearAllCache() {
        playerAttributeCache.clear();
        playerCacheTimestamps.clear();
    }
    
    /**
     * 检查缓存是否过期
     */
    private static boolean isCacheExpired(Player player) {
        Long timestamp = playerCacheTimestamps.get(player);
        if (timestamp == null) {
            return true;
        }
        
        long currentTime = System.currentTimeMillis();
        return (currentTime - timestamp) > CACHE_DURATION_MS;
    }
    
    /**
     * 缓存的属性数据
     */
    private static class CachedAttribute {
        final float value;
        final long timestamp;
        
        CachedAttribute(float value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }
    }
    
    // 私有构造函数，防止实例化
    private AttributeCache() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}