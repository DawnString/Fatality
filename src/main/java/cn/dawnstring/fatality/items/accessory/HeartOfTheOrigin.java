package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class HeartOfTheOrigin extends AccessoryItem
{
    // 伤害免疫冷却跟踪
    private static final Map<UUID, Long> damageImmunityCooldownMap = new HashMap<>();
    private static final long COOLDOWN_DURATION = 120000; // 120秒冷却时间（毫秒）

    public HeartOfTheOrigin(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getHealthBonus() {
        return 100.0f;
    }

    @Override
    public float getDefenseBonus() {
        return 30.0f;
    }

    @Override
    public float getHealthRegenerationBonus() {
        return 0.2f;
    }

    @Override
    public float getCriticalChanceBonus() {
        return 0.1f;
    }

    @Override
    public void applyEffects(Player player, ItemStack stack) {
        // 先调用父类的效果应用
        super.applyEffects(player, stack);

        // 添加负面效果免疫
        applyNegativeEffectImmunity(player);
    }

    @Override
    public void removeEffects(Player player, ItemStack stack) {
        // 移除负面效果免疫
        removeNegativeEffectImmunity(player);

        // 调用父类的效果移除
        super.removeEffects(player, stack);
    }

    /**
     * 应用负面效果免疫
     */
    private void applyNegativeEffectImmunity(Player player) {
        // 免疫中毒效果
        if (player.hasEffect(MobEffects.POISON)) {
            player.removeEffect(MobEffects.POISON);
        }

        // 免疫凋零效果
        if (player.hasEffect(MobEffects.WITHER)) {
            player.removeEffect(MobEffects.WITHER);
        }

        // 免疫缓慢效果
        if (player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        }

        // 免疫饥饿效果
        if (player.hasEffect(MobEffects.HUNGER)) {
            player.removeEffect(MobEffects.HUNGER);
        }

        // 免疫失明效果
        if (player.hasEffect(MobEffects.BLINDNESS)) {
            player.removeEffect(MobEffects.BLINDNESS);
        }
    }

    /**
     * 移除负面效果免疫（主要是清理工作）
     */
    private void removeNegativeEffectImmunity(Player player)
    {
    }

    // 伤害处理事件 - 免疫伤害效果（从MoltenLightShield移动过来）
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            // 检查玩家是否佩戴起源之心
            if (hasHeartOfTheOriginEquipped(player)) {
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

    // 生成免疫粒子效果（从MoltenLightShield移动过来）
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
        }
    }

    // 检查玩家是否佩戴起源之心
    private static boolean hasHeartOfTheOriginEquipped(Player player) {
        // 检查饰品栏
        var accessoryInventory = cn.dawnstring.fatality.inventory.AccessoryInventory.get(player);
        if (accessoryInventory != null) {
            for (int i = 0; i < accessoryInventory.getItemHandler().getSlots(); i++) {
                ItemStack accessory = accessoryInventory.getItemHandler().getStackInSlot(i);
                if (accessory.getItem() instanceof HeartOfTheOrigin) {
                    return true;
                }
            }
        }
        return false;
    }
}