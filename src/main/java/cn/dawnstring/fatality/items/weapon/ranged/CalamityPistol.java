package cn.dawnstring.fatality.items.weapon.ranged;

import cn.dawnstring.fatality.entity.projectile.BulletProjectile;
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
import net.minecraft.world.phys.Vec3;

public class CalamityPistol extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 5; // 冷却时间5tick（0.25秒）
    private static final float BASE_BULLET_DAMAGE = 33.0f; // 基础子弹伤害33

    public CalamityPistol()
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
        }, new Properties(), (int)BASE_BULLET_DAMAGE, 0.25f, 1f, 0.08f, 0.89f, 0.4f, WeaponEnum.RANGED);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算子弹伤害（使用BaseWeapon的伤害计算逻辑）
            float bulletDamage = calculateBulletDamage(player, itemstack);

            // 创建子弹投射物
            BulletProjectile bullet = new BulletProjectile(level, player, itemstack, bulletDamage);

            // 设置子弹位置和方向
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();

            bullet.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
            bullet.shoot(lookVec.x, lookVec.y, lookVec.z, 5.0F, 0.5F); // 5.0速度，0.5散布（更精准）

            // 添加到世界
            level.addFreshEntity(bullet);

            // 播放射击音效 - 更响亮的音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIREWORK_ROCKET_SHOOT, SoundSource.PLAYERS, 1.2F, 1.0F);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 计算子弹伤害（使用BaseWeapon相同的计算方法）
     */
    public float calculateBulletDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }
}