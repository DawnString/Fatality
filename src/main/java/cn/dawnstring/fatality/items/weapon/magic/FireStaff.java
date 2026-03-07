package cn.dawnstring.fatality.items.weapon.magic;

import cn.dawnstring.fatality.entity.TornadoEffect;
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

public class FireStaff extends BaseWeapon
{
    private static final float MANA_COST = 5; // 每次施法消耗5点魔法值
    private static final int COOLDOWN_TICKS = 40; // 冷却时间2秒
    private static final float BASE_MAGIC_DAMAGE = 8.0f; // 基础魔法伤害（持续伤害）
    private static final double TARGET_RANGE = 20.0; // 检测目标的最大距离

    public FireStaff()
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
                return 0; // 法书本身没有攻击伤害加成
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
        }, new Properties(), 0, 0.6f, 1, 0.05f, 0.06f, 0.3f, WeaponEnum.MAGIC);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        // 检查魔法值是否小于消耗值的2倍（释放失败条件）
        if (ManaSystem.getCurrentMana(player) < MANA_COST * 2) {
            // 如果魔法值不足消耗值的2倍，释放失败
            if (level.isClientSide()) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c魔法值不足！需要至少" + (MANA_COST * 2) + "点魔法值才能释放"), true);
            }
            return InteractionResultHolder.fail(itemstack);
        }

        // 检查玩家是否有足够的魔法值
        if (!ManaSystem.hasEnoughMana(player, MANA_COST)) {
            // 如果魔法值不足，提示玩家
            if (level.isClientSide()) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c魔法值不足！需要" + MANA_COST + "点魔法值"), true);
            }
            return InteractionResultHolder.fail(itemstack);
        }

        // 检测玩家准星对准的最近生物
        LivingEntity target = findTargetEntity(player, level);

        if (target != null) {
            // 调试信息：显示找到的目标
            if (level.isClientSide()) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§a找到目标: " + target.getName().getString()), true);
            }

            // 计算龙卷风伤害
            float tornadoDamage = calculateTornadoDamage(player, itemstack);

            // 创建龙卷风效果（在服务器端创建实体）
            if (!level.isClientSide()) {
                TornadoEffect tornado = new TornadoEffect(level, player, target, tornadoDamage);
                level.addFreshEntity(tornado);

                // 调试信息：显示创建的龙卷风实体
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§a创建龙卷风实体，目标ID: " + target.getId()), true);

                // 播放施法音效
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.8F);

                // 显示目标信息
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§6对 " + target.getName().getString() + " 施放了龙卷风！"), true);

                // 魔法释放成功后才消耗魔法值
                ManaSystem.consumeMana(player, MANA_COST);
            } else {
                // 在客户端也播放音效和显示信息
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.8F);
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§6对 " + target.getName().getString() + " 施放了龙卷风！"), true);
            }
        } else {
            // 如果没有找到目标，提示玩家
            if (level.isClientSide()) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c未找到目标！请对准生物使用"), true);
            }
            return InteractionResultHolder.fail(itemstack);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 检测玩家准星对准的最近生物
     */
    private LivingEntity findTargetEntity(Player player, Level level) {
    Vec3 start = player.getEyePosition();
    Vec3 lookVec = player.getLookAngle();
    Vec3 end = start.add(lookVec.scale(TARGET_RANGE));

    // 获取玩家视线方向上的所有实体
    AABB searchBox = new AABB(start, end).inflate(3.0); // 扩大搜索范围

    LivingEntity closestTarget = null;
    double closestDistance = Double.MAX_VALUE;

    for (Entity entity : level.getEntities(player, searchBox)) {
        if (entity instanceof LivingEntity livingEntity && entity != player) {
            // 简化视线检测，只要在范围内就认为是目标
            double distance = player.distanceToSqr(livingEntity);
            if (distance < closestDistance && distance <= TARGET_RANGE * TARGET_RANGE) {
                closestDistance = distance;
                closestTarget = livingEntity;
            }
        }
    }

    return closestTarget;
}

    /**
     * 检查实体是否在玩家视线范围内（简化版本）
     */
    private boolean isEntityInSight(Player player, LivingEntity entity, double maxDistance) {
        Vec3 playerEyes = player.getEyePosition();
        Vec3 entityCenter = entity.getBoundingBox().getCenter();

        // 计算距离
        double distance = playerEyes.distanceTo(entityCenter);
        if (distance > maxDistance) {
            return false;
        }

        // 简化视线检测，只要在玩家前方就认为是可见的
        Vec3 lookVec = player.getLookAngle();
        Vec3 toEntity = entityCenter.subtract(playerEyes).normalize();

        // 放宽视线角度要求（从60度放宽到90度）
        double dotProduct = lookVec.dot(toEntity);
        return dotProduct > 0.0; // 只要在玩家前方就认为是可见的
    }

    public float calculateTornadoDamage(Player player, ItemStack stack) {
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

        return Math.max(1.0f, finalDamage); // 确保最小伤害为1
    }
}