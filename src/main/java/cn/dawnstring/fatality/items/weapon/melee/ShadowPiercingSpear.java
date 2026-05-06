package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.entity.projectile.ShadowSpearProjectile;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 破影之矛 - 近战武器
 * 特性：右键释放，玩家半径10格内的生物（除了玩家），从其上方召唤出一个破影之矛，向下方目标射出
 * 射出后攻击到目标或撞到方块时，产生爆炸，半径2格，伤害为基础伤害0.8倍
 * 伤害15000 暴击率34 暴击伤害36 浮动0.3 攻击速度5s
 */
public class ShadowPiercingSpear extends BaseWeapon {
    
    private static final int COOLDOWN_TICKS = 100; // 冷却时间5秒（100ticks）
    private static final float SPEAR_RANGE = 10.0f; // 矛的召唤范围10格
    private static final float EXPLOSION_RADIUS = 2.0f; // 爆炸半径2格
    private static final float EXPLOSION_DAMAGE_MULTIPLIER = 0.8f; // 爆炸伤害为基础伤害的0.8倍
    private static final float SPEAR_HEIGHT_OFFSET = 5.0f; // 矛生成的高度偏移（目标上方5格）
    private static final float SPEAR_SPEED = 2.0f; // 矛的下落速度
    
    public ShadowPiercingSpear() {
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
        }, new Properties().fireResistant(), 15000, 5.0f, 1f, 0.34f, 1.36f, 0.3f, WeaponEnum.MELEE);
        
        setStory("破影之矛，能够召唤阴影之力攻击敌人的神秘武器。\n" +
                "右键释放时，会在周围敌人上方召唤破影之矛，向下射出造成毁灭性打击。\n" +
                "矛击中目标或地面时会产生爆炸，对周围敌人造成范围伤害。");
    }

    /**
     * 重写右键使用方法，实现破影之矛召唤效果
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 服务器端：执行破影之矛召唤逻辑
            summonShadowPiercingSpears(level, player, itemstack);

            // 添加冷却时间
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        } else {
            // 客户端：播放破影之矛召唤音效
            level.playSound(player, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.8F);
        }

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 召唤破影之矛攻击范围内的敌人
     */
    private void summonShadowPiercingSpears(Level level, Player player, ItemStack weapon) {
        // 获取玩家周围的敌人
        List<LivingEntity> enemies = findEnemiesInRange(level, player);
        
        if (enemies.isEmpty()) {
            return;
        }

        // 计算基础伤害
        float baseDamage = calculateSpearDamage(player, weapon);
        
        // 为每个敌人召唤破影之矛
        for (LivingEntity enemy : enemies) {
            summonSpearForEnemy(level, player, weapon, enemy, baseDamage);
        }
    }

    /**
     * 查找玩家周围的敌人
     */
    private List<LivingEntity> findEnemiesInRange(Level level, Player player) {
        // 创建搜索范围（玩家为中心，半径10格）
        AABB searchArea = new AABB(
                player.getX() - SPEAR_RANGE, player.getY() - SPEAR_RANGE, player.getZ() - SPEAR_RANGE,
                player.getX() + SPEAR_RANGE, player.getY() + SPEAR_RANGE, player.getZ() + SPEAR_RANGE
        );

        // 获取范围内的所有生物实体
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, searchArea);

        // 过滤出敌人（排除玩家自己）
        return entities.stream()
                .filter(entity -> entity != player && entity.isAlive())
                .toList();
    }

    /**
     * 为单个敌人召唤破影之矛
     */
    private void summonSpearForEnemy(Level level, Player player, ItemStack weapon, LivingEntity enemy, float baseDamage) {
        // 计算矛的生成位置（敌人上方5格）
        Vec3 spawnPos = enemy.position().add(0, SPEAR_HEIGHT_OFFSET, 0);
        
        // 计算矛的下落方向（垂直向下）
        Vec3 direction = new Vec3(0, -1, 0);
        
        // 创建破影之矛投射物（召唤模式）
        ShadowSpearProjectile spear = new ShadowSpearProjectile(level, player, weapon, baseDamage, true);
        
        // 设置矛的位置和方向
        spear.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        spear.shoot(direction.x, direction.y, direction.z, SPEAR_SPEED, 0.0F); // 垂直向下，无散布
        
        // 添加到世界
        level.addFreshEntity(spear);
        
        // 服务器端生成召唤粒子效果
        if (!level.isClientSide()) {
            spawnSummonParticles(level, spawnPos);
        }
    }

    /**
     * 计算破影之矛伤害
     */
    private float calculateSpearDamage(Player player, ItemStack weapon) {
        return calculateFinalDamage(player, weapon, null);
    }

    /**
     * 生成召唤粒子效果
     */
    private void spawnSummonParticles(Level level, Vec3 position) {
        // 生成紫色阴影粒子效果
        for (int i = 0; i < 20; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 2.0;
            double offsetY = (level.random.nextDouble() - 0.5) * 2.0;
            double offsetZ = (level.random.nextDouble() - 0.5) * 2.0;
            
            level.addParticle(ParticleTypes.PORTAL,
                    position.x + offsetX, position.y + offsetY, position.z + offsetZ,
                    0, 0.1, 0);
            
            level.addParticle(ParticleTypes.SMOKE,
                    position.x + offsetX * 0.5, position.y + offsetY * 0.5, position.z + offsetZ * 0.5,
                    0, -0.05, 0);
        }
    }

    /**
     * 获取武器特殊效果描述
     */
    @Override
    public String getSpecialEffectDescription() {
        return "破影之矛 | 基础伤害: " + String.format("%.0f", 15000.0f) +
                " | 暴击率: " + String.format("%.1f%%", 0.34f * 100) +
                " | 暴击伤害: " + String.format("%.1f", 0.36f) + "倍" +
                " | 特殊效果: 召唤破影之矛攻击周围敌人，击中产生爆炸（半径2格，伤害80%）";
    }
}