package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.entity.projectile.StarProjectile;
import cn.dawnstring.fatality.entity.projectile.StarSpearProjectile;
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

public class Star extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 20; // 冷却时间20tick（1秒）
    private static final float STAR_DAMAGE_MULTIPLIER = 0.6f; // 星星弹幕伤害为基础值的0.6倍
    private static final int STAR_SPAWN_INTERVAL = 10; // 每10tick（0.5秒）生成一次星星弹幕

    public Star()
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
        }, new Properties().stacksTo(1), 975, 1.0f, 1f, 0.2f, 1.27f, 0.3f, WeaponEnum.MELEE);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (!level.isClientSide()) {
            // 设置冷却时间
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
            
            // 投掷星辰矛
            throwStarSpear(level, player, stack);
            
            // 播放投掷音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 0.8F, 0.8F);
        }
        
        return InteractionResultHolder.success(stack);
    }

    /**
     * 投掷星辰矛
     */
    private void throwStarSpear(Level level, Player player, ItemStack weapon) {
        // 计算投掷方向
        Vec3 lookVec = player.getLookAngle();
        Vec3 spawnPos = player.getEyePosition().add(lookVec.scale(1.5));
        
        // 创建星辰矛投射物
        StarSpearProjectile starSpear = new StarSpearProjectile(level, player, weapon, calculateStarDamage(player, weapon));
        starSpear.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        
        // 设置投掷速度和方向（受重力影响）
        float speed = 1.5f;
        starSpear.setDeltaMovement(lookVec.x * speed, lookVec.y * speed, lookVec.z * speed);
        starSpear.setNoGravity(false); // 受重力影响
        
        // 添加到世界
        level.addFreshEntity(starSpear);
    }

    /**
     * 生成星星弹幕
     */
    private void spawnStarProjectile(Level level, Vec3 spawnPos, Player player, ItemStack weapon) {
        // 计算星星伤害
        float starDamage = calculateStarDamage(player, weapon);
        
        // 创建星星弹幕
        StarProjectile star = new StarProjectile(level, player, weapon, starDamage);
        star.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        
        // 设置随机方向（向下掉落）
        double randomX = (Math.random() - 0.5) * 0.2;
        double randomZ = (Math.random() - 0.5) * 0.2;
        star.setDeltaMovement(randomX, -0.5, randomZ); // 向下掉落
        
        // 添加到世界
        level.addFreshEntity(star);
    }

    /**
     * 计算星星弹幕的伤害
     */
    private float calculateStarDamage(Player player, ItemStack stack) {
        // 获取基础伤害
        float baseDamage = getBaseDamage(player, stack);
        
        // 应用星星伤害倍率
        return baseDamage * STAR_DAMAGE_MULTIPLIER;
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