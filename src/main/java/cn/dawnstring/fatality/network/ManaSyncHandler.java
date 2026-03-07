package cn.dawnstring.fatality.network;

import cn.dawnstring.fatality.system.ManaSystem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import cn.dawnstring.fatality.Fatality;

@Mod.EventBusSubscriber(modid = Fatality.MODID)
public class ManaSyncHandler {
    
    /**
     * 向客户端同步魔法数据
     */
    public static void syncManaDataToClient(ServerPlayer player) {
        if (player == null || player.level().isClientSide()) return;
        
        // 获取玩家的魔法数据
        float currentMana = ManaSystem.getCurrentMana(player);
        float maxMana = ManaSystem.getMaxMana(player);
        float bonusMana = ManaSystem.getBonusMana(player);
        
        // 创建并发送同步包
        ManaSyncPacket packet = new ManaSyncPacket(currentMana, maxMana, bonusMana);
        NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
    
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncManaDataToClient(serverPlayer);
        }
    }
    
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncManaDataToClient(serverPlayer);
        }
    }
    
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncManaDataToClient(serverPlayer);
        }
    }
}