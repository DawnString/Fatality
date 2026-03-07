package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber
public class Flippers extends AccessoryItem {

    // 存储玩家是否在水中和移动速度加成状态
    private static final Map<UUID, Boolean> playerInWaterMap = new HashMap<>();
    private static final Map<UUID, UUID> speedModifierMap = new HashMap<>();

    // 水下移动速度加成（50%）
    private static final float UNDERWATER_SPEED_BONUS = 0.5f;

    public Flippers(Properties properties) {
        super(properties);
    }

    @Override
    public float getMovementSpeedBonus() {
        // 基础移动速度加成（10%），无论是否在水中
        return 0.1f;
    }

    @Override
    public void applyEffects(Player player, ItemStack stack) {
        // 先调用父类方法应用基础效果
        super.applyEffects(player, stack);

        // 初始化玩家状态
        UUID playerId = player.getUUID();
        playerInWaterMap.put(playerId, false);
    }

    @Override
    public void removeEffects(Player player, ItemStack stack) {
        // 移除水下移动速度加成
        UUID playerId = player.getUUID();
        removeUnderwaterSpeedBonus(player, playerId);

        // 清理状态
        playerInWaterMap.remove(playerId);
        speedModifierMap.remove(playerId);

        // 调用父类方法移除基础效果
        super.removeEffects(player, stack);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            UUID playerId = player.getUUID();

            // 检查玩家是否装备了脚蹼
            if (hasFlippersEquipped(player)) {
                // 检查玩家是否在水中
                boolean isInWater = player.isInWater();
                boolean wasInWater = playerInWaterMap.getOrDefault(playerId, false);

                if (isInWater && !wasInWater) {
                    // 玩家刚进入水中，应用水下移动速度加成
                    applyUnderwaterSpeedBonus(player, playerId);
                    playerInWaterMap.put(playerId, true);
                } else if (!isInWater && wasInWater) {
                    // 玩家刚离开水中，移除水下移动速度加成
                    removeUnderwaterSpeedBonus(player, playerId);
                    playerInWaterMap.put(playerId, false);
                }
            } else {
                // 玩家没有装备脚蹼，确保移除所有效果
                removeUnderwaterSpeedBonus(player, playerId);
                playerInWaterMap.remove(playerId);
                speedModifierMap.remove(playerId);
            }
        }
    }

    // 应用水下移动速度加成
    private static void applyUnderwaterSpeedBonus(Player player, UUID playerId) {
        var movementSpeedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeedAttribute != null) {
            // 生成唯一的UUID用于标识这个修改器
            UUID modifierUUID = UUID.nameUUIDFromBytes(("flippers-underwater-speed-" + playerId.toString()).getBytes());

            // 移除可能存在的旧修改器
            movementSpeedAttribute.removeModifier(modifierUUID);

            // 添加新的移动速度加成修改器
            movementSpeedAttribute.addTransientModifier(new AttributeModifier(
                    modifierUUID,
                    "Flippers Underwater Speed Bonus",
                    UNDERWATER_SPEED_BONUS,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            ));

            // 存储修改器UUID
            speedModifierMap.put(playerId, modifierUUID);
        }
    }

    // 移除水下移动速度加成
    private static void removeUnderwaterSpeedBonus(Player player, UUID playerId) {
        var movementSpeedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeedAttribute != null) {
            UUID modifierUUID = speedModifierMap.get(playerId);
            if (modifierUUID != null) {
                movementSpeedAttribute.removeModifier(modifierUUID);
                speedModifierMap.remove(playerId);
            }
        }
    }

    // 检查玩家是否装备了脚蹼
    private static boolean hasFlippersEquipped(Player player) {
        // 检查饰品栏
        var accessoryInventory = cn.dawnstring.fatality.inventory.AccessoryInventory.get(player);
        if (accessoryInventory != null) {
            for (int i = 0; i < accessoryInventory.getItemHandler().getSlots(); i++) {
                ItemStack accessory = accessoryInventory.getItemHandler().getStackInSlot(i);
                if (accessory.getItem() instanceof Flippers) {
                    return true;
                }
            }
        }
        return false;
    }
}
