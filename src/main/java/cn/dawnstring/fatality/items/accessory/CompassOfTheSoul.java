package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class CompassOfTheSoul extends AccessoryItem
{
    // 复活冷却跟踪
    private static final Map<UUID, Long> revivalCooldownMap = new HashMap<>();
    private static final long COOLDOWN_DURATION = 300000; // 300秒冷却时间（毫秒）
    private static final float HEALTH_RESTORE_PERCENTAGE = 0.20f; // 恢复20%血量

    public CompassOfTheSoul(Properties properties)
    {
        super(properties);
    }

    // 致命伤害处理事件 - 复活效果
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            // 检查玩家是否佩戴灵魂指南针
            if (hasCompassOfTheSoulEquipped(player)) {
                UUID playerId = player.getUUID();
                long currentTime = System.currentTimeMillis();

                // 检查是否在冷却期内
                Long lastRevivalTime = revivalCooldownMap.get(playerId);
                if (lastRevivalTime != null && currentTime - lastRevivalTime < COOLDOWN_DURATION) {
                    // 冷却中，不触发复活效果
                    return;
                }

                // 触发复活效果
                revivalCooldownMap.put(playerId, currentTime);

                // 取消死亡事件
                event.setCanceled(true);

                // 恢复玩家20%最大生命值
                float maxHealth = player.getMaxHealth();
                float restoreAmount = maxHealth * HEALTH_RESTORE_PERCENTAGE;
                float newHealth = Math.min(maxHealth, player.getHealth() + restoreAmount);
                player.setHealth(newHealth);

                // 播放复活音效
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0f, 1.0f);

                // 生成复活粒子效果
                spawnRevivalParticles(player);
            }
        }
    }

    // 生成复活粒子效果
    private static void spawnRevivalParticles(Player player) {
        if (player.level().isClientSide()) {
            Vec3 playerPos = player.position();

            // 生成灵魂粒子效果（使用SOUL_FIRE_FLAME和SOUL粒子）
            for (int i = 0; i < 30; i++) {
                double x = playerPos.x + (player.getRandom().nextDouble() - 0.5) * 3.0;
                double y = playerPos.y + player.getRandom().nextDouble() * 2.0;
                double z = playerPos.z + (player.getRandom().nextDouble() - 0.5) * 3.0;

                player.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        x, y, z, 0.0, 0.1, 0.0);
            }

            // 生成灵魂粒子环绕效果
            for (int i = 0; i < 20; i++) {
                double angle = (i / 20.0) * 2 * Math.PI;
                double radius = 1.5;
                double x = playerPos.x + Math.cos(angle) * radius;
                double y = playerPos.y + 1.0;
                double z = playerPos.z + Math.sin(angle) * radius;

                player.level().addParticle(ParticleTypes.SOUL,
                        x, y, z, 0.0, 0.05, 0.0);
            }
        }
    }

    // 检查玩家是否佩戴灵魂指南针
    private static boolean hasCompassOfTheSoulEquipped(Player player) {
        var accessoryInventory = cn.dawnstring.fatality.inventory.AccessoryInventory.get(player);
        if (accessoryInventory != null) {
            for (int i = 0; i < accessoryInventory.getItemHandler().getSlots(); i++) {
                ItemStack accessory = accessoryInventory.getItemHandler().getStackInSlot(i);
                if (accessory.getItem() instanceof CompassOfTheSoul) {
                    return true;
                }
            }
        }
        return false;
    }
}