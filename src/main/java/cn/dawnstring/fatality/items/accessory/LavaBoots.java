package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class LavaBoots extends AccessoryItem {

    // 跟踪玩家是否在岩浆上
    private static final Map<UUID, Boolean> playerOnLavaMap = new HashMap<>();

    // 跟踪玩家在岩浆上方的状态
    private static final Map<UUID, Boolean> playerAboveLavaMap = new HashMap<>();
    private static final Map<UUID, Double> playerTargetHeightMap = new HashMap<>();

    public LavaBoots() {
        super(new Properties()
                .stacksTo(1)
                .rarity(Rarity.RARE)
                .fireResistant());
    }

    @Override
    public void applyEffects(Player player, ItemStack stack) {
        // 应用基础效果
        super.applyEffects(player, stack);

        // 初始化岩浆行走状态
        UUID playerId = player.getUUID();
        playerOnLavaMap.putIfAbsent(playerId, false);
        playerAboveLavaMap.putIfAbsent(playerId, false);
    }

    @Override
    public void removeEffects(Player player, ItemStack stack) {
        // 移除基础效果
        super.removeEffects(player, stack);

        // 移除岩浆行走状态跟踪
        UUID playerId = player.getUUID();
        playerOnLavaMap.remove(playerId);
        playerAboveLavaMap.remove(playerId);
        playerTargetHeightMap.remove(playerId);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            UUID playerId = player.getUUID();

            // 检查玩家是否装备了岩浆靴
            boolean hasLavaBoots = hasLavaBootsEquipped(player);

            if (!hasLavaBoots) {
                // 如果没有装备岩浆靴，移除状态跟踪
                playerOnLavaMap.remove(playerId);
                playerAboveLavaMap.remove(playerId);
                playerTargetHeightMap.remove(playerId);
                return;
            }

            // 检测玩家是否站在岩浆上或岩浆上方
            boolean isOnLava = isPlayerOnLava(player);
            boolean isAboveLava = isPlayerAboveLava(player);

            playerOnLavaMap.put(playerId, isOnLava);
            playerAboveLavaMap.put(playerId, isAboveLava);

            // 如果站在岩浆上或岩浆上方，应用岩浆行走效果
            if (isOnLava || isAboveLava) {
                handleLavaWalking(player, isOnLava, isAboveLava);
            }
        }
    }

    // 检测玩家是否在岩浆上方（即将掉入岩浆）
    private static boolean isPlayerAboveLava(Player player) {
        // 检查玩家下方3格范围内是否有岩浆（减少检测范围）
        for (int y = 1; y <= 3; y++) {
            BlockState blockBelow = player.level().getBlockState(
                    player.blockPosition().below(y)
            );

            // 如果下方有岩浆相关方块
            if (blockBelow.is(Blocks.LAVA) || blockBelow.is(Blocks.MAGMA_BLOCK) ||
                    blockBelow.is(Blocks.LAVA_CAULDRON)) {
                return true;
            }

            // 如果下方有固体方块，停止检查（玩家不会掉入岩浆）
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

                // 检查玩家是否在流动的岩浆中（但不在岩浆源中）
                if (player.isInLava() && !player.isSwimming()) {
                    return true;
                }
            }
        }

        return false;
    }

    // 处理岩浆行走效果
    private static void handleLavaWalking(Player player, boolean isOnLava, boolean isAboveLava) {
        // 防止玩家在岩浆中受到伤害
        player.clearFire();

        Vec3 motion = player.getDeltaMovement();
        UUID playerId = player.getUUID();

        // 如果玩家在岩浆中，提供强力浮力
        if (isOnLava) {
            // 提供强力的向上浮力，确保玩家能稳定站在岩浆表面
            double buoyancy = 0.15; // 增强浮力
            double newY = Math.min(motion.y + buoyancy, 0.4); // 提高最大上升速度

            player.setDeltaMovement(motion.x, newY, motion.z);

            // 强力防止玩家下沉
            if (motion.y < -0.05) {
                player.setDeltaMovement(motion.x, 0.1, motion.z); // 直接向上推，防止下沉
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
            double targetHeight = lavaSurfaceY + 0.8; // 岩浆上方0.8格

            double currentY = player.getY();

            // 强力防止玩家掉入岩浆
            if (currentY <= targetHeight + 0.5) {
                // 如果玩家接近岩浆，提供强力向上推力
                double pushForce = calculateRequiredPushForce(player, targetHeight, currentY, motion.y);
                double newY = Math.max(motion.y + pushForce, 0.05); // 确保有向上的速度

                player.setDeltaMovement(motion.x, newY, motion.z);

                // 如果玩家非常接近岩浆，直接锁定高度
                if (currentY - targetHeight < 0.3) {
                    lockPlayerHeight(player, targetHeight);
                }
            }

            // 强制保持稳定高度
            maintainStableHeight(player, targetHeight, currentY, motion);

            // 更新目标高度
            playerTargetHeightMap.put(playerId, targetHeight);
        }

        // 提供岩浆行走的移动加成
        applyLavaWalkingMovementBonus(player);

        // 额外：强力防止玩家意外掉入岩浆
        preventAccidentalLavaFall(player, isAboveLava);
    }

    // 保持稳定高度
    private static void maintainStableHeight(Player player, double targetHeight, double currentY, Vec3 motion) {
        double heightDifference = targetHeight - currentY;

        // 如果高度差很小，保持稳定
        if (Math.abs(heightDifference) < 0.1) {
            player.setDeltaMovement(motion.x, 0, motion.z); // 消除垂直运动
        }
        // 如果高度差适中，温和调整
        else if (Math.abs(heightDifference) < 0.5) {
            double stabilityAdjustment = heightDifference * 0.08;
            player.setDeltaMovement(motion.x, motion.y + stabilityAdjustment, motion.z);
        }
        // 如果高度差较大，强力调整
        else {
            double strongAdjustment = heightDifference * 0.15;
            player.setDeltaMovement(motion.x, strongAdjustment, motion.z);
        }
    }

    // 计算需要的向上推力
    private static double calculateRequiredPushForce(Player player, double targetHeight, double currentY, double currentVelocity) {
        double distanceToTarget = targetHeight - currentY;
        double velocityFactor = Math.max(0, -currentVelocity); // 如果正在下降，增加推力

        // 基础推力 + 距离因素 + 速度因素
        return 0.12 + (distanceToTarget * 0.1) + (velocityFactor * 0.08);
    }

    // 锁定玩家高度
    private static void lockPlayerHeight(Player player, double targetHeight) {
        double currentY = player.getY();
        Vec3 motion = player.getDeltaMovement();

        // 如果高度差较大，直接设置位置
        if (Math.abs(currentY - targetHeight) > 0.5) {
            player.setPos(player.getX(), targetHeight, player.getZ());
            player.setDeltaMovement(motion.x, 0, motion.z); // 重置垂直速度
        }
        // 如果高度差较小，调整速度
        else if (Math.abs(currentY - targetHeight) > 0.1) {
            double heightAdjustment = (targetHeight - currentY) * 0.2;
            player.setDeltaMovement(motion.x, heightAdjustment, motion.z);
        }
        // 如果高度合适，保持稳定
        else {
            player.setDeltaMovement(motion.x, 0, motion.z); // 消除垂直运动
        }
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
                surfaceHeight = player.blockPosition().getY() - y + 1.0; // 岩浆表面高度
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

    // 防止玩家意外掉入岩浆 - 强力版本
    private static void preventAccidentalLavaFall(Player player, boolean isAboveLava) {
        if (isAboveLava) {
            Vec3 motion = player.getDeltaMovement();

            // 强力防止快速下降
            if (motion.y < -0.2) {
                // 完全阻止下降，转为上升
                player.setDeltaMovement(motion.x, 0.1, motion.z);
            }

            // 检测玩家是否在危险高度
            double lavaSurfaceY = findLavaSurfaceHeight(player);
            double currentY = player.getY();

            // 如果玩家接近岩浆表面，强力推高
            if (currentY - lavaSurfaceY < 1.0) {
                double emergencyPush = 0.2 + (1.0 - (currentY - lavaSurfaceY)) * 0.1;
                player.setDeltaMovement(motion.x, Math.max(motion.y, emergencyPush), motion.z);
            }
        }
    }

    // 应用岩浆行走的移动加成
    private static void applyLavaWalkingMovementBonus(Player player) {
        // 在岩浆上行走时提供移动速度加成
        if (player.onGround() || isPlayerOnLavaSurface(player)) {
            // 获取当前移动速度
            float currentSpeed = player.getSpeed();

            // 在岩浆上行走时增加移动速度（但比正常地面慢一些）
            float lavaWalkingSpeed = Math.max(currentSpeed, 0.08f); // 最低移动速度

            // 如果玩家正在移动，应用速度加成
            if (player.zza != 0 || player.xxa != 0) {
                // 设置移动速度（比正常行走稍慢，但比在岩浆中游泳快）
                player.setSpeed(lavaWalkingSpeed);
            }
        }
    }

    // 检测玩家是否在岩浆表面（站立在岩浆上）
    private static boolean isPlayerOnLavaSurface(Player player) {
        // 检查玩家是否在岩浆中但接近表面
        if (player.isInLava()) {
            // 获取玩家下方的方块
            BlockState blockBelow = player.level().getBlockState(player.blockPosition().below());

            // 如果下方是岩浆块或岩浆，且玩家位置接近岩浆表面
            if (blockBelow.is(Blocks.LAVA) || blockBelow.is(Blocks.MAGMA_BLOCK)) {
                // 检查玩家是否在岩浆表面附近（Y坐标接近岩浆表面）
                double lavaSurfaceY = player.blockPosition().getY() + 1.0;
                return player.getY() >= lavaSurfaceY - 0.3; // 更精确的表面检测
            }
        }

        // 检查玩家是否站在岩浆块上
        BlockState standingBlock = player.level().getBlockState(player.blockPosition().below());
        if (standingBlock.is(Blocks.MAGMA_BLOCK)) {
            return true;
        }

        return false;
    }

    // 检查玩家是否装备了岩浆靴
    public static boolean hasLavaBootsEquipped(Player player) {
        // 检查饰品栏
        var accessoryInventory = cn.dawnstring.fatality.inventory.AccessoryInventory.get(player);
        if (accessoryInventory != null) {
            for (int i = 0; i < accessoryInventory.getItemHandler().getSlots(); i++) {
                ItemStack stack = accessoryInventory.getItemHandler().getStackInSlot(i);
                if (stack.getItem() instanceof LavaBoots) {
                    return true;
                }
            }
        }
        return false;
    }

    // 岩浆靴提供的属性加成
    @Override
    public float getMovementSpeedBonus() {
        // 在岩浆上行走时提供额外的移动速度加成
        return 0.1f; // 10%移动速度加成
    }

    @Override
    public float getDefenseBonus() {
        // 提供火焰抗性相关的防御加成
        return 2.0f; // 2点防御加成
    }
}