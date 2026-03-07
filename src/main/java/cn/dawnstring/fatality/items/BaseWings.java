package cn.dawnstring.fatality.items;

import cn.dawnstring.fatality.util.TooltipHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class BaseWings extends AccessoryItem
{
    private String story = "";
    private String attributesDescription = "";
    // 飞行状态跟踪
    private static final Map<UUID, WingFlightData> flightDataMap = new HashMap<>();

    // 翅膀参数
    protected final float maxFlightTime;      // 最大飞行时间（秒）
    protected final float maxHorizontalSpeed;     // 最大水平速度
    protected final float maxVerticalSpeed;     // 最大垂直速度
    protected final float horizontalAcceleration; // 水平加速度
    protected final float verticalAcceleration;  // 垂直加速度
    protected final float glideSpeed;         // 滑行速度

    /**
     * 基础翅膀类
     *  最大飞行速度 1.5 = 30b/s
     * @param properties 物品属性
     * @param maxFlightTime 最大飞行时间（秒）
     * @param maxHorizontalSpeed 最大水平速度
     * @param maxVerticalSpeed 最大垂直速度
     * @param horizontalAcceleration 水平加速度
     * @param verticalAcceleration 垂直加速度 >0.08
     * @param glideSpeed 滑行速度
     */
    public BaseWings(Properties properties, float maxFlightTime, float maxHorizontalSpeed, float maxVerticalSpeed,
                     float horizontalAcceleration, float verticalAcceleration, float glideSpeed) {
        super(properties);
        this.maxFlightTime = maxFlightTime;
        this.maxHorizontalSpeed = maxHorizontalSpeed;
        this.maxVerticalSpeed = maxVerticalSpeed;
        this.horizontalAcceleration = horizontalAcceleration;
        this.verticalAcceleration = verticalAcceleration;
        this.glideSpeed = glideSpeed;
    }

    @Override
    public void applyEffects(Player player, ItemStack stack) {
        // 翅膀佩戴时初始化飞行数据
        UUID playerId = player.getUUID();
        if (!flightDataMap.containsKey(playerId)) {
            flightDataMap.put(playerId, new WingFlightData(maxFlightTime, maxHorizontalSpeed,
                    maxVerticalSpeed, horizontalAcceleration, verticalAcceleration, glideSpeed));
        }
    }

    @Override
    public void removeEffects(Player player, ItemStack stack) {
        // 翅膀卸下时移除飞行数据
        UUID playerId = player.getUUID();
        flightDataMap.remove(playerId);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            UUID playerId = player.getUUID();

            // 首先检查玩家是否在饰品栏装备了翅膀
            boolean hasWings = hasWingsEquipped(player);

            if (!hasWings) {
                // 如果没有装备翅膀，确保移除飞行数据并返回
                flightDataMap.remove(playerId);
                return;
            }

            // 获取飞行数据（如果不存在则创建）
            WingFlightData flightData = flightDataMap.get(playerId);
            if (flightData == null) {
                // 如果玩家装备翅膀但没有飞行数据，创建新的飞行数据
                flightData = new WingFlightData(5.0f, 1.5f, 0.1f, 1.0f, 1.0f, 0.1f); // 使用天使翅膀的默认参数
                flightDataMap.put(playerId, flightData);
            }

            handleFlight(player, flightData);
        }
    }

    // 摔落伤害免疫事件
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player) {
            // 检查玩家是否佩戴翅膀
            if (hasWingsEquipped(player)) {
                // 佩戴翅膀时免疫摔落伤害
                event.setCanceled(true);
            }
        }
    }

    public static boolean hasWingsEquipped(Player player) {
        // 只检查饰品栏，不检查主背包
        var accessoryInventory = cn.dawnstring.fatality.inventory.AccessoryInventory.get(player);
        if (accessoryInventory != null) {
            for (int i = 0; i < accessoryInventory.getItemHandler().getSlots(); i++) {
                ItemStack stack = accessoryInventory.getItemHandler().getStackInSlot(i);
                if (stack.getItem() instanceof BaseWings) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void handleFlight(Player player, WingFlightData flightData) {
        boolean isOnGround = player.onGround();
        boolean isInAir = !isOnGround;

        // 检测空格键是否按下
        boolean isSpacePressed = isSpaceKeyPressed(player);

        if (isInAir) {
            // 玩家在空中
            if (isSpacePressed) {
                // 按住空格键
                if (flightData.remainingFlightTime > 0) {
                    // 有飞行时间 - 正常飞行
                    flightData.isFlying = true;
                    flightData.isGliding = false;

                    // 处理飞行移动
                    handleFlyingMovement(player, flightData);

                    // 只有按下空格时才消耗飞行时间
                    flightData.remainingFlightTime -= 0.05f;

                } else {
                    // 飞行时间耗尽 - 滑行模式
                    flightData.isFlying = false;
                    flightData.isGliding = true;

                    // 处理滑行移动
                    handleGlidingMovement(player, flightData);
                }
            } else {
                // 没有按住空格键 - 自由落体
                flightData.isFlying = false;
                flightData.isGliding = false;

                // 正常重力下落
                Vec3 motion = player.getDeltaMovement();
                player.setDeltaMovement(motion.x, Math.max(motion.y - 0.08, -3.0), motion.z); // 限制最大下落速度
            }
        } else {
            // 玩家在地面上
            flightData.isFlying = false;
            flightData.isGliding = false;
            flightData.currentHorizontalSpeed = 0;
            flightData.currentVerticalSpeed = 0;
            flightData.targetHorizontalSpeed = 0;
            flightData.targetVerticalSpeed = 0;
            flightData.currentDirection = Vec3.ZERO;
            flightData.targetDirection = Vec3.ZERO;

            // 恢复飞行时间
            if (flightData.remainingFlightTime < flightData.maxFlightTime) {
                flightData.remainingFlightTime = Math.min(flightData.remainingFlightTime + 0.3f, flightData.maxFlightTime);
            }
        }
    }

    // 处理飞行移动（带平滑处理）
    protected static void handleFlyingMovement(Player player, WingFlightData flightData) {
        Vec3 currentMotion = player.getDeltaMovement();

        // 检测玩家输入方向
        boolean isMovingForward = player.zza > 0;  // W键
        boolean isMovingBackward = player.zza < 0; // S键
        boolean isMovingLeft = player.xxa < 0;     // A键
        boolean isMovingRight = player.xxa > 0;    // D键

        // 计算目标移动方向
        flightData.targetDirection = calculateTargetDirection(player, isMovingForward, isMovingBackward, isMovingLeft, isMovingRight);

        // 平滑过渡方向
        if (flightData.targetDirection.length() > 0) {
            // 有输入时，平滑过渡到目标方向
            if (flightData.currentDirection.length() == 0) {
                flightData.currentDirection = flightData.targetDirection.normalize();
            } else {
                flightData.currentDirection = smoothDirectionTransition(
                    flightData.currentDirection, 
                    flightData.targetDirection.normalize(), 
                    flightData.directionSmoothFactor
                );
            }
        } else {
            // 没有输入时，保持当前方向但逐渐减速
            if (flightData.currentDirection.length() > 0) {
                // 逐渐减小方向向量的大小（模拟惯性衰减）
                flightData.currentDirection = flightData.currentDirection.scale(0.95);
                if (flightData.currentDirection.length() < 0.01) {
                    flightData.currentDirection = Vec3.ZERO;
                }
            }
        }

        // 计算当前水平速度
        double currentHorizontalSpeed = new Vec3(currentMotion.x, 0, currentMotion.z).length();
        double currentVerticalSpeed = currentMotion.y;

        // 计算目标速度 - 修复初始化问题
        // 确保目标速度从合理值开始计算
        if (flightData.targetHorizontalSpeed < 0) {
            flightData.targetHorizontalSpeed = 0;
        }
        if (flightData.targetVerticalSpeed < 0) {
            flightData.targetVerticalSpeed = 0;
        }

        if (flightData.targetDirection.length() > 0) {
            // 有移动输入，加速到目标速度
            flightData.targetHorizontalSpeed = Math.min(
                flightData.targetHorizontalSpeed + flightData.horizontalAcceleration, 
                flightData.maxHorizontalSpeed
            );
        } else {
            // 没有移动输入，减速
            flightData.targetHorizontalSpeed = Math.max(
                flightData.targetHorizontalSpeed - flightData.horizontalAcceleration * 3, 
                0
            );
        }

        // 确保目标水平速度不超过最大水平速度限制
        flightData.targetHorizontalSpeed = Math.min(flightData.targetHorizontalSpeed, flightData.maxHorizontalSpeed);

        // 垂直速度目标（总是向上加速）- 确保从0开始加速
        flightData.targetVerticalSpeed = Math.min(
            flightData.targetVerticalSpeed + flightData.verticalAcceleration, 
            flightData.maxVerticalSpeed
        );

        // 平滑过渡到目标速度 - 优化平滑参数
        // 垂直速度使用更快的平滑，确保能快速起飞
        double newHorizontalSpeed = smoothSpeedTransition(
            currentHorizontalSpeed, 
            flightData.targetHorizontalSpeed, 
            flightData.speedSmoothFactor
        );
        
        // 垂直速度使用更激进的平滑，确保能快速获得上升速度
        double newVerticalSpeed = smoothSpeedTransition(
            currentVerticalSpeed, 
            flightData.targetVerticalSpeed, 
            flightData.speedSmoothFactor * 3.0f // 垂直速度变化更快
        );

        // 计算新的水平移动向量
        Vec3 newHorizontalMotion;
        if (flightData.currentDirection.length() > 0) {
            // 使用平滑后的方向
            newHorizontalMotion = flightData.currentDirection.normalize().scale(newHorizontalSpeed);
        } else if (currentHorizontalSpeed > 0) {
            // 没有方向但还有速度，保持当前水平方向
            Vec3 horizontalMotion = new Vec3(currentMotion.x, 0, currentMotion.z);
            newHorizontalMotion = horizontalMotion.normalize().scale(newHorizontalSpeed);
        } else {
            // 完全停止
            newHorizontalMotion = Vec3.ZERO;
        }

        // 组合最终移动向量
        Vec3 newMotion = new Vec3(newHorizontalMotion.x, newVerticalSpeed, newHorizontalMotion.z);
        player.setDeltaMovement(newMotion);

        // 更新当前速度
        flightData.currentHorizontalSpeed = (float) newHorizontalSpeed;
        flightData.currentVerticalSpeed = (float) newVerticalSpeed;
    }

    // 处理滑行移动（带平滑处理）
    protected static void handleGlidingMovement(Player player, WingFlightData flightData) {
        Vec3 currentMotion = player.getDeltaMovement();

        // 检测玩家输入方向
        boolean isMovingForward = player.zza > 0;  // W键
        boolean isMovingBackward = player.zza < 0; // S键
        boolean isMovingLeft = player.xxa < 0;     // A键
        boolean isMovingRight = player.xxa > 0;    // D键

        // 计算目标移动方向
        flightData.targetDirection = calculateTargetDirection(player, isMovingForward, isMovingBackward, isMovingLeft, isMovingRight);

        // 平滑过渡方向（滑行模式使用更快的过渡）
        if (flightData.targetDirection.length() > 0) {
            // 有输入时，平滑过渡到目标方向
            if (flightData.currentDirection.length() == 0) {
                flightData.currentDirection = flightData.targetDirection.normalize();
            } else {
                flightData.currentDirection = smoothDirectionTransition(
                    flightData.currentDirection, 
                    flightData.targetDirection.normalize(), 
                    flightData.directionSmoothFactor * 1.5f // 滑行时方向变化更快
                );
            }
        } else {
            // 没有输入时，保持当前方向但逐渐减速
            if (flightData.currentDirection.length() > 0) {
                // 逐渐减小方向向量的大小
                flightData.currentDirection = flightData.currentDirection.scale(0.9);
                if (flightData.currentDirection.length() < 0.01) {
                    flightData.currentDirection = Vec3.ZERO;
                }
            }
        }

        // 滑行：水平移动，垂直缓慢下落
        double glideY = Math.max(currentMotion.y - 0.02, -flightData.maxVerticalSpeed * 0.3);
        
        // 计算目标水平速度
        double targetHorizontalSpeed;
        if (flightData.targetDirection.length() > 0) {
            targetHorizontalSpeed = Math.min(flightData.maxHorizontalSpeed * 0.8, flightData.maxHorizontalSpeed); // 滑行时水平速度为最大水平速度的80%，但不超过最大限制
        } else {
            // 没有输入时，逐渐减速
            double currentHorizontalSpeed = new Vec3(currentMotion.x, 0, currentMotion.z).length();
            targetHorizontalSpeed = Math.max(currentHorizontalSpeed - 0.02, 0);
        }

        // 确保目标水平速度不超过最大水平速度限制
        targetHorizontalSpeed = Math.min(targetHorizontalSpeed, flightData.maxHorizontalSpeed);

        // 平滑过渡到目标速度 - 优化滑行模式参数
        double currentHorizontalSpeed = new Vec3(currentMotion.x, 0, currentMotion.z).length();
        double newHorizontalSpeed = smoothSpeedTransition(
            currentHorizontalSpeed, 
            targetHorizontalSpeed, 
            flightData.speedSmoothFactor * 2.0f // 滑行模式更快的平滑
        );
        
        // 滑行模式垂直速度使用更快的平滑
        double newVerticalSpeed = smoothSpeedTransition(
            currentMotion.y, 
            glideY, 
            flightData.speedSmoothFactor * 4.0f // 滑行模式垂直速度变化更快
        );

        Vec3 newHorizontalMotion;
        if (flightData.currentDirection.length() > 0) {
            // 使用平滑后的方向
            newHorizontalMotion = flightData.currentDirection.normalize().scale(newHorizontalSpeed);
        } else if (currentHorizontalSpeed > 0) {
            // 没有方向但还有速度，保持当前水平方向
            Vec3 horizontalMotion = new Vec3(currentMotion.x, 0, currentMotion.z);
            newHorizontalMotion = horizontalMotion.normalize().scale(newHorizontalSpeed);
        } else {
            // 完全停止
            newHorizontalMotion = Vec3.ZERO;
        }

        Vec3 newMotion = new Vec3(newHorizontalMotion.x, newVerticalSpeed, newHorizontalMotion.z);
        player.setDeltaMovement(newMotion);
    }

    // 计算目标移动方向（基于玩家输入和视角）
    private static Vec3 calculateTargetDirection(Player player, boolean forward, boolean backward, boolean left, boolean right) {
        float yaw = player.getYRot();
        Vec3 direction = Vec3.ZERO;

        if (forward) {
            // 前方向
            direction = direction.add(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw)));
        }
        if (backward) {
            // 后方向
            direction = direction.add(Math.sin(Math.toRadians(yaw)), 0, -Math.cos(Math.toRadians(yaw)));
        }
        if (left) {
            // 左方向
            direction = direction.add(Math.sin(Math.toRadians(yaw - 90)), 0, -Math.cos(Math.toRadians(yaw - 90)));
        }
        if (right) {
            // 右方向
            direction = direction.add(Math.sin(Math.toRadians(yaw + 90)), 0, -Math.cos(Math.toRadians(yaw + 90)));
        }

        return direction;
    }

    // 检测空格键是否被按下
    protected static boolean isSpaceKeyPressed(Player player) {
        // 客户端检测空格键
        if (player.level().isClientSide()) {
            return net.minecraft.client.Minecraft.getInstance().options.keyJump.isDown();
        }

        // 服务器端：通过运动状态判断（更精确的判断）
        return player.getDeltaMovement().y > 0.01 || !player.onGround();
    }

    // 获取剩余飞行时间（用于显示在UI上）
    public static float getRemainingFlightTime(Player player) {
        WingFlightData data = flightDataMap.get(player.getUUID());
        return data != null ? data.remainingFlightTime : 0;
    }

    // 获取飞行状态（用于显示在UI上）
    public static boolean isFlying(Player player) {
        WingFlightData data = flightDataMap.get(player.getUUID());
        return data != null && data.isFlying;
    }

    // 获取滑行状态（用于显示在UI上）
    public static boolean isGliding(Player player) {
        WingFlightData data = flightDataMap.get(player.getUUID());
        return data != null && data.isGliding;
    }

    public static float getMaxFlightTime(Player player)
    {
        WingFlightData data = flightDataMap.get(player.getUUID());
        return data != null ? data.maxFlightTime : 0;
    }

    // 平滑方向过渡函数
    private static Vec3 smoothDirectionTransition(Vec3 current, Vec3 target, float smoothFactor) {
        // 线性插值实现方向平滑过渡
        double x = current.x + (target.x - current.x) * smoothFactor;
        double y = current.y + (target.y - current.y) * smoothFactor;
        double z = current.z + (target.z - current.z) * smoothFactor;
        
        Vec3 result = new Vec3(x, y, z);
        
        // 确保结果向量不为零向量
        if (result.length() > 0) {
            return result;
        } else {
            return target; // 如果结果为零向量，直接返回目标方向
        }
    }

    // 平滑速度过渡函数
    private static double smoothSpeedTransition(double current, double target, float smoothFactor) {
        // 线性插值实现速度平滑过渡
        return current + (target - current) * smoothFactor;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag)
    {

        // 构建详细的属性描述
        StringBuilder descriptionBuilder = new StringBuilder();

        descriptionBuilder.append("\n§a最大飞行时间：").append(this.maxFlightTime).append("秒")
                .append("\n§a滑行速度：").append(String.format("%.2f", this.glideSpeed * 20)).append(" block/s")
                .append("\n§a水平加速度：").append(String.format("%.2f", this.horizontalAcceleration * 20)).append(" block/s²")
                .append("\n§a垂直加速度：").append(String.format("%.2f", this.verticalAcceleration * 20)).append(" block/s²")
                .append("\n§a最大水平速度：").append(String.format("%.2f", this.maxHorizontalSpeed * 20)).append(" block/s")
                .append("\n§a最大垂直速度：").append(String.format("%.2f", this.maxVerticalSpeed * 20)).append(" block/s");

        // 如果有属性描述，添加到tooltip中
        if (descriptionBuilder.length() > 0) {
            this.attributesDescription = descriptionBuilder.toString().trim();
        }

        TooltipHelper.addDescriptiveTooltip(stack, level, tooltip, flag, story, attributesDescription);
    }

    // 飞行数据内部类
    public static class WingFlightData {
        public final float maxFlightTime;
        public float remainingFlightTime;
        public boolean isFlying = false;
        public boolean isGliding = false;
        public float currentHorizontalSpeed = 0;
        public float currentVerticalSpeed = 0;
        public final float horizontalAcceleration;
        public final float verticalAcceleration;
        public final float maxHorizontalSpeed;
        public final float maxVerticalSpeed;
        public final float glideSpeed;
        
        // 平滑处理相关变量
        public Vec3 currentDirection = Vec3.ZERO;          // 当前移动方向
        public Vec3 targetDirection = Vec3.ZERO;           // 目标移动方向
        public float directionSmoothFactor = 0.2f;         // 方向平滑因子
        public float speedSmoothFactor = 0.15f;            // 速度平滑因子
        public float targetHorizontalSpeed = 0;            // 目标水平速度
        public float targetVerticalSpeed = 0;              // 目标垂直速度

        public WingFlightData(float maxFlightTime, float maxHorizontalSpeed, float maxVerticalSpeed,
                              float horizontalAcceleration, float verticalAcceleration, float glideSpeed) {
            this.maxFlightTime = maxFlightTime;
            this.remainingFlightTime = maxFlightTime;
            this.horizontalAcceleration = horizontalAcceleration;
            this.verticalAcceleration = verticalAcceleration;
            this.maxHorizontalSpeed = maxHorizontalSpeed;
            this.maxVerticalSpeed = maxVerticalSpeed;
            this.glideSpeed = glideSpeed;
        }
    }
}