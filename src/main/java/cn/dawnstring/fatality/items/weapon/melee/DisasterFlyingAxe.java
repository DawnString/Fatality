package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.entity.projectile.DisasterFlyingAxeProjectile;
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
 * 灾厄飞斧 - 具有快速攻击速度和投掷功能的强力近战武器
 * 特点：高攻击速度、投掷能力、旋转飞行效果
 */
public class DisasterFlyingAxe extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 5; // 冷却时间5tick（0.25秒）
    private static final float BASE_AXE_DAMAGE = 14.0f; // 基础飞斧伤害
    private static final float PROJECTILE_SPEED = 3.0F; // 投射物速度（更快）
    private static final float PROJECTILE_INACCURACY = 0.3F; // 投射物散布（更精准）

    public DisasterFlyingAxe()
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
        }, new Properties(), 14, 0.25f, 1f, 0.08f, 0.08f, 0.4f, WeaponEnum.MELEE);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算飞斧伤害（使用BaseWeapon的伤害计算逻辑）
            float axeDamage = calculateAxeDamage(player, itemstack);

            // 创建飞斧投射物
            DisasterFlyingAxeProjectile flyingAxe = new DisasterFlyingAxeProjectile(level, player, itemstack, axeDamage);

            // 设置飞斧位置和方向
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();

            // 设置飞斧起始位置（稍微偏移，避免从玩家眼睛射出）
            flyingAxe.setPos(
                    eyePos.x + lookVec.x * 0.5,
                    eyePos.y + lookVec.y * 0.5 - 0.1, // 稍微降低高度
                    eyePos.z + lookVec.z * 0.5
            );

            // 发射飞斧（更快的速度，更小的散布）
            flyingAxe.shoot(lookVec.x, lookVec.y, lookVec.z, PROJECTILE_SPEED, PROJECTILE_INACCURACY);

            // 添加到世界
            level.addFreshEntity(flyingAxe);

            // 播放投掷音效 - 更尖锐的音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 0.8F, 1.3F);
        }

        // 设置冷却时间（不消耗物品）
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 计算飞斧伤害（使用BaseWeapon相同的计算方法）
     */
    public float calculateAxeDamage(Player player, ItemStack stack) {
        // 使用BaseWeapon的伤害计算逻辑，但基于飞斧伤害
        float baseDamage = BASE_AXE_DAMAGE;

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
     * 重写命中敌人时的回调，添加飞斧的特殊效果
     */
    @Override
    protected void onHitEnemy(Player player, LivingEntity target, ItemStack stack, float damage) {
        // 飞斧命中时播放特殊音效
        if (!player.level().isClientSide()) {
            player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.7F, 1.2F);
        }
    }
}