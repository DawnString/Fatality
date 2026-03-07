package cn.dawnstring.fatality.items.weapon.ranged;

import cn.dawnstring.fatality.entity.projectile.EnderFlyingKnifeProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

/**
 * 末影飞刀 - 远程武器
 * 特性：右键投掷，不受重力影响，同时投掷3把，飞行10格后每把分裂为3把新飞刀，新飞刀不再分裂
 * 伤害167 暴击率15 暴击伤害16 浮动0.3 攻击速度0.25s
 */
public class EnderFlyingKnife extends BaseWeapon {

    private static final int COOLDOWN_TICKS = 5; // 冷却时间5tick（0.25秒）

    public EnderFlyingKnife() {
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
        }, new Properties().fireResistant(), 167, 0.25f, 1f, 0.15f, 1.16f, 0.3f, WeaponEnum.RANGED);

        setStory("末影飞刀，蕴含着神秘的末影之力。\n" +
                "投掷时不受重力影响，同时发射3把飞刀，\n" +
                "飞行10格后每把飞刀分裂为3把新飞刀，造成连锁伤害。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算飞刀伤害
            float knifeDamage = calculateKnifeDamage(player, itemstack);

            // 同时发射3把飞刀
            for (int i = 0; i < 3; i++) {
                // 生成末影飞刀投射物
                EnderFlyingKnifeProjectile projectile = new EnderFlyingKnifeProjectile(
                        level, player, knifeDamage, 0); // 初始分裂等级为0

                // 设置投射物位置和方向
                projectile.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());

                // 添加轻微的角度偏移，使3把飞刀略有分散
                float yawOffset = (i - 1) * 5.0f; // 左右各偏移5度
                projectile.shootFromRotation(player, player.getXRot(), player.getYRot() + yawOffset, 0.0F, 2.0F, 1.0F);

                // 添加到世界
                level.addFreshEntity(projectile);
            }

            // 播放攻击音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.5F);

            // 统计使用次数
            player.awardStat(Stats.ITEM_USED.get(this));
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }

    /**
     * 计算飞刀伤害
     */
    public float calculateKnifeDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // 播放命中音效
        if (attacker instanceof Player) {
            Player player = (Player) attacker;
            player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.ENDERMAN_HURT, SoundSource.NEUTRAL, 0.6F, 1.0F);
        }

        return super.hurtEnemy(stack, target, attacker);
    }
}