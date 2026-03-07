package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber
public class MoltenLightShield extends AccessoryItem
{
    // 伤害免疫冷却跟踪
    private static final Map<UUID, Long> damageImmunityCooldownMap = new HashMap<>();
    private static final long COOLDOWN_DURATION = 180000; // 180秒冷却时间（毫秒）

    public MoltenLightShield(Properties properties)
    {
        super(properties);
    }

    // 伤害处理事件 - 免疫伤害效果
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            // 检查玩家是否佩戴熔光护盾
            if (hasMoltenLightShieldEquipped(player)) {
                UUID playerId = player.getUUID();
                long currentTime = System.currentTimeMillis();

                // 检查是否在冷却期内
                Long lastTriggerTime = damageImmunityCooldownMap.get(playerId);
                if (lastTriggerTime != null && currentTime - lastTriggerTime < COOLDOWN_DURATION) {
                    // 冷却中，不触发免疫
                    return;
                }

                // 触发伤害免疫
                damageImmunityCooldownMap.put(playerId, currentTime);

                // 取消本次伤害
                event.setCanceled(true);

                // 播放粒子效果
                spawnImmunityParticles(player);
            }
        }
    }

    // 生成免疫粒子效果
    private static void spawnImmunityParticles(Player player) {
        if (player.level().isClientSide()) {
            Vec3 pos = player.position();

            // 生成圆形向外释放的橙色火焰粒子
            int particleCount = 16;
            double radius = 1.5;

            for (int i = 0; i < particleCount; i++) {
                double angle = 2 * Math.PI * i / particleCount;
                double x = pos.x + Math.cos(angle) * radius;
                double y = pos.y + 1.0; // 玩家腰部高度
                double z = pos.z + Math.sin(angle) * radius;

                // 计算向外移动的方向
                double dx = Math.cos(angle) * 0.2;
                double dy = 0.1;
                double dz = Math.sin(angle) * 0.2;

                player.level().addParticle(net.minecraft.core.particles.ParticleTypes.FLAME,
                        x, y, z, dx, dy, dz);
            }

            // 生成一些随机上升的熔岩粒子
            for (int i = 0; i < 8; i++) {
                double x = pos.x + (player.getRandom().nextDouble() - 0.5) * 2.0;
                double y = pos.y + player.getRandom().nextDouble() * 1.5;
                double z = pos.z + (player.getRandom().nextDouble() - 0.5) * 2.0;

                player.level().addParticle(net.minecraft.core.particles.ParticleTypes.LAVA,
                        x, y, z, 0.0, 0.15, 0.0);
            }
        }
    }

    // 检查玩家是否佩戴熔光护盾
    private static boolean hasMoltenLightShieldEquipped(Player player) {
        // 检查饰品栏
        var accessoryInventory = cn.dawnstring.fatality.inventory.AccessoryInventory.get(player);
        if (accessoryInventory != null) {
            for (int i = 0; i < accessoryInventory.getItemHandler().getSlots(); i++) {
                ItemStack accessory = accessoryInventory.getItemHandler().getStackInSlot(i);
                if (accessory.getItem() instanceof MoltenLightShield) {
                    return true;
                }
            }
        }
        return false;
    }
}