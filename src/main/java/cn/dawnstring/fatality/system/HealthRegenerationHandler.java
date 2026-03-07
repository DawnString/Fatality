package cn.dawnstring.fatality.system;

import cn.dawnstring.fatality.registry.ModEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class HealthRegenerationHandler {

    public static final float BASE_HEALTH_REGEN_RATE = 1.0f; // 基础每秒恢复1点生命值
    private static final float TICK_INTERVAL = 0.05f; // 每tick的时间间隔（20tick/秒）
    private static final float INTERVAL_TIME = 3.0f; // 受到伤害后恢复暂停3秒
    private static boolean isRegenerating = false; // 是否正在恢复生命值
    private static float timeSinceLastRegen = 0.0f; // 上次恢复生命值的时间

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

                if(player.isHurt())
                {
                    // 重置恢复时间
                    timeSinceLastRegen = 0.0f;
                    isRegenerating = false;
                }

                timeSinceLastRegen += TICK_INTERVAL;

                // 检查是否已过恢复间隔时间
                if (timeSinceLastRegen >= INTERVAL_TIME)
                {
                    isRegenerating = true;
                }
                else
                {
                    timeSinceLastRegen += TICK_INTERVAL;
                }

                if (isRegenerating)
                {
                    // 计算实际恢复速率（基础 + 饰品加成）
                    float actualRegenRate = AttributeSystem.getHealthRegenerationRate(player);
                    
                    // 检查是否有治疗饱和效果，减少50%生命恢复
                    if (player.hasEffect(ModEffects.TREATMENT_SATURATION.get())) {
                        actualRegenRate *= 0.5f; // 减少50%生命恢复
                    }

                    // 每tick恢复的生命值
                    float healthPerTick = actualRegenRate * TICK_INTERVAL;

                    // 如果玩家生命值不满，进行恢复
                    if (player.getHealth() < player.getMaxHealth()) {
                        float newHealth = Math.min(player.getMaxHealth(), player.getHealth() + healthPerTick);
                        player.setHealth(newHealth);
                    }
                }
            }
        }
    }
}