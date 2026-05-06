package cn.dawnstring.fatality.items.weapon.magic;

import cn.dawnstring.fatality.entity.projectile.DecayOrbProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import cn.dawnstring.fatality.system.ManaSystem;
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
 * 衰败 - 魔法武器
 * 特性：右键蓄力生成球体，蓄力分为三级，每级增加伤害和球体大小
 * 伤害748 暴击率15 暴击伤害20 浮动0.4 攻击速度1s
 */
public class Decay extends BaseWeapon {
    
    private static final int COOLDOWN_TICKS = 20;
    private static final float MANA_COST = 8.0f;
    private static final int CHARGE_TIME_PER_LEVEL = 40;
    
    public Decay() {
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
        }, new Properties().fireResistant(), 748, 1.0f, 1f, 0.15f, 1.20f, 0.4f, WeaponEnum.MAGIC);
        
        setStory("衰败，蕴含着腐朽力量的魔法武器。\n" +
                "右键蓄力生成衰败球体，蓄力时间越长伤害越高。\n" +
                "球体飞行时会持续释放衰败能量，对周围造成伤害。");
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
            
            // 生成球体粒子效果
            generateOrbParticles(level, player, chargeLevel);
        }
    }
    
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player && !level.isClientSide()) {
            Player player = (Player) entity;
            
            if (!ManaSystem.safeConsumeMana(player, MANA_COST)) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§c魔力不足！"),
                        true
                );
                return;
            }
            
            int chargeLevel = Math.min(2, timeCharged / CHARGE_TIME_PER_LEVEL);
            float damageMultiplier = getDamageMultiplierForChargeLevel(chargeLevel);
            
            // 计算最终伤害
            float orbDamage = calculateOrbDamage(player, stack) * damageMultiplier;
            
            // 创建衰败球体投射物
            DecayOrbProjectile orb = new DecayOrbProjectile(level, player, orbDamage, chargeLevel);
            orb.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            orb.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(orb);
            
            // 播放释放音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.0F, 0.8F);
            
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
     * 计算球体伤害
     */
    public float calculateOrbDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }
    
    /**
     * 根据蓄力等级获取伤害倍数
     */
    private float getDamageMultiplierForChargeLevel(int chargeLevel) {
        switch (chargeLevel) {
            case 0: return 0.6f; // 一级蓄力：60%伤害
            case 1: return 1.0f; // 二级蓄力：100%伤害
            case 2: return 1.8f; // 三级蓄力：180%伤害
            default: return 1.0f;
        }
    }
    
    /**
     * 生成球体粒子效果
     */
    private void generateOrbParticles(Level level, Player player, int chargeLevel) {
        if (level.isClientSide()) {
            return;
        }
        
        // 根据蓄力等级计算球体半径
        float radius = 0.5f + chargeLevel * 0.3f;
        int particleCount = 12 + chargeLevel * 6;
        
        // 玩家前方位置
        double playerX = player.getX();
        double playerY = player.getEyeY();
        double playerZ = player.getZ();
        
        // 玩家视线方向
        float yaw = player.getYRot() * ((float)Math.PI / 180F);
        float pitch = player.getXRot() * ((float)Math.PI / 180F);
        
        // 计算前方位置（距离玩家1.5格）
        double forwardX = -Math.sin(yaw) * Math.cos(pitch) * 1.5;
        double forwardY = -Math.sin(pitch) * 1.5;
        double forwardZ = Math.cos(yaw) * Math.cos(pitch) * 1.5;
        
        double centerX = playerX + forwardX;
        double centerY = playerY + forwardY;
        double centerZ = playerZ + forwardZ;
        
        // 生成球体粒子
        for (int i = 0; i < particleCount; i++) {
            // 球面坐标
            double phi = Math.acos(-1.0 + 2.0 * i / particleCount);
            double theta = Math.sqrt(particleCount * Math.PI) * phi;
            
            double x = radius * Math.sin(phi) * Math.cos(theta);
            double y = radius * Math.sin(phi) * Math.sin(theta);
            double z = radius * Math.cos(phi);
            
            level.addParticle(net.minecraft.core.particles.ParticleTypes.DRAGON_BREATH,
                    centerX + x, centerY + y, centerZ + z,
                    0, 0.05, 0);
        }
    }
    
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // 播放命中音效
        if (attacker instanceof Player) {
            Player player = (Player) attacker;
            player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.WITHER_HURT, SoundSource.NEUTRAL, 0.6F, 1.0F);
        }
        
        return super.hurtEnemy(stack, target, attacker);
    }
}