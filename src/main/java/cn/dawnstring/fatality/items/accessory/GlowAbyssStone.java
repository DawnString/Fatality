package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;
import net.minecraft.core.particles.ParticleTypes;
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
public class GlowAbyssStone extends AccessoryItem
{
    // 伤害减少冷却跟踪
    private static final Map<UUID, Long> damageReductionCooldownMap = new HashMap<>();

    // 冷却时间
    private static final long COOLDOWN_DURATION = 60000; // 60秒冷却时间（毫秒）

    // 减伤比例
    private static final float DAMAGE_REDUCTION_PERCENTAGE = 0.70f; // 70%减伤

    public GlowAbyssStone(Properties properties)
    {
        super(properties);
    }

    // 伤害处理事件 - 应用减伤效果
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            // 检查玩家是否佩戴发光深渊石
            if (hasGlowAbyssStoneEquipped(player)) {
                UUID playerId = player.getUUID();
                long currentTime = System.currentTimeMillis();

                // 检查是否在冷却期内
                Long lastTriggerTime = damageReductionCooldownMap.get(playerId);
                if (lastTriggerTime != null && currentTime - lastTriggerTime < COOLDOWN_DURATION) {
                    // 冷却中，不触发伤害减少效果
                    return;
                }

                // 触发伤害减少效果
                damageReductionCooldownMap.put(playerId, currentTime);

                // 应用伤害减少
                float originalDamage = event.getAmount();
                float reducedDamage = originalDamage * (1 - DAMAGE_REDUCTION_PERCENTAGE);
                event.setAmount(reducedDamage);

                // 播放启动粒子效果 - 圆形向外释放发光粒子
                spawnDamageReductionParticles(player);
            }
        }
    }

    // 生成伤害减少粒子效果 - 圆形向外释放发光粒子
    private static void spawnDamageReductionParticles(Player player) {
        if (player.level().isClientSide()) {
            Vec3 pos = player.position();
            int particleCount = 24; // 圆形粒子数量
            double radius = 1.8; // 粒子释放半径

            // 生成圆形向外释放的发光粒子
            for (int i = 0; i < particleCount; i++) {
                double angle = 2 * Math.PI * i / particleCount;
                double x = pos.x + Math.cos(angle) * radius;
                double y = pos.y + 1.2; // 玩家胸部高度
                double z = pos.z + Math.sin(angle) * radius;

                // 计算向外移动的方向
                double dx = Math.cos(angle) * 0.15;
                double dy = 0.08;
                double dz = Math.sin(angle) * 0.15;

                player.level().addParticle(ParticleTypes.GLOW,
                        x, y, z, dx, dy, dz);
            }

            // 额外生成一些随机发光粒子增强效果
            for (int i = 0; i < 12; i++) {
                double x = pos.x + (player.getRandom().nextDouble() - 0.5) * 2.5;
                double y = pos.y + player.getRandom().nextDouble() * 1.8;
                double z = pos.z + (player.getRandom().nextDouble() - 0.5) * 2.5;

                player.level().addParticle(ParticleTypes.END_ROD,
                        x, y, z, 0.0, 0.06, 0.0);
            }

            // 生成一些蓝色粒子，符合深渊主题
            for (int i = 0; i < 8; i++) {
                double x = pos.x + (player.getRandom().nextDouble() - 0.5) * 1.5;
                double y = pos.y + player.getRandom().nextDouble() * 1.0;
                double z = pos.z + (player.getRandom().nextDouble() - 0.5) * 1.5;

                player.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        x, y, z, 0.0, 0.04, 0.0);
            }
        }
    }

    // 检查玩家是否佩戴发光深渊石
    private static boolean hasGlowAbyssStoneEquipped(Player player) {
        // 检查饰品栏
        var accessoryInventory = cn.dawnstring.fatality.inventory.AccessoryInventory.get(player);
        if (accessoryInventory != null) {
            for (int i = 0; i < accessoryInventory.getItemHandler().getSlots(); i++) {
                ItemStack accessory = accessoryInventory.getItemHandler().getStackInSlot(i);
                if (accessory.getItem() instanceof GlowAbyssStone) {
                    return true;
                }
            }
        }
        return false;
    }
}