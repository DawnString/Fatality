package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.AccessoryItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class CrownOfTheSupremeDemigod extends AccessoryItem
{
    // 属性修改器UUID
    private static final UUID HEALTH_UUID = UUID.fromString("12345655-1224-1234-1234-127456789abc");
    private static final UUID ARMOR_UUID = UUID.fromString("22345655-1224-1234-1234-127456789abc");
    private static final UUID DAMAGE_REDUCTION_UUID = UUID.fromString("32345655-1224-1234-1234-127456789abc");

    // 伤害免疫冷却跟踪
    private static final Map<UUID, Long> damageImmunityCooldownMap = new HashMap<>();
    private static final Map<UUID, Long> damageImmunityStartMap = new HashMap<>();
    private static final Map<UUID, Float> particleAngleMap = new HashMap<>(); // 粒子旋转角度跟踪
    private static final long IMMUNITY_DURATION = 9000; // 9秒免疫持续时间（毫秒）
    private static final long COOLDOWN_DURATION = 180000; // 3分钟冷却时间（毫秒）

    // 减伤比例
    private static final float DAMAGE_REDUCTION_PERCENTAGE = 0.50f; // 50%减伤

    public CrownOfTheSupremeDemigod(Properties properties)
    {
        super(properties);

        setStory("「当大地的哀嚎穿透岩层渗入地底，我告诫同胞，须铭记圣灵骸骨里未冷的星火。」\n" +
                "「当暴政的铁蹄碾过每寸土壤，我向追随者昭示，当以断剑丈量登神长阶的陡峭。」\n" +
                "「纵然复仇之路注定要以神血浸染双手，我却从未熄灭重塑天平的渴望。」\n" +
                "「若伪神将王座筑于众生脊背，我便撕裂地壳令罪孽沉入岩浆。」\n" +
                "「仰望穹顶而战栗的人们终将挺直脊梁，蜷缩阴影者亦能目睹山巅崩裂时的天光。」\n" +
                "「我的征伐必令枷锁化作尘烟，令万千嘶吼汇成自由钟声的轰鸣。」\n" +
                "「到那时，权柄不再代代相承，悲鸣与奴役将永封于褪色的史诗。」\n" +
                "「所有压迫与谎言都将消散，而染血的冠冕将被锻造成通向黎明的桥梁。」\n" +
                "「倘若无人燃起烽火，元素之灵将永远沉寂于沉默的墓碑。」\n" +
                "「倘若无人踏碎深渊，地底冤魂怎得见伪神宝座下的尸骸？」\n" +
                "「他们斥责我，蝼蚁本可苟活于罅隙，何苦拖拽苍生共赴焚身烈火。」\n" +
                "「他们讥讽我，弱肉强食本是天地律法，逆势而行只会酿造更深的苦痛。」\n" +
                "「但我，我不能容忍羔羊跪拜屠夫时还称颂刀锋的仁慈。」\n" +
                "「我不忍听闻圣灵临终哀鸣被谱写成暴君加冕的颂诗。」\n" +
                "「我愿作裂世惊雷，劈开永夜为文明开辟刑场与沃土。」\n" +
                "「若神圣契约早已被鲜血玷污，我便以罪孽重塑法典。」\n" +
                "「若我不曾目睹残暴的屠杀，此刻仍信着神明怜恤众生。」\n" +
                "「我不会让所谓宿命掐灭最后的火种。」\n" +
                "「我趟过血河而将亡魂铭刻为史诗的标点。」\n" +
                "「我承载肮脏与荣光，誓令每双眼睛看见云层后的曙光。」\n" +
                "「因我立誓，要有光。」\n" +
                "「终有一日，大陆即证道之坛，众生即涅槃之凤。」");
        setAttributesDescription("增加20%最大生命值\n" +
                "增加50%伤害减免\n" +
                "启用创造模式飞行\n" +
                "当受到伤害时，免疫本次伤害，获得9秒无敌。冷却3分钟" );
    }

    @Override
    public float getHealthPercentageBonus() {
        return 0.20f; // 20%血量加成
    }

    @Override
    public float getDamageReductionBonus() {
        return 0.50f; // 50%伤害减免
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
            if (hasCrownOfTheSupremeDemigodEquipped(player)) {
                // 佩戴无名王冠时免疫摔落伤害
                event.setCanceled(true);
            }
        }
    }

    // 伤害处理事件 - 应用减伤效果
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            // 检查玩家是否佩戴至高半神王冠
            if (hasCrownOfTheSupremeDemigodEquipped(player)) {
                UUID playerId = player.getUUID();
                long currentTime = System.currentTimeMillis();

                // 检查是否在冷却期内
                Long lastTriggerTime = damageImmunityCooldownMap.get(playerId);
                if (lastTriggerTime != null && currentTime - lastTriggerTime < COOLDOWN_DURATION) {
                    // 冷却中，不触发免疫，但应用减伤效果
                    applyDamageReduction(event);
                    System.out.println("player damage reduction");
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
        System.out.println("player damage reduction" + originalDamage + " to " + reducedDamage);
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
                if (accessory.getItem() instanceof CrownOfTheSupremeDemigod) {
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
                        player.level().addParticle(ParticleTypes.TOTEM_OF_UNDYING,
                                x, y, z, 0.0, 0.0, 0.0);
                    } else if (ring == 1) {
                        player.level().addParticle(ParticleTypes.FIREWORK,
                                x, y, z, 0.0, 0.0, 0.0);
                    } else {
                        player.level().addParticle(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                                x, y, z, 0.0, 0.0, 0.0);
                    }
                }
            }
        }
    }

    // 检查玩家是否佩戴无名王冠
    private static boolean hasCrownOfTheSupremeDemigodEquipped(Player player) {
        // 检查饰品栏
        var accessoryInventory = cn.dawnstring.fatality.inventory.AccessoryInventory.get(player);
        if (accessoryInventory != null) {
            for (int i = 0; i < accessoryInventory.getItemHandler().getSlots(); i++) {
                ItemStack accessory = accessoryInventory.getItemHandler().getStackInSlot(i);
                if (accessory.getItem() instanceof CrownOfTheSupremeDemigod) {
                    return true;
                }
            }
        }
        return false;
    }
}
