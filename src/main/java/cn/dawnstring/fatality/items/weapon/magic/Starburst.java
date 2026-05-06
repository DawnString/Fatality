package cn.dawnstring.fatality.items.weapon.magic;

import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import cn.dawnstring.fatality.system.ManaSystem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 星爆
 * 魔法武器
 * 普通攻击发射小型恒星碎片（基础伤害） 命中后产生微型引力坍缩，吸引周围敌人（除开玩家与boss） 每第三次命中触发"核聚变"，造成范围爆炸（基础伤害的1.2倍）
 * 长按右键，蓄力15s，发射超新星，超新星碰撞到目标或方块时，产生星爆领域，半径4格，领域内的目标被吸引至中心（除了玩家与boss），并持续造成伤害（基础伤害）
 * 星爆领域持续5s后，从中心发生爆炸，伤害为基础伤害的5倍
 * 伤害950 暴击率30 暴击伤害35 浮动0.4 攻击速度0.25s
 */
public class Starburst extends BaseWeapon {
    
    // 武器属性
    private static final int BASE_DAMAGE = 950;
    private static final float ATTACK_SPEED = 4.0f; // 0.25秒攻击速度
    private static final float BASE_DAMAGE_MULTIPLIER = 1.0f;
    private static final float CRITICAL_CHANCE = 0.30f;
    private static final float CRITICAL_DAMAGE = 0.35f;
    private static final float DAMAGE_FLUCTUATION = 0.4f;
    
    // 恒星碎片参数
    private static final float STAR_FRAGMENT_RANGE = 12.0f; // 恒星碎片射程
    private static final float GRAVITY_COLLAPSE_RADIUS = 3.0f; // 引力坍缩半径
    private static final float GRAVITY_PULL_FORCE = 0.3f; // 引力拉力
    private static final int NUCLEAR_FUSION_TRIGGER = 3; // 每3次命中触发核聚变
    private static final float NUCLEAR_FUSION_DAMAGE_MULTIPLIER = 1.2f; // 核聚变伤害倍率
    
    // 超新星参数
    private static final float SUPERNOVA_RANGE = 20.0f; // 超新星射程
    private static final float STARBURST_FIELD_RADIUS = 4.0f; // 星爆领域半径
    private static final int SUPERNOVA_CHARGE_TIME_TICKS = 300; // 蓄力时间15秒（20 ticks/秒）
    private static final float STARBURST_FIELD_DAMAGE_MULTIPLIER = 1.0f; // 领域持续伤害倍率
    private static final float STARBURST_EXPLOSION_DAMAGE_MULTIPLIER = 5.0f; // 最终爆炸伤害倍率
    
    // 魔力消耗
    private static final int MANA_COST_NORMAL = 15; // 普通攻击魔力消耗
    private static final int MANA_COST_SUPERNOVA = 50; // 超新星魔力消耗
    
    // 命中计数
    private final Map<UUID, Integer> hitCountMap = new HashMap<>();
    // 蓄力状态
    private final Map<UUID, Long> chargingPlayers = new HashMap<>();
    // 星爆领域状态（替代 Thread.sleep）
    private Vec3 activeFieldCenter = null;
    private int activeFieldTicks = 0;
    private ItemStack activeFieldWeapon = null;
    private boolean fieldExploded = false;
    
    public Starburst() {
        super(new Tier() {
            @Override
            public int getUses() {
                return 0; // 无限耐久
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
                return 0; // 不能附魔
            }

            @Override
            public Ingredient getRepairIngredient() {
                return null; // 不能修复
            }
        }, new Properties().stacksTo(1).fireResistant(),
              BASE_DAMAGE, // 基础攻击伤害
              ATTACK_SPEED, // 攻击速度
              BASE_DAMAGE_MULTIPLIER, // 基础伤害倍率
              CRITICAL_CHANCE, // 暴击率
              CRITICAL_DAMAGE, // 暴击伤害倍率
              DAMAGE_FLUCTUATION, // 伤害浮动
              WeaponEnum.MAGIC // 武器类型：魔法
         );
     }
     
     /**
      * 右键使用方法 - 普通攻击或蓄力超新星
      */
     @Override
     public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
         ItemStack itemstack = player.getItemInHand(hand);
         
