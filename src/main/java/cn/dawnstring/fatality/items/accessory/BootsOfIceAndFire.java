package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class BootsOfIceAndFire extends AccessoryItem {

    // 跟踪玩家状态
    private static final Map<UUID, Boolean> playerOnWaterMap = new HashMap<>();
    private static final Map<UUID, Boolean> playerOnLavaMap = new HashMap<>();
    private static final Map<UUID, Boolean> playerAboveLavaMap = new HashMap<>();
    private static final Map<UUID, Double> playerTargetHeightMap = new HashMap<>();
    private static final Map<UUID, IceFireWalkingData> walkingDataMap = new HashMap<>();

    public BootsOfIceAndFire() {
        super(new Properties()
                .stacksTo(1)
                .rarity(Rarity.EPIC)
                .fireResistant());
    }

    @Override
    public void applyEffects(Player player, ItemStack stack) {
        // 应用基础效果
        super.applyEffects(player, stack);

        // 初始化状态跟踪
        UUID playerId = player.getUUID();
        playerOnWaterMap.putIfAbsent(playerId, false);
        playerOnLavaMap.putIfAbsent(playerId, false);
        playerAboveLavaMap.putIfAbsent(playerId, false);
        walkingDataMap.putIfAbsent(playerId, new IceFireWalkingData());
    }

    @Override
    public void removeEffects(Player player, ItemStack stack) {
        // 移除基础效果
        super.removeEffects(player, stack);

        // 移除状态跟踪
        UUID playerId = player.getUUID();
        playerOnWaterMap.remove(playerId);
        playerOnLavaMap.remove(playerId);
        playerAboveLavaMap.remove(playerId);
        playerTargetHeightMap.remove(playerId);
        walkingDataMap.remove(playerId);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            UUID playerId = player.getUUID();

            // 检查玩家是否装备了冰火靴
            boolean hasIceFireBoots = hasIceFireBootsEquipped(player);

            if (!hasIceFireBoots) {
                // 如果没有装备冰火靴，移除状态跟踪
                playerOnWaterMap.remove(playerId);
                playerOnLavaMap.remove(playerId);
                playerAboveLavaMap.remove(playerId);
                playerTargetHeightMap.remove(playerId);
                walkingDataMap.remove(playerId);
                return;
            }

            // 检测玩家状态
            boolean isOnWater = isPlayerOnWater(player);
            boolean isOnLava = isPlayerOnLava(player);
            boolean isAboveLava = isPlayerAboveLava(player);

            playerOnWaterMap.put(playerId, isOnWater);
            playerOnLavaMap.put(playerId, isOnLava);
            playerAboveLavaMap.put(playerId, isAboveLava);

            // 获取行走数据
            IceFireWalkingData walkingData = walkingDataMap.get(playerId);
            if (walkingData == null) {
                walkingData = new IceFireWalkingData();
                walkingDataMap.put(playerId, walkingData);
            }

            // 应用冰火行走效果
            if (isOnWater || isOnLava || isAboveLava) {
                handleIceFireWalking(player, isOnWater, isOnLava, isAboveLava, walkingData);
            } else {
                // 不在特殊地形时重置状态
                walkingData.wasOnSpecialTerrainLastTick = false;
            }
        }
    }

    // 检测玩家是否在水上
    private static boolean isPlayerOnWater(Player player) {
        // 获取玩家脚下的方块位置
        AABB playerBounds = player.getBoundingBox();

        // 检查玩家脚下1格范围内的流体
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                // 检查玩家站立位置下方的流体状态
                FluidState fluidState = player.level().getFluidState(
                        player.blockPosition().offset(x, -1, z)
                );

                // 如果是水，返回true
                if (!fluidState.isEmpty() && fluidState.isSource()) {
                    return true;
                }

                // 检查玩家是否在水中但接近水面
                if (player.isInWater() && isPlayerNearWaterSurface(player)) {
                    return true;
                }
            }
        }

        return false;
    }

    // 检测玩家是否接近水面
    private static boolean isPlayerNearWaterSurface(Player player) {
        if (player.isInWater()) {
            // 获取玩家上方的方块，检查是否接近水面
            BlockState blockAbove = player.level().getBlockState(player.blockPosition().above());

            // 如果玩家上方是空气，说明接近水面
            return blockAbove.isAir();
        }
        return false;
    }

    // 检测玩家是否在岩浆上方
    private static boolean isPlayerAboveLava(Player player) {
        // 检查玩家下方3格范围内是否有岩浆
        for (int y = 1; y <= 3; y++) {
            BlockState blockBelow = player.level().getBlockState(
                    player.blockPosition().below(y)
            );

            // 如果下方有岩浆相关方块
            if (blockBelow.is(Blocks.LAVA) || blockBelow.is(Blocks.MAGMA_BLOCK) ||
                    blockBelow.is(Blocks.LAVA_CAULDRON)) {
                return true;
            }

            // 如果下方有固体方块，停止检查
            if (blockBelow.isSolid() && y > 1) {
                break;
            }
        }

        return false;
    }

    // 检测玩家是否站在岩浆上
    private static boolean isPlayerOnLava(Player player) {
        // 获取玩家脚下的方块位置
        AABB playerBounds = player.getBoundingBox();

        // 检查玩家脚下1格范围内的方块
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                // 检查玩家站立位置下方的方块
                BlockState blockState = player.level().getBlockState(
                        player.blockPosition().offset(x, -1, z)
                );

                // 如果是岩浆块或岩浆，返回true
                if (blockState.is(Blocks.LAVA) || blockState.is(Blocks.LAVA_CAULDRON) ||
                        blockState.is(Blocks.MAGMA_BLOCK)) {
                    return true;
                }

                // 检查玩家是否在流动的岩浆中
                if (player.isInLava() && !player.isSwimming()) {
                    return true;
                }
            }
        }

        return false;
    }

    // 处理冰火行走效果
    private static void handleIceFireWalking(Player player, boolean isOnWater, boolean isOnLava,
                                             boolean isAboveLava, IceFireWalkingData walkingData) {
        // 防止玩家在岩浆中受到伤害
        player.clearFire();

        Vec3 motion = player.getDeltaMovement();
        UUID playerId = player.getUUID();

        // 水上行走效果
        if (isOnWater) {
            handleWaterWalking(player, motion, walkingData);
        }

        // 岩浆行走效果
        if (isOnLava || isAboveLava) {
            handleLavaWalking(player, isOnLava, isAboveLava, motion, playerId);
        }

        // 应用冰火行走的特殊移动加成
        applyIceFireMovementBonus(player, isOnWater, isOnLava);

        walkingData.wasOnSpecialTerrainLastTick = true;
    }

    // 处理水上行走
    private static void handleWaterWalking(Player player, Vec3 motion, IceFireWalkingData walkingData) {
        // 防止玩家在水中下沉
        if (player.isInWater()) {
            // 提供向上的浮力，让玩家能够站在水面上
            double buoyancy = 0.15; // 比普通水上行走更强的浮力
            double newY = Math.min(motion.y + buoyancy, 0.25);

            // 如果玩家正在下沉，提供更强的浮力
            if (motion.y < -0.1) {
                newY = Math.min(motion.y + buoyancy * 2, 0.2);
            }

            player.setDeltaMovement(motion.x, newY, motion.z);

            // 当玩家接近水面时，将其推到水面上
            if (isPlayerNearWaterSurface(player) && player.getY() < getWaterSurfaceY(player)) {
                double surfaceY = getWaterSurfaceY(player);
                if (surfaceY - player.getY() < 0.5) {
                    // 将玩家推到水面上
                    player.setPos(player.getX(), surfaceY + 0.1, player.getZ());
                }
            }
        }
    }

    // 处理岩浆行走
    private static void handleLavaWalking(Player player, boolean isOnLava, boolean isAboveLava,
                                          Vec3 motion, UUID playerId) {
        // 如果玩家在岩浆中，提供强力浮力
        if (isOnLava) {
            // 提供强力的向上浮力，确保玩家能稳定站在岩浆表面
            double buoyancy = 0.18; // 增强浮力
            double newY = Math.min(motion.y + buoyancy, 0.45);

            player.setDeltaMovement(motion.x, newY, motion.z);

            // 强力防止玩家下沉
            if (motion.y < -0.05) {
                player.setDeltaMovement(motion.x, 0.12, motion.z);
            }

            // 设置目标高度为岩浆表面上方0.5格
            double lavaSurfaceY = player.blockPosition().getY() + 1.0;
            playerTargetHeightMap.put(playerId, lavaSurfaceY);

            // 强制锁定高度在岩浆表面
            lockPlayerHeight(player, lavaSurfaceY + 0.5);
        }
        // 如果玩家在岩浆上方，强力防止掉入岩浆
        else if (isAboveLava) {
            // 获取岩浆表面的高度
            double lavaSurfaceY = findLavaSurfaceHeight(player);
            double targetHeight = lavaSurfaceY + 0.8;

            double currentY = player.getY();

            // 强力防止玩家掉入岩浆
            if (currentY <= targetHeight + 0.5) {
                double pushForce = calculateRequiredPushForce(player, targetHeight, currentY, motion.y);
                double newY = Math.max(motion.y + pushForce, 0.08);

                player.setDeltaMovement(motion.x, newY, motion.z);

                // 如果玩家非常接近岩浆，直接锁定高度
                if (currentY - targetHeight < 0.3) {
                    lockPlayerHeight(player, targetHeight);
                }
            }

            // 强制保持稳定高度
            maintainStableHeight(player, targetHeight, currentY, motion);
        }
    }

    // 应用冰火行走的特殊移动加成
    private static void applyIceFireMovementBonus(Player player, boolean isOnWater, boolean isOnLava) {
        // 在水上行走时提供移动速度加成
        if (isOnWater) {
            // 获取当前移动速度
            float currentSpeed = player.getSpeed();

            // 在水上行走时增加移动速度
            float waterWalkingSpeed = Math.max(currentSpeed, 0.1f);

            // 如果玩家正在移动，应用速度加成
            if (player.zza != 0 || player.xxa != 0) {
                player.setSpeed(waterWalkingSpeed);
            }
        }

        // 在岩浆上行走时提供移动速度加成
        if (isOnLava) {
            // 获取当前移动速度
            float currentSpeed = player.getSpeed();

            // 在岩浆上行走时增加移动速度（但比正常地面慢一些）
            float lavaWalkingSpeed = Math.max(currentSpeed, 0.09f);

            // 如果玩家正在移动，应用速度加成
            if (player.zza != 0 || player.xxa != 0) {
                player.setSpeed(lavaWalkingSpeed);
            }
        }
    }

    // 获取水面Y坐标
    private static double getWaterSurfaceY(Player player) {
        // 找到玩家所在位置的水面高度
        for (int y = (int) player.getY(); y < player.level().getMaxBuildHeight(); y++) {
            BlockState blockState = player.level().getBlockState(player.blockPosition().atY(y));
            if (blockState.isAir() || !blockState.getFluidState().isEmpty()) {
                // 找到第一个非流体或空气的方块，水面在其下方
                for (int checkY = y - 1; checkY >= player.level().getMinBuildHeight(); checkY--) {
                    FluidState fluidState = player.level().getFluidState(player.blockPosition().atY(checkY));
                    if (!fluidState.isEmpty()) {
                        return checkY + 1.0;
                    }
                }
                break;
            }
        }
        return player.getY();
    }

    // 查找岩浆表面的高度
    private static double findLavaSurfaceHeight(Player player) {
        double surfaceHeight = player.getY();

        // 从玩家下方开始查找岩浆方块
        for (int y = 1; y <= 5; y++) {
            BlockState blockBelow = player.level().getBlockState(
                    player.blockPosition().below(y)
            );

            // 如果找到岩浆方块，返回其表面高度
            if (blockBelow.is(Blocks.LAVA) || blockBelow.is(Blocks.MAGMA_BLOCK) ||
                    blockBelow.is(Blocks.LAVA_CAULDRON)) {
                surfaceHeight = player.blockPosition().getY() - y + 1.0;
                break;
            }

            // 如果找到固体方块，停止搜索
            if (blockBelow.isSolid() && y > 1) {
                surfaceHeight = player.blockPosition().getY() - y + 1.0;
                break;
            }
        }

        return surfaceHeight;
    }

    // 锁定玩家高度
    private static void lockPlayerHeight(Player player, double targetHeight) {
        double currentY = player.getY();
        Vec3 motion = player.getDeltaMovement();

        if (Math.abs(currentY - targetHeight) > 0.5) {
            player.setPos(player.getX(), targetHeight, player.getZ());
            player.setDeltaMovement(motion.x, 0, motion.z);
        } else if (Math.abs(currentY - targetHeight) > 0.1) {
            double heightAdjustment = (targetHeight - currentY) * 0.2;
            player.setDeltaMovement(motion.x, heightAdjustment, motion.z);
        } else {
            player.setDeltaMovement(motion.x, 0, motion.z);
        }
    }

    // 保持稳定高度
    private static void maintainStableHeight(Player player, double targetHeight, double currentY, Vec3 motion) {
        if (Math.abs(currentY - targetHeight) > 0.5) {
            player.setPos(player.getX(), targetHeight, player.getZ());
            player.setDeltaMovement(motion.x, 0, motion.z);
        } else if (Math.abs(currentY - targetHeight) > 0.1) {
            double heightAdjustment = (targetHeight - currentY) * 0.2;
            player.setDeltaMovement(motion.x, heightAdjustment, motion.z);
        } else {
            player.setDeltaMovement(motion.x, 0, motion.z);
        }
    }

    // 计算所需的推力
    private static double calculateRequiredPushForce(Player player, double targetHeight, double currentY, double currentVelocity) {
        double heightDifference = targetHeight - currentY;
        double requiredVelocity = heightDifference * 0.3;

        // 确保有足够的向上推力
        return Math.max(requiredVelocity - currentVelocity, 0.05);
    }

    // 检查玩家是否装备了冰火靴
    public static boolean hasIceFireBootsEquipped(Player player) {
        // 检查饰品栏
        var accessoryInventory = cn.dawnstring.fatality.inventory.AccessoryInventory.get(player);
        if (accessoryInventory != null) {
            for (int i = 0; i < accessoryInventory.getItemHandler().getSlots(); i++) {
                ItemStack stack = accessoryInventory.getItemHandler().getStackInSlot(i);
                if (stack.getItem() instanceof BootsOfIceAndFire) {
                    return true;
                }
            }
        }
        return false;
    }

    // 冰火靴提供的属性加成
    @Override
    public float getMovementSpeedBonus() {
        // 提供额外的移动速度加成
        return 0.15f;
    }

    @Override
    public float getDefenseBonus() {
        // 提供火焰和冰冻抗性相关的防御加成
        return 3.0f;
    }

    // 冰火行走数据内部类
    public static class IceFireWalkingData {
        public boolean wasOnSpecialTerrainLastTick = false;
        public long lastSpecialTerrainContactTime = 0;
    }
}
