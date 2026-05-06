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

public class Pistol extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 12; // 冷却时间12tick（0.6秒）
    private static final float BASE_BULLET_DAMAGE = 12.0f; // 基础子弹伤害

    public Pistol()
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
        }, new Properties(), (int)BASE_BULLET_DAMAGE, 0.6f, 1f, 0.05f, 0.05f, 0.2f, WeaponEnum.RANGED);
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
            bullet.shoot(lookVec.x, lookVec.y, lookVec.z, 4.0F, 1.0F); // 4.0速度，标准散布

            // 添加到世界
            level.addFreshEntity(bullet);

            // 播放射击音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIREWORK_ROCKET_SHOOT, SoundSource.PLAYERS, 1.0F, 1.2F);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    public float calculateBulletDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }
}