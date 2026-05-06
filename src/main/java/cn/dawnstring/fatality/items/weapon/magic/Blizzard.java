package cn.dawnstring.fatality.items.weapon.magic;

import cn.dawnstring.fatality.entity.projectile.IcicleProjectile;
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
 * 雪暴法杖 - 召唤大量冰锥对目标造成伤害的魔法武器
 * 特性：高基础伤害、冰锥范围攻击、冰冻效果
 */
public class Blizzard extends BaseWeapon
{
    private static final float MANA_COST = 6.0f;
    private static final int COOLDOWN_TICKS = 20;
    private static final double TARGET_RANGE = 25.0;
    private static final int ICICLE_COUNT = 12;
    private static final double ICICLE_SPREAD = 3.0;
    private static final float BASE_MAGIC_DAMAGE = 12.0f;

    public Blizzard() {
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
        }, new Properties(), (int)BASE_MAGIC_DAMAGE, 1.0f, 1f, 0.08f, 0.09f, 0.3f, WeaponEnum.MAGIC);
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

        // 检测玩家准星对准的最近生物
        LivingEntity target = findTargetEntity(player, level);

        if (target != null) {
            // 计算冰锥伤害
            float icicleDamage = calculateFinalDamage(player, itemstack, null);

            // 在目标上方召唤大量冰锥（在服务器端创建实体）
            if (!level.isClientSide()) {
                summonIcicles(level, player, target, icicleDamage);

                // 播放雪暴施法音效
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.POWDER_SNOW_BREAK, SoundSource.PLAYERS, 1.2F, 0.7F);

                // 显示目标信息
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§b对 " + target.getName().getString() + " 召唤了雪暴冰锥！"), true);
            } else {
                // 在客户端也播放音效和显示信息
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.POWDER_SNOW_BREAK, SoundSource.PLAYERS, 1.2F, 0.7F);
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§b对 " + target.getName().getString() + " 召唤了雪暴冰锥！"), true);
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
     * 在目标上方召唤大量冰锥
     */
    private void summonIcicles(Level level, Player player, LivingEntity target, float damage) {
        Vec3 targetPos = target.position();

        // 在目标上方一定高度召唤冰锥
        double spawnHeight = targetPos.y + 16.0; // 在目标上方8格高度召唤

        for (int i = 0; i < ICICLE_COUNT; i++) {
            // 计算冰锥的随机散布位置
            double offsetX = (Math.random() - 0.5) * ICICLE_SPREAD;
            double offsetZ = (Math.random() - 0.5) * ICICLE_SPREAD;

            Vec3 spawnPos = new Vec3(
                    targetPos.x + offsetX,
                    spawnHeight,
                    targetPos.z + offsetZ
            );

            // 创建冰锥投射物
            IcicleProjectile icicle = new IcicleProjectile(level, player, damage);

            // 设置冰锥位置
            icicle.setPos(spawnPos.x, spawnPos.y, spawnPos.z);

            // 设置冰锥垂直下落
            icicle.shoot(0, -1.0, 0, 2.0F, 0.1F); // 垂直下落，2.0速度，小散布

            // 添加到世界
            level.addFreshEntity(icicle);
        }

        // 播放冰锥召唤音效
        level.playSound(null, targetPos.x, targetPos.y, targetPos.z,
                SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 0.8F, 1.5F);
    }
}