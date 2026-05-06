package cn.dawnstring.fatality.items.weapon.magic;

import cn.dawnstring.fatality.entity.projectile.FireballProjectile;
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
import net.minecraft.world.phys.Vec3;

public class FireballSpellbook extends BaseWeapon
{
    private static final float MANA_COST = 5.0f; // 每次施法消耗5点魔法值
    private static final int COOLDOWN_TICKS = 16; // 冷却时间16tick（0.8秒）
    private static final float BASE_MAGIC_DAMAGE = 10.0f; // 基础魔法伤害

    public FireballSpellbook() {
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
        }, new Properties(), (int)BASE_MAGIC_DAMAGE, 1, 1, 0.05f, 1.0f, 0.3f, WeaponEnum.MAGIC);
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

        if (!level.isClientSide()) {
            // 计算火球伤害
            float fireballDamage = calculateFinalDamage(player, itemstack, null);

            // 创建火球投射物，传递伤害信息
            FireballProjectile projectile = new FireballProjectile(level, player, itemstack, fireballDamage);

            // 设置火球位置和方向
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();

            projectile.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
            projectile.shoot(lookVec.x, lookVec.y, lookVec.z, 1.2F, 1.0F);

            // 添加到世界
            level.addFreshEntity(projectile);

            // 播放施法音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

}