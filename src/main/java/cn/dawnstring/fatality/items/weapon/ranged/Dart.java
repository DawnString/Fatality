package cn.dawnstring.fatality.items.weapon.ranged;

import cn.dawnstring.fatality.entity.projectile.DartProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
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
import net.minecraft.world.phys.Vec3;

public class Dart extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 10; // 冷却时间10tick（0.5秒），非常快速
    private static final float BASE_DART_DAMAGE = 8.0f; // 基础飞镖伤害

    public Dart()
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
        }, new Properties(), 0, 0.5f, 1f, 0.05f, 0.05f, 0.4f, WeaponEnum.RANGED);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算飞镖伤害（使用BaseWeapon的伤害计算逻辑）
            float dartDamage = calculateDartDamage(player, itemstack);

            // 创建飞镖投射物
            DartProjectile dart = new DartProjectile(level, player, itemstack, dartDamage);

            // 设置飞镖位置和方向
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();

            dart.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
            dart.shoot(lookVec.x, lookVec.y, lookVec.z, 4.0F, 0.5F); // 4.0速度，低散布

            // 添加到世界
            level.addFreshEntity(dart);

            // 播放发射音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.8F, 1.5F); // 更高音调
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 计算飞镖伤害（使用BaseWeapon相同的计算方法）
     */
    public float calculateDartDamage(Player player, ItemStack stack) {
        // 使用BaseWeapon的伤害计算逻辑，但基于飞镖伤害
        float baseDamage = BASE_DART_DAMAGE;

        // 计算基础伤害加成（基于饰品）
        float accessoryBaseBonus = calculateAccessoryBaseBonus(player);

        // 计算其他伤害加成（饰品、药水等）
        float otherBonus = calculateOtherBonus(player);

        // 计算伤害浮动值
        float fluctuation = calculateDamageFluctuation();

        // 判断是否暴击
        boolean isCritical = isCriticalHit(player);

        float finalDamage;
        if (isCritical) {
            // 暴击伤害公式（与BaseWeapon保持一致）
            float criticalBonus = getCriticalDamageMultiplier(player);
            finalDamage = baseDamage * accessoryBaseBonus * otherBonus * 0.8f * criticalBonus * fluctuation;
        } else {
            // 普通伤害公式（与BaseWeapon保持一致）
            finalDamage = baseDamage * accessoryBaseBonus * otherBonus * 0.9f * fluctuation;
        }

        return Math.max(0, finalDamage);
    }
}