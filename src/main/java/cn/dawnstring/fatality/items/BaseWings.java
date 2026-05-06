package cn.dawnstring.fatality.items;

import cn.dawnstring.fatality.mixins.LivingEntityAccessor;
import cn.dawnstring.fatality.utils.TooltipHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
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
    private static final Map<UUID, WingFlightData> flightDataMap = new HashMap<>();

    protected final float maxFlightTime;
    protected final float maxHorizontalSpeed;
    protected final float maxVerticalSpeed;
    protected final float horizontalAcceleration;
    protected final float verticalAcceleration;
    protected final float glideSpeed;

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
        UUID playerId = player.getUUID();
        if (!flightDataMap.containsKey(playerId)) {
            flightDataMap.put(playerId, new WingFlightData(maxFlightTime, maxHorizontalSpeed,
                    maxVerticalSpeed, horizontalAcceleration, verticalAcceleration, glideSpeed));
        }
    }

    @Override
    public void removeEffects(Player player, ItemStack stack) {
        flightDataMap.remove(player.getUUID());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        UUID playerId = player.getUUID();

        if (!hasWingsEquipped(player)) {
            flightDataMap.remove(playerId);
            return;
        }

        WingFlightData flightData = flightDataMap.get(playerId);
        if (flightData == null) {
            flightData = createFlightDataFromEquippedWings(player);
            if (flightData == null) return;
            flightDataMap.put(playerId, flightData);
        }

        handleFlight(player, flightData);
    }

    private static WingFlightData createFlightDataFromEquippedWings(Player player) {
        var accessoryInventory = cn.dawnstring.fatality.inventory.AccessoryInventory.get(player);
        if (accessoryInventory == null) return null;
        for (int i = 0; i < accessoryInventory.getItemHandler().getSlots(); i++) {
            ItemStack stack = accessoryInventory.getItemHandler().getStackInSlot(i);
            if (stack.getItem() instanceof BaseWings wings) {
                return new WingFlightData(
                        wings.maxFlightTime,
                        wings.maxHorizontalSpeed,
                        wings.maxVerticalSpeed,
                        wings.horizontalAcceleration,
                        wings.verticalAcceleration,
                        wings.glideSpeed
                );
            }
        }
        return null;
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player && hasWingsEquipped(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        flightDataMap.remove(event.getEntity().getUUID());
    }

    public static boolean hasWingsEquipped(Player player) {
        var accessoryInventory = cn.dawnstring.fatality.inventory.AccessoryInventory.get(player);
        if (accessoryInventory == null) return false;
        for (int i = 0; i < accessoryInventory.getItemHandler().getSlots(); i++) {
            if (accessoryInventory.getItemHandler().getStackInSlot(i).getItem() instanceof BaseWings) {
                return true;
            }
        }
        return false;
    }

    private static boolean isJumpKeyDown(Player player) {
        if (player.level().isClientSide() && FMLEnvironment.dist.isClient()) {
            return net.minecraft.client.Minecraft.getInstance().options.keyJump.isDown();
        }
        return ((LivingEntityAccessor) player).getJumping();
    }

    private static void handleFlight(Player player, WingFlightData flightData) {
        if (player.getAbilities().flying) return;

        boolean holdingJump = isJumpKeyDown(player);
        boolean onGround = player.onGround();

        if (onGround) {
            flightData.isFlying = false;
            flightData.isGliding = false;
            flightData.currentHorizontalSpeed = 0;
            flightData.currentVerticalSpeed = 0;
            flightData.currentDirection = Vec3.ZERO;
            flightData.targetDirection = Vec3.ZERO;
            flightData.remainingFlightTime = Math.min(flightData.remainingFlightTime + 0.5f, flightData.maxFlightTime);
            return;
        }

        if (player.isInWater() || player.onClimbable()) {
            flightData.isFlying = false;
            flightData.isGliding = false;
            return;
        }

        if (holdingJump) {
            if (flightData.remainingFlightTime > 0) {
                flightData.isFlying = true;
                flightData.isGliding = false;
                handleActiveFlight(player, flightData);
            } else {
                flightData.isFlying = false;
                flightData.isGliding = true;
                handleGlide(player, flightData);
            }
        } else {
            flightData.isFlying = false;
            flightData.isGliding = false;
            handleFreeFall(player, flightData);
        }
    }

    private static void handleActiveFlight(Player player, WingFlightData flightData) {
        Vec3 motion = player.getDeltaMovement();

        boolean hasForward = player.zza > 0;
        boolean hasBackward = player.zza < 0;
        boolean hasLeft = player.xxa < 0;
        boolean hasRight = player.xxa > 0;

        flightData.targetDirection = calcDirection(player, hasForward, hasBackward, hasLeft, hasRight);

        if (flightData.currentDirection.length() == 0 && flightData.targetDirection.length() > 0) {
            flightData.currentDirection = flightData.targetDirection.normalize();
        } else if (flightData.targetDirection.length() > 0) {
            flightData.currentDirection = lerpDir(flightData.currentDirection, flightData.targetDirection.normalize(), 0.25f);
        } else if (flightData.currentDirection.length() > 0) {
            flightData.currentDirection = flightData.currentDirection.scale(0.9);
            if (flightData.currentDirection.length() < 0.01) flightData.currentDirection = Vec3.ZERO;
        }

        float targetHSpeed = flightData.targetDirection.length() > 0 ? flightData.maxHorizontalSpeed : 0;
        double hSpeed = lerpSpeed(motion.horizontalDistance(), targetHSpeed, flightData.horizontalAcceleration * 0.2f);

        double vSpeed = lerpSpeed(motion.y, flightData.maxVerticalSpeed, flightData.verticalAcceleration * 0.15f);

        Vec3 hMotion = flightData.currentDirection.length() > 0
                ? flightData.currentDirection.normalize().scale(hSpeed)
                : Vec3.ZERO;

        player.setDeltaMovement(hMotion.x, vSpeed, hMotion.z);
        flightData.currentHorizontalSpeed = (float) hSpeed;
        flightData.currentVerticalSpeed = (float) vSpeed;

        flightData.remainingFlightTime = Math.max(0, flightData.remainingFlightTime - 0.05f);
    }

    private static void handleGlide(Player player, WingFlightData flightData) {
        Vec3 motion = player.getDeltaMovement();

        boolean hasForward = player.zza > 0;
        boolean hasBackward = player.zza < 0;
        boolean hasLeft = player.xxa < 0;
        boolean hasRight = player.xxa > 0;

        flightData.targetDirection = calcDirection(player, hasForward, hasBackward, hasLeft, hasRight);

        if (flightData.targetDirection.length() > 0) {
            flightData.currentDirection = flightData.currentDirection.length() == 0
                    ? flightData.targetDirection.normalize()
                    : lerpDir(flightData.currentDirection, flightData.targetDirection.normalize(), 0.3f);
        } else if (flightData.currentDirection.length() > 0) {
            flightData.currentDirection = flightData.currentDirection.scale(0.9);
            if (flightData.currentDirection.length() < 0.01) flightData.currentDirection = Vec3.ZERO;
        }

        double hSpeed = motion.horizontalDistance();
        double targetH = flightData.targetDirection.length() > 0
                ? flightData.maxHorizontalSpeed * 0.7
                : Math.max(hSpeed - 0.02, 0);
        hSpeed = lerpSpeed(hSpeed, targetH, 0.25f);

        double glideY = Math.max(motion.y - 0.01, -1.5);

        Vec3 hMotion = flightData.currentDirection.length() > 0
                ? flightData.currentDirection.normalize().scale(hSpeed)
                : Vec3.ZERO;

        player.setDeltaMovement(hMotion.x, glideY, hMotion.z);
    }

    private static void handleFreeFall(Player player, WingFlightData flightData) {
        flightData.currentDirection = Vec3.ZERO;
        flightData.currentHorizontalSpeed = 0;
        flightData.currentVerticalSpeed = 0;
    }

    private static Vec3 calcDirection(Player player, boolean forward, boolean backward, boolean left, boolean right) {
        float yaw = player.getYRot() * (float) Math.PI / 180.0f;
        float sin = (float) Math.sin(yaw);
        float cos = (float) Math.cos(yaw);

        double dx = 0, dz = 0;
        if (forward)  { dx -= sin; dz += cos; }
        if (backward) { dx += sin; dz -= cos; }
        if (left)     { dx -= cos; dz -= sin; }
        if (right)    { dx += cos; dz += sin; }

        return new Vec3(dx, 0, dz);
    }

    private static Vec3 lerpDir(Vec3 from, Vec3 to, float factor) {
        double x = from.x + (to.x - from.x) * factor;
        double y = from.y + (to.y - from.y) * factor;
        double z = from.z + (to.z - from.z) * factor;
        Vec3 r = new Vec3(x, y, z);
        return r.length() > 0 ? r : to;
    }

    private static double lerpSpeed(double current, double target, float factor) {
        return current + (target - current) * factor;
    }

    public static float getRemainingFlightTime(Player player) {
        WingFlightData data = flightDataMap.get(player.getUUID());
        return data != null ? data.remainingFlightTime : 0;
    }

    public static boolean isFlying(Player player) {
        WingFlightData data = flightDataMap.get(player.getUUID());
        return data != null && data.isFlying;
    }

    public static boolean isGliding(Player player) {
        WingFlightData data = flightDataMap.get(player.getUUID());
        return data != null && data.isGliding;
    }

    public static float getMaxFlightTime(Player player) {
        WingFlightData data = flightDataMap.get(player.getUUID());
        return data != null ? data.maxFlightTime : 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        StringBuilder desc = new StringBuilder();
        desc.append("\n§a最大飞行时间：").append(this.maxFlightTime).append("秒")
                .append("\n§a滑行速度：").append(String.format("%.2f", this.glideSpeed * 20)).append(" block/s")
                .append("\n§a水平加速度：").append(String.format("%.2f", this.horizontalAcceleration * 20)).append(" block/s²")
                .append("\n§a垂直加速度：").append(String.format("%.2f", this.verticalAcceleration * 20)).append(" block/s²")
                .append("\n§a最大水平速度：").append(String.format("%.2f", this.maxHorizontalSpeed * 20)).append(" block/s")
                .append("\n§a最大垂直速度：").append(String.format("%.2f", this.maxVerticalSpeed * 20)).append(" block/s");

        if (desc.length() > 0) {
            this.attributesDescription = desc.toString().trim();
        }

        TooltipHelper.addDescriptiveTooltip(stack, level, tooltip, flag, story, attributesDescription);
    }

    public BaseWings setStory(String story) {
        this.story = story;
        return this;
    }

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

        public Vec3 currentDirection = Vec3.ZERO;
        public Vec3 targetDirection = Vec3.ZERO;

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
