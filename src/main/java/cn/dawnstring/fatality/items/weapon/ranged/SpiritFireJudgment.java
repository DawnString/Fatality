package cn.dawnstring.fatality.items.weapon.ranged;

import cn.dawnstring.fatality.entity.projectile.SpiritFireJudgmentProjectile;
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
import net.minecraft.world.phys.Vec3;

/**
 * 灵火审判狙击枪 - 高精度远程武器
 * 特性：高基础伤害、高暴击率、高暴击伤害、子弹散射、锥形区域伤害、护甲粉碎、灵火灼烧效果
 */
public class SpiritFireJudgment extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 20; // 冷却时间20tick（1秒）
    private static final float BASE_BULLET_DAMAGE = 798.0f; // 基础子弹伤害798

    public SpiritFireJudgment()
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
                return Ingredient.of(net.minecraft.world.item.Items.NETHERITE_INGOT); // 修复材料：下界合金锭
            }
        }, new Properties(), (int)BASE_BULLET_DAMAGE, 1.0f, 1f, 0.18f, 0.20f, 0.4f, WeaponEnum.RANGED);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算子弹伤害（使用BaseWeapon的伤害计算逻辑）
            float bulletDamage = calculateBulletDamage(player, itemstack);

            // 创建灵火审判子弹投射物
            SpiritFireJudgmentProjectile bullet = new SpiritFireJudgmentProjectile(level, player, itemstack, bulletDamage);

            // 设置子弹位置和方向
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();

            bullet.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
            bullet.shoot(lookVec.x, lookVec.y, lookVec.z, 5.0F, 0.05F); // 5.0速度，极小散布（高精度）

            // 添加到世界
            level.addFreshEntity(bullet);

            // 播放狙击枪射击音效（使用弓箭射击音效，更高音调）
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.5F, 0.6F);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    public float calculateBulletDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }

    /**
     * 重写暴击特效，添加灵火审判特有的暴击效果
     */
    @Override
    protected void onCriticalHit(Player player, LivingEntity target, float damage) {
        super.onCriticalHit(player, target, damage);

        // 添加额外的灵火审判暴击特效
        if (player.level().isClientSide) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§6灵火审判暴击！ " + String.format("%.1f", damage) + " 伤害"),
                    true
            );
        }
    }

    /**
     * 获取武器特殊效果描述
     */
    @Override
    public String getSpecialEffectDescription() {
        return "灵火审判狙击枪 | 基础伤害: " + String.format("%.0f", BASE_BULLET_DAMAGE) +
                " | 暴击率: " + String.format("%.1f%%", 0.18f * 100) +
                " | 暴击伤害: " + String.format("%.1f", 0.20f) + "倍" +
                " | 伤害浮动: " + String.format("%.1f%%", 0.4f * 100) +
                " | 特殊效果: 子弹散射、锥形区域伤害、护甲粉碎、灵火灼烧";
    }
}