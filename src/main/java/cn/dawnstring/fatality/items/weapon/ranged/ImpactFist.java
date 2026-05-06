package cn.dawnstring.fatality.items.weapon.ranged;

import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 冲击拳套 - 高伤害远程拳击武器
 * 特性：极高基础伤害、中等暴击率、远程冲击波攻击
 */
public class ImpactFist extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 20; // 冷却时间20tick（1秒）
    private static final float BASE_IMPACT_DAMAGE = 110.0f; // 基础冲击伤害110

    public ImpactFist()
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
        }, new Properties(), (int)BASE_IMPACT_DAMAGE, 1f, 1f, 0.10f, 0.10f, 0.2f, WeaponEnum.RANGED);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算冲击伤害（使用BaseWeapon的伤害计算逻辑）
            float impactDamage = calculateImpactDamage(player, itemstack);

            // 创建冲击波投射物（使用Arrow作为基础）
            Arrow impactWave = new Arrow(level, player);

            impactWave.setNoGravity(true);

            // 设置冲击波伤害（基于BaseWeapon计算的结果）
            impactWave.setBaseDamage(impactDamage);

            // 设置冲击波位置和方向
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();

            impactWave.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
            impactWave.shoot(lookVec.x, lookVec.y, lookVec.z, 3.5F, 0.3F); // 3.5速度，较小散布

            // 添加到世界
            level.addFreshEntity(impactWave);

            // 播放冲击波音效（使用更沉重的打击声）
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 0.6F); // 更低的音调
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 计算冲击伤害（使用BaseWeapon相同的计算方法）
     */
    public float calculateImpactDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }
}