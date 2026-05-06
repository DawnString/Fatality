package cn.dawnstring.fatality.items.weapon.magic;

import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import cn.dawnstring.fatality.system.ManaSystem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * 起源法杖 - 魔法武器
 * 特性：
 * - 左键攻击：外放魔法粒子，半径7格，5格内的生物（除了玩家）受到伤害
 * - 右键功能：玩家获得5秒无敌状态，且造成的伤害提高20%，冷却20秒
 * 属性：伤害920 暴击率30 暴击伤害32 浮动0.4 攻击速度0.25s
 */
public class StaffOfOrigins extends BaseWeapon {
    
    // 武器属性常量
    private static final int BASE_DAMAGE = 920;
    private static final float ATTACK_SPEED = 0.25f; // 0.25秒攻击速度
    private static final float BASE_DAMAGE_MULTIPLIER = 1.0f;
    private static final float CRITICAL_CHANCE = 0.30f;
    private static final float CRITICAL_DAMAGE = 0.32f;
    private static final float DAMAGE_FLUCTUATION = 0.4f;
    
    // 魔法粒子参数
    private static final float PARTICLE_RADIUS = 7.0f; // 粒子作用半径7格
    private static final float DAMAGE_RADIUS = 5.0f; // 伤害半径5格
    private static final int PARTICLE_COUNT = 50; // 粒子数量
    
    // 右键技能参数
    private static final int INVINCIBILITY_DURATION = 100; // 无敌状态持续时间5秒（100ticks）
    private static final float DAMAGE_BOOST_MULTIPLIER = 0.20f; // 伤害提升20%
    private static final int COOLDOWN_TICKS = 400;
    private static final float MANA_COST_SHIELD = 20.0f;
    private static final float MANA_COST_ATTACK = 5.0f;
    
    private long lastSkillUseTime = 0;
    
