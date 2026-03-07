package cn.dawnstring.fatality.items.weapon.ranged;

import cn.dawnstring.fatality.entity.projectile.GreenLeafFlyingKnifeProjectile;
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
 * 绿叶飞刀 - 远程武器
 * 特性：右键朝前方发射5个飞刀，其中3个能够追踪目标，其他2个不能追踪目标
 * 伤害480 暴击率26 暴击伤害34 浮动0.4 攻击速度0.2s
 */
public class GreenLeafFlyingKnife extends BaseWeapon {

    private static final int COOLDOWN_TICKS = 4; // 冷却时间4tick（0.2秒）

    public GreenLeafFlyingKnife() {
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
        }, new Properties().fireResistant(), 480, 0.2f, 1f, 0.26f, 1.34f, 0.4f, WeaponEnum.RANGED);

        setStory("绿叶飞刀，蕴含着自然的生机与力量。\n" +
                "右键投掷时，同时发射5把飞刀，其中3把具有追踪能力，\n" +
                "能够自动寻找并追击敌人，展现自然的智慧与精准。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算飞刀伤害
            float knifeDamage = calculateKnifeDamage(player, itemstack);

            // 同时发射5把飞刀，其中3把追踪，2把不追踪
            for (int i = 0; i < 5; i++) {
                boolean isTracking = i < 3; // 前3把追踪，后2把不追踪
                
                // 生成绿叶飞刀投射物
                GreenLeafFlyingKnifeProjectile projectile = new GreenLeafFlyingKnifeProjectile(
                        level, player, knifeDamage, isTracking);

                // 设置投射物位置和方向
                projectile.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());

                // 添加轻微的角度偏移，使5把飞刀形成扇形分布
                float yawOffset = (i - 2) * 3.0f; // 左右各偏移6度，形成扇形
                projectile.shootFromRotation(player, player.getXRot(), player.getYRot() + yawOffset, 0.0F, 1.8F, 1.0F);

                // 添加到世界
                level.addFreshEntity(projectile);
            }

            // 播放攻击音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.2F);

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
                    SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.NEUTRAL, 0.6F, 1.0F);
        }

        return super.hurtEnemy(stack, target, attacker);
    }
}