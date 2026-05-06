package cn.dawnstring.fatality.items.weapon.magic;

import cn.dawnstring.fatality.entity.projectile.StarProjectile;
import cn.dawnstring.fatality.entity.projectile.TrackingStarProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import cn.dawnstring.fatality.system.ManaSystem;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;

/**
 * 星夜 - 魔法武器
 * 右键发射星星弹幕，星星弹幕有2种：
 * 1. 普通星星弹幕，直线飞出，速度快（60%概率）
 * 2. 特殊星星弹幕，能够追踪最近目标，速度稍慢（40%概率）
 * 伤害250 暴击率20 暴击伤害24 浮动0.4 攻击速度0.25s
 */
public class StarryNight extends BaseWeapon
{
    private static final Random random = new Random();
    
    // 弹幕概率配置
    private static final float NORMAL_STAR_PROBABILITY = 0.60f; // 60%普通星星
    private static final float TRACKING_STAR_PROBABILITY = 0.40f;
    private static final float MANA_COST = 5.0f;
    
    private static final float NORMAL_STAR_SPEED = 1.8f;
    private static final float TRACKING_STAR_SPEED = 1.2f; // 追踪星星速度
    
    public StarryNight() {
        super(new Tier() {
            @Override
            public int getUses() {
                return 0; // 无限耐久
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
                return 4; // 材料等级（钻石级）
            }

            @Override
            public int getEnchantmentValue() {
                return 0; // 不能附魔
            }

            @Override
            public Ingredient getRepairIngredient() {
                return null; // 不能修复
            }
        }, new Properties().stacksTo(1).fireResistant(),
              250, // 基础攻击伤害
              0.25f, // 攻击速度
              1.0f, // 基础伤害倍率
              0.2f, // 暴击率
              0.24f, // 暴击伤害倍率
              0.4f, // 伤害浮动
              WeaponEnum.MAGIC // 武器类型：魔法
        );
    }
    
    /**
     * 右键使用方法 - 发射星星弹幕
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        if (!level.isClientSide()) {
            if (!ManaSystem.safeConsumeMana(player, MANA_COST)) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§c魔力不足！"),
                        true
                );
                return InteractionResultHolder.fail(itemstack);
            }

            float calculatedDamage = calculateSpellDamage(player, itemstack);
            
            // 查找最近的敌人作为追踪目标
            LivingEntity target = findNearestTarget(player, level, 20.0f);
            
            // 根据概率决定发射哪种星星弹幕
            if (random.nextFloat() < NORMAL_STAR_PROBABILITY) {
                // 发射普通星星弹幕
                spawnNormalStar(level, player, itemstack, calculatedDamage);
            } else {
                // 发射追踪星星弹幕
                spawnTrackingStar(level, player, itemstack, calculatedDamage, target);
            }
            
            // 播放发射音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.0F);
            
            // 生成发射粒子效果
            spawnCastParticles(level, player);
            
            // 设置冷却时间
            player.getCooldowns().addCooldown(this, (int)(1f * 20)); // 转换为tick
        }
        
        return InteractionResultHolder.success(itemstack);
    }
    
    /**
     * 生成普通星星弹幕
     */
    private void spawnNormalStar(Level level, Player player, ItemStack weapon, float damage) {
        Vec3 lookVec = player.getLookAngle();
        Vec3 spawnPos = player.getEyePosition().add(lookVec.scale(1.5)); // 在玩家前方1.5格处生成
        
        // 创建星星弹幕
        StarProjectile star = new StarProjectile(level, player, weapon, damage);
        star.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        
        // 设置弹幕方向和速度
        star.shoot(lookVec.x, lookVec.y, lookVec.z, NORMAL_STAR_SPEED, 0.0f);
        
        // 添加到世界
        level.addFreshEntity(star);
    }
    
    /**
     * 生成追踪星星弹幕
     */
    private void spawnTrackingStar(Level level, Player player, ItemStack weapon, float damage, LivingEntity target) {
        Vec3 lookVec = player.getLookAngle();
        Vec3 spawnPos = player.getEyePosition().add(lookVec.scale(0.3)); // 在玩家前方0.3格处生成
        
        // 创建追踪星星弹幕
        TrackingStarProjectile trackingStar = new TrackingStarProjectile(level, player, target, damage);
        trackingStar.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        
        // 设置弹幕初始方向和速度
        trackingStar.shoot(lookVec.x, lookVec.y, lookVec.z, TRACKING_STAR_SPEED, 0.0f);
        
        // 添加到世界
        level.addFreshEntity(trackingStar);
    }
    
    /**
     * 计算法术伤害（基于BaseWeapon的伤害计算逻辑）
     */
    private float calculateSpellDamage(Player player, ItemStack stack) {
        // 使用BaseWeapon的伤害计算方法
        return calculateFinalDamage(player, stack, null);
    }
    
    /**
     * 查找最近的敌人作为追踪目标
     */
    private LivingEntity findNearestTarget(Player player, Level level, double range) {
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, 
                player.getBoundingBox().inflate(range));
        
        LivingEntity nearestTarget = null;
        double nearestDistance = Double.MAX_VALUE;
        
        for (LivingEntity entity : entities) {
            // 排除玩家自己和友好生物
            if (entity == player || entity.isAlliedTo(player)) {
                continue;
            }
            
            double distance = player.distanceToSqr(entity);
            if (distance < nearestDistance && distance <= range * range) {
                nearestTarget = entity;
                nearestDistance = distance;
            }
        }
        
        return nearestTarget;
    }
    
    /**
     * 生成施法粒子效果
     */
    private void spawnCastParticles(Level level, Player player) {
        if (level.isClientSide()) {
            Vec3 pos = player.getEyePosition();
            
            // 生成星光粒子效果
            for (int i = 0; i < 8; i++) {
                level.addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD,
                        pos.x + (random.nextDouble() - 0.5) * 1.0,
                        pos.y + (random.nextDouble() - 0.5) * 1.0,
                        pos.z + (random.nextDouble() - 0.5) * 1.0,
                        0, 0.1, 0);
            }
            
            // 生成闪烁粒子效果
            for (int i = 0; i < 5; i++) {
                level.addParticle(net.minecraft.core.particles.ParticleTypes.GLOW,
                        pos.x + (random.nextDouble() - 0.5) * 0.8,
                        pos.y + (random.nextDouble() - 0.5) * 0.8,
                        pos.z + (random.nextDouble() - 0.5) * 0.8,
                        0, 0.05, 0);
            }
        }
    }
}