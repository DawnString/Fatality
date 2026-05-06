package cn.dawnstring.fatality.items.weapon.ranged;

import cn.dawnstring.fatality.entity.projectile.TrackingBulletProjectile;
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
 * 血灵流动之光 - 吸血追踪狙击枪
 * 特性：右键攻击，子弹能够追踪最近的目标，恢复造成伤害的1%的血量
 */
public class BloodSpiritFlowingLight extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 40; // 冷却时间（2秒）
    private static final float BASE_DAMAGE = 50.0f; // 基础伤害
    private static final float MAX_RANGE = 30.0f; // 最大追踪距离

    public BloodSpiritFlowingLight()
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
        }, new Properties(), (int)BASE_DAMAGE, 0.8f, 1.2f, 0.15f, 0.1f, 0.12f, WeaponEnum.RANGED);
        
        setStory("一把蕴含血灵力量的追踪狙击枪，能够自动锁定目标并吸取敌人的生命力来治疗使用者。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 寻找最近的敌人
            LivingEntity target = findNearestTarget(level, player);
            
            if (target != null) {
                // 发射追踪子弹
                shootTrackingBullet(level, player, target);
                
                // 播放射击音效
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 0.8F, 0.6F);
            } else {
                // 没有找到目标，播放失败音效
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5F, 1.5F);
            }
        }

        // 在客户端生成射击粒子效果
        if (level.isClientSide()) {
            spawnShootParticles(level, player);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 寻找最近的敌人
     */
    private LivingEntity findNearestTarget(Level level, Player player) {
        LivingEntity nearestTarget = null;
        double nearestDistance = MAX_RANGE;
        
        // 获取玩家周围的所有实体
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, 
                player.getBoundingBox().inflate(MAX_RANGE))) {
            if (entity != player && entity.isAlive() && !entity.isInvulnerable()) {
                double distance = player.distanceTo(entity);
                if (distance < nearestDistance) {
                    nearestTarget = entity;
                    nearestDistance = distance;
                }
            }
        }
        
        return nearestTarget;
    }

    /**
     * 发射追踪子弹
     */
    private void shootTrackingBullet(Level level, Player player, LivingEntity target) {
        // 计算子弹伤害
        float damage = calculateBulletDamage(player);
        
        // 创建追踪子弹实体
        TrackingBulletProjectile bullet = new TrackingBulletProjectile(level, player, target, damage);
        
        // 设置子弹位置和方向
        Vec3 playerLook = player.getLookAngle();
        Vec3 spawnPos = player.getEyePosition().add(playerLook.scale(1.0));
        
        bullet.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        bullet.shoot(playerLook.x, playerLook.y, playerLook.z, 2.0f, 0.0f);
        
        // 添加子弹到世界
        level.addFreshEntity(bullet);
    }

    private float calculateBulletDamage(Player player) {
        return calculateFinalDamage(player, player.getMainHandItem(), null);
    }

    /**
     * 生成射击粒子效果
     */
    private void spawnShootParticles(Level level, Player player) {
        Vec3 playerPos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        
        // 沿着射击方向生成粒子
        for (int i = 0; i < 10; i++) {
            double progress = (double) i / 10.0 * 3.0;
            Vec3 particlePos = playerPos.add(lookVec.scale(progress));
            
            // 生成蓝色追踪粒子
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    particlePos.x,
                    particlePos.y,
                    particlePos.z,
                    (Math.random() - 0.5) * 0.05,
                    (Math.random() - 0.5) * 0.05,
                    (Math.random() - 0.5) * 0.05);
            
            // 生成红色血灵粒子
            if (i % 3 == 0) {
                level.addParticle(ParticleTypes.DRIPPING_LAVA,
                        particlePos.x,
                        particlePos.y,
                        particlePos.z,
                        lookVec.x * 0.1,
                        lookVec.y * 0.1,
                        lookVec.z * 0.1);
            }
        }
    }
}