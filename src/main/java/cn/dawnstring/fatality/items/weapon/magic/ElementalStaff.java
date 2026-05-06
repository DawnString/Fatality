package cn.dawnstring.fatality.items.weapon.magic;

import cn.dawnstring.fatality.entity.projectile.ElementalBarrageProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import cn.dawnstring.fatality.system.ManaSystem;
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

import java.util.Random;

/**
 * 元素法杖 - 魔法武器
 * 特性：右键时从玩家附近召唤3-6个元素弹幕，弹幕能够追踪最近的目标
 * 伤害640 暴击率28 暴击伤害36 浮动0.3 攻击速度0.2s
 */
public class ElementalStaff extends BaseWeapon
{
    private static final Random random = new Random();
    
    // 魔法消耗配置
    private static final float MANA_COST_PER_ATTACK = 12.0f; // 每次攻击消耗12点魔法值
    private static final int ATTACK_COOLDOWN_TICKS = 4; // 攻击冷却时间4tick（0.2秒）
    
    // 弹幕数量配置
    private static final int MIN_BARRAGE_COUNT = 3; // 最少弹幕数量
    private static final int MAX_BARRAGE_COUNT = 6; // 最多弹幕数量
    
    // 基础魔法伤害
    private static final float BASE_MAGIC_DAMAGE = 640.0f;

    public ElementalStaff() {
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
                return 0; // 法杖本身没有攻击伤害加成
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
              (int)BASE_MAGIC_DAMAGE,
              0.2f,
              1.0f,
              0.28f,
              0.36f,
              0.3f,
              WeaponEnum.MAGIC);
        
        setStory("元素法杖，蕴含着四大元素的神秘力量。\n" +
                "右键召唤元素弹幕，弹幕会自动追踪最近的敌人，\n" +
                "造成强大的元素伤害。");
    }
    
    /**
     * 右键使用方法 - 召唤元素弹幕
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        // 检查攻击冷却时间
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(itemstack);
        }

        // 检查魔法值是否足够
        if (!ManaSystem.hasEnoughMana(player, MANA_COST_PER_ATTACK)) {
            if (level.isClientSide()) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c魔法值不足！"), true);
            }
            return InteractionResultHolder.fail(itemstack);
        }

        // 消耗魔法值
        ManaSystem.consumeMana(player, MANA_COST_PER_ATTACK);

        // 设置攻击冷却时间
        player.getCooldowns().addCooldown(this, ATTACK_COOLDOWN_TICKS);

        // 召唤元素弹幕
        if (!level.isClientSide()) {
            performElementalBarrageAttack(level, player, itemstack);
        }

        // 播放元素法杖施法音效
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.0F);

        return InteractionResultHolder.consume(itemstack);
    }
    
    /**
     * 执行元素弹幕攻击
     */
    private void performElementalBarrageAttack(Level level, Player player, ItemStack itemstack) {
        // 计算元素伤害
        float elementalDamage = calculateFinalDamage(player, itemstack, null);
        
        // 随机生成3-6个弹幕
        int barrageCount = level.random.nextInt(MAX_BARRAGE_COUNT - MIN_BARRAGE_COUNT + 1) + MIN_BARRAGE_COUNT;
        
        // 获取玩家位置和视线方向
        Vec3 playerPos = player.position();
        Vec3 lookVec = player.getLookAngle();
        
        for (int i = 0; i < barrageCount; i++) {
            // 计算每个弹幕的生成位置（玩家周围随机位置）
            double spreadX = (level.random.nextDouble() - 0.5) * 4.0; // 4格范围内
            double spreadY = (level.random.nextDouble() - 0.5) * 2.0 + 1.0; // 玩家高度附近
            double spreadZ = (level.random.nextDouble() - 0.5) * 4.0;
            
            Vec3 spawnPos = playerPos.add(spreadX, spreadY, spreadZ);
            
            // 计算初始方向（略微朝向玩家视线方向）
            Vec3 initialDirection = lookVec.add(
                (level.random.nextDouble() - 0.5) * 0.3,
                (level.random.nextDouble() - 0.5) * 0.3,
                (level.random.nextDouble() - 0.5) * 0.3
            ).normalize();
            
            // 创建元素弹幕
            ElementalBarrageProjectile projectile = new ElementalBarrageProjectile(level, player, elementalDamage);
            projectile.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            projectile.shoot(initialDirection.x, initialDirection.y, initialDirection.z, 1.0F, 0.0F);
            
            // 添加到世界
            level.addFreshEntity(projectile);
        }
        
        // 生成施法粒子效果
        spawnCastParticles(level, player);
    }
    
    /**
     * 生成施法粒子效果
     */
    private void spawnCastParticles(Level level, Player player) {
        if (level.isClientSide()) {
            Vec3 pos = player.position();
            
            // 生成元素法杖施法粒子效果
            for (int i = 0; i < 15; i++) {
                double angle = Math.random() * Math.PI * 2;
                double radius = Math.random() * 2.0;
                double offsetX = Math.cos(angle) * radius;
                double offsetZ = Math.sin(angle) * radius;
                double offsetY = (Math.random() - 0.5) * 1.5 + 1.0;
                
                // 随机选择元素粒子类型
                int elementType = i % 4;
                net.minecraft.core.particles.ParticleOptions particleType;
                
                switch (elementType) {
                    case 0:
                        particleType = net.minecraft.core.particles.ParticleTypes.FLAME; // 火元素
                        break;
                    case 1:
                        particleType = net.minecraft.core.particles.ParticleTypes.DRIPPING_WATER; // 水元素
                        break;
                    case 2:
                        particleType = net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK; // 雷元素
                        break;
                    case 3:
                        particleType = net.minecraft.core.particles.ParticleTypes.ENCHANT; // 风元素
                        break;
                    default:
                        particleType = net.minecraft.core.particles.ParticleTypes.FLAME;
                }
                
                level.addParticle(particleType,
                        pos.x + offsetX,
                        pos.y + offsetY,
                        pos.z + offsetZ,
                        0, 0.1, 0);
            }
        }
    }
    
    /**
     * 重写暴击特效，添加元素法杖特有的暴击效果
     */
    @Override
    protected void onCriticalHit(Player player, net.minecraft.world.entity.LivingEntity target, float damage) {
        super.onCriticalHit(player, target, damage);

        // 添加额外的元素法杖暴击特效
        if (player.level().isClientSide) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§b元素暴击！ " + String.format("%.1f", damage) + " 伤害"),
                    true
            );
        }
    }
    
    /**
     * 获取武器特殊效果描述
     */
    @Override
    public String getSpecialEffectDescription() {
        return "元素法杖 | 基础伤害: " + String.format("%.0f", BASE_MAGIC_DAMAGE) +
                " | 暴击率: " + String.format("%.1f%%", criticalChance * 100) +
                " | 暴击伤害: " + String.format("%.1f", criticalDamageMultiplier) + "倍" +
                " | 弹幕数量: " + MIN_BARRAGE_COUNT + "-" + MAX_BARRAGE_COUNT + "个" +
                " | 追踪效果: 弹幕自动追踪15格内最近目标";
    }
}