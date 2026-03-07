package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.entity.projectile.BrokenBladeSwordWaveProjectile;
import cn.dawnstring.fatality.items.WeaponEnum;
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
 * 破碎之刃 - 近战武器
 * 特性：左键攻击时能够释放剑气，剑气竖直宽大，向前飞行
 * 伤害628 暴击率26 暴击伤害32 浮动0.3 攻击速度0.4s
 */
public class BrokenBlade extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 8; // 冷却时间8tick（0.4秒）

    public BrokenBlade()
    {
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
        }, new Properties().fireResistant(), 628, 0.4f, 1f, 0.26f, 0.32f, 0.3f, WeaponEnum.MELEE);

        setStory("一把破碎但依然强大的剑刃，蕴含着古老的力量。\n" +
                "攻击时能够释放出宽大的剑气，向前飞行穿透敌人，\n" +
                "造成毁灭性的伤害。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算剑气伤害
            float swordWaveDamage = calculateSwordWaveDamage(player, itemstack);
            
            // 创建破碎之刃剑气投射物
            BrokenBladeSwordWaveProjectile swordWave = new BrokenBladeSwordWaveProjectile(level, player, swordWaveDamage);
            
            // 设置投射物位置和方向
            swordWave.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            swordWave.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.0F, 0.0F); // 中等飞行速度
            
            level.addFreshEntity(swordWave);
            
            // 播放剑气音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 0.8F);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 计算剑气伤害
     */
    public float calculateSwordWaveDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }

    /**
     * 获取攻击距离（破碎之刃有较长的攻击距离）
     */
    @Override
    public double getAttackRange(Player player) {
        return 4.0; // 破碎之刃有4格攻击距离
    }

    /**
     * 获取武器特殊效果描述
     */
    @Override
    public String getSpecialEffectDescription() {
        return "破碎之刃 - 左键攻击时释放宽大剑气，向前飞行穿透敌人";
    }
}