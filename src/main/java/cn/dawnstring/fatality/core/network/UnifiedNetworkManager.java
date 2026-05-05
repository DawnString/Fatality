package cn.dawnstring.fatality.core.network;

import cn.dawnstring.fatality.Fatality;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

import java.util.function.Supplier;

/**
 * 统一网络管理器
 * 提供统一的网络包注册和发送接口
 */
public class UnifiedNetworkManager {
    
    private static final String PROTOCOL_VERSION = "1";
    private static final int MAX_PACKET_SIZE = 1048576;
    
    private static final UnifiedNetworkManager INSTANCE = new UnifiedNetworkManager();
    
    private SimpleChannel channel;
    private int packetId = 0;
    
    private UnifiedNetworkManager() {
        // 私有构造函数
    }
    
    public static UnifiedNetworkManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * 初始化网络通道
     */
    public void initialize() {
        channel = ChannelBuilder.named(ResourceLocation.fromNamespaceAndPath(Fatality.MODID, "main"))
                .networkProtocolVersion(1)
                .simpleChannel();
        
        Fatality.LOGGER.info("Network channel initialized");
    }
    
    /**
     * 注册网络包
     * @param packetClass 网络包类
     * @param encoder 编码器
     * @param decoder 解码器
     * @param handler 处理器
     * @param <MSG> 消息类型
     */
    public <MSG> void registerPacket(Class<MSG> packetClass, 
                                     NetworkEncoder<MSG> encoder,
                                     NetworkDecoder<MSG> decoder,
                                     NetworkHandler<MSG> handler) {
        channel.messageBuilder(packetClass, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(encoder::encode)
                .decoder(decoder::decode)
                .consumerMainThread(handler::handle)
                .add();
        
        Fatality.LOGGER.debug("Network packet registered: {}", packetClass.getSimpleName());
    }
    
    /**
     * 注册客户端到服务器网络包
     * @param packetClass 网络包类
     * @param encoder 编码器
     * @param decoder 解码器
     * @param handler 处理器
     * @param <MSG> 消息类型
     */
    public <MSG> void registerClientToServerPacket(Class<MSG> packetClass,
                                                   NetworkEncoder<MSG> encoder,
                                                   NetworkDecoder<MSG> decoder,
                                                   NetworkHandler<MSG> handler) {
        channel.messageBuilder(packetClass, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(encoder::encode)
                .decoder(decoder::decode)
                .consumerMainThread(handler::handle)
                .add();
    }
    
    /**
     * 注册服务器到客户端网络包
     * @param packetClass 网络包类
     * @param encoder 编码器
     * @param decoder 解码器
     * @param handler 处理器
     * @param <MSG> 消息类型
     */
    public <MSG> void registerServerToClientPacket(Class<MSG> packetClass,
                                                   NetworkEncoder<MSG> encoder,
                                                   NetworkDecoder<MSG> decoder,
                                                   NetworkHandler<MSG> handler) {
        channel.messageBuilder(packetClass, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(encoder::encode)
                .decoder(decoder::decode)
                .consumerMainThread(handler::handle)
                .add();
    }
    
    /**
     * 发送网络包到服务器
     * @param packet 网络包
     * @param <MSG> 消息类型
     */
    public <MSG> void sendToServer(MSG packet) {
        channel.send(packet, PacketDistributor.SERVER.noArg());
    }
    
    /**
     * 发送网络包到玩家
     * @param packet 网络包
     * @param player 玩家
     * @param <MSG> 消息类型
     */
    public <MSG> void sendToPlayer(MSG packet, net.minecraft.server.level.ServerPlayer player) {
        channel.send(packet, PacketDistributor.PLAYER.with(player));
    }
    
    /**
     * 发送网络包到所有玩家
     * @param packet 网络包
     * @param <MSG> 消息类型
     */
    public <MSG> void sendToAllPlayers(MSG packet) {
        channel.send(packet, PacketDistributor.ALL.noArg());
    }
    
    /**
     * 发送网络包到所有跟踪实体的玩家
     * @param packet 网络包
     * @param entity 实体
     * @param <MSG> 消息类型
     */
    public <MSG> void sendToTracking(MSG packet, net.minecraft.world.entity.Entity entity) {
        channel.send(packet, PacketDistributor.TRACKING_ENTITY.with(entity));
    }
    
    /**
     * 发送网络包到所有跟踪实体及实体本身的玩家
     * @param packet 网络包
     * @param entity 实体
     * @param <MSG> 消息类型
     */
    public <MSG> void sendToTrackingAndSelf(MSG packet, net.minecraft.world.entity.Entity entity) {
        channel.send(packet, PacketDistributor.TRACKING_ENTITY_AND_SELF.with(entity));
    }
    
    /**
     * 获取网络通道
     * @return 网络通道
     */
    public SimpleChannel getChannel() {
        return channel;
    }
    
    /**
     * 网络编码器接口
     */
    @FunctionalInterface
    public interface NetworkEncoder<MSG> {
        void encode(MSG message, net.minecraft.network.FriendlyByteBuf buffer);
    }
    
    /**
     * 网络解码器接口
     */
    @FunctionalInterface
    public interface NetworkDecoder<MSG> {
        MSG decode(net.minecraft.network.FriendlyByteBuf buffer);
    }
    
    /**
     * 网络处理器接口
     */
    @FunctionalInterface
    public interface NetworkHandler<MSG> {
        void handle(MSG message, Supplier<net.minecraftforge.network.NetworkEvent.Context> context);
    }
}