package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.entity.projectile.WitherSpearProjectile;
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
 * 凋零长矛 - 近战武器
 * 特性：右键蓄力投掷，蓄力分为三级，每级增加伤害和粒子效果
 * 伤害745 暴击率15 暴击伤害20 浮动0.3 攻击速度1s
 */
public class WitherSpear extends BaseWeapon {
    
    private static final int COOLDOWN_TICKS = 20; // 冷却时间20tick（1秒）
    private static final int CHARGE_TIME_PER_LEVEL = 40; // 每级蓄力时间40tick（2秒）
    
    public WitherSpear() {
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
        }, new Properties().fireResistant(), 745, 1.0f, 1f, 0.15f, 1.20f, 0.3f, WeaponEnum.MELEE);
        
        setStory("凋零长矛，蕴含着凋零力量的强大武器。\n" +
                "右键蓄力投掷，蓄力时间越长伤害越高。\n" +
                "高级蓄力会在投掷路径上留下尾焰粒子，造成持续伤害。");
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
            generateCircleParticles(level, player, chargeLevel);
        }
    }
    
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player && !level.isClientSide()) {
            Player player = (Player) entity;
            
            // 计算蓄力等级和伤害倍数
            int chargeLevel = Math.min(2, timeCharged / CHARGE_TIME_PER_LEVEL);
            float damageMultiplier = getDamageMultiplierForChargeLevel(chargeLevel);
            
            // 计算最终伤害
            float spearDamage = calculateSpearDamage(player, stack) * damageMultiplier;
            
            // 创建长矛投射物
            WitherSpearProjectile spear = new WitherSpearProjectile(level, player, spearDamage, chargeLevel);
            spear.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            spear.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.0F, 1.0F);
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
     * 计算长矛伤害
     */
    public float calculateSpearDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }
    
    /**
     * 根据蓄力等级获取伤害倍数
     */
    private float getDamageMultiplierForChargeLevel(int chargeLevel) {
        switch (chargeLevel) {
            case 0: return 0.8f; // 一级蓄力：80%伤害
            case 1: return 1.0f; // 二级蓄力：100%伤害
            case 2: return 1.5f; // 三级蓄力：150%伤害
            default: return 1.0f;
        }
    }
    
    /**
     * 生成圆形粒子效果
     */
    private void generateCircleParticles(Level level, Player player, int chargeLevel) {
        if (level.isClientSide()) {
            return;
        }
        
        // 根据蓄力等级计算粒子半径
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
            
            level.addParticle(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                    centerX + offsetX, centerY, centerZ + offsetZ,
                    0, 0.1, 0);
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