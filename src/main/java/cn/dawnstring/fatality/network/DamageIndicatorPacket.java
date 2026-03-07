package cn.dawnstring.fatality.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;
import cn.dawnstring.fatality.client.DamageIndicatorManager;

import java.util.function.Supplier;

public class DamageIndicatorPacket {
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
        System.out.println("DamageIndicatorPacket.handleClientSide: 开始处理");

        var minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft.level != null && minecraft.player != null) {
            System.out.println("DamageIndicatorPacket: 客户端环境正常");

            // 获取目标实体
            var target = minecraft.level.getEntity(packet.targetId);
            if (target instanceof LivingEntity livingTarget) {
                System.out.println("DamageIndicatorPacket: 找到目标实体: " + livingTarget.getType().getDescription().getString());

                // 获取攻击者玩家
                var attacker = minecraft.level.getEntity(packet.attackerId);
                if (attacker instanceof Player playerAttacker) {
                    System.out.println("DamageIndicatorPacket: 找到攻击者: " + playerAttacker.getName().getString());

                    // 显示伤害数值
                    System.out.println("DamageIndicatorPacket: 显示伤害: " + packet.damage);
                    DamageIndicatorManager.addDamageIndicator(livingTarget, packet.damage, playerAttacker);
                } else {
                    System.out.println("DamageIndicatorPacket: 攻击者不是玩家或不存在");
                }
            } else {
                System.out.println("DamageIndicatorPacket: 目标实体不存在或不是生物");
            }
        } else {
            System.out.println("DamageIndicatorPacket: 客户端环境不完整");
        }
    }
}