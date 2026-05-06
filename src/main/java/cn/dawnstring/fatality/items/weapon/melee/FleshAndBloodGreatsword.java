package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

/**
 * 血肉巨剑 - 高伤害近战武器
 * 特性：极高基础伤害、中等暴击率、快速攻击速度
 */
public class FleshAndBloodGreatsword extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 10; // 冷却时间10tick（0.5秒）
    private static final float BASE_GREATSWORD_DAMAGE = 65.0f; // 基础巨剑伤害65

    public FleshAndBloodGreatsword()
    {
        super(new Tier() {
            @Override
            public int getUses() {
                return 0;
            }

            @Override
            public float getSpeed() {
                return 0;
            }

            @Override
            public float getAttackDamageBonus() {
                return 0;
            }

            @Override
            public int getLevel() {
                return 0;
            }

            @Override
            public int getEnchantmentValue() {
                return 0;
            }

            @Override
            public Ingredient getRepairIngredient() {
                return null;
            }
        }, new Properties(), 65, 0.5f, 1f, 0.10f, 0.10f, 0.3f, WeaponEnum.MELEE);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 播放挥砍音效（使用血肉撕裂的声音）
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 0.7F); // 更低的音调
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 重写伤害计算方法，应用血肉巨剑的特殊伤害计算
     */
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            // 计算最终伤害（使用BaseWeapon的伤害计算逻辑）
            float finalDamage = calculateGreatswordDamage(player, stack, target);

            // 应用伤害
            if (finalDamage > 0) {
                // 确保伤害值足够大，避免被游戏忽略
                float effectiveDamage = Math.max(0.5f, finalDamage);

                // 使用正确的伤害源
                boolean damageApplied = target.hurt(target.damageSources().playerAttack(player), effectiveDamage);

                // 如果伤害应用失败，尝试使用不同的伤害源
                if (!damageApplied) {
                    damageApplied = target.hurt(target.damageSources().generic(), effectiveDamage);
                }

                // 如果伤害应用成功，触发特效
                if (damageApplied) {
                    // 触发暴击特效
                    if (isCriticalHit(player)) {
                        onCriticalHit(player, target, finalDamage);
                    }

                    // 触发血肉巨剑特效（吸血效果）
                    onFleshAndBloodHit(player, target, stack, finalDamage);
                }

                return damageApplied;
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    /**
     * 计算巨剑伤害（使用BaseWeapon相同的计算方法）
     */
    public float calculateGreatswordDamage(Player player, ItemStack stack, LivingEntity target) {
        return calculateFinalDamage(player, stack, target);
    }

    /**
     * 血肉巨剑命中时的特效（吸血效果）
     */
    protected void onFleshAndBloodHit(Player player, LivingEntity target, ItemStack stack, float damage) {
        // 播放血肉撕裂音效
        player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 0.9F, 0.8F);

        // 发送血肉巨剑命中消息
        if (player.level().isClientSide) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§4血肉撕裂！ " + String.format("%.1f", damage) + " 伤害"),
                    true
            );
        }

        // 简单的吸血效果：恢复少量生命值
        float healAmount = damage * 0.05f; // 恢复造成伤害的5%
        if (healAmount > 0) {
            player.heal(healAmount);

            if (player.level().isClientSide) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§c+ " + String.format("%.1f", healAmount) + " 生命值"),
                        true
                );
            }
        }
    }

    /**
     * 重写暴击特效，添加血肉巨剑特有的暴击效果
     */
    @Override
    protected void onCriticalHit(Player player, LivingEntity target, float damage) {
        super.onCriticalHit(player, target, damage);

        // 添加额外的血肉巨剑暴击特效
        if (player.level().isClientSide) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§4血肉暴击！ " + String.format("%.1f", damage) + " 伤害"),
                    true
            );
        }

        // 暴击时恢复更多生命值
        float healAmount = damage * 0.08f; // 暴击时恢复造成伤害的8%
        if (healAmount > 0) {
            player.heal(healAmount);

            if (player.level().isClientSide) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§c+ " + String.format("%.1f", healAmount) + " 生命值（暴击）"),
                        true
                );
            }
        }
    }

    /**
     * 获取攻击距离（血肉巨剑有更长的攻击距离）
     */
    @Override
    public double getAttackRange(Player player) {
        return 4.0; // 巨剑有4格攻击距离
    }
}