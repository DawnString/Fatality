package cn.dawnstring.fatality.system;

import cn.dawnstring.fatality.network.ManaSyncHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ManaRegenerationHandler {

    private static final float TICK_INTERVAL = 0.05f; // 每tick的时间间隔（20tick/秒）
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
                
                // 调用魔法恢复方法
                ManaSystem.regenerateMana(player, TICK_INTERVAL);
                
                // 检查魔法值是否发生变化，如果变化则同步到客户端
                float newMana = ManaSystem.getCurrentMana(player);
                if (oldMana != newMana && player instanceof ServerPlayer serverPlayer) {
                    // 每20tick同步一次，避免过于频繁的网络传输
                    syncCounter++;
                    if (syncCounter >= 20) {
                        ManaSyncHandler.syncManaDataToClient(serverPlayer);
                        syncCounter = 0;
                    }
                }
            }
        }
    }
}