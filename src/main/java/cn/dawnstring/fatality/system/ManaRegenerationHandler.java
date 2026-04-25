package cn.dawnstring.fatality.system;

import cn.dawnstring.fatality.network.ManaSyncHandler;
import cn.dawnstring.fatality.system.accessories.AccessoryEffectHandlerManager;
import cn.dawnstring.fatality.utils.GameConstants;
import cn.dawnstring.fatality.utils.PlayerBaseAttributes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 魔法恢复处理器 - 迁移到新的事件驱动架构
 * 使用新的事件系统和饰品效果处理器
 */
@Mod.EventBusSubscriber
public class ManaRegenerationHandler {

    private static int syncCounter = 0; // 同步计数器，每20tick同步一次

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;

            // 只在服务器端执行
            if (!player.level().isClientSide())
            {
                // 保存恢复前的魔法值
                float oldMana = ManaSystem.getCurrentMana(player);
                
                // 调用魔法恢复方法（使用AttributeSystem获取恢复速率）
                ManaSystem.regenerateMana(player, GameConstants.TICK_INTERVAL);
                
                // 使用新的饰品效果处理器管理器来更新饰品效果
                AccessoryEffectHandlerManager.getInstance().updateAccessoryEffects(player);
                
                // 检查魔法值是否发生变化，如果变化则同步到客户端
                float newMana = ManaSystem.getCurrentMana(player);
                if (oldMana != newMana && player instanceof ServerPlayer serverPlayer) {
                    // 每20tick同步一次，避免过于频繁的网络传输
                    syncCounter++;
                    if (syncCounter >= GameConstants.SYNC_INTERVAL_TICKS) {
                        ManaSyncHandler.syncManaDataToClient(serverPlayer);
                        syncCounter = 0;
                    }
                }
            }
        }
    }

    /**
     * 计算实际的魔法恢复速率（考虑基础值和饰品加成）
     * @param player 玩家
     * @return 实际恢复速率
     */
    public static float calculateActualManaRegenRate(Player player) {
        float baseRate = PlayerBaseAttributes.getBaseManaRegenRate(player);
        float accessoryBonus = AttributeSystem.getManaRegenerationRate(player) - baseRate;
        return baseRate + accessoryBonus;
    }
}