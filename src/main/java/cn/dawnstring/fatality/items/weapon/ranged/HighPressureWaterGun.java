package cn.dawnstring.fatality.items.weapon.ranged;

import cn.dawnstring.fatality.entity.projectile.WaterStreamProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import net.minecraft.network.chat.Component;
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
 * 高压水枪 - 持续发射蓝色粒子构成的水柱
 * 特性：103伤害 12暴击率 14暴击伤害 0.3浮动 0.3攻击速度
 * 玩家右键时能够持续不断的发射蓝色粒子构成的水柱
 */
public class HighPressureWaterGun extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 6; // 冷却时间6tick（0.3秒）
    private static final float BASE_WATER_STREAM_DAMAGE = 103.0f; // 基础水柱伤害103
    private static final int STREAM_LENGTH = 15; // 水柱长度15格

    public HighPressureWaterGun()
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
        }, new Properties(), 0, 0.3f, 1f, 0.12f, 0.14f, 0.3f, WeaponEnum.RANGED);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算水柱伤害（使用BaseWeapon的伤害计算逻辑）
            float waterStreamDamage = calculateWaterStreamDamage(player, itemstack);

            // 创建水柱投射物
            WaterStreamProjectile waterStream = new WaterStreamProjectile(level, player, itemstack, waterStreamDamage, STREAM_LENGTH);

            // 设置水柱位置和方向
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();

            waterStream.setPos(eyePos.x + lookVec.x * 0.5, eyePos.y + lookVec.y * 0.5, eyePos.z + lookVec.z * 0.5);
            waterStream.shoot(lookVec.x, lookVec.y, lookVec.z, 3.0F, 0.5F); // 3.0速度，中等散布

            // 添加到世界
            level.addFreshEntity(waterStream);

            // 播放水枪射击音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 0.8F, 1.5F); // 使用水桶倒水音效，较高音调
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 计算水柱伤害（使用BaseWeapon相同的计算方法）
     */
    public float calculateWaterStreamDamage(Player player, ItemStack stack) {
        // 使用BaseWeapon的伤害计算逻辑，但基于水柱伤害
        float baseDamage = BASE_WATER_STREAM_DAMAGE;

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
     * 获取攻击距离（高压水枪有较长的攻击距离）
     */
    @Override
    public double getAttackRange(Player player) {
        return STREAM_LENGTH; // 水柱长度即为攻击距离
    }
}