         if (level.isClientSide()) {
             // 客户端处理：开始蓄力计时
             startCharging(player);
         } else {
             // 服务器端处理：检查蓄力时间
             long chargeTime = getChargeTime(player);
             
             if (chargeTime >= SUPERNOVA_CHARGE_TIME_TICKS) {
                 if (ManaSystem.safeConsumeMana(player, MANA_COST_SUPERNOVA)) {
                     fireSupernova(player, level, itemstack);
                     stopCharging(player);
                     return InteractionResultHolder.success(itemstack);
                 } else {
                     player.displayClientMessage(
                             net.minecraft.network.chat.Component.literal("§c魔力不足！"),
                             true
                     );
                     stopCharging(player);
                     return InteractionResultHolder.fail(itemstack);
                 }
             } else {
                 if (ManaSystem.safeConsumeMana(player, MANA_COST_NORMAL)) {
                     fireStarFragment(player, level, itemstack);

                     player.getCooldowns().addCooldown(this, 5);

                     return InteractionResultHolder.success(itemstack);
                 } else {
                     player.displayClientMessage(
                             net.minecraft.network.chat.Component.literal("§c魔力不足！"),
                             true
                     );
                     return InteractionResultHolder.fail(itemstack);
                 }
             }
         }
         
         return InteractionResultHolder.pass(itemstack);
     }
     
     /**
      * 发射恒星碎片
      */
     private void fireStarFragment(Player player, Level level, ItemStack weapon) {
         // 播放发射音效
         level.playSound(null, player.getX(), player.getY(), player.getZ(),
                 SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.8F, 1.2F);
         
         // 生成恒星碎片粒子效果
         spawnStarFragmentParticles(level, player);
         
         // 射线检测命中目标
         Vec3 startPos = player.getEyePosition();
         Vec3 lookVec = player.getLookAngle();
         Vec3 endPos = startPos.add(lookVec.scale(STAR_FRAGMENT_RANGE));
         
         // 射线检测
         Entity hitEntity = raycastForTarget(level, player, startPos, endPos);
         
         if (hitEntity instanceof LivingEntity target) {
             // 计算伤害
             float damage = calculateFinalDamage(player, weapon, target);
             
             // 造成伤害
             target.hurt(target.damageSources().magic(), damage);
             
             // 产生微型引力坍缩
             createGravityCollapse(target, level, player);
             
             // 增加命中计数
             incrementHitCount(target);
             
             // 检查是否触发核聚变
             if (getHitCount(target) % NUCLEAR_FUSION_TRIGGER == 0) {
                 triggerNuclearFusion(target, level, player, weapon);
             }
         }
     }
     
     /**
      * 产生微型引力坍缩
      */
     private void createGravityCollapse(LivingEntity centerEntity, Level level, Player player) {
         // 播放引力音效
         level.playSound(null, centerEntity.getX(), centerEntity.getY(), centerEntity.getZ(),
                 SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.5F, 0.8F);
         
         // 生成引力坍缩粒子效果
         spawnGravityCollapseParticles(level, centerEntity);
         
         // 获取引力范围内的所有实体
         AABB gravityBounds = new AABB(
                 centerEntity.getX() - GRAVITY_COLLAPSE_RADIUS, centerEntity.getY() - GRAVITY_COLLAPSE_RADIUS, centerEntity.getZ() - GRAVITY_COLLAPSE_RADIUS,
                 centerEntity.getX() + GRAVITY_COLLAPSE_RADIUS, centerEntity.getY() + GRAVITY_COLLAPSE_RADIUS, centerEntity.getZ() + GRAVITY_COLLAPSE_RADIUS
         );
         
         for (Entity entity : level.getEntities(centerEntity, gravityBounds)) {
             if (entity instanceof LivingEntity target && target != player && !isBoss(target)) {
                 // 将实体拉向中心
                 pullEntityToCenter(target, centerEntity, GRAVITY_PULL_FORCE);
             }
         }
     }
     
     /**
      * 触发核聚变
      */
     private void triggerNuclearFusion(LivingEntity centerEntity, Level level, Player player, ItemStack weapon) {
         // 播放核聚变音效
         level.playSound(null, centerEntity.getX(), centerEntity.getY(), centerEntity.getZ(),
                 SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0F, 0.7F);
         
         // 生成核聚变粒子效果
         spawnNuclearFusionParticles(level, centerEntity);
         
         // 计算核聚变伤害
         float baseDamage = calculateFinalDamage(player, weapon, centerEntity);
         float fusionDamage = baseDamage * NUCLEAR_FUSION_DAMAGE_MULTIPLIER;
         
         // 获取爆炸范围内的所有实体
         AABB explosionBounds = new AABB(
                 centerEntity.getX() - GRAVITY_COLLAPSE_RADIUS * 1.5f, centerEntity.getY() - GRAVITY_COLLAPSE_RADIUS * 1.5f, centerEntity.getZ() - GRAVITY_COLLAPSE_RADIUS * 1.5f,
                 centerEntity.getX() + GRAVITY_COLLAPSE_RADIUS * 1.5f, centerEntity.getY() + GRAVITY_COLLAPSE_RADIUS * 1.5f, centerEntity.getZ() + GRAVITY_COLLAPSE_RADIUS * 1.5f
         );
         
         for (Entity entity : level.getEntities(centerEntity, explosionBounds)) {
             if (entity instanceof LivingEntity target && target != player) {
                 // 造成核聚变伤害
                 target.hurt(target.damageSources().explosion(player, player), fusionDamage);
             }
         }
         
         // 重置命中计数
         resetHitCount(centerEntity);
     }
     
     /**
      * 发射超新星
      */
     private void fireSupernova(Player player, Level level, ItemStack weapon) {
         // 播放超新星发射音效
         level.playSound(null, player.getX(), player.getY(), player.getZ(),
                 SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.5F, 0.5F);
         
         // 生成超新星粒子效果
         spawnSupernovaParticles(level, player);
         
         // 射线检测碰撞点
         Vec3 startPos = player.getEyePosition();
         Vec3 lookVec = player.getLookAngle();
         Vec3 endPos = startPos.add(lookVec.scale(SUPERNOVA_RANGE));
         
         // 射线检测
         Entity hitEntity = raycastForTarget(level, player, startPos, endPos);
         Vec3 impactPos;
         
         if (hitEntity != null) {
             impactPos = hitEntity.getEyePosition();
         } else {
             // 没有命中实体，检测方块碰撞
             impactPos = raycastForBlock(level, player, startPos, endPos);
         }
         
         // 创建星爆领域
         createStarburstField(impactPos, level, player, weapon);
     }
     
     /**
      * 创建星爆领域
      */
     private void createStarburstField(Vec3 centerPos, Level level, Player player, ItemStack weapon) {
         // 播放星爆领域生成音效
         level.playSound(null, centerPos.x, centerPos.y, centerPos.z,
                 SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.AMBIENT, 1.0F, 0.6F);
         
         // 启动星爆领域计时器
         startStarburstFieldTimer(centerPos, level, player, weapon);
         
         // 显示星爆领域信息
         if (level.isClientSide()) {
             player.displayClientMessage(
                     net.minecraft.network.chat.Component.literal("§6星爆领域已生成！持续5秒..."),
                     true
             );
         }
     }
     
     /**
      * 启动星爆领域计时器
      */
     private void startStarburstFieldTimer(Vec3 centerPos, Level level, Player player, ItemStack weapon) {
        this.activeFieldCenter = centerPos;
        this.activeFieldTicks = 0;
        this.activeFieldWeapon = weapon.copy();
        this.fieldExploded = false;
    }
     
     /**
      * 应用星爆领域效果
      */
     private void applyStarburstFieldEffect(Vec3 centerPos, Level level, Player player, ItemStack weapon, int second) {
         // 获取领域范围内的所有实体
         AABB fieldBounds = new AABB(
                 centerPos.x - STARBURST_FIELD_RADIUS, centerPos.y - STARBURST_FIELD_RADIUS, centerPos.z - STARBURST_FIELD_RADIUS,
                 centerPos.x + STARBURST_FIELD_RADIUS, centerPos.y + STARBURST_FIELD_RADIUS, centerPos.z + STARBURST_FIELD_RADIUS
         );
         
         for (Entity entity : level.getEntities(player, fieldBounds)) {
             if (entity instanceof LivingEntity target && target != player && !isBoss(target)) {
                 // 将目标拉向中心
                 pullEntityToCenter(target, centerPos, GRAVITY_PULL_FORCE * 2); // 更强的引力
                 
                 // 造成持续伤害
                 float baseDamage = calculateFinalDamage(player, weapon, target);
                 float fieldDamage = baseDamage * STARBURST_FIELD_DAMAGE_MULTIPLIER;
                 target.hurt(target.damageSources().magic(), fieldDamage);
                 
                 // 生成领域效果粒子
                 spawnStarburstFieldParticles(level, target);
             }
         }
         
         // 显示领域效果信息
         if (level.isClientSide()) {
             player.displayClientMessage(
                     net.minecraft.network.chat.Component.literal("§b星爆领域第" + second + "秒 - 持续伤害中..."),
                     true
             );
         }
     }
     
     /**
      * 触发星爆最终爆炸
      */
     private void triggerStarburstExplosion(Vec3 centerPos, Level level, Player player, ItemStack weapon) {
         // 播放最终爆炸音效
         level.playSound(null, centerPos.x, centerPos.y, centerPos.z,
                 SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 2.0F, 0.5F);
         
         // 生成最终爆炸粒子效果
         spawnStarburstExplosionParticles(level, centerPos);
         
         // 计算爆炸伤害
         float baseDamage = calculateFinalDamage(player, weapon, null);
         float explosionDamage = baseDamage * STARBURST_EXPLOSION_DAMAGE_MULTIPLIER;
         
         // 获取爆炸范围内的所有实体
         AABB explosionBounds = new AABB(
                 centerPos.x - STARBURST_FIELD_RADIUS * 1.5f, centerPos.y - STARBURST_FIELD_RADIUS * 1.5f, centerPos.z - STARBURST_FIELD_RADIUS * 1.5f,
                 centerPos.x + STARBURST_FIELD_RADIUS * 1.5f, centerPos.y + STARBURST_FIELD_RADIUS * 1.5f, centerPos.z + STARBURST_FIELD_RADIUS * 1.5f
         );
         
         for (Entity entity : level.getEntities(player, explosionBounds)) {
             if (entity instanceof LivingEntity target && target != player) {
                 // 造成最终爆炸伤害
                 target.hurt(target.damageSources().explosion(player, player), explosionDamage);
             }
         }
         
         // 显示最终爆炸信息
         if (level.isClientSide()) {
             player.displayClientMessage(
                     net.minecraft.network.chat.Component.literal("§6星爆领域最终爆炸！ 伤害: " + String.format("%.1f", explosionDamage)),
                     true
             );
         }
     }
     
     // 辅助方法
     
     /**
      * 射线检测目标
      */
     private Entity raycastForTarget(Level level, Player player, Vec3 startPos, Vec3 endPos) {
         // 简化实现：使用AABB检测
         AABB rayBounds = new AABB(
                 Math.min(startPos.x, endPos.x), Math.min(startPos.y, endPos.y), Math.min(startPos.z, endPos.z),
                 Math.max(startPos.x, endPos.x), Math.max(startPos.y, endPos.y), Math.max(startPos.z, endPos.z)
         );
         
         Entity closestEntity = null;
         double closestDistance = Double.MAX_VALUE;
         
         for (Entity entity : level.getEntities(player, rayBounds)) {
             if (entity instanceof LivingEntity target && target != player) {
                 double distance = startPos.distanceTo(target.getEyePosition());
                 if (distance < closestDistance) {
                     closestDistance = distance;
                     closestEntity = entity;
                 }
             }
         }
         
         return closestEntity;
     }
     
     /**
      * 射线检测方块碰撞点
      */
     private Vec3 raycastForBlock(Level level, Player player, Vec3 startPos, Vec3 endPos) {
         // 简化实现：返回射线终点
         return endPos;
     }
     
     /**
      * 将实体拉向中心
      */
     private void pullEntityToCenter(LivingEntity entity, Vec3 centerPos, float force) {
         Vec3 entityPos = entity.getEyePosition();
         Vec3 direction = centerPos.subtract(entityPos).normalize();
         
         // 应用拉力
         entity.setDeltaMovement(entity.getDeltaMovement().add(direction.scale(force)));
     }
     
     private void pullEntityToCenter(LivingEntity entity, LivingEntity centerEntity, float force) {
         pullEntityToCenter(entity, centerEntity.getEyePosition(), force);
     }
     
     /**
      * 检查是否为Boss
      */
     private boolean isBoss(LivingEntity entity) {
         // 检查是否继承BaseBoss类
         //TODO
         return false;
     }
     
     // 命中计数管理
     private void incrementHitCount(LivingEntity target) {
         hitCountMap.put(target.getUUID(), getHitCount(target) + 1);
     }
     
     private int getHitCount(LivingEntity target) {
         return hitCountMap.getOrDefault(target.getUUID(), 0);
     }
     
     private void resetHitCount(LivingEntity target) {
         hitCountMap.remove(target.getUUID());
     }
     
     // 蓄力状态管理
     private void startCharging(Player player) {
         chargingPlayers.put(player.getUUID(), System.currentTimeMillis());
     }
     
     private long getChargeTime(Player player) {
         Long startTime = chargingPlayers.get(player.getUUID());
         if (startTime != null) {
             return (System.currentTimeMillis() - startTime) / 50; // 转换为ticks
         }
         return 0;
     }
     
     private void stopCharging(Player player) {
         chargingPlayers.remove(player.getUUID());
     }
     
     // 粒子效果生成方法
     private void spawnStarFragmentParticles(Level level, Player player) {
         if (level.isClientSide()) {
             Vec3 startPos = player.getEyePosition();
             Vec3 lookVec = player.getLookAngle();
             
             for (int i = 0; i < 15; i++) {
                 double progress = i * 0.8;
                 double spread = (Math.random() - 0.5) * 0.3;
                 
                 level.addParticle(ParticleTypes.FIREWORK,
                         startPos.x + lookVec.x * progress + spread,
                         startPos.y + lookVec.y * progress + spread,
                         startPos.z + lookVec.z * progress + spread,
                         lookVec.x * 0.2, lookVec.y * 0.2, lookVec.z * 0.2);
             }
         }
     }
     
     private void spawnGravityCollapseParticles(Level level, LivingEntity centerEntity) {
         if (level.isClientSide()) {
             Vec3 pos = centerEntity.getEyePosition();
             
             for (int i = 0; i < 30; i++) {
                 level.addParticle(ParticleTypes.PORTAL,
                         pos.x + (Math.random() - 0.5) * 3.0,
                         pos.y + (Math.random() - 0.5) * 3.0,
                         pos.z + (Math.random() - 0.5) * 3.0,
                         (Math.random() - 0.5) * 0.1,
                         -0.1, // 向下吸引
                         (Math.random() - 0.5) * 0.1);
             }
         }
     }
     
     private void spawnNuclearFusionParticles(Level level, LivingEntity centerEntity) {
         if (level.isClientSide()) {
             Vec3 pos = centerEntity.getEyePosition();
             
             for (int i = 0; i < 50; i++) {
                 level.addParticle(ParticleTypes.FLASH,
                         pos.x + (Math.random() - 0.5) * 5.0,
                         pos.y + (Math.random() - 0.5) * 5.0,
                         pos.z + (Math.random() - 0.5) * 5.0,
                         (Math.random() - 0.5) * 0.5,
                         (Math.random() - 0.5) * 0.5,
                         (Math.random() - 0.5) * 0.5);
             }
         }
     }
     
     private void spawnSupernovaParticles(Level level, Player player) {
         if (level.isClientSide()) {
             Vec3 startPos = player.getEyePosition();
             Vec3 lookVec = player.getLookAngle();
             
             for (int i = 0; i < 100; i++) {
                 double progress = i * 0.2;
                 double spread = (Math.random() - 0.5) * 0.5;
                 
                 level.addParticle(ParticleTypes.DRAGON_BREATH,
                         startPos.x + lookVec.x * progress + spread,
                         startPos.y + lookVec.y * progress + spread,
                         startPos.z + lookVec.z * progress + spread,
                         lookVec.x * 0.3, lookVec.y * 0.3, lookVec.z * 0.3);
             }
         }
     }
     
     private void spawnStarburstFieldParticles(Level level, LivingEntity target) {
         if (level.isClientSide()) {
             Vec3 pos = target.getEyePosition();
             
             for (int i = 0; i < 5; i++) {
                 level.addParticle(ParticleTypes.ELECTRIC_SPARK,
                         pos.x + (Math.random() - 0.5) * 0.5,
                         pos.y + (Math.random() - 0.5) * 0.5,
                         pos.z + (Math.random() - 0.5) * 0.5,
                         0, 0.02, 0);
             }
         }
     }
     
     private void spawnStarburstExplosionParticles(Level level, Vec3 centerPos) {
         if (level.isClientSide()) {
             for (int i = 0; i < 200; i++) {
                 level.addParticle(ParticleTypes.EXPLOSION_EMITTER,
                         centerPos.x + (Math.random() - 0.5) * 8.0,
                         centerPos.y + (Math.random() - 0.5) * 8.0,
                         centerPos.z + (Math.random() - 0.5) * 8.0,
                         (Math.random() - 0.5) * 0.5,
                         (Math.random() - 0.5) * 0.5,
                         (Math.random() - 0.5) * 0.5);
             }
         }
     }
     
     @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (!level.isClientSide() && entity instanceof Player player) {
            if (activeFieldCenter != null && activeFieldWeapon != null && !fieldExploded) {
                activeFieldTicks++;

                if (activeFieldTicks % 20 == 0 && activeFieldTicks <= 100) {
                    int second = activeFieldTicks / 20;
                    applyStarburstFieldEffect(activeFieldCenter, level, player, activeFieldWeapon, second);
                }

                if (activeFieldTicks >= 100) {
                    triggerStarburstExplosion(activeFieldCenter, level, player, activeFieldWeapon);
                    fieldExploded = true;
                    activeFieldCenter = null;
                    activeFieldWeapon = null;
                    activeFieldTicks = 0;
                }
            }
        }
    }
}