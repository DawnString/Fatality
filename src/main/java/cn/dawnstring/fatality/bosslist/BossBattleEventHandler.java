package cn.dawnstring.fatality.bosslist;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import cn.dawnstring.fatality.entity.boss.BossList;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "fatality")
public class BossBattleEventHandler {
    
    // 存储boss战斗开始时间
    private static final Map<Player, Map<BossList, Long>> bossBattleStartTimes = new HashMap<>();
    
    /**
     * 记录boss战斗开始
     */
    public static void startBossBattle(Player player, BossList boss) {
        bossBattleStartTimes.computeIfAbsent(player, k -> new HashMap<>())
                           .put(boss, System.currentTimeMillis());
        
        // 记录尝试次数
        BossProgressManager.recordBossAttempt(player, boss);
    }
    
    /**
     * 记录boss战斗结束（击败）
     */
    public static void endBossBattle(Player player, BossList boss) {
        Map<BossList, Long> playerBattles = bossBattleStartTimes.get(player);
        if (playerBattles != null && playerBattles.containsKey(boss)) {
            long startTime = playerBattles.get(boss);
            long timeTaken = System.currentTimeMillis() - startTime;
            
            // 记录击败
            BossProgressManager.recordBossDefeat(player, boss, timeTaken);
            
            // 清理战斗记录
            playerBattles.remove(boss);
        }
    }
    
    /**
     * 处理boss死亡事件
     */
    @SubscribeEvent
    public static void onBossDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        
        // 检查是否是boss实体
        BossList boss = getBossFromEntity(entity);
        if (boss != null) {
            // 查找最近的玩家（作为击杀者）
            Player killer = entity.level().getNearestPlayer(entity, 50);
            if (killer != null) {
                endBossBattle(killer, boss);
            }
        }
    }
    
    /**
     * 根据实体判断对应的boss
     */
    private static BossList getBossFromEntity(LivingEntity entity) {
        // 这里需要根据实体类型映射到BossList中的枚举
        // 暂时返回null，需要根据具体实现来完善
        
        // 示例实现：根据实体名称或类型进行映射
        String entityName = entity.getType().toString();
        
        // 这里应该根据实际的boss实体类型进行映射
        // 例如：
        // if (entityName.contains("wither")) {
        //     return BossList.wither;
        // } else if (entityName.contains("ender_dragon")) {
        //     return BossList.End_Dragon;
        // }
        
        // 暂时返回null，需要在实际的boss实体实现中完善此方法
        return null;
    }
    
    /**
     * 玩家登出时清理数据
     */
    public static void onPlayerLogout(Player player) {
        bossBattleStartTimes.remove(player);
        BossProgressManager.onPlayerLogout(player);
    }
}