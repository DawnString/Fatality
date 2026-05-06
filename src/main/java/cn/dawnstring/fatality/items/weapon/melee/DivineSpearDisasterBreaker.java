package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.entity.projectile.DivineSpearDisasterBreakerProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

/**
 * 神枪-破灾 - 近战武器
 * 特性：矛类，右键投掷，投掷后直线飞行，不受重力影响。
 * 右键有三段蓄力，每段蓄力需要2s，每到达一段蓄力时玩家周边出现不同颜色粒子效果
 */
public class DivineSpearDisasterBreaker extends BaseWeapon {
    
    private static final int COOLDOWN_TICKS = 20; // 冷却时间20tick（1秒）
    private static final int CHARGE_TIME_PER_LEVEL = 40; // 每级蓄力时间40tick（2秒）
    
    // 三段蓄力的属性配置
    private static final int[] DAMAGE_LEVELS = {3600, 3700, 3800};
    private static final float[] CRITICAL_CHANCE_LEVELS = {0.20f, 0.34f, 0.42f};
    private static final float[] CRITICAL_DAMAGE_LEVELS = {1.30f, 1.40f, 1.50f};
    private static final int[] SPLIT_DAMAGE_LEVELS = {3500, 3500, 3600};
    
    public DivineSpearDisasterBreaker() {
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
        }, new Properties().fireResistant(), 3600, 1.0f, 1f, 0.20f, 1.30f, 0.3f, WeaponEnum.MELEE);
        
        setStory("神枪-破灾，蕴含着神圣力量的终极武器。\n" +
                "右键蓄力投掷，三段蓄力带来不同的威力效果。\n" +
                "高级蓄力会在命中后分裂并引发爆炸，摧毁一切灾厄。");
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        // 开始蓄力
        player.startUsingItem(hand);
        
        return InteractionResultHolder.consume(itemstack);
    }
    
    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (entity instanceof Player && !level.isClientSide()) {
            Player player = (Player) entity;
            
            // 计算蓄力等级（0-2级）
            int useDuration = getUseDuration(stack) - remainingUseDuration;
            int chargeLevel = Math.min(2, useDuration / CHARGE_TIME_PER_LEVEL);
            
            // 生成圆形粒子效果
            generateChargeParticles(level, player, chargeLevel);
        }
    }
    
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player && !level.isClientSide()) {
            Player player = (Player) entity;
            
            // 计算蓄力等级
            int chargeLevel = Math.min(2, timeCharged / CHARGE_TIME_PER_LEVEL);
            
            // 根据蓄力等级获取属性
            int damage = DAMAGE_LEVELS[chargeLevel];
            float criticalChance = CRITICAL_CHANCE_LEVELS[chargeLevel];
            float criticalDamage = CRITICAL_DAMAGE_LEVELS[chargeLevel];
            int splitDamage = SPLIT_DAMAGE_LEVELS[chargeLevel];
            
            // 计算最终伤害（考虑玩家属性加成）
            float finalDamage = calculateSpearDamage(player, stack, damage);
            
            // 创建神枪投射物
            DivineSpearDisasterBreakerProjectile spear = new DivineSpearDisasterBreakerProjectile(
                    level, player, finalDamage, chargeLevel, splitDamage);
            spear.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            spear.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 1.0F);
            level.addFreshEntity(spear);
            
            // 播放投掷音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
            
            // 统计使用次数
            player.awardStat(Stats.ITEM_USED.get(this));
            
            // 设置冷却时间
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        }
    }
    
    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000; // 最大使用时间（足够长）
    }
    
    /**
     * 计算神枪伤害
     */
    public float calculateSpearDamage(Player player, ItemStack stack, int baseDamage) {
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
     * 生成蓄力粒子效果
     */
    private void generateChargeParticles(Level level, Player player, int chargeLevel) {
        if (!level.isClientSide()) {
            return;
        }
        
        // 根据蓄力等级选择粒子颜色
        String particleType;
        switch (chargeLevel) {
            case 0: particleType = "SOUL_FIRE_FLAME"; break; // 暗黄色
            case 1: particleType = "FLAME"; break;           // 黄色
            case 2: particleType = "GLOW"; break;            // 金色
            default: particleType = "SOUL_FIRE_FLAME";
        }
        
        // 根据蓄力等级计算粒子半径和数量
        float radius = 1.0f + chargeLevel * 0.5f;
        int particleCount = 8 + chargeLevel * 4;
        
        // 玩家前方位置
        double playerX = player.getX();
        double playerY = player.getEyeY();
        double playerZ = player.getZ();
        
        // 玩家视线方向
        float yaw = player.getYRot() * ((float)Math.PI / 180F);
        float pitch = player.getXRot() * ((float)Math.PI / 180F);
        
        // 计算前方位置（距离玩家2格）
        double forwardX = -Math.sin(yaw) * Math.cos(pitch) * 2.0;
        double forwardY = -Math.sin(pitch) * 2.0;
        double forwardZ = Math.cos(yaw) * Math.cos(pitch) * 2.0;
        
        double centerX = playerX + forwardX;
        double centerY = playerY + forwardY;
        double centerZ = playerZ + forwardZ;
        
        // 生成圆形粒子
        for (int i = 0; i < particleCount; i++) {
            double angle = 2 * Math.PI * i / particleCount;
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;
            
            // 根据粒子类型生成不同的粒子
            switch (particleType) {
                case "SOUL_FIRE_FLAME":
                    level.addParticle(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                            centerX + offsetX, centerY, centerZ + offsetZ,
                            0, 0.1, 0);
                    break;
                case "FLAME":
                    level.addParticle(net.minecraft.core.particles.ParticleTypes.FLAME,
                            centerX + offsetX, centerY, centerZ + offsetZ,
                            0, 0.1, 0);
                    break;
                case "GLOW":
                    level.addParticle(net.minecraft.core.particles.ParticleTypes.GLOW,
                            centerX + offsetX, centerY, centerZ + offsetZ,
                            0, 0.1, 0);
                    break;
            }
        }
    }
    
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // 播放命中音效
        if (attacker instanceof Player) {
            Player player = (Player) attacker;
            player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.NEUTRAL, 0.8F, 1.0F);
        }
        
        return super.hurtEnemy(stack, target, attacker);
    }
}