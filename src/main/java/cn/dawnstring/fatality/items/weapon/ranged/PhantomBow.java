package cn.dawnstring.fatality.items.weapon.ranged;

import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 幻影弓 - 高伤害远程武器
 * 特性：高基础伤害、中等暴击率、快速攻击速度
 */
public class PhantomBow extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 20; // 冷却时间20tick（1秒）
    private static final float BASE_ARROW_DAMAGE = 34.0f; // 基础箭矢伤害34

    public PhantomBow()
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
        }, new Properties(), (int)BASE_ARROW_DAMAGE, 1.0f, 1f, 0.08f, 0.10f, 0.2f, WeaponEnum.RANGED);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算箭矢伤害（使用BaseWeapon的伤害计算逻辑）
            float arrowDamage = calculateArrowDamage(player, itemstack);

            // 创建箭矢投射物
            Arrow arrow = new Arrow(level, player);

            // 设置箭矢伤害（基于BaseWeapon计算的结果）
            arrow.setBaseDamage(arrowDamage);

            // 设置箭矢位置和方向
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();

            arrow.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
            arrow.shoot(lookVec.x, lookVec.y, lookVec.z, 3.5F, 1.0F); // 3.5速度，标准散布

            // 添加到世界
            level.addFreshEntity(arrow);

            // 播放射箭音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 0.9F); // 稍微降低音调
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 计算箭矢伤害（使用BaseWeapon相同的计算方法）
     */
    public float calculateArrowDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }
}