package cn.dawnstring.fatality.items.weapon.ranged;

import cn.dawnstring.fatality.entity.projectile.DeathRayLaserProjectile;
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

/**
 * 死光炮 - 高伤害激光远程武器
 * 特性：右键发射一道激光，激光移动速度快，攻击到目标能够造成伤害并穿透目标，最多穿透3个
 * 伤害980 暴击率20 暴击伤害25 浮动0.3 攻击速度1s
 */
public class DeathRay extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 20; // 冷却时间20tick（1秒）
    private static final float BASE_LASER_DAMAGE = 980.0f; // 基础激光伤害980
    private static final int MAX_PENETRATION = 3; // 最大穿透3个目标
    private static final double LASER_RANGE = 100.0; // 激光射程100格
    private static final float LASER_SPEED = 5.0f; // 激光速度

    public DeathRay()
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
        }, new Properties(), 980, 1.0f, 1f, 0.20f, 0.25f, 0.3f, WeaponEnum.RANGED);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算激光伤害（使用BaseWeapon的伤害计算逻辑）
            float laserDamage = calculateLaserDamage(player, itemstack);

            // 创建激光投射物
            DeathRayLaserProjectile laser = new DeathRayLaserProjectile(level, player, itemstack, laserDamage, MAX_PENETRATION);

            // 设置激光位置和方向
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();

            laser.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
            laser.shoot(lookVec.x, lookVec.y, lookVec.z, LASER_SPEED, 0.0f); // 高速，无散布

            // 添加到世界
            level.addFreshEntity(laser);

            // 播放激光发射音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5F, 0.8F);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 计算激光伤害（使用BaseWeapon相同的计算方法）
     */
    public float calculateLaserDamage(Player player, ItemStack stack) {
        // 使用BaseWeapon的伤害计算逻辑，但基于激光伤害
        float baseDamage = BASE_LASER_DAMAGE;

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

    /**
     * 获取攻击距离（重写为激光的射程）
     */
    @Override
    public double getAttackRange(Player player) {
        return LASER_RANGE;
    }

    /**
     * 获取武器特殊效果描述
     */
    @Override
    public String getSpecialEffectDescription() {
        return "激光伤害: " + String.format("%.1f", BASE_LASER_DAMAGE) +
                " | 最大穿透: " + MAX_PENETRATION + "个目标" +
                " | 射程: " + LASER_RANGE + "格";
    }
}