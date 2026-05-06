package cn.dawnstring.fatality.items.weapon.magic;

import cn.dawnstring.fatality.entity.projectile.TornadoProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import cn.dawnstring.fatality.system.ManaSystem;
import cn.dawnstring.fatality.registry.ModEntities;
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
 * 龙卷风 - 魔法武器
 * 特性：右键释放直线前进的龙卷风，吸引3格内实体并造成伤害
 * 伤害550 暴击率20 暴击伤害28 浮动0.3 攻击速度0.5s
 */
public class Tornado extends BaseWeapon
{
    private static final float BASE_MAGIC_DAMAGE = 550.0f; // 基础魔法伤害550
    private static final float MANA_COST_PER_ATTACK = 15.0f; // 每次攻击消耗15点魔法值
    private static final int ATTACK_COOLDOWN_TICKS = 10; // 攻击冷却时间10tick（0.5秒）

    public Tornado() {
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
        }, new Properties(), (int)BASE_MAGIC_DAMAGE, 0.5f, 1f, 0.20f, 0.28f, 0.3f, WeaponEnum.MAGIC);
        
        setStory("一把能够召唤龙卷风的魔法武器，释放的龙卷风会直线前进，吸引周围的敌人并造成持续伤害。");
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

        // 释放龙卷风
        if (!level.isClientSide()) {
            performTornadoAttack(level, player, itemstack);
        }

        return InteractionResultHolder.consume(itemstack);
    }
    
    /**
     * 执行龙卷风攻击
     */
    private void performTornadoAttack(Level level, Player player, ItemStack itemstack) {
        // 计算龙卷风伤害
        float tornadoDamage = calculateFinalDamage(player, itemstack, null);
        
        // 创建龙卷风投射物
        TornadoProjectile tornado = new TornadoProjectile(level, player, tornadoDamage);
        
        // 设置投射物位置和方向
        tornado.setPos(player.getEyePosition().x, player.getEyePosition().y, player.getEyePosition().z);
        tornado.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 1.5f, 1.0f); // 1.5速度，标准散布
        
        // 添加到世界
        level.addFreshEntity(tornado);
    }
    
    /**
     * 重写暴击特效，添加龙卷风特有的暴击效果
     */
    @Override
    protected void onCriticalHit(Player player, net.minecraft.world.entity.LivingEntity target, float damage) {
        super.onCriticalHit(player, target, damage);

        // 添加额外的龙卷风暴击特效
        if (player.level().isClientSide) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§b龙卷风暴击！ " + String.format("%.1f", damage) + " 伤害"),
                    true
            );
        }
    }

    /**
     * 检查是否有足够的魔法值
     */
    public static boolean hasEnoughMana(Player player, float requiredMana) {
        return ManaSystem.getCurrentMana(player) >= requiredMana;
    }
}