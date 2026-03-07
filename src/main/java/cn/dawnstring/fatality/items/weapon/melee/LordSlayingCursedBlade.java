package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static cn.dawnstring.fatality.Fatality.DOT_ITEM_DES;

@Mod.EventBusSubscriber
public class LordSlayingCursedBlade extends BaseWeapon
{
    // 诅咒之刃的特殊材料
    private static final Tier CURSED_BLADE_TIER = new Tier() {
        @Override
        public int getUses() {
            return 0; // 耐久度
        }

        @Override
        public float getSpeed() {
            return 0; // 挖掘速度
        }

        @Override
        public float getAttackDamageBonus() {
            return 0; // 基础攻击伤害加成
        }

        @Override
        public int getLevel() {
            return 0; // 材料等级（钻石级）
        }

        @Override
        public int getEnchantmentValue() {
            return 0; // 附魔能力
        }

        @Override
        public Ingredient getRepairIngredient() {
            // 使用暗渊锭修复
            return null ;
        }
    };

    public LordSlayingCursedBlade() {
        super(CURSED_BLADE_TIER, new Properties(), 1000, 1f, 1f, 0.25f, 1.5f, 0.3f, WeaponEnum.MELEE);

        setStory("你握着这把刀\n " +
                "钻心般的疼痛从手心传来\n" +
                "这使你的手不断颤抖\n" +
                "你感觉它对你的灵魂进行了侵蚀\n" +
                "它成为了你的一部分\n" +
                "而被它侵蚀的灵魂越多\n" +
                "它的力量就越强\n" +
                "当然 这也标好了价格\n" +
                DOT_ITEM_DES);
    }

    /**
     * 重写伤害计算方法，添加基于失去生命值的伤害加成
     */
    @Override
    protected float calculateFinalDamage(Player player, ItemStack stack, LivingEntity target) {
        // 计算基础伤害
        float baseDamage = super.calculateFinalDamage(player, stack, target);

        // 计算基于失去生命值的伤害加成
        float healthBonus = calculateHealthBasedBonus(player);

        // 应用伤害加成
        float finalDamage = baseDamage * healthBonus;

        return Math.max(1.0f, finalDamage); // 确保至少造成1点伤害
    }

    /**
     * 计算基于失去生命值的伤害加成
     * 失去的生命值越多，伤害加成越高
     */
    private float calculateHealthBasedBonus(Player player) {
        float maxHealth = player.getMaxHealth();
        float currentHealth = player.getHealth();
        float lostHealth = maxHealth - currentHealth;

        // 计算失去生命值的百分比
        float lostHealthPercentage = lostHealth / maxHealth;

        // 伤害加成公式：基础1.0倍 + 失去生命值百分比 * 2.0
        // 最大加成：当生命值为1时，加成达到最大（约3.0倍）
        return 1.0f + (lostHealthPercentage * 2.0f);
    }

    /**
     * 重写命中敌人时的回调，添加粒子效果
     */
    @Override
    protected void onHitEnemy(Player player, LivingEntity target, ItemStack stack, float damage) {
        super.onHitEnemy(player, target, stack, damage);

        // 生成红色粒子效果
        spawnRedParticles(player, target);

        // 播放攻击音效
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0f, 0.8f);
    }

    /**
     * 生成红色粒子效果
     */
    private void spawnRedParticles(Player player, LivingEntity target) {
        if (player.level().isClientSide()) {
            // 在玩家周围生成红色粒子
            Vec3 playerPos = player.position();
            for (int i = 0; i < 15; i++) {
                double x = playerPos.x + (player.getRandom().nextDouble() - 0.5) * 3.0;
                double y = playerPos.y + player.getRandom().nextDouble() * 2.0;
                double z = playerPos.z + (player.getRandom().nextDouble() - 0.5) * 3.0;

                player.level().addParticle(ParticleTypes.DAMAGE_INDICATOR,
                        x, y, z, 0.0, 0.1, 0.0);
            }

            // 在目标周围生成红色粒子
            Vec3 targetPos = target.position();
            for (int i = 0; i < 10; i++) {
                double x = targetPos.x + (player.getRandom().nextDouble() - 0.5) * 2.0;
                double y = targetPos.y + player.getRandom().nextDouble() * target.getBbHeight();
                double z = targetPos.z + (player.getRandom().nextDouble() - 0.5) * 2.0;

                player.level().addParticle(ParticleTypes.DAMAGE_INDICATOR,
                        x, y, z, 0.0, 0.05, 0.0);
            }
        }
    }

    /**
     * 玩家Tick事件，检测玩家是否持有此剑并施加凋零效果
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;

            // 检查玩家主手或副手是否持有诅咒之刃
            boolean hasCursedBlade = isHoldingCursedBlade(player);

            if (hasCursedBlade) {
                // 施加凋零2效果（持续5秒，等级1）
                player.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1)); // 5秒凋零2效果

                // 每10秒生成一次诅咒粒子效果
                if (player.tickCount % 200 == 0) {
                    spawnCursedParticles(player);
                }
            }
        }
    }

    /**
     * 检查玩家是否持有诅咒之刃
     */
    private static boolean isHoldingCursedBlade(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        return mainHand.getItem() instanceof LordSlayingCursedBlade ||
                offHand.getItem() instanceof LordSlayingCursedBlade;
    }

    /**
     * 生成诅咒粒子效果
     */
    private static void spawnCursedParticles(Player player) {
        if (player.level().isClientSide()) {
            Vec3 pos = player.position();
            for (int i = 0; i < 8; i++) {
                double x = pos.x + (player.getRandom().nextDouble() - 0.5) * 2.0;
                double y = pos.y + player.getRandom().nextDouble() * 2.0;
                double z = pos.z + (player.getRandom().nextDouble() - 0.5) * 2.0;

                player.level().addParticle(ParticleTypes.SMOKE,
                        x, y, z, 0.0, 0.02, 0.0);
            }
        }
    }
}
