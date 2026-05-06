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

public class WaterStaff extends BaseWeapon
{
    private static final float MANA_COST = 5; // 每次施法消耗5点魔法值
    private static final int COOLDOWN_TICKS = 40; // 冷却时间2秒
    private static final float BASE_MAGIC_DAMAGE = 8.0f; // 基础魔法伤害（持续伤害）
    private static final double TARGET_RANGE = 20.0; // 检测目标的最大距离

    public WaterStaff()
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
        }, new Properties(), (int)BASE_MAGIC_DAMAGE, 0.8f, 1, 0.05f, 0.06f, 0.3f, WeaponEnum.MAGIC);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            if (!ManaSystem.safeConsumeMana(player, MANA_COST)) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c魔法值不足！需要" + MANA_COST + "点魔法值"), true);
                return InteractionResultHolder.fail(itemstack);
            }

            LivingEntity target = findTargetEntity(player, level);

            if (target != null) {
                float tornadoDamage = calculateFinalDamage(player, itemstack, null);

                TornadoEffect tornado = new TornadoEffect(level, player, target, tornadoDamage);
                level.addFreshEntity(tornado);

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.PLAYERS, 1.0F, 1.2F);
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§b对 " + target.getName().getString() + " 施放了水龙卷！"), true);
            } else {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c未找到目标！请对准生物使用"), true);
                return InteractionResultHolder.fail(itemstack);
            }

            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

            return InteractionResultHolder.success(itemstack);
        }

        return InteractionResultHolder.pass(itemstack);
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
}