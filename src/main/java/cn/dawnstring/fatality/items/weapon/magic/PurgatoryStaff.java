package cn.dawnstring.fatality.items.weapon.magic;

import cn.dawnstring.fatality.entity.projectile.PurgatoryMissileProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import cn.dawnstring.fatality.system.ManaSystem;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 炼狱法杖 - 魔法武器
 * 右键：在玩家半径10格内生物（除玩家外）上方召唤死灵飞弹攻击目标
 * 属性：710伤害、26%暴击率、35暴击伤害、0.3浮动值、0.25s攻击速度
 */
public class PurgatoryStaff extends BaseWeapon
{
    private static final float MANA_COST = 15.0f; // 每次施法消耗15点魔法值
    private static final int COOLDOWN_TICKS = 40; // 冷却时间40tick（2秒）
    private static final float BASE_MAGIC_DAMAGE = 710.0f; // 基础魔法伤害710
    private static final double TARGET_RANGE = 10.0; // 检测目标的最大距离
    private static final int MISSILE_COUNT = 3; // 死灵飞弹数量

    public PurgatoryStaff() {
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
                return 0; // 法杖本身没有攻击伤害加成
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
        }, new Properties(), 0, 1.0f, 1f, 0.26f, 0.3f, 0.25f, WeaponEnum.MAGIC);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        // 检查玩家是否有足够的魔法值
        if (!ManaSystem.safeConsumeMana(player, MANA_COST)) {
            // 如果魔法值不足，提示玩家
            if (level.isClientSide()) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c魔法值不足！需要" + MANA_COST + "点魔法值"), true);
            }
            return InteractionResultHolder.fail(itemstack);
        }

        // 检测玩家周围半径10格内的所有生物（除玩家外）
        java.util.List<LivingEntity> targets = findTargetEntities(player, level);

        if (!targets.isEmpty()) {
            // 计算死灵飞弹伤害
            float missileDamage = calculateMissileDamage(player, itemstack);

            // 对每个目标召唤死灵飞弹（在服务器端创建实体）
            if (!level.isClientSide()) {
                summonMissiles(level, player, targets, missileDamage);

                // 播放炼狱施法音效
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.0F, 0.9F);

                // 显示目标信息
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§6对 " + targets.size() + " 个目标召唤了死灵飞弹！"), true);
            } else {
                // 在客户端也播放音效和显示信息
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.0F, 0.9F);
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§6对 " + targets.size() + " 个目标召唤了死灵飞弹！"), true);
            }
        } else {
            // 如果没有找到目标，提示玩家
            if (level.isClientSide()) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c周围没有可攻击的目标！"), true);
            }
            return InteractionResultHolder.fail(itemstack);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 检测玩家周围半径10格内的所有生物（除玩家外）
     */
    private java.util.List<LivingEntity> findTargetEntities(Player player, Level level) {
        Vec3 playerPos = player.position();
        AABB searchBox = new AABB(playerPos, playerPos).inflate(TARGET_RANGE);

        java.util.List<LivingEntity> targets = new java.util.ArrayList<>();

        for (Entity entity : level.getEntities(player, searchBox)) {
            if (entity instanceof LivingEntity livingEntity && entity != player) {
                // 检查距离是否在范围内
                double distance = player.distanceToSqr(livingEntity);
                if (distance <= TARGET_RANGE * TARGET_RANGE) {
                    targets.add(livingEntity);
                }
            }
        }

        return targets;
    }

    /**
     * 对每个目标召唤死灵飞弹
     */
    private void summonMissiles(Level level, Player player, java.util.List<LivingEntity> targets, float damage) {
        for (LivingEntity target : targets) {
            // 对每个目标召唤多个死灵飞弹
            for (int i = 0; i < MISSILE_COUNT; i++) {
                // 在目标上方一定高度召唤死灵飞弹
                Vec3 targetPos = target.position();
                double spawnHeight = targetPos.y + 8.0 + (Math.random() * 4.0); // 在目标上方8-12格高度召唤

                // 计算飞弹的随机散布位置
                double offsetX = (Math.random() - 0.5) * 3.0;
                double offsetZ = (Math.random() - 0.5) * 3.0;

                Vec3 spawnPos = new Vec3(
                        targetPos.x + offsetX,
                        spawnHeight,
                        targetPos.z + offsetZ
                );

                // 创建死灵飞弹投射物
                PurgatoryMissileProjectile missile = new PurgatoryMissileProjectile(level, player, target, damage);

                // 设置飞弹位置
                missile.setPos(spawnPos.x, spawnPos.y, spawnPos.z);

                // 设置飞弹初始速度朝向目标
                Vec3 targetDirection = targetPos.subtract(spawnPos).normalize();
                missile.shoot(targetDirection.x, targetDirection.y, targetDirection.z, 1.0F, 0.5F);

                // 添加到世界
                level.addFreshEntity(missile);
            }

            // 播放目标锁定音效
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.SOUL_ESCAPE, SoundSource.HOSTILE, 0.6F, 1.1F);
        }
    }

    /**
     * 计算死灵飞弹伤害（使用BaseWeapon相同的计算方法）
     */
    public float calculateMissileDamage(Player player, ItemStack stack) {
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
}