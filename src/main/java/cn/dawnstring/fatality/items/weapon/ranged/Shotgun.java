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

public class Shotgun extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 20; // 冷却时间20tick（1秒）
    private static final float BASE_BULLET_DAMAGE = 3.0f; // 单发子弹基础伤害
    private static final int MAX_PELLET_COUNT = 12; // 最大弹丸数量
    private static final int MIN_PELLET_COUNT = 6; // 最小弹丸数量
    private static final float MAX_SPREAD_ANGLE = 25.0f; // 最大散射角度（度）
    private static final float MIN_SPREAD_ANGLE = 10.0f; // 最小散射角度（度）

    public Shotgun()
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
        }, new Properties(), (int)BASE_BULLET_DAMAGE, 2.0f, 1f, 0.1f, 0.1f, 0.3f, WeaponEnum.RANGED);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 随机弹丸数量（每次射击都重新随机）
            int pelletCount = getRandomPelletCount();

            // 随机散射角度（每次射击都不同）
            float spreadAngle = getRandomSpreadAngle();

            // 计算单发子弹伤害（使用BaseWeapon的伤害计算逻辑）
            float bulletDamage = calculateBulletDamage(player, itemstack);

            // 发射多发弹丸
            for (int i = 0; i < pelletCount; i++)
            {
                // 创建子弹投射物
                BulletProjectile bullet = new BulletProjectile(level, player, itemstack, bulletDamage);

                // 设置子弹位置和方向
                Vec3 lookVec = player.getLookAngle();
                Vec3 eyePos = player.getEyePosition();

                // 计算锥形散布方向
                Vec3 spreadVec = calculateConeSpreadDirection(lookVec, i, pelletCount, spreadAngle);

                bullet.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
                bullet.shoot(spreadVec.x, spreadVec.y, spreadVec.z, 3.5F, 0.1F); // 3.5速度，小散布

                // 添加到世界
                level.addFreshEntity(bullet);
            }

            // 播放霰弹枪射击音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.2F, 0.9F);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 获取随机弹丸数量
     */
    private int getRandomPelletCount() {
        return MIN_PELLET_COUNT + (int)(Math.random() * (MAX_PELLET_COUNT - MIN_PELLET_COUNT + 1));
    }

    /**
     * 获取随机散射角度
     */
    private float getRandomSpreadAngle() {
        return MIN_SPREAD_ANGLE + (float)(Math.random() * (MAX_SPREAD_ANGLE - MIN_SPREAD_ANGLE));
    }

    protected Vec3 calculateConeSpreadDirection(Vec3 baseDirection, int pelletIndex, int totalPellets, float spreadAngle) {
        return super.calculateConeSpreadDirection(baseDirection, pelletIndex, totalPellets, spreadAngle);
    }

    public float calculateBulletDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }
}