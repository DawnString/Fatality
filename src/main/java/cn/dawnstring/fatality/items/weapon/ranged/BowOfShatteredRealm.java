package cn.dawnstring.fatality.items.weapon.ranged;

import cn.dawnstring.fatality.entity.projectile.ShatteredRealmArrow;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

/**
 * 破碎领域之弓 - 高伤害空间坍缩远程武器
 * 特性：箭矢无视重力，命中后创造空间坍缩吸附实体并喷发黑色粒子造成伤害
 * 属性：伤害3650、暴击率32%、暴击伤害34%、浮动0.2、攻击速度1s
 */
public class BowOfShatteredRealm extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 20; // 冷却时间20tick（1秒）
    private static final float BASE_ARROW_DAMAGE = 3650.0f; // 基础箭矢伤害3650
    
    // 空间坍缩参数
    private static final float COLLAPSE_RADIUS = 5.0f; // 坍缩吸附半径5格
    private static final int COLLAPSE_DURATION = 60; // 坍缩持续时间60tick（3秒）
    private static final float COLLAPSE_DAMAGE_MULTIPLIER = 0.3f; // 坍缩伤害倍率30%

    public BowOfShatteredRealm()
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
        }, new Properties().fireResistant(), 0, 1.0f, 1f, 0.32f, 0.34f, 0.2f, WeaponEnum.RANGED);
        
        setStory("破碎领域之弓，蕴含着撕裂空间的力量。\n" +
                "射出的箭矢无视重力，命中目标后会在该位置创造空间坍缩，\n" +
                "吸附周围的实体，并喷发出毁灭性的黑色粒子，造成持续伤害。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        ItemStack itemstack = player.getItemInHand(hand);
        
        if (!level.isClientSide()) {
            // 计算箭矢伤害（使用BaseWeapon的伤害计算逻辑）
            float arrowDamage = calculateArrowDamage(player, itemstack);
            
            // 创建破碎领域箭矢投射物
            ShatteredRealmArrow arrow = new ShatteredRealmArrow(level, player, arrowDamage, COLLAPSE_RADIUS, COLLAPSE_DURATION, COLLAPSE_DAMAGE_MULTIPLIER);
            
            // 设置箭矢位置和方向
            arrow.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 3.0f, 1.0f);
            
            // 设置箭矢无视重力
            arrow.setNoGravity(true);
            
            // 添加到世界
            level.addFreshEntity(arrow);
            
            // 播放空间撕裂音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.5F);
        }
        
        // 统计使用次数
        player.awardStat(Stats.ITEM_USED.get(this));
        
        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }

    /**
     * 计算箭矢伤害（使用BaseWeapon相同的计算方法）
     */
    public float calculateArrowDamage(Player player, ItemStack stack) {
        // 使用BaseWeapon的伤害计算逻辑，但基于箭矢伤害
        float baseDamage = BASE_ARROW_DAMAGE;

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