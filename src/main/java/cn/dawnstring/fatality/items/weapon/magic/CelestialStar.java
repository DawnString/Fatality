package cn.dawnstring.fatality.items.weapon.magic;

import cn.dawnstring.fatality.entity.projectile.CelestialStarProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import cn.dawnstring.fatality.system.ManaSystem;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

/**
 * 天星 - 魔法武器
 * 特性：右键时，从天空召唤天星，对目标造成伤害，同时对周围目标造成伤害
 * 伤害2100 暴击率24 暴击伤害32 浮动0.4 攻击速度1s
 */
public class CelestialStar extends BaseWeapon
{
    private static final float BASE_MAGIC_DAMAGE = 2100.0f; // 基础魔法伤害2100
    private static final float MANA_COST_PER_ATTACK = 25.0f; // 每次攻击消耗25点魔法值
    private static final int ATTACK_COOLDOWN_TICKS = 20; // 攻击冷却时间20tick（1秒）
    private static final float STAR_FALL_HEIGHT = 30.0f; // 天星下落高度
    private static final float AOE_RADIUS = 5.0f; // 范围伤害半径

    public CelestialStar() {
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
        }, new Properties(), 0, 1.0f, 1f, 0.24f, 0.32f, 0.4f, WeaponEnum.MAGIC);
        
        setStory("一把能够召唤天星的神秘魔法武器，从天空召唤陨石般的星辰，对目标及其周围敌人造成毁灭性打击。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        // 检查攻击冷却时间
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(itemstack);
        }

        // 检查魔法值是否足够
        if (!ManaSystem.hasEnoughMana(player, MANA_COST_PER_ATTACK)) {
            if (level.isClientSide()) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c魔法值不足！"), true);
            }
            return InteractionResultHolder.fail(itemstack);
        }

        // 消耗魔法值
        ManaSystem.consumeMana(player, MANA_COST_PER_ATTACK);

        // 设置攻击冷却时间
        player.getCooldowns().addCooldown(this, ATTACK_COOLDOWN_TICKS);

        // 召唤天星
        if (!level.isClientSide()) {
            performCelestialStarAttack(level, player, itemstack);
        }

        // 播放音效
        level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                       SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.0f, 0.8f);

        return InteractionResultHolder.consume(itemstack);
    }
    
    /**
     * 执行天星攻击
     */
    private void performCelestialStarAttack(Level level, Player player, ItemStack itemstack) {
        // 获取玩家瞄准的目标位置
        net.minecraft.world.phys.HitResult hitResult = player.pick(20.0, 1.0f, false);
        
        if (hitResult.getType() == net.minecraft.world.phys.HitResult.Type.MISS) {
            // 如果没有命中目标，使用玩家前方20格的位置
            net.minecraft.world.phys.Vec3 lookVec = player.getLookAngle();
            net.minecraft.world.phys.Vec3 targetPos = player.getEyePosition().add(lookVec.x * 20, lookVec.y * 20, lookVec.z * 20);
            summonCelestialStar(level, player, itemstack, targetPos);
        } else {
            // 如果命中目标，使用命中位置
            summonCelestialStar(level, player, itemstack, hitResult.getLocation());
        }
    }
    
    /**
     * 召唤天星
     */
    private void summonCelestialStar(Level level, Player player, ItemStack itemstack, net.minecraft.world.phys.Vec3 targetPos) {
        // 计算天星伤害
        float starDamage = calculateStarDamage(player, itemstack);
        
        // 创建天星投射物
        CelestialStarProjectile star = new CelestialStarProjectile(level, player, starDamage, AOE_RADIUS);
        
        // 设置天星位置（从天空下落）
        double spawnX = targetPos.x;
        double spawnY = targetPos.y + STAR_FALL_HEIGHT;
        double spawnZ = targetPos.z;
        
        star.setPos(spawnX, spawnY, spawnZ);
        
        // 添加到世界
        level.addFreshEntity(star);
    }
    
    /**
     * 计算天星伤害（使用BaseWeapon相同的计算方法）
     */
    public float calculateStarDamage(Player player, ItemStack stack) {
        // 使用BaseWeapon的伤害计算逻辑，但基于魔法伤害
        float baseDamage = BASE_MAGIC_DAMAGE;

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
     * 重写暴击特效，添加天星特有的暴击效果
     */
    @Override
    protected void onCriticalHit(Player player, net.minecraft.world.entity.LivingEntity target, float damage) {
        super.onCriticalHit(player, target, damage);

        // 添加额外的天星暴击特效
        if (player.level().isClientSide) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§6天星暴击！ " + String.format("%.1f", damage) + " 伤害"),
                    true
            );
        }
    }
}