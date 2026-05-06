package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.entity.projectile.FurnaceSpearProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import cn.dawnstring.fatality.registry.ModEffects;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

/**
 * 熔炉长矛 - 带有灼烧效果的近战武器
 * 特性：中等伤害、中等暴击率、快速攻击速度、右键攻击给予灼烧效果
 */
public class FurnaceSpear extends BaseWeapon
{
    private static final int BURN_DURATION = 100; // 灼烧效果持续时间（5秒 = 100tick）
    private static final float BASE_SPEAR_DAMAGE = 79.0f; // 基础长矛伤害

    public FurnaceSpear()
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
        }, new Properties(), (int)BASE_SPEAR_DAMAGE, 0.2f, 1f, 0.15f, 0.15f, 0.3f, WeaponEnum.MELEE);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算长矛伤害（使用BaseWeapon的伤害计算逻辑）
            float spearDamage = calculateSpearDamage(player, itemstack);

            // 创建熔炉长矛投射物
            FurnaceSpearProjectile spear = new FurnaceSpearProjectile(level, player, itemstack, spearDamage);

            // 设置长矛位置和方向
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();

            spear.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
            spear.shoot(lookVec.x, lookVec.y, lookVec.z, 3.0F, 0.1F); // 3.0速度，极小散布

            // 添加到世界
            level.addFreshEntity(spear);

            // 播放投掷音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, 20); // 1秒冷却

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 重写伤害计算方法，应用熔炉长矛的特殊伤害计算和灼烧效果
     */
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            // 计算最终伤害（使用BaseWeapon的伤害计算逻辑）
            float finalDamage = calculateSpearDamage(player, stack, target);

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

                    // 触发熔炉长矛特效（灼烧效果）
                    onFurnaceSpearHit(player, target, stack, finalDamage);
                }

                return damageApplied;
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    /**
     * 计算熔炉长矛伤害（使用BaseWeapon相同的计算方法）
     */
    public float calculateSpearDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }

    /**
     * 计算熔炉长矛伤害（重载版本，包含目标参数）
     */
    public float calculateSpearDamage(Player player, ItemStack stack, LivingEntity target) {
        return calculateSpearDamage(player, stack);
    }

    /**
     * 熔炉长矛命中时的特效（灼烧效果）
     */
    protected void onFurnaceSpearHit(Player player, LivingEntity target, ItemStack stack, float damage) {
        // 应用灼烧效果
        applyBurnEffect(target);
    }

    /**
     * 给目标添加灼烧效果
     */
    protected void applyBurnEffect(LivingEntity target) {
        if (ModEffects.BURN != null) {
            MobEffectInstance burnEffect = new MobEffectInstance(ModEffects.BURN.get(), BURN_DURATION, 0);
            target.addEffect(burnEffect);
        }
    }

    /**
     * 重写暴击特效，添加熔炉长矛特有的暴击效果
     */
    @Override
    protected void onCriticalHit(Player player, LivingEntity target, float damage) {
        super.onCriticalHit(player, target, damage);

        // 暴击时延长灼烧效果时间
        if (ModEffects.BURN != null) {
            MobEffectInstance burnEffect = new MobEffectInstance(ModEffects.BURN.get(), BURN_DURATION * 2, 0); // 暴击时持续时间加倍
            target.addEffect(burnEffect);
        }
    }

    /**
     * 获取攻击距离（长矛有更长的攻击距离）
     */
    @Override
    public double getAttackRange(Player player) {
        return 4.0; // 长矛有4格攻击距离
    }
}