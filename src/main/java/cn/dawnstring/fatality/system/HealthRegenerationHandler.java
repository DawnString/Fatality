package cn.dawnstring.fatality.system;

import cn.dawnstring.fatality.registry.ModEffects;
import cn.dawnstring.fatality.system.accessories.AccessoryEffectHandlerManager;
import cn.dawnstring.fatality.utils.GameConstants;
import cn.dawnstring.fatality.utils.PlayerBaseAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 生命恢复处理器 - 迁移到新的事件驱动架构
 * 使用新的事件系统和饰品效果处理器
 */
@Mod.EventBusSubscriber
public class HealthRegenerationHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;

            // 只在服务器端执行
            if (!player.level().isClientSide()) {
                // 检查玩家是否死亡，如果死亡则不进行生命恢复
                if (player.isDeadOrDying()) {
                    return;
                }

                // 使用新的饰品效果处理器管理器来更新饰品效果
                AccessoryEffectHandlerManager.getInstance().updateAccessoryEffects(player);
            }
        }
    }

    /**
     * 计算实际的生命恢复速率（考虑基础值和饰品加成）
     * @param player 玩家
     * @return 实际恢复速率
     */
    public static float calculateActualRegenRate(Player player) {
        float baseRate = PlayerBaseAttributes.getBaseHealthRegenRate(player);
        float accessoryBonus = AttributeSystem.getHealthRegenerationRate(player) - baseRate;
        float actualRegenRate = baseRate + accessoryBonus;

        // 检查是否有治疗饱和效果，减少50%生命恢复
        if (player.hasEffect(ModEffects.TREATMENT_SATURATION.get())) {
            actualRegenRate *= GameConstants.TREATMENT_SATURATION_PENALTY;
        }

        return actualRegenRate;
    }
}