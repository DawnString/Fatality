package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class HeartOfTheElements extends AccessoryItem {

    // 存储玩家的充能状态：玩家UUID -> 充能时间(秒)
    private static final Map<UUID, Float> playerChargeMap = new HashMap<>();
    // 存储玩家的激活状态：玩家UUID -> 激活剩余时间(秒)
    private static final Map<UUID, Float> playerActiveMap = new HashMap<>();

    // 充能相关常量
    private static final float MAX_CHARGE_TIME = 100.0f; // 100秒充能完毕
    private static final float ACTIVE_DURATION = 10.0f; // 激活持续10秒
    private static final float DAMAGE_BONUS = 1.00f; // 100%伤害加成

    // 属性修改器UUID
    private static final UUID DAMAGE_BONUS_UUID = UUID.fromString("87654321-4321-4321-4321-987654321abc");

    public HeartOfTheElements(Properties properties) {
        super(properties);
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * 获取玩家的充能进度（0.0到1.0）
     */
    public static float getChargeProgress(Player player) {
        UUID playerId = player.getUUID();
        if (playerChargeMap.containsKey(playerId)) {
            return Math.min(playerChargeMap.get(playerId) / MAX_CHARGE_TIME, 1.0f);
        }
        return 0.0f;
    }

    /**
     * 检查玩家是否充能完毕
     */
    public static boolean isFullyCharged(Player player) {
        return getChargeProgress(player) >= 1.0f;
    }

    /**
     * 检查玩家是否处于激活状态
     */
    public static boolean isActive(Player player) {
        return playerActiveMap.containsKey(player.getUUID()) && playerActiveMap.get(player.getUUID()) > 0;
    }

    /**
     * 获取激活剩余时间
     */
    public static float getActiveRemainingTime(Player player) {
        UUID playerId = player.getUUID();
        if (playerActiveMap.containsKey(playerId)) {
            return playerActiveMap.get(playerId);
        }
        return 0.0f;
    }

    /**
     * 激活元素之心效果
     */
    public static boolean activate(Player player) {
        if (isFullyCharged(player) && !isActive(player)) {
            UUID playerId = player.getUUID();
            playerActiveMap.put(playerId, ACTIVE_DURATION);
            playerChargeMap.put(playerId, 0.0f); // 重置充能
            return true;
        }
        return false;
    }

    /**
     * 检查玩家是否佩戴元素之心
     */
    public static boolean hasHeartOfTheElementsEquipped(Player player) {
        var accessoryInventory = cn.dawnstring.fatality.inventory.AccessoryInventory.get(player);
        if (accessoryInventory != null) {
            for (int i = 0; i < accessoryInventory.getItemHandler().getSlots(); i++) {
                ItemStack accessory = accessoryInventory.getItemHandler().getStackInSlot(i);
                if (accessory.getItem() instanceof HeartOfTheElements) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 应用饰品效果
     */
    @Override
    public void applyEffects(Player player, ItemStack stack) {
        super.applyEffects(player, stack);

        // 如果处于激活状态，应用伤害加成
        if (isActive(player)) {
            applyDamageBonus(player);
        }
    }

    /**
     * 移除饰品效果
     */
    @Override
    public void removeEffects(Player player, ItemStack stack) {
        super.removeEffects(player, stack);
        removeDamageBonus(player);
    }

    /**
     * 应用伤害加成
     */
    private void applyDamageBonus(Player player) {
        // 这里需要实现具体的伤害加成逻辑
        // 由于AttributeSystem已经处理了饰品加成，我们只需要在getPanelDamageBonus中返回加成值
    }

    /**
     * 移除伤害加成
     */
    private void removeDamageBonus(Player player) {
        // 移除伤害加成
    }

    /**
     * 获取面板伤害加成（激活状态下返回100%加成）
     */
    @Override
    public float getPanelDamageBonus() {
        // 这个方法会在AttributeSystem中被调用
        // 我们需要在这里检查激活状态并返回相应的加成
        return 0.0f; // 基础状态下没有加成
    }

    /**
     * 获取当前的面板伤害加成（考虑激活状态）
     */
    public float getCurrentPanelDamageBonus(Player player) {
        if (isActive(player)) {
            return DAMAGE_BONUS; // 100%伤害加成
        }
        return 0.0f;
    }

    /**
     * 每tick更新充能和激活状态
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;

            if (hasHeartOfTheElementsEquipped(player)) {
                UUID playerId = player.getUUID();

                // 更新充能状态
                if (!isActive(player)) {
                    float currentCharge = playerChargeMap.getOrDefault(playerId, 0.0f);
                    if (currentCharge < MAX_CHARGE_TIME) {
                        playerChargeMap.put(playerId, currentCharge + 0.05f); // 每tick增加0.05秒（每秒1秒）
                    }
                }

                // 更新激活状态
                if (isActive(player)) {
                    float remainingTime = playerActiveMap.get(playerId);
                    remainingTime -= 0.05f; // 每tick减少0.05秒（每秒1秒）

                    if (remainingTime <= 0) {
                        playerActiveMap.remove(playerId);
                        // 激活结束，需要更新属性
                        var accessoryInventory = cn.dawnstring.fatality.inventory.AccessoryInventory.get(player);
                        if (accessoryInventory != null) {
                            accessoryInventory.updatePlayerAttributes();
                        }
                    } else {
                        playerActiveMap.put(playerId, remainingTime);
                    }
                }
            } else {
                // 玩家没有佩戴元素之心，清理状态
                UUID playerId = player.getUUID();
                playerChargeMap.remove(playerId);
                if (playerActiveMap.containsKey(playerId)) {
                    playerActiveMap.remove(playerId);
                    // 清理属性
                    var accessoryInventory = cn.dawnstring.fatality.inventory.AccessoryInventory.get(player);
                    if (accessoryInventory != null) {
                        accessoryInventory.updatePlayerAttributes();
                    }
                }
            }
        }
    }
}