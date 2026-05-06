package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class WaterBloomBoots extends AccessoryItem
{
    private static final Map<UUID, Boolean> playerOnWaterMap = new HashMap<>();
    private static final Map<UUID, WaterWalkingData> waterWalkingDataMap = new HashMap<>();

    public WaterBloomBoots() {
        super(new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.UNCOMMON)
                .fireResistant());
    }

    @Override
    public void applyEffects(Player player, ItemStack stack) {
        super.applyEffects(player, stack);
        UUID playerId = player.getUUID();
        playerOnWaterMap.putIfAbsent(playerId, false);
        waterWalkingDataMap.putIfAbsent(playerId, new WaterWalkingData());
    }

    @Override
    public void removeEffects(Player player, ItemStack stack) {
        super.removeEffects(player, stack);
        UUID playerId = player.getUUID();
        playerOnWaterMap.remove(playerId);
        waterWalkingDataMap.remove(playerId);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            UUID playerId = player.getUUID();

            boolean hasWaterStriderBoots = hasAccessoryEquipped(player, WaterBloomBoots.class);

            if (!hasWaterStriderBoots) {
                playerOnWaterMap.remove(playerId);
                waterWalkingDataMap.remove(playerId);
                return;
            }

            boolean isOnWater = isPlayerOnWater(player);
            playerOnWaterMap.put(playerId, isOnWater);

            WaterWalkingData waterData = waterWalkingDataMap.get(playerId);
            if (waterData == null) {
                waterData = new WaterWalkingData();
                waterWalkingDataMap.put(playerId, waterData);
            }

            if (isOnWater) {
                handleWaterWalking(player, waterData);
            } else {
                waterData.wasOnWaterLastTick = false;
            }
        }
    }

    private static boolean isPlayerOnWater(Player player) {
        AABB playerBounds = player.getBoundingBox();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                FluidState fluidState = player.level().getFluidState(
                        player.blockPosition().offset(x, -1, z)
                );

                if (!fluidState.isEmpty() && fluidState.isSource()) {
                    return true;
                }

                if (player.isInWater() && isPlayerNearWaterSurface(player)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean isPlayerNearWaterSurface(Player player) {
        if (player.isInWater()) {
            BlockState blockAbove = player.level().getBlockState(player.blockPosition().above());
            return blockAbove.isAir();
        }
        return false;
    }

    private static void handleWaterWalking(Player player, WaterWalkingData waterData) {
        if (player.isInWater()) {
            Vec3 motion = player.getDeltaMovement();

            double buoyancy = 0.12;
            double newY = Math.min(motion.y + buoyancy, 0.2);

            if (motion.y < -0.1) {
                newY = Math.min(motion.y + buoyancy * 2, 0.15);
            }

            player.setDeltaMovement(motion.x, newY, motion.z);

            if (isPlayerNearWaterSurface(player) && player.getY() < getWaterSurfaceY(player)) {
                double surfaceY = getWaterSurfaceY(player);
                if (surfaceY - player.getY() < 0.5) {
                    player.setPos(player.getX(), surfaceY + 0.1, player.getZ());
                }
            }
        }

        if (isPlayerOnWaterSurface(player)) {
            applyWaterWalkingMovementBonus(player, waterData);
        }

        waterData.wasOnWaterLastTick = true;
    }

    private static boolean isPlayerOnWaterSurface(Player player) {
        if (!player.isInWater() && isPlayerOnWater(player)) {
            return true;
        }

        if (player.isInWater() && isPlayerNearWaterSurface(player)) {
            double waterSurfaceY = getWaterSurfaceY(player);
            return player.getY() >= waterSurfaceY - 0.3;
        }

        return false;
    }

    private static double getWaterSurfaceY(Player player) {
        for (int y = (int) player.getY(); y < player.level().getMaxBuildHeight(); y++) {
            BlockState blockState = player.level().getBlockState(player.blockPosition().atY(y));
            if (blockState.isAir() || !blockState.getFluidState().isEmpty()) {
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

    private static void applyWaterWalkingMovementBonus(Player player, WaterWalkingData waterData) {
        float waterWalkingSpeed = 0.1f;

        if (player.zza != 0 || player.xxa != 0) {
            player.setSpeed(waterWalkingSpeed);

            Vec3 motion = player.getDeltaMovement();
            double horizontalResistance = 0.8;
            player.setDeltaMovement(motion.x * horizontalResistance, motion.y, motion.z * horizontalResistance);
        }

        if (player.fallDistance > 0) {
            player.fallDistance *= 0.5f;
        }
    }

    @Override
    public float getMovementSpeedBonus() {
        return 0.15f;
    }

    @Override
    public float getDefenseBonus() {
        return 1.0f;
    }

    public static class WaterWalkingData {
        public boolean wasOnWaterLastTick = false;
        public long lastWaterContactTime = 0;
    }
}
