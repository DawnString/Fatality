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
 * 碎石者巨剑 - 高伤害近战武器
 * 特性：高基础伤害、高暴击伤害、快速攻击速度
 */
public class StonebreakerGreatsword extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 10; // 冷却时间10tick（0.5秒）
    private static final float BASE_GREATSWORD_DAMAGE = 55.0f; // 基础巨剑伤害55

    public StonebreakerGreatsword()
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
        }, new Properties(), 55, 0.5f, 1f, 0.10f, 0.15f, 0.3f, WeaponEnum.MELEE);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 播放挥砍音效（使用更沉重的剑击声）
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 0.8F); // 更低的音调
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 重写伤害计算方法，应用巨剑的特殊伤害计算
     */
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            // 计算最终伤害（使用BaseWeapon的伤害计算逻辑）
            float finalDamage = calculateGreatswordDamage(player, stack, target);

            System.out.println("StonebreakerGreatsword.hurtEnemy: 计算伤害 = " + finalDamage);
            System.out.println("StonebreakerGreatsword.hurtEnemy: 目标 = " + target.getType().getDescription().getString());

            // 应用伤害
            if (finalDamage > 0) {
                // 确保伤害值足够大，避免被游戏忽略
                float effectiveDamage = Math.max(0.5f, finalDamage);

                // 使用正确的伤害源
                boolean damageApplied = target.hurt(target.damageSources().playerAttack(player), effectiveDamage);
                System.out.println("StonebreakerGreatsword.hurtEnemy: 伤害是否应用成功 = " + damageApplied);

                // 如果伤害应用失败，尝试使用不同的伤害源
                if (!damageApplied) {
                    System.out.println("StonebreakerGreatsword.hurtEnemy: 尝试使用通用伤害源");
                    damageApplied = target.hurt(target.damageSources().generic(), effectiveDamage);
                    System.out.println("StonebreakerGreatsword.hurtEnemy: 通用伤害源是否成功 = " + damageApplied);
                }

                // 无论伤害是否应用成功，都发送伤害显示数据包
                if (!target.level().isClientSide()) {
                    System.out.println("StonebreakerGreatsword.hurtEnemy: 发送伤害网络包: " + finalDamage);
                    // 这里需要调用网络管理器发送伤害显示包
                }

                // 如果伤害应用成功，触发特效
                if (damageApplied) {
                    // 触发暴击特效
                    if (isCriticalHit(player)) {
                        onCriticalHit(player, target, finalDamage);
                    }

                    // 触发巨剑特效（重击效果）
                    onGreatswordHit(player, target, stack, finalDamage);
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
     * 巨剑命中时的特效（重击效果）
     */
    protected void onGreatswordHit(Player player, LivingEntity target, ItemStack stack, float damage) {
        // 播放重击音效
        player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 0.8F, 0.9F);

        // 发送巨剑重击消息
        if (player.level().isClientSide) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§2碎石者重击！ " + String.format("%.1f", damage) + " 伤害"),
                    true
            );
        }

        // 可以在这里添加击退效果或其他特效
        // target.knockback(1.0f, player.getX() - target.getX(), player.getZ() - target.getZ());
    }

    /**
     * 重写暴击特效，添加巨剑特有的暴击效果
     */
    @Override
    protected void onCriticalHit(Player player, LivingEntity target, float damage) {
        super.onCriticalHit(player, target, damage);

        // 添加额外的巨剑暴击特效
        if (player.level().isClientSide) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§6碎石者暴击！ " + String.format("%.1f", damage) + " 伤害"),
                    true
            );
        }
    }

    /**
     * 获取攻击距离（巨剑有更长的攻击距离）
     */
    @Override
    public double getAttackRange(Player player) {
        return 4.0; // 巨剑有4格攻击距离
    }
}