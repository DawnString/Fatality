package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.entity.projectile.BurningHeavenProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import net.minecraft.core.particles.ParticleTypes;
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
 * 燃烧天堂 - 近战长矛武器
 * 特性：右键投掷长矛，不受重力影响，击中目标后产生爆炸效果
 */
public class BurningHeaven extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 4; // 冷却时间4tick（0.2秒）

    public BurningHeaven()
    {
        super(new Tier() {
            @Override
            public int getUses() {
                return 0; // 耐久度
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
                return 0; // 材料等级（钻石级）
            }

            @Override
            public int getEnchantmentValue() {
                return 0; // 附魔能力
            }

            @Override
            public Ingredient getRepairIngredient() {
                // 使用下界合金锭修复
                return null;
            }
        }, new Properties(), 94, 0.2f, 1f, 0.16f, 1.6f, 0.3f, WeaponEnum.MELEE);

        setStory("燃烧天堂长矛，蕴含着炽热的天堂之力。\n" +
                "投掷时不受重力束缚，击中目标后引发爆炸，\n" +
                "将敌人燃烧殆尽。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算长矛伤害
            float spearDamage = calculateSpearDamage(player, itemstack);
            
            // 创建燃烧天堂长矛投射物
            BurningHeavenProjectile spear = new BurningHeavenProjectile(level, player, spearDamage);
            
            // 设置投射物位置和方向
            spear.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            spear.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 0.0F); // 无重力，高速度
            
            level.addFreshEntity(spear);
            
            // 播放投掷音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        // 在客户端生成粒子效果
        if (level.isClientSide()) {
            spawnThrowParticles(level, player);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 计算长矛伤害
     */
    public float calculateSpearDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }

    /**
     * 生成投掷粒子效果
     */
    private void spawnThrowParticles(Level level, Player player) {
        // 生成火焰粒子效果
        for (int i = 0; i < 10; i++) {
            double offsetX = (Math.random() - 0.5) * 0.5;
            double offsetY = (Math.random() - 0.5) * 0.5;
            double offsetZ = (Math.random() - 0.5) * 0.5;
            
            level.addParticle(ParticleTypes.FLAME,
                    player.getX() + offsetX,
                    player.getY() + player.getEyeHeight() + offsetY,
                    player.getZ() + offsetZ,
                    0, 0, 0);
        }
    }
}