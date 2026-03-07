package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class BootsOfTheWaterStrider extends AccessoryItem {

    // 跟踪玩家是否在水上
    private static final Map<UUID, Boolean> playerOnWaterMap = new HashMap<>();
    // 跟踪玩家水上行走状态
    private static final Map<UUID, WaterWalkingData> waterWalkingDataMap = new HashMap<>();

    public BootsOfTheWaterStrider() {
        super(new Properties()
                .stacksTo(1)
                .rarity(Rarity.UNCOMMON)
                .fireResistant());
    }

    @Override
    public void applyEffects(Player player, ItemStack stack) {
        // 应用基础效果
        super.applyEffects(player, stack);

        // 初始化水上行走状态
        UUID playerId = player.getUUID();
        playerOnWaterMap.putIfAbsent(playerId, false);
        waterWalkingDataMap.putIfAbsent(playerId, new WaterWalkingData());
    }

    @Override
    public void removeEffects(Player player, ItemStack stack) {
        // 移除基础效果
        super.removeEffects(player, stack);

        // 移除水上行走状态跟踪
        UUID playerId = player.getUUID();
        playerOnWaterMap.remove(playerId);
        waterWalkingDataMap.remove(playerId);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            UUID playerId = player.getUUID();

            // 检查玩家是否装备了水上行走靴
            boolean hasWaterStriderBoots = hasWaterStriderBootsEquipped(player);

            if (!hasWaterStriderBoots) {
                // 如果没有装备水上行走靴，移除状态跟踪
                playerOnWaterMap.remove(playerId);
                waterWalkingDataMap.remove(playerId);
                return;
            }

            // 检测玩家是否在水上
            boolean isOnWater = isPlayerOnWater(player);
            playerOnWaterMap.put(playerId, isOnWater);

            // 获取水上行走数据
            WaterWalkingData waterData = waterWalkingDataMap.get(playerId);
            if (waterData == null) {
                waterData = new WaterWalkingData();
                waterWalkingDataMap.put(playerId, waterData);
            }

            // 如果在水上，应用水上行走效果
            if (isOnWater) {
                handleWaterWalking(player, waterData);
            } else {
                // 不在水上时重置状态
                waterData.wasOnWaterLastTick = false;
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

    // 处理水上行走效果
    private static void handleWaterWalking(Player player, WaterWalkingData waterData) {
        // 防止玩家在水中下沉
        if (player.isInWater()) {
            // 提供向上的浮力，让玩家能够站在水面上
            Vec3 motion = player.getDeltaMovement();

            // 水上行走的浮力效果
            double buoyancy = 0.12; // 比正常浮力稍强
            double newY = Math.min(motion.y + buoyancy, 0.2); // 限制最大上升速度

            // 如果玩家正在下沉，提供更强的浮力
            if (motion.y < -0.1) {
                newY = Math.min(motion.y + buoyancy * 2, 0.15);
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

        // 在水面上行走时提供移动加成
        if (isPlayerOnWaterSurface(player)) {
            applyWaterWalkingMovementBonus(player, waterData);
        }

        waterData.wasOnWaterLastTick = true;
    }

    // 检测玩家是否在水面上（站立在水上）
    private static boolean isPlayerOnWaterSurface(Player player) {
        // 玩家不在水中但脚下是水
        if (!player.isInWater() && isPlayerOnWater(player)) {
            return true;
        }

        // 玩家在水中但接近水面
        if (player.isInWater() && isPlayerNearWaterSurface(player)) {
            // 检查玩家是否在水面附近
            double waterSurfaceY = getWaterSurfaceY(player);
            return player.getY() >= waterSurfaceY - 0.3;
        }

        return false;
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
                        return checkY + 1.0; // 水面在流体方块上方
                    }
                }
                break;
            }
        }
        return player.getY();
    }

    // 应用水上行走的移动加成
    private static void applyWaterWalkingMovementBonus(Player player, WaterWalkingData waterData) {
        // 在水面上行走时提供正常的移动速度
        float waterWalkingSpeed = 0.1f; // 正常行走速度

        // 如果玩家正在移动，应用速度加成
        if (player.zza != 0 || player.xxa != 0) {
            player.setSpeed(waterWalkingSpeed);

            // 减少水中的阻力
            Vec3 motion = player.getDeltaMovement();
            double horizontalResistance = 0.8; // 比正常水中阻力小
            player.setDeltaMovement(motion.x * horizontalResistance, motion.y, motion.z * horizontalResistance);
        }

        // 水上行走的特殊效果：减少坠落伤害
        if (player.fallDistance > 0) {
            player.fallDistance *= 0.5f; // 减少50%坠落伤害
        }
    }

    // 检查玩家是否装备了水上行走靴
    public static boolean hasWaterStriderBootsEquipped(Player player) {
        // 检查饰品栏
        var accessoryInventory = cn.dawnstring.fatality.inventory.AccessoryInventory.get(player);
        if (accessoryInventory != null) {
            for (int i = 0; i < accessoryInventory.getItemHandler().getSlots(); i++) {
                ItemStack stack = accessoryInventory.getItemHandler().getStackInSlot(i);
                if (stack.getItem() instanceof BootsOfTheWaterStrider) {
                    return true;
                }
            }
        }
        return false;
    }

    // 水上行走提供的属性加成
    @Override
    public float getMovementSpeedBonus() {
        // 在水上行走时提供额外的移动速度加成
        return 0.15f; // 15%移动速度加成
    }

    @Override
    public float getDefenseBonus() {
        // 提供水中相关的防御加成
        return 1.0f; // 1点防御加成
    }

    // 水上行走数据内部类
    public static class WaterWalkingData {
        public boolean wasOnWaterLastTick = false;
        public long lastWaterContactTime = 0;
    }
}