package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.entity.projectile.HarpoonSpearProjectile;
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
 * 鱼叉 - 海洋风格的近战投掷武器
 * 特性：右键投掷鱼叉，受重力影响，鱼叉飞行中在其身后生成鱼，鱼竖直落下并造成伤害，每0.5秒生成1条鱼
 * 伤害1460 暴击率25% 暴击伤害32% 浮动0.3 攻击速度1s
 */
public class Harpoon extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 20; // 冷却时间20tick（1秒）
    private static final float FISH_DAMAGE_MULTIPLIER = 0.5f; // 鱼伤害为基础值的0.5倍

    public Harpoon()
    {
        super(new Tier() {
            @Override
            public int getUses() {
                return 0; // 无限耐久
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
                return 4; // 材料等级4
            }

            @Override
            public int getEnchantmentValue() {
                return 0; // 不可附魔
            }

            @Override
            public Ingredient getRepairIngredient() {
                return null; // 不可修复
            }
        }, new Properties().stacksTo(1), 1460, 1.0f, 1f, 0.25f, 1.32f, 0.3f, WeaponEnum.MELEE);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (!level.isClientSide()) {
            // 设置冷却时间
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
            
            // 投掷鱼叉
            throwHarpoon(level, player, stack);
            
            // 播放投掷音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 0.8F, 0.8F);
        }
        
        return InteractionResultHolder.success(stack);
    }

    /**
     * 投掷鱼叉
     */
    private void throwHarpoon(Level level, Player player, ItemStack weapon) {
        // 计算投掷方向
        Vec3 lookVec = player.getLookAngle();
        Vec3 spawnPos = player.getEyePosition().add(lookVec.scale(1.5));
        
        // 创建鱼叉投射物
        HarpoonSpearProjectile harpoon = new HarpoonSpearProjectile(level, player, weapon, calculateHarpoonDamage(player, weapon));
        harpoon.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        
        // 设置投掷速度和方向（受重力影响）
        float speed = 1.5f;
        harpoon.setDeltaMovement(lookVec.x * speed, lookVec.y * speed, lookVec.z * speed);
        harpoon.setNoGravity(false); // 受重力影响
        
        // 添加到世界
        level.addFreshEntity(harpoon);
    }

    /**
     * 计算鱼叉投掷伤害
     */
    private float calculateHarpoonDamage(Player player, ItemStack stack) {
        // 获取基础伤害
        float baseDamage = getBaseDamage(player, stack);
        
        // 使用BaseWeapon的伤害计算逻辑
        return calculateFinalDamage(player, stack, null);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // 近战攻击逻辑
        if (attacker instanceof Player player) {
            // 计算最终伤害
            float finalDamage = calculateFinalDamage(player, stack, target);

            // 应用伤害
            if (finalDamage > 0) {
                float effectiveDamage = Math.max(0.5f, finalDamage);
                boolean damageApplied = target.hurt(target.damageSources().playerAttack(player), effectiveDamage);

                // 播放近战攻击音效
                if (damageApplied) {
                    player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 0.8F, 1.0F);
                }

                return damageApplied;
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}