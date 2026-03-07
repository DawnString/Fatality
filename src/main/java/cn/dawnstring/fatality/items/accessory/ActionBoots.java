package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class ActionBoots extends AccessoryItem
{

    // 飞行状态跟踪
    private static final Map<UUID, ActionBootsFlightData> flightDataMap = new HashMap<>();

    // 喷气靴参数
    private final float maxFlightTime;        // 最大飞行时间（秒）
    private final float maxFlightSpeed;       // 最大飞行速度
    private final float verticalAcceleration; // 垂直加速度

    public ActionBoots() {
        super(new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.RARE)
                .fireResistant());

        // 喷气靴参数设置
        this.maxFlightTime = 2.0f;           // 2秒最大飞行时间
        this.maxFlightSpeed = 1.5f;           // 最大飞行速度
        this.verticalAcceleration = 0.15f;   // 垂直加速度
    }

    @Override
    public void applyEffects(Player player, ItemStack stack) {
        // 喷气靴佩戴时初始化飞行数据
        UUID playerId = player.getUUID();
        if (!flightDataMap.containsKey(playerId)) {
            flightDataMap.put(playerId, new ActionBootsFlightData(maxFlightTime, maxFlightSpeed, verticalAcceleration));
        }
    }

    @Override
    public void removeEffects(Player player, ItemStack stack) {
        // 喷气靴卸下时移除飞行数据
        UUID playerId = player.getUUID();
        flightDataMap.remove(playerId);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            UUID playerId = player.getUUID();

            // 检查玩家是否装备了喷气靴
            boolean hasActionBoots = hasActionBootsEquipped(player);

            if (!hasActionBoots) {
                // 如果没有装备喷气靴，确保移除飞行数据并返回
                flightDataMap.remove(playerId);
                return;
            }

            // 获取飞行数据（如果不存在则创建）
            ActionBootsFlightData flightData = flightDataMap.get(playerId);
            if (flightData == null) {
                flightData = new ActionBootsFlightData(10.0f, 1.5f, 0.15f);
                flightDataMap.put(playerId, flightData);
            }

            handleActionBootsFlight(player, flightData);
        }
    }

    // 处理喷气靴飞行逻辑
    private static void handleActionBootsFlight(Player player, ActionBootsFlightData flightData) {
        boolean isOnGround = player.onGround();
        boolean isInAir = !isOnGround;

        // 检测空格键是否按下
        boolean isSpacePressed = isSpaceKeyPressed(player);

        if (isInAir) {
            // 玩家在空中
            if (isSpacePressed && flightData.remainingFlightTime > 0) {
                // 按住空格键且有飞行时间 - 向上飞行
                flightData.isFlying = true;

                // 处理向上飞行
                handleUpwardFlight(player, flightData);

                // 更新已使用飞行时间
                flightData.usedFlightTime += 0.05f;
                flightData.remainingFlightTime = Math.max(0, flightData.maxFlightTime - flightData.usedFlightTime);

            } else {
                // 没有按住空格键或飞行时间用完 - 自由落体
                flightData.isFlying = false;

                // 处理自由落体
                handleFreeFall(player);
            }
        } else {
            // 玩家在地面上
            flightData.isFlying = false;
            flightData.usedFlightTime = 0;  // 重置已使用飞行时间
            flightData.remainingFlightTime = flightData.maxFlightTime;  // 重置剩余飞行时间
        }
    }

    // 处理向上飞行
    private static void handleUpwardFlight(Player player, ActionBootsFlightData flightData) {
        Vec3 currentMotion = player.getDeltaMovement();

        // 计算当前垂直速度
        double currentVerticalSpeed = currentMotion.y;

        // 应用垂直加速度（向上飞行）
        double newVerticalSpeed = Math.min(currentVerticalSpeed + flightData.verticalAcceleration, flightData.maxFlightSpeed);

        // 保持水平移动不变，只改变垂直速度
        Vec3 newMotion = new Vec3(currentMotion.x, newVerticalSpeed, currentMotion.z);
        player.setDeltaMovement(newMotion);

        // 更新当前速度
        flightData.currentVerticalSpeed = (float) newVerticalSpeed;
    }

    // 处理自由落体
    private static void handleFreeFall(Player player) {
        Vec3 motion = player.getDeltaMovement();
        // 正常重力下落，限制最大下落速度
        player.setDeltaMovement(motion.x, Math.max(motion.y - 0.08, -3.0), motion.z);
    }

    // 检测空格键是否被按下（改进的实现）
    protected static boolean isSpaceKeyPressed(Player player) {
        // 客户端检测空格键
        if (player.level().isClientSide()) {
            return net.minecraft.client.Minecraft.getInstance().options.keyJump.isDown();
        }

        // 服务器端：通过玩家输入状态判断（更准确的方式）
        // 这里我们使用一个更可靠的判断方式，避免误判
        return false; // 服务器端无法准确判断按键状态，返回false避免误判
    }

    // 检查玩家是否装备了喷气靴
    public static boolean hasActionBootsEquipped(Player player) {
        // 检查饰品栏
        var accessoryInventory = cn.dawnstring.fatality.inventory.AccessoryInventory.get(player);
        if (accessoryInventory != null) {
            for (int i = 0; i < accessoryInventory.getItemHandler().getSlots(); i++) {
                ItemStack stack = accessoryInventory.getItemHandler().getStackInSlot(i);
                if (stack.getItem() instanceof ActionBoots) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public float getMovementSpeedBonus()
    {
        return 0.05f;
    }


    // 喷气靴飞行数据内部类
    public static class ActionBootsFlightData {
        public final float maxFlightTime;
        public float remainingFlightTime;
        public float usedFlightTime = 0;
        public boolean isFlying = false;
        public float currentVerticalSpeed = 0;
        public final float verticalAcceleration;
        public final float maxFlightSpeed;

        public ActionBootsFlightData(float maxFlightTime, float maxFlightSpeed, float verticalAcceleration) {
            this.maxFlightTime = maxFlightTime;
            this.remainingFlightTime = maxFlightTime;
            this.verticalAcceleration = verticalAcceleration;
            this.maxFlightSpeed = maxFlightSpeed;
        }
    }
}
