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

public class DesertShotgun extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 20; // 冷却时间20tick（1秒）
    private static final float BASE_BULLET_DAMAGE = 24.0f; // 单发子弹基础伤害24
    private static final int MAX_PELLET_COUNT = 8; // 最大弹丸数量
    private static final int MIN_PELLET_COUNT = 4; // 最小弹丸数量
    private static final float MAX_SPREAD_ANGLE = 20.0f; // 最大散射角度（度）
    private static final float MIN_SPREAD_ANGLE = 8.0f; // 最小散射角度（度）

    public DesertShotgun()
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
        }, new Properties(), 0, 1.0f, 1f, 0.08f, 0.10f, 0.4f, WeaponEnum.RANGED);
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
                bullet.shoot(spreadVec.x, spreadVec.y, spreadVec.z, 4.0F, 0.1F); // 4.0速度，小散布

                // 添加到世界
                level.addFreshEntity(bullet);
            }

            // 播放霰弹枪射击音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.5F, 0.8F);
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

    /**
     * 计算锥形散布方向（改进的算法）
     * @param baseDirection 基础方向
     * @param pelletIndex 弹丸索引
     * @param totalPellets 总弹丸数量
     * @param spreadAngle 散射角度
     * @return 散布后的方向向量
     */
    private Vec3 calculateConeSpreadDirection(Vec3 baseDirection, int pelletIndex, int totalPellets, float spreadAngle) {
        // 将角度转换为弧度
        double spreadRad = Math.toRadians(spreadAngle);

        // 计算弹丸在锥形中的角度（均匀分布）
        double angleStep = 2 * Math.PI / totalPellets;
        double angle = pelletIndex * angleStep;

        // 计算弹丸在锥形中的半径（距离中心的角度）
        double radius = Math.random() * spreadRad;

        // 计算水平偏移（基于极坐标）
        double horizontalOffset = radius * Math.cos(angle);
        double verticalOffset = radius * Math.sin(angle);

        // 获取基础方向的垂直和水平分量
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = baseDirection.cross(up).normalize();
        Vec3 forward = baseDirection.normalize();

        // 计算最终方向向量
        Vec3 spreadVec = forward
                .add(right.scale(horizontalOffset))
                .add(up.scale(verticalOffset))
                .normalize();

        return spreadVec;
    }

    /**
     * 计算子弹伤害（使用BaseWeapon相同的计算方法）
     */
    public float calculateBulletDamage(Player player, ItemStack stack) {
        // 使用BaseWeapon的伤害计算逻辑，但基于子弹伤害
        float baseDamage = BASE_BULLET_DAMAGE;

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