package cn.dawnstring.fatality.network;

import cn.dawnstring.fatality.Fatality;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;
import cn.dawnstring.fatality.client.DamageIndicatorManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class DamageIndicatorPacket {
    private static final Logger LOGGER = Fatality.LOGGER;
    private final int targetId;
    private final float damage;
    private final int attackerId;

    public DamageIndicatorPacket(int targetId, float damage, int attackerId) {
        this.targetId = targetId;
        this.damage = damage;
        this.attackerId = attackerId;
    }

    public static void encode(DamageIndicatorPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.targetId);
        buffer.writeFloat(packet.damage);
        buffer.writeInt(packet.attackerId);
    }

    public static DamageIndicatorPacket decode(FriendlyByteBuf buffer) {
        return new DamageIndicatorPacket(buffer.readInt(), buffer.readFloat(), buffer.readInt());
    }

    public static void handle(DamageIndicatorPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            // 在客户端处理伤害显示
            if (context.get().getDirection().getReceptionSide().isClient()) {
                // 使用环境检查来避免服务端加载客户端代码
                if (FMLEnvironment.dist == Dist.CLIENT) {
                    handleClientSide(packet);
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClientSide(DamageIndicatorPacket packet) {
        LOGGER.debug("DamageIndicatorPacket.handleClientSide: 开始处理");

        var minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft.level != null && minecraft.player != null) {

            var target = minecraft.level.getEntity(packet.targetId);
            if (target instanceof LivingEntity livingTarget) {

                var attacker = minecraft.level.getEntity(packet.attackerId);
                if (attacker instanceof Player playerAttacker) {
                    DamageIndicatorManager.addDamageIndicator(livingTarget, packet.damage, playerAttacker);
                }
            }
        }
    }
}