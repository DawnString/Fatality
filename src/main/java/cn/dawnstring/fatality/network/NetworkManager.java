package cn.dawnstring.fatality.network;

import cn.dawnstring.fatality.Fatality;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkManager {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(Fatality.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        // 注册网络包，包括DamageIndicatorPacket
        INSTANCE.registerMessage(packetId++, AccessorySyncPacket.class,
                AccessorySyncPacket::encode,
                AccessorySyncPacket::decode,
                AccessorySyncPacket::handle);

        INSTANCE.registerMessage(packetId++, DamageIndicatorPacket.class,
                DamageIndicatorPacket::encode,
                DamageIndicatorPacket::decode,
                DamageIndicatorPacket::handle);

        INSTANCE.registerMessage(packetId++, ManaSyncPacket.class,
                ManaSyncPacket::encode,
                ManaSyncPacket::decode,
                ManaSyncPacket::handle);
    }
}