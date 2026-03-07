package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

import static cn.dawnstring.fatality.Fatality.DOT_ITEM_DES;

@Mod.EventBusSubscriber
public class NamelessCrown extends AccessoryItem
{
    // 属性修改器UUID
    private static final UUID HEALTH_UUID = UUID.fromString("12345655-1224-1234-1234-127456789abc");
    private static final UUID ARMOR_UUID = UUID.fromString("22345655-1224-1234-1234-127456789abc");
    private static final UUID DAMAGE_REDUCTION_UUID = UUID.fromString("32345655-1224-1234-1234-127456789abc");

    // 伤害免疫冷却跟踪
    private static final Map<UUID, Long> damageImmunityCooldownMap = new HashMap<>();
    private static final Map<UUID, Long> damageImmunityStartMap = new HashMap<>();
    private static final Map<UUID, Float> particleAngleMap = new HashMap<>(); // 粒子旋转角度跟踪
    private static final long IMMUNITY_DURATION = 7500; // 7.5秒免疫持续时间（毫秒）
    private static final long COOLDOWN_DURATION = 180000; // 3分钟冷却时间（毫秒）

    // 减伤比例
    private static final float DAMAGE_REDUCTION_PERCENTAGE = 0.25f; // 25%减伤


    public NamelessCrown(Properties properties)
    {
        super(properties);

        setStory("你值得它\n" +
                DOT_ITEM_DES);
        setAttributesDescription("增加5%最大生命值\n" +
                "增加5%盔甲值\n" +
                "增加25%伤害减免\n" +
                "启用创造模式飞行" +
                "当受到伤害时，免疫本次伤害，获得7.5秒无敌。冷却3分钟" );
    }

    @Override
    public float getHealthPercentageBonus() {
        return 0.05f; // 5%血量加成
    }

    @Override
    public float getDefensePercentageBonus() {
        return 0.05f; // 5%防御加成
    }

    @Override
    public float getDamageReductionBonus() {
        return 0.25f; // 25%伤害减免
    }

    @Override
    public void applyEffects(Player player, ItemStack stack) {
        // 使用基类的统一方法应用属性加成
        super.applyEffects(player, stack);

        // 启用创造模式飞行
        enableCreativeFlight(player);
    }

    @Override
    public void removeEffects(Player player, ItemStack stack) {
        // 禁用创造模式飞行
        disableCreativeFlight(player);

        // 使用基类的统一方法移除属性加成
        super.removeEffects(player, stack);
    }

