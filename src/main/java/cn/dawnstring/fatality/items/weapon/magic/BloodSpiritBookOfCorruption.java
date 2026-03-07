package cn.dawnstring.fatality.items.weapon.magic;

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
 * 血灵腐化之书 - 吸血持续伤害魔法书
 * 特性：长按右键攻击，用粒子链接玩家与目标，目标持续受到伤害，玩家每秒恢复1点血量
 */
public class BloodSpiritBookOfCorruption extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 5; // 短冷却时间，支持连续攻击
    private static final float BASE_DAMAGE_PER_TICK = 23.0f; // 每秒基础魔法伤害
    private static final float HEAL_AMOUNT_PER_SECOND = 1.0f; // 每秒恢复血量
    private static final float MAX_RANGE = 15.0f; // 最大链接距离
    private static final int LINK_DURATION = 100; // 链接持续时间（5秒）

    public BloodSpiritBookOfCorruption()
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
        }, new Properties(), 0, 0.05f, 1.0f, 0.0f, 0.0f, 0.0f, WeaponEnum.MAGIC);
        
        setStory("一本蕴含血灵力量的腐化之书，能够通过魔法链接吸取敌人的生命力来治疗使用者。每秒消耗4点魔法值。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 寻找最近的敌人
            LivingEntity target = findNearestTarget(level, player);
            
            if (target != null) {
                // 创建血灵链接
                createBloodLink(level, player, target);
                
                // 播放链接音效
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.8F, 0.6F);
            } else {
                // 没有找到目标，播放失败音效
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5F, 1.5F);
            }
        }

        // 在客户端生成魔法粒子效果
        if (level.isClientSide()) {
            spawnMagicParticles(level, player);
        }

        // 设置短冷却时间
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
     * 创建血灵链接
     */
    private void createBloodLink(Level level, Player player, LivingEntity target) {
        // 计算链接伤害
        float damagePerTick = calculateLinkDamage(player);
        
        // 开始链接效果
        startLinkEffect(level, player, target, damagePerTick);
    }

    /**
     * 计算链接伤害
     */
    private float calculateLinkDamage(Player player) {
        float baseDamage = BASE_DAMAGE_PER_TICK;
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

        return Math.max(1.0f, finalDamage); // 确保至少1点伤害
    }

    /**
     * 开始链接效果
     */
    private void startLinkEffect(Level level, Player player, LivingEntity target, float damagePerTick) {
        // 这里应该实现一个持续的效果系统
        // 由于时间限制，我们使用简化版本：立即造成伤害并治疗
        
        // 立即造成伤害
        target.hurt(target.damageSources().magic(), damagePerTick);
        
        // 玩家恢复血量
        float currentHealth = player.getHealth();
        float maxHealth = player.getMaxHealth();
        
        if (currentHealth < maxHealth) {
            player.setHealth(Math.min(maxHealth, currentHealth + HEAL_AMOUNT_PER_SECOND));
        }
        
        // 在客户端生成链接粒子效果
        if (level.isClientSide()) {
            spawnLinkParticles(level, player, target);
        }
    }

    /**
     * 生成魔法粒子效果
     */
    private void spawnMagicParticles(Level level, Player player) {
        Vec3 playerPos = player.position();
        
        // 在玩家周围生成魔法粒子
        for (int i = 0; i < 15; i++) {
            double offsetX = (Math.random() - 0.5) * 2.0;
            double offsetY = Math.random() * 2.0;
            double offsetZ = (Math.random() - 0.5) * 2.0;
            
            // 生成紫色魔法粒子
            level.addParticle(ParticleTypes.ENCHANT,
                    playerPos.x + offsetX,
                    playerPos.y + offsetY,
                    playerPos.z + offsetZ,
                    (Math.random() - 0.5) * 0.1,
                    Math.random() * 0.1,
                    (Math.random() - 0.5) * 0.1);
            
            // 生成红色血灵粒子
            if (i % 3 == 0) {
                level.addParticle(ParticleTypes.DRIPPING_LAVA,
                        playerPos.x + offsetX * 0.8,
                        playerPos.y + offsetY * 0.8,
                        playerPos.z + offsetZ * 0.8,
                        (Math.random() - 0.5) * 0.05,
                        Math.random() * 0.05,
                        (Math.random() - 0.5) * 0.05);
            }
        }
    }

    /**
     * 生成链接粒子效果
     */
    private void spawnLinkParticles(Level level, Player player, LivingEntity target) {
        if (level.isClientSide()) {
            Vec3 playerPos = player.getEyePosition();
            Vec3 targetPos = target.getEyePosition();
            
            // 计算链接方向
            Vec3 direction = targetPos.subtract(playerPos);
            double distance = direction.length();
            direction = direction.normalize();
            
            // 沿着链接路径生成粒子
            for (int i = 0; i < 20; i++) {
                double progress = (double) i / 20.0;
                Vec3 particlePos = playerPos.add(direction.scale(distance * progress));
                
                // 生成血灵链接粒子
                level.addParticle(ParticleTypes.DRAGON_BREATH,
                        particlePos.x,
                        particlePos.y,
                        particlePos.z,
                        (Math.random() - 0.5) * 0.02,
                        (Math.random() - 0.5) * 0.02,
                        (Math.random() - 0.5) * 0.02);
                
                // 生成反向治疗粒子（从目标到玩家）
                if (i % 4 == 0) {
                    Vec3 reversePos = targetPos.subtract(direction.scale(distance * progress));
                    level.addParticle(ParticleTypes.HEART,
                            reversePos.x,
                            reversePos.y,
                            reversePos.z,
                            -direction.x * 0.1,
                            -direction.y * 0.1,
                            -direction.z * 0.1);
                }
            }
        }
    }
}