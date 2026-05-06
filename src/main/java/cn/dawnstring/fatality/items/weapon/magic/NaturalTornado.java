package cn.dawnstring.fatality.items.weapon.magic;

import cn.dawnstring.fatality.entity.projectile.NaturalTornadoProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import cn.dawnstring.fatality.system.ManaSystem;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

/**
 * 自然龙卷 - 魔法武器
 * 特性：右键朝前方发射3个绿色龙卷风，龙卷风伤害半径3格，在龙卷风内的生物持续受到伤害
 * 伤害613 暴击率26 暴击伤害34 浮动0.3 攻击速度0.25s
 */
public class NaturalTornado extends BaseWeapon
{
    private static final float BASE_MAGIC_DAMAGE = 613.0f; // 基础魔法伤害613
    private static final float MANA_COST_PER_ATTACK = 18.0f; // 每次攻击消耗18点魔法值
    private static final int ATTACK_COOLDOWN_TICKS = 5; // 攻击冷却时间5tick（0.25秒）
    private static final int TORNADO_COUNT = 3; // 发射3个龙卷风

    public NaturalTornado() {
        super(new Tier() {
            @Override
            public int getUses() {
                return 0; // 无限耐久
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
                return 4; // 材料等级（钻石级）
            }

            @Override
            public int getEnchantmentValue() {
                return 0; // 不能附魔
            }

            @Override
            public Ingredient getRepairIngredient() {
                return null; // 不能修复
            }
        }, new Properties(), (int)BASE_MAGIC_DAMAGE, 0.25f, 1f, 0.26f, 0.34f, 0.3f, WeaponEnum.MAGIC);
        
        setStory("一把蕴含自然之力的魔法武器，能够召唤3个绿色的龙卷风。\n" +
                "每个龙卷风都会吸引周围的敌人并造成持续伤害，\n" +
                "是清理大量敌人的绝佳选择。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        // 检查攻击冷却时间
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(itemstack);
        }

        // 检查魔法值是否足够
        if (!ManaSystem.hasEnoughMana(player, MANA_COST_PER_ATTACK)) {
            if (level.isClientSide()) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c魔法值不足！"), true);
            }
            return InteractionResultHolder.fail(itemstack);
        }

        // 消耗魔法值
        ManaSystem.consumeMana(player, MANA_COST_PER_ATTACK);

        // 设置攻击冷却时间
        player.getCooldowns().addCooldown(this, ATTACK_COOLDOWN_TICKS);

        // 释放自然龙卷风
        if (!level.isClientSide()) {
            performNaturalTornadoAttack(level, player, itemstack);
        }

        // 播放攻击音效
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS,
                1.0F, 1.0F);

        return InteractionResultHolder.consume(itemstack);
    }
    
    /**
     * 执行自然龙卷风攻击
     */
    private void performNaturalTornadoAttack(Level level, Player player, ItemStack itemstack) {
        // 计算龙卷风伤害
        float tornadoDamage = calculateFinalDamage(player, itemstack, null);
        
        // 发射3个龙卷风，略微分散角度
        for (int i = 0; i < TORNADO_COUNT; i++) {
            // 创建自然龙卷风投射物
            NaturalTornadoProjectile tornado = new NaturalTornadoProjectile(level, player, tornadoDamage);
            
            // 设置投射物位置和方向
            tornado.setPos(player.getEyePosition().x, player.getEyePosition().y, player.getEyePosition().z);
            
            // 添加轻微的角度偏移，使3个龙卷风略有分散
            float yawOffset = (i - 1) * 5.0f; // 左右各偏移5度
            tornado.shootFromRotation(player, player.getXRot(), player.getYRot() + yawOffset, 0.0f, 1.2f, 1.0f); // 1.2速度，标准散布
            
            // 添加到世界
            level.addFreshEntity(tornado);
        }
    }
    
    /**
     * 重写暴击特效，添加自然龙卷风特有的暴击效果
     */
    @Override
    protected void onCriticalHit(Player player, net.minecraft.world.entity.LivingEntity target, float damage) {
        super.onCriticalHit(player, target, damage);

        // 添加额外的自然龙卷风暴击特效
        if (player.level().isClientSide) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§a自然龙卷风暴击！ " + String.format("%.1f", damage) + " 伤害"),
                    true
            );
        }
    }

    /**
     * 检查是否有足够的魔法值
     */
    public static boolean hasEnoughMana(Player player, float requiredMana) {
        return ManaSystem.getCurrentMana(player) >= requiredMana;
    }
}