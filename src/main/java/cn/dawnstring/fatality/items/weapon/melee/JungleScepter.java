package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.entity.projectile.JungleSwordWaveProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
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
 * 丛林权杖 - 近战武器
 * 特性：右键挥舞权杖，并召唤绿色剑气飞出
 * 伤害600 暴击率28 暴击伤害36 浮动0.3 攻击速度0.25s
 */
public class JungleScepter extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 10; // 冷却时间10tick（0.5秒）

    public JungleScepter()
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
                return null; // 使用藤蔓修复
            }
        }, new Properties(), 600, 0.25f, 1f, 0.28f, 0.36f, 0.3f, WeaponEnum.MELEE);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算剑气伤害
            float swordWaveDamage = calculateSwordWaveDamage(player, itemstack);
            
            // 创建丛林权杖剑气投射物
            JungleSwordWaveProjectile swordWave = new JungleSwordWaveProjectile(level, player, swordWaveDamage);
            
            // 设置投射物位置和方向
            swordWave.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            swordWave.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.0F, 0.0F); // 中等飞行速度
            
            level.addFreshEntity(swordWave);
            
            // 播放剑气音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.2F);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 计算剑气伤害
     */
    public float calculateSwordWaveDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }
}