    // 摔落伤害免疫事件
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player) {
            // 检查玩家是否佩戴无名王冠
            if (hasNamelessCrownEquipped(player)) {
                // 佩戴无名王冠时免疫摔落伤害
                event.setCanceled(true);
            }
        }
    }

    // 伤害处理事件 - 应用减伤效果
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            // 检查玩家是否佩戴无名王冠
            if (hasNamelessCrownEquipped(player)) {
                UUID playerId = player.getUUID();
                long currentTime = System.currentTimeMillis();

                // 检查是否在冷却期内
                Long lastTriggerTime = damageImmunityCooldownMap.get(playerId);
                if (lastTriggerTime != null && currentTime - lastTriggerTime < COOLDOWN_DURATION) {
                    // 冷却中，不触发免疫，但应用减伤效果
                    applyDamageReduction(event);
                    return;
                }

                // 触发伤害免疫
                damageImmunityCooldownMap.put(playerId, currentTime);
                damageImmunityStartMap.put(playerId, currentTime);
                particleAngleMap.put(playerId, 0f); // 初始化粒子旋转角度

                // 取消本次伤害
                event.setCanceled(true);

                // 给玩家添加伤害免疫效果视觉反馈
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 150, 4, false, false)); // 5秒伤害抗性
                player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 150, 0, false, false)); // 5秒发光效果

                // 播放启动粒子效果 - 圆形向外释放黄色粒子
                spawnImmunityActivationParticles(player);
            }
        }
    }

    // 应用减伤效果
    private static void applyDamageReduction(LivingHurtEvent event) {
        float originalDamage = event.getAmount();
        float reducedDamage = originalDamage * (1 - DAMAGE_REDUCTION_PERCENTAGE);
        event.setAmount(reducedDamage);
    }

    // 玩家tick事件，用于管理伤害免疫持续时间和粒子效果
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            UUID playerId = player.getUUID();

            // 检查是否有活跃的伤害免疫效果
            Long immunityStartTime = damageImmunityStartMap.get(playerId);
            if (immunityStartTime != null) {
                long currentTime = System.currentTimeMillis();

                // 检查免疫持续时间是否结束
                if (currentTime - immunityStartTime >= IMMUNITY_DURATION) {
                    damageImmunityStartMap.remove(playerId);
                    particleAngleMap.remove(playerId); // 移除粒子角度跟踪
                    // 移除视觉反馈效果
                    player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
                    player.removeEffect(MobEffects.GLOWING);
                } else {
                    // 免疫持续中，生成环绕粒子效果
                    spawnImmunityRingParticles(player);
                }
            }
        }
    }

    public static boolean isPlayerWearingCrown(Player player) {
        // 检查饰品栏
        var accessoryInventory = cn.dawnstring.fatality.inventory.AccessoryInventory.get(player);
        if (accessoryInventory != null) {
            for (int i = 0; i < accessoryInventory.getItemHandler().getSlots(); i++) {
                ItemStack accessory = accessoryInventory.getItemHandler().getStackInSlot(i);
                if (accessory.getItem() instanceof NamelessCrown) {
                    return true;
                }
            }
        }
        return false;
    }

    // 生成免疫启动粒子效果 - 圆形向外释放黄色粒子
    private static void spawnImmunityActivationParticles(Player player) {
        if (player.level().isClientSide()) {
            Vec3 pos = player.position();
            int particleCount = 36; // 圆形粒子数量
            double radius = 2.0; // 粒子释放半径

            // 生成圆形向外释放的黄色粒子
            for (int i = 0; i < particleCount; i++) {
                double angle = 2 * Math.PI * i / particleCount;
                double x = pos.x + Math.cos(angle) * radius;
                double y = pos.y + 1.0; // 玩家腰部高度
                double z = pos.z + Math.sin(angle) * radius;

                // 计算向外移动的方向
                double dx = Math.cos(angle) * 0.1;
                double dy = 0.05;
                double dz = Math.sin(angle) * 0.1;

                player.level().addParticle(net.minecraft.core.particles.ParticleTypes.FLAME,
                        x, y, z, dx, dy, dz);
            }

            // 额外生成一些随机粒子增强效果
            for (int i = 0; i < 15; i++) {
                double x = pos.x + (player.getRandom().nextDouble() - 0.5) * 3.0;
                double y = pos.y + player.getRandom().nextDouble() * 2.0;
                double z = pos.z + (player.getRandom().nextDouble() - 0.5) * 3.0;

                player.level().addParticle(net.minecraft.core.particles.ParticleTypes.FLAME,
                        x, y, z, 0.0, 0.05, 0.0);
            }
        }
    }

    // 生成免疫持续环绕粒子效果 - 围绕玩家运行的粒子
    private static void spawnImmunityRingParticles(Player player) {
        if (player.level().isClientSide()) {
            UUID playerId = player.getUUID();
            Vec3 pos = player.position();

            // 获取或更新粒子旋转角度
            float currentAngle = particleAngleMap.getOrDefault(playerId, 0f);
            currentAngle += 0.2f; // 旋转速度
            if (currentAngle >= 360f) currentAngle = 0f;
            particleAngleMap.put(playerId, currentAngle);

            // 生成环绕粒子环
            int ringCount = 3; // 3个粒子环
            double[] ringRadii = {1.5, 2.0, 2.5}; // 不同环的半径
            double[] ringHeights = {0.8, 1.2, 1.6}; // 不同环的高度

            for (int ring = 0; ring < ringCount; ring++) {
                double radius = ringRadii[ring];
                double height = ringHeights[ring];
                int particlesPerRing = 12; // 每个环的粒子数量

                for (int i = 0; i < particlesPerRing; i++) {
                    double angle = Math.toRadians(currentAngle + (360.0 / particlesPerRing) * i);
                    double x = pos.x + Math.cos(angle) * radius;
                    double y = pos.y + height;
                    double z = pos.z + Math.sin(angle) * radius;

                    // 使用不同颜色的粒子增强视觉效果
                    if (ring == 0) {
                        player.level().addParticle(net.minecraft.core.particles.ParticleTypes.FLAME,
                                x, y, z, 0.0, 0.0, 0.0);
                    } else if (ring == 1) {
                        player.level().addParticle(net.minecraft.core.particles.ParticleTypes.GLOW,
                                x, y, z, 0.0, 0.0, 0.0);
                    } else {
                        player.level().addParticle(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                                x, y, z, 0.0, 0.0, 0.0);
                    }
                }
            }

            // 随机生成一些上升粒子
            if (player.tickCount % 5 == 0) {
                for (int i = 0; i < 3; i++) {
                    double x = pos.x + (player.getRandom().nextDouble() - 0.5) * 1.5;
                    double y = pos.y + player.getRandom().nextDouble() * 0.5;
                    double z = pos.z + (player.getRandom().nextDouble() - 0.5) * 1.5;

                    player.level().addParticle(net.minecraft.core.particles.ParticleTypes.FIREWORK,
                            x, y, z, 0.0, 0.1, 0.0);
                }
            }
        }
    }

    // 检查玩家是否佩戴无名王冠
    private static boolean hasNamelessCrownEquipped(Player player) {
        // 检查饰品栏
        var accessoryInventory = cn.dawnstring.fatality.inventory.AccessoryInventory.get(player);
        if (accessoryInventory != null) {
            for (int i = 0; i < accessoryInventory.getItemHandler().getSlots(); i++) {
                ItemStack accessory = accessoryInventory.getItemHandler().getStackInSlot(i);
                if (accessory.getItem() instanceof NamelessCrown) {
                    return true;
                }
            }
        }
        return false;
    }
}