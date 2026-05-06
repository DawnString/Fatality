package cn.dawnstring.fatality.items.weapon.ranged;

import cn.dawnstring.fatality.entity.projectile.BulletProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import cn.dawnstring.fatality.registry.ModEffects;
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
import java.util.List;

/**
 * 龙焰喷射器 - 发射龙炎子弹的霰弹枪
 * 特性：发射10-16发龙炎子弹，目标获得龙炎燃烧效果，枪口前的实体燃烧
 */
public class DragonFlameBlaster extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 20; // 冷却时间1秒
    private static final float BASE_BULLET_DAMAGE = 582.0f; // 单发子弹基础伤害
    private static final int MAX_PELLET_COUNT = 16; // 最大弹丸数量
    private static final int MIN_PELLET_COUNT = 10; // 最小弹丸数量
    private static final float MAX_SPREAD_ANGLE = 20.0f; // 最大散射角度
    private static final float MIN_SPREAD_ANGLE = 10.0f; // 最小散射角度
    private static final int DRAGONFIRE_DURATION = 100; // 龙炎燃烧持续时间（5秒）

    public DragonFlameBlaster()
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
        }, new Properties().fireResistant(), (int)BASE_BULLET_DAMAGE, 1.0f, 1f, 0.15f, 0.15f, 0.3f, WeaponEnum.RANGED);
        
        setStory("龙焰喷射器，蕴含着强大龙炎力量的霰弹武器。\n" +
                "发射10-16发龙炎子弹，被命中的目标将遭受龙炎燃烧之苦，\n" +
                "枪口前的实体也会被龙炎点燃。");
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
                bullet.shoot(spreadVec.x, spreadVec.y, spreadVec.z, 3.5F, 0.1F); // 中等速度，小散布
                
                level.addFreshEntity(bullet);
            }
            
            // 枪口前实体燃烧效果
            burnEntitiesInFront(player, level);
            
            // 播放龙焰喷射音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.2F, 0.8F);
        }

        // 在客户端生成龙焰粒子效果
        if (level.isClientSide()) {
            spawnDragonFlameParticles(level, player);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 枪口前实体燃烧效果
     */
    private void burnEntitiesInFront(Player player, Level level) {
        Vec3 lookVec = player.getLookAngle();
        Vec3 eyePos = player.getEyePosition();
        
        // 枪口前方扇形区域检测
        double range = 5.0; // 检测范围5格
        double angle = Math.toRadians(30); // 扇形角度30度
        
        // 获取枪口前方扇形区域内的所有实体
        List<LivingEntity> entitiesInFront = level.getEntitiesOfClass(LivingEntity.class, 
                player.getBoundingBox().expandTowards(lookVec.scale(range)).inflate(range));
        
        for (LivingEntity entity : entitiesInFront) {
            if (entity != player && !entity.isAlliedTo(player)) {
                // 检查实体是否在扇形区域内
                Vec3 toEntity = entity.position().subtract(eyePos);
                double dotProduct = lookVec.dot(toEntity.normalize());
                double entityAngle = Math.acos(dotProduct);
                
                if (entityAngle <= angle && toEntity.length() <= range) {
                    // 对实体施加燃烧效果
                    entity.setSecondsOnFire(3); // 燃烧3秒
                    
                    // 播放燃烧音效
                    level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                            SoundEvents.BLAZE_BURN, SoundSource.NEUTRAL, 0.6F, 1.0F);
                }
            }
        }
    }

    /**
     * 生成龙焰粒子效果
     */
    private void spawnDragonFlameParticles(Level level, Player player) {
        Vec3 lookVec = player.getLookAngle();
        Vec3 eyePos = player.getEyePosition();
        
        // 枪口位置
        Vec3 muzzlePos = eyePos.add(lookVec.scale(1.5));
        
        // 生成龙焰特有的火焰粒子效果
        for (int i = 0; i < 30; i++) {
            double offsetX = (Math.random() - 0.5) * 1.0;
            double offsetY = (Math.random() - 0.5) * 1.0;
            double offsetZ = (Math.random() - 0.5) * 1.0;
            
            // 生成橙色火焰粒子（龙焰颜色）
            level.addParticle(ParticleTypes.FLAME,
                    muzzlePos.x + offsetX,
                    muzzlePos.y + offsetY,
                    muzzlePos.z + offsetZ,
                    lookVec.x * 0.5 + (Math.random() - 0.5) * 0.3,
                    lookVec.y * 0.5 + (Math.random() - 0.5) * 0.3,
                    lookVec.z * 0.5 + (Math.random() - 0.5) * 0.3);
            
            // 生成烟雾粒子
            if (i % 2 == 0) {
                level.addParticle(ParticleTypes.LARGE_SMOKE,
                        muzzlePos.x + offsetX * 0.7,
                        muzzlePos.y + offsetY * 0.7,
                        muzzlePos.z + offsetZ * 0.7,
                        (Math.random() - 0.5) * 0.2,
                        Math.random() * 0.1,
                        (Math.random() - 0.5) * 0.2);
            }
            
            // 生成火花粒子
            if (i % 3 == 0) {
                level.addParticle(ParticleTypes.FLASH,
                        muzzlePos.x + offsetX * 0.3,
                        muzzlePos.y + offsetY * 0.3,
                        muzzlePos.z + offsetZ * 0.3,
                        0, 0, 0);
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

    /**
     * 子弹击中目标后的龙炎燃烧效果
     */
    public static void onBulletHit(LivingEntity target, Level level, Player player) {
        if (!level.isClientSide()) {
            // 对目标施加龙炎燃烧效果
            applyDragonfireBurnEffect(target, level);
            
            // 播放龙炎燃烧音效
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.BLAZE_BURN, SoundSource.NEUTRAL, 0.8F, 1.2F);
        }
    }

    /**
     * 施加龙炎燃烧效果
     */
    private static void applyDragonfireBurnEffect(LivingEntity target, Level level) {
        if (ModEffects.DRAGONFIRE_BURN != null) {
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    ModEffects.DRAGONFIRE_BURN.get(), DRAGONFIRE_DURATION, 0));
            
            // 生成龙炎燃烧粒子效果
            if (level.isClientSide()) {
                spawnDragonfireBurnParticles(level, target);
            }
        }
    }

    /**
     * 生成龙炎燃烧粒子效果
     */
    private static void spawnDragonfireBurnParticles(Level level, LivingEntity target) {
        // 在目标周围生成龙炎燃烧粒子
        for (int i = 0; i < 15; i++) {
            double offsetX = (Math.random() - 0.5) * 1.5;
            double offsetY = Math.random() * 2.0;
            double offsetZ = (Math.random() - 0.5) * 1.5;
            
            level.addParticle(ParticleTypes.FLAME,
                    target.getX() + offsetX,
                    target.getY() + offsetY,
                    target.getZ() + offsetZ,
                    (Math.random() - 0.5) * 0.1,
                    Math.random() * 0.2,
                    (Math.random() - 0.5) * 0.1);
        }
    }
}