    public StaffOfOrigins() {
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
        }, new Properties().fireResistant(), BASE_DAMAGE, ATTACK_SPEED, 
           BASE_DAMAGE_MULTIPLIER, CRITICAL_CHANCE, CRITICAL_DAMAGE, 
           DAMAGE_FLUCTUATION, WeaponEnum.MAGIC);

        setStory("蕴含世界起源之力的魔法法杖，能够操控最基础的魔法粒子。\n" +
                "左键释放魔法粒子攻击周围敌人，右键激活起源护盾获得短暂无敌。");
    }
    
    /**
     * 右键使用方法 - 激活起源护盾
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        // 检查冷却时间
        long currentTime = level.getGameTime();
        if (currentTime - lastSkillUseTime < COOLDOWN_TICKS) {
            if (level.isClientSide()) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "§c技能冷却中..."), true);
            }
            return InteractionResultHolder.fail(itemstack);
        }
        
        if (!ManaSystem.safeConsumeMana(player, MANA_COST_SHIELD)) {
            if (level.isClientSide()) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "§c魔力不足！"), true);
            }
            return InteractionResultHolder.fail(itemstack);
        }
        
        activateOriginShield(player, level);
        
        // 更新技能使用时间
        lastSkillUseTime = currentTime;
        
        // 设置物品冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        
        return InteractionResultHolder.success(itemstack);
    }
    
    /**
     * 激活起源护盾 - 给予玩家无敌状态和伤害提升
     */
    private void activateOriginShield(Player player, Level level) {
        // 给予无敌效果
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, INVINCIBILITY_DURATION, 255));
        
        // 给予伤害提升效果
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, INVINCIBILITY_DURATION, 0));
        
        // 播放音效和粒子效果
        if (!level.isClientSide()) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.0f);
        } else {
            // 客户端生成粒子效果
            generateShieldParticles(player, level);
        }
        
        // 显示提示信息
        if (level.isClientSide()) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                "§b起源护盾已激活！获得5秒无敌和伤害提升！"), true);
        }
    }
    
    /**
     * 生成护盾粒子效果
     */
    private void generateShieldParticles(Player player, Level level) {
        if (!level.isClientSide()) return;
        
        double x = player.getX();
        double y = player.getY() + 1.0;
        double z = player.getZ();
        
        // 生成球形护盾粒子
        for (int i = 0; i < 30; i++) {
            double angle = (Math.PI * 2 * i) / 30;
            double offsetX = Math.cos(angle) * 2.0;
            double offsetZ = Math.sin(angle) * 2.0;
            
            level.addParticle(ParticleTypes.END_ROD,
                x + offsetX, y, z + offsetZ,
                0, 0.1, 0);
        }
    }
    
    /**
     * 左键攻击敌人时的回调 - 释放魔法粒子攻击
     */
    @Override
    protected void onHitEnemy(Player player, LivingEntity target, ItemStack stack, float damage) {
        super.onHitEnemy(player, target, stack, damage);
        
        if (ManaSystem.safeConsumeMana(player, MANA_COST_ATTACK)) {
            releaseMagicParticles(player, player.level());
        }
    }
    
    /**
     * 释放魔法粒子攻击周围敌人
     */
    private void releaseMagicParticles(Player player, Level level) {
        if (level.isClientSide()) {
            // 客户端生成粒子效果
            generateMagicParticles(player, level);
        } else {
            // 服务器端计算伤害
            applyMagicParticleDamage(player, level);
        }
    }
    
    /**
     * 生成魔法粒子效果
     */
    private void generateMagicParticles(Player player, Level level) {
        if (!level.isClientSide()) return;
        
        double centerX = player.getX();
        double centerY = player.getY() + 1.0;
        double centerZ = player.getZ();
        
        // 生成魔法粒子
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            double angle = (Math.PI * 2 * i) / PARTICLE_COUNT;
            double distance = PARTICLE_RADIUS * Math.random();
            double offsetX = Math.cos(angle) * distance;
            double offsetZ = Math.sin(angle) * distance;
            double offsetY = (Math.random() - 0.5) * 2.0;
            
            level.addParticle(ParticleTypes.ENCHANT,
                centerX + offsetX, centerY + offsetY, centerZ + offsetZ,
                0, 0, 0);
        }
    }
    
    /**
     * 应用魔法粒子伤害
     */
    private void applyMagicParticleDamage(Player player, Level level) {
        if (level.isClientSide()) return;
        
        // 搜索伤害范围内的生物
        AABB searchBox = new AABB(
            player.getX() - DAMAGE_RADIUS, player.getY() - DAMAGE_RADIUS, player.getZ() - DAMAGE_RADIUS,
            player.getX() + DAMAGE_RADIUS, player.getY() + DAMAGE_RADIUS, player.getZ() + DAMAGE_RADIUS
        );
        
        var entities = level.getEntitiesOfClass(LivingEntity.class, searchBox);
        
        for (LivingEntity entity : entities) {
            // 跳过玩家自己
            if (entity == player) continue;
            
            // 计算到玩家的距离
            double distance = entity.distanceTo(player);
            
            // 只在伤害半径内造成伤害
            if (distance <= DAMAGE_RADIUS) {
                // 计算伤害（距离越近伤害越高）
                float distanceMultiplier = (float)(1.0 - (distance / DAMAGE_RADIUS));
                float particleDamage = calculateFinalDamage(player, null, entity) * distanceMultiplier * 0.5f;
                
                // 应用伤害
                if (particleDamage > 0) {
                    entity.hurt(entity.damageSources().indirectMagic(player, player), particleDamage);
                }
            }
        }
    }
    
    /**
     * 获取武器特殊效果描述
     */
    @Override
    public String getSpecialEffectDescription() {
        return "左键：释放魔法粒子攻击周围敌人（半径5格）\n" +
               "右键：激活起源护盾（5秒无敌+20%伤害提升，冷却20秒）";
    }
}