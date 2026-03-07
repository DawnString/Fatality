package cn.dawnstring.fatality.items.weapon.ranged;

import cn.dawnstring.fatality.entity.projectile.BulletProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 血灵爆破者 - 吸血霰弹枪
 * 特性：发射8-12发子弹，击中目标后玩家恢复1点血量
 */
public class BloodSpiritBlaster extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 25; // 冷却时间1.25秒
    private static final float BASE_BULLET_DAMAGE = 80.0f; // 单发子弹基础伤害
    private static final int MAX_PELLET_COUNT = 12; // 最大弹丸数量
    private static final int MIN_PELLET_COUNT = 8; // 最小弹丸数量
    private static final float MAX_SPREAD_ANGLE = 25.0f; // 最大散射角度
    private static final float MIN_SPREAD_ANGLE = 15.0f; // 最小散射角度
    private static final float HEAL_AMOUNT = 1.0f; // 每次命中恢复的血量

    public BloodSpiritBlaster()
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
        }, new Properties(), 0, 1.0f, 1f, 0.08f, 0.12f, 0.25f, WeaponEnum.RANGED);
        
        setStory("一把充满血灵力量的霰弹枪，每次命中敌人都能为使用者恢复生命值。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 随机弹丸数量
            int pelletCount = getRandomPelletCount();
            
            // 随机散射角度
            float spreadAngle = getRandomSpreadAngle();
            
            // 计算单发子弹伤害
            float bulletDamage = calculateBulletDamage(player, itemstack);

            // 发射多发弹丸
            for (int i = 0; i < pelletCount; i++)
            {
                // 创建子弹投射物
                BulletProjectile bullet = new BulletProjectile(level, player, itemstack, bulletDamage);

                Vec3 lookVec = player.getLookAngle();
                Vec3 eyePos = player.getEyePosition();
                
                // 计算锥形散布方向
                Vec3 spreadVec = calculateConeSpreadDirection(lookVec, i, pelletCount, spreadAngle);
                
                bullet.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
                bullet.shoot(spreadVec.x, spreadVec.y, spreadVec.z, 3.5F, 0.15F); // 中等速度，中等散布
                
                level.addFreshEntity(bullet);
            }
            
            // 播放血灵爆破音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        // 在客户端生成血灵粒子效果
        if (level.isClientSide()) {
            spawnBloodSpiritParticles(level, player);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 生成血灵粒子效果
     */
    private void spawnBloodSpiritParticles(Level level, Player player) {
        Vec3 lookVec = player.getLookAngle();
        Vec3 eyePos = player.getEyePosition();
        
        // 枪口位置
        Vec3 muzzlePos = eyePos.add(lookVec.scale(1.5));
        
        // 生成血灵特有的红色粒子效果
        for (int i = 0; i < 25; i++) {
            double offsetX = (Math.random() - 0.5) * 0.8;
            double offsetY = (Math.random() - 0.5) * 0.8;
            double offsetZ = (Math.random() - 0.5) * 0.8;
            
            // 生成红色火焰粒子
            level.addParticle(ParticleTypes.FLAME,
                    muzzlePos.x + offsetX,
                    muzzlePos.y + offsetY,
                    muzzlePos.z + offsetZ,
                    lookVec.x * 0.3 + (Math.random() - 0.5) * 0.2,
                    lookVec.y * 0.3 + (Math.random() - 0.5) * 0.2,
                    lookVec.z * 0.3 + (Math.random() - 0.5) * 0.2);
            
            // 生成血滴粒子
            if (i % 3 == 0) {
                level.addParticle(ParticleTypes.DRIPPING_LAVA,
                        muzzlePos.x + offsetX * 0.5,
                        muzzlePos.y + offsetY * 0.5,
                        muzzlePos.z + offsetZ * 0.5,
                        (Math.random() - 0.5) * 0.1,
                        Math.random() * 0.1,
                        (Math.random() - 0.5) * 0.1);
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

    /**
     * 计算锥形散布方向
     */
    private Vec3 calculateConeSpreadDirection(Vec3 baseDirection, int pelletIndex, int totalPellets, float spreadAngle) {
        double spreadRad = Math.toRadians(spreadAngle);
        double angleStep = 2 * Math.PI / totalPellets;
        double angle = pelletIndex * angleStep;
        double radius = Math.random() * spreadRad;
        
        double horizontalOffset = radius * Math.cos(angle);
        double verticalOffset = radius * Math.sin(angle);
        
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = baseDirection.cross(up).normalize();
        Vec3 forward = baseDirection.normalize();
        
        Vec3 spreadVec = forward
                .add(right.scale(horizontalOffset))
                .add(up.scale(verticalOffset))
                .normalize();
        
        return spreadVec;
    }

    /**
     * 计算子弹伤害
     */
    public float calculateBulletDamage(Player player, ItemStack stack) {
        float baseDamage = BASE_BULLET_DAMAGE;
        float accessoryBaseBonus = calculateAccessoryBaseBonus(player);
        float otherBonus = calculateOtherBonus(player);
        float fluctuation = calculateDamageFluctuation();
        boolean isCritical = isCriticalHit(player);

        float finalDamage;
        if (isCritical) {
            float criticalBonus = getCriticalDamageMultiplier(player);
            finalDamage = baseDamage * accessoryBaseBonus * otherBonus * 0.8f * criticalBonus * fluctuation;
        } else {
            finalDamage = baseDamage * accessoryBaseBonus * otherBonus * 0.9f * fluctuation;
        }

        return Math.max(0, finalDamage);
    }

    /**
     * 子弹击中目标后的吸血效果
     */
    public static void onBulletHit(LivingEntity target, Level level, Player player) {
        if (!level.isClientSide()) {
            // 玩家恢复1点血量
            float currentHealth = player.getHealth();
            float maxHealth = player.getMaxHealth();
            
            if (currentHealth < maxHealth) {
                player.setHealth(Math.min(maxHealth, currentHealth + HEAL_AMOUNT));
                
                // 生成吸血粒子效果
                spawnHealParticles(level, player);
                
                // 播放吸血音效
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.3F, 1.5F);
            }
        }
    }

    /**
     * 生成吸血粒子效果
     */
    private static void spawnHealParticles(Level level, Player player) {
        if (level.isClientSide()) {
            // 在玩家周围生成治疗粒子
            for (int i = 0; i < 10; i++) {
                double offsetX = (Math.random() - 0.5) * 2.0;
                double offsetY = Math.random() * 2.0;
                double offsetZ = (Math.random() - 0.5) * 2.0;
                
                level.addParticle(ParticleTypes.HEART,
                        player.getX() + offsetX,
                        player.getY() + offsetY,
                        player.getZ() + offsetZ,
                        0, 0.1, 0);
            }
        }
    }
}