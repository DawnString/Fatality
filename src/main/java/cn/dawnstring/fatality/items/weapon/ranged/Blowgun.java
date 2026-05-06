package cn.dawnstring.fatality.items.weapon.ranged;

import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Blowgun extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 10; // 冷却时间10tick（0.5秒）
    private static final float BASE_ARROW_DAMAGE = 10.0f; // 基础箭矢伤害
    private static final int POISON_DURATION = 100; // 中毒效果持续时间（5秒，100ticks）

    public Blowgun()
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
        }, new Properties(), (int)BASE_ARROW_DAMAGE, 0.8f, 1f, 0.05f, 0.05f, 0.4f, WeaponEnum.RANGED);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算箭矢伤害（使用BaseWeapon的伤害计算逻辑）
            float arrowDamage = calculateBulletDamage(player, itemstack);

            // 创建中毒箭矢投射物
            Arrow arrow = new Arrow(level, player);

            // 设置箭矢为中毒箭
            arrow.setEffectsFromItem(new ItemStack(Items.TIPPED_ARROW));
            arrow.addEffect(new MobEffectInstance(MobEffects.POISON, POISON_DURATION, 0)); // 中毒效果，持续5秒，等级0

            // 设置箭矢伤害（基于BaseWeapon计算的结果）
            arrow.setBaseDamage(arrowDamage);

            // 设置箭矢位置和方向
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();

            arrow.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
            arrow.shoot(lookVec.x, lookVec.y, lookVec.z, 3.0F, 1.0F); // 3.0速度，标准散布

            // 添加到世界
            level.addFreshEntity(arrow);

            // 播放射箭音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    public float calculateBulletDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }
}