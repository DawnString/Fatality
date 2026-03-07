package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.entity.projectile.SacredFlyingAxeProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SacredFlyingAxe extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 6; // 冷却时间6tick（0.3秒）
    private static final float BASE_AXE_DAMAGE = 165.0f; // 基础飞斧伤害

    public SacredFlyingAxe()
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
        }, new Properties(), 165, 0.3f, 1, 0.13f, 0.14f, 0.4f, WeaponEnum.MELEE);

        // 设置物品故事
        setStory("神圣的光芒在斧刃上闪耀\n" +
                "你握着这把神圣之斧\n" +
                "感受着神圣力量的庇护\n");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算飞斧伤害（使用BaseWeapon的伤害计算逻辑）
            float axeDamage = calculateAxeDamage(player, itemstack);

            // 创建飞斧投射物
            SacredFlyingAxeProjectile axe = new SacredFlyingAxeProjectile(level, player, itemstack, axeDamage);

            // 设置飞斧位置和方向
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();

            axe.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y - 0.1, eyePos.z + lookVec.z);
            axe.shoot(lookVec.x, lookVec.y, lookVec.z, 3.0F, 1.0F); // 3.0速度，1.0散布

            // 添加到世界
            level.addFreshEntity(axe);

            // 播放投掷音效 - 使用三叉戟投掷音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        // 设置冷却时间（不消耗物品）
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 计算飞斧伤害（使用BaseWeapon相同的计算方法）
     */
    public float calculateAxeDamage(Player player, ItemStack stack) {
        // 使用BaseWeapon的伤害计算逻辑，但基于飞斧伤害
        float baseDamage = BASE_AXE_DAMAGE;

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