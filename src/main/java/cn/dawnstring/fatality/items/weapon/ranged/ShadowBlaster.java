package cn.dawnstring.fatality.items.weapon.ranged;

import cn.dawnstring.fatality.entity.projectile.BulletProjectile;
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
import net.minecraft.world.phys.Vec3;

/**
 * 暗影爆破者 - 高伤害霰弹武器
 * 特性：极高基础伤害、黑色粒子效果、霰弹攻击方式
 */
public class ShadowBlaster extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 20; // 冷却时间20tick（1秒）
    private static final float BASE_BULLET_DAMAGE = 190.0f; // 单发子弹基础伤害190
    private static final int MAX_PELLET_COUNT = 8; // 最大弹丸数量
    private static final int MIN_PELLET_COUNT = 4; // 最小弹丸数量
    private static final float MAX_SPREAD_ANGLE = 20.0f; // 最大散射角度（度）
    private static final float MIN_SPREAD_ANGLE = 8.0f; // 最小散射角度（度）

    public ShadowBlaster()
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
        }, new Properties(), (int)BASE_BULLET_DAMAGE, 1.0f, 1f, 0.10f, 0.10f, 0.3f, WeaponEnum.RANGED);
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

            // 播放暗影爆破音效（使用龙息弹的声音）
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.PLAYERS, 1.5F, 0.8F);
        }

        // 在客户端生成黑色粒子效果
        if (level.isClientSide()) {
            spawnShadowParticles(level, player);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 生成暗影粒子效果（枪口喷出黑色大粒子）
     */
    private void spawnShadowParticles(Level level, Player player) {
        Vec3 lookVec = player.getLookAngle();
        Vec3 eyePos = player.getEyePosition();

        // 枪口位置（稍微向前偏移）
        Vec3 muzzlePos = eyePos.add(lookVec.scale(1.5));

        // 大幅增加粒子数量，创造大烟雾效果
        for (int i = 0; i < 35; i++) { // 从15增加到35个粒子
            // 增大随机偏移量，创造更分散的烟雾效果
            double offsetX = (Math.random() - 0.5) * 1.2;
            double offsetY = (Math.random() - 0.5) * 1.2;
            double offsetZ = (Math.random() - 0.5) * 1.2;

            // 增大粒子速度，创造更强的喷射效果
            double velocityX = lookVec.x * 1.2 + (Math.random() - 0.5) * 0.3;
            double velocityY = lookVec.y * 1.2 + (Math.random() - 0.5) * 0.3;
            double velocityZ = lookVec.z * 1.2 + (Math.random() - 0.5) * 0.3;

            // 生成黑色大粒子（使用更大的烟幕粒子）
            level.addParticle(ParticleTypes.LARGE_SMOKE, // 使用大烟幕粒子
                    muzzlePos.x + offsetX,
                    muzzlePos.y + offsetY,
                    muzzlePos.z + offsetZ,
                    velocityX, velocityY, velocityZ);

            // 生成更大的黑色粒子（使用龙息粒子）- 增加频率
            if (i % 2 == 0) { // 从每3个改为每2个
                level.addParticle(ParticleTypes.DRAGON_BREATH,
                        muzzlePos.x + offsetX * 0.8,
                        muzzlePos.y + offsetY * 0.8,
                        muzzlePos.z + offsetZ * 0.8,
                        velocityX * 0.8, velocityY * 0.8, velocityZ * 0.8);
            }

            // 生成暗影火花粒子 - 增加频率
            if (i % 3 == 0) { // 从每5个改为每3个
                level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        muzzlePos.x + offsetX * 0.6,
                        muzzlePos.y + offsetY * 0.6,
                        muzzlePos.z + offsetZ * 0.6,
                        velocityX * 0.6, velocityY * 0.6, velocityZ * 0.6);
            }

            // 添加新的暗影爆炸粒子效果
            if (i % 4 == 0) {
                level.addParticle(ParticleTypes.FLAME,
                        muzzlePos.x + offsetX * 0.4,
                        muzzlePos.y + offsetY * 0.4,
                        muzzlePos.z + offsetZ * 0.4,
                        velocityX * 0.4, velocityY * 0.4, velocityZ * 0.4);
            }
        }
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