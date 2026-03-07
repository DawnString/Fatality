package cn.dawnstring.fatality.events;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 事件刷怪修改器 - 兼容Forge 1.20.1
 */
@Mod.EventBusSubscriber
public class EventSpawnModifier {

    /**
     * 检查特殊刷怪事件 - 使用MobSpawnEvent.FinalizeSpawn
     */
    @SubscribeEvent
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        Level level = event.getLevel().getLevel();

        if (!GameEventManager.hasActiveEvent(level)) {
            return;
        }

        EntityType<?> entityType = event.getEntity().getType();
        float multiplier = GameEventManager.getEntitySpawnMultiplier(level, entityType);

        // 如果乘数小于1，有概率阻止刷怪
        if (multiplier < 1.0f && level.getRandom().nextFloat() > multiplier) {
            event.setSpawnCancelled(true);
        }
    }

    /**
     * 修改刷怪率 - 使用MobSpawnEvent.PositionCheck
     */
    @SubscribeEvent
    public static void onPositionCheck(MobSpawnEvent.PositionCheck event) {
        Level level = event.getLevel().getLevel();

        if (!GameEventManager.hasActiveEvent(level)) {
            return;
        }

        // 应用刷怪率乘数
        float spawnRateMultiplier = GameEventManager.getSpawnRateMultiplier(level);

        // 如果乘数大于1，有概率允许在通常不允许的位置刷怪
        if (spawnRateMultiplier > 1.0f && event.getResult() == Event.Result.DENY) {
            if (level.getRandom().nextFloat() < (spawnRateMultiplier - 1.0f) * 0.05f) {
                event.setResult(Event.Result.DEFAULT);
            }
        }
    }

    /**
     * 修改生物消失行为 - 使用MobSpawnEvent.AllowDespawn
     * 注意：这个事件不能取消，只能修改结果
     */
    @SubscribeEvent
    public static void onAllowDespawn(MobSpawnEvent.AllowDespawn event) {
        Level level = event.getEntity().level();

        if (!GameEventManager.hasActiveEvent(level)) {
            return;
        }

        EntityType<?> entityType = event.getEntity().getType();
        float multiplier = GameEventManager.getEntitySpawnMultiplier(level, entityType);

        // 如果乘数大于1，有概率阻止生物消失（设置结果为允许）
        if (multiplier > 1.0f && level.getRandom().nextFloat() < (multiplier - 1.0f) * 0.1f) {
            event.setResult(Event.Result.ALLOW);
        }
    }
}