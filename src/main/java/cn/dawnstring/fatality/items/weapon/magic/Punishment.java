package cn.dawnstring.fatality.items.weapon.magic;

import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import cn.dawnstring.fatality.entity.BaseBoss;
import cn.dawnstring.fatality.registry.ModEffects;
import cn.dawnstring.fatality.system.ManaSystem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
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
 * 刑罚
 * 魔法武器
 * 火焰凝聚为三条符文锁链向前抽打 造成直接伤害的同时，施加"罪印"标记 被标记的目标受到的伤害提升30%
 * 伤害720 暴击率26 暴击伤害34 浮动0.4 攻击速度0.25s
 */
public class Punishment extends BaseWeapon
{
    // 武器属性
    private static final int BASE_DAMAGE = 720;
    private static final float ATTACK_SPEED = 4.0f; // 0.25秒攻击速度
    private static final float BASE_DAMAGE_MULTIPLIER = 1.0f;
    private static final float CRITICAL_CHANCE = 0.26f;
    private static final float CRITICAL_DAMAGE = 0.34f;
    private static final float DAMAGE_FLUCTUATION = 0.4f;
    
    // 符文锁链参数
    private static final float CHAIN_RANGE = 6.0f; // 锁链抽打范围
    private static final float CHAIN_WIDTH = 1.5f; // 每条锁链的宽度
    private static final int CHAIN_COUNT = 3; // 锁链数量
    private static final float CHAIN_SPREAD_ANGLE = 30.0f; // 锁链展开角度
    
    // 罪印标记参数
    private static final float SIN_MARK_DAMAGE_BONUS = 0.30f; // 罪印伤害加成
    private static final int SIN_MARK_DURATION_TICKS = 100; // 罪印持续时间（5秒）
    
    // 魔力消耗
    private static final int MANA_COST = 15;
    
    // 存储罪印标记
    private final Map<UUID, Long> sinMarkedTargets = new HashMap<>();
    
    public Punishment() {
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
      * 右键使用方法 - 符文锁链抽打
      */
     @Override
     public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
         ItemStack itemstack = player.getItemInHand(hand);
         
         if (!level.isClientSide()) {
             // 检查魔力是否足够
             if (hasEnoughMana(player)) {
                 // 消耗魔力
                 consumeMana(player);
                 
                 // 符文锁链抽打
                 whipWithRuneChains(player, level, itemstack);
                 
                 // 设置冷却时间
                 player.getCooldowns().addCooldown(this, 5); // 0.25秒冷却
                 
                 return InteractionResultHolder.success(itemstack);
             } else {
                 // 魔力不足提示
                 if (level.isClientSide()) {
                     player.displayClientMessage(
                             net.minecraft.network.chat.Component.literal("§c魔力不足！"),
                             true
                     );
                 }
                 return InteractionResultHolder.fail(itemstack);
             }
         }
         
         return InteractionResultHolder.pass(itemstack);
     }
     
     /**
      * 符文锁链抽打
      */
     private void whipWithRuneChains(Player player, Level level, ItemStack weapon) {
         // 播放抽打音效
         level.playSound(null, player.getX(), player.getY(), player.getZ(),
                 SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.2F, 0.8F);
         
         // 生成符文锁链粒子效果
         spawnRuneChainParticles(level, player);
         
         // 计算三条锁链的方向
         Vec3 playerLook = player.getLookAngle();
         Vec3 playerPos = player.getEyePosition();
         
         // 生成三条锁链
         for (int i = 0; i < CHAIN_COUNT; i++) {
             // 计算当前锁链的方向（中间、左偏、右偏）
             Vec3 chainDirection = calculateChainDirection(playerLook, i);
             
             // 对锁链路径上的目标造成伤害并施加罪印
             attackWithChain(player, level, weapon, playerPos, chainDirection);
         }
     }
     
     /**
      * 计算锁链方向
      */
     private Vec3 calculateChainDirection(Vec3 baseDirection, int chainIndex) {
         if (chainIndex == 0) {
             // 中间锁链
             return baseDirection;
         } else {
             // 左右两侧锁链
             float angle = (chainIndex == 1 ? -1 : 1) * CHAIN_SPREAD_ANGLE;
             return rotateVectorAroundY(baseDirection, angle);
         }
     }
     
     /**
      * 绕Y轴旋转向量
      */
     private Vec3 rotateVectorAroundY(Vec3 vector, float angleDegrees) {
         double angleRad = Math.toRadians(angleDegrees);
         double cos = Math.cos(angleRad);
         double sin = Math.sin(angleRad);
         
         return new Vec3(
                 vector.x * cos + vector.z * sin,
                 vector.y,
                 -vector.x * sin + vector.z * cos
         );
     }
     
     /**
      * 使用锁链攻击
      */
     private void attackWithChain(Player player, Level level, ItemStack weapon, Vec3 startPos, Vec3 direction) {
         Vec3 endPos = startPos.add(direction.scale(CHAIN_RANGE));
         
         // 计算锁链路径的边界框
         AABB chainBounds = new AABB(
                 Math.min(startPos.x, endPos.x) - CHAIN_WIDTH, 
                 Math.min(startPos.y, endPos.y) - CHAIN_WIDTH, 
                 Math.min(startPos.z, endPos.z) - CHAIN_WIDTH,
                 Math.max(startPos.x, endPos.x) + CHAIN_WIDTH, 
                 Math.max(startPos.y, endPos.y) + CHAIN_WIDTH, 
                 Math.max(startPos.z, endPos.z) + CHAIN_WIDTH
         );
         
         // 对锁链路径上的实体造成伤害
         for (Entity entity : level.getEntities(player, chainBounds)) {
             if (entity instanceof LivingEntity target && target != player) {
                 // 检查实体是否在锁链路径上
                 if (isEntityInChainPath(startPos, endPos, target, CHAIN_WIDTH)) {
                     // 计算伤害（考虑罪印加成）
                     float baseDamage = calculateFinalDamage(player, weapon, target);
                     boolean hasSinMark = hasSinMark(target);
                     float damage = baseDamage * (hasSinMark ? (1.0f + SIN_MARK_DAMAGE_BONUS) : 1.0f);
                     
                     // 造成伤害
                     target.hurt(target.damageSources().magic(), damage);
                     
                     // 施加罪印标记
                     applySinMark(target, level, player);
                     
                     // 生成伤害粒子
                     spawnChainDamageParticles(level, target);
                 }
             }
         }
     }
     
     /**
      * 检查实体是否在锁链路径上
      */
     private boolean isEntityInChainPath(Vec3 start, Vec3 end, LivingEntity entity, float width) {
         Vec3 entityPos = entity.getEyePosition();
         
         // 计算点到线段的距离
         Vec3 lineVec = end.subtract(start);
         Vec3 pointVec = entityPos.subtract(start);
         
         double lineLength = lineVec.length();
         Vec3 lineDir = lineVec.normalize();
         
         // 点在直线上的投影长度
         double projection = pointVec.dot(lineDir);
         
         // 如果投影在线段范围内
         if (projection >= 0 && projection <= lineLength) {
             // 计算点到直线的距离
             Vec3 closestPoint = start.add(lineDir.scale(projection));
             double distance = entityPos.distanceTo(closestPoint);
             
             return distance <= width;
         }
         
         return false;
     }
     
     /**
      * 检查目标是否有罪印标记
      */
     private boolean hasSinMark(LivingEntity target) {
         UUID targetUUID = target.getUUID();
         if (sinMarkedTargets.containsKey(targetUUID)) {
             long markTime = sinMarkedTargets.get(targetUUID);
             // 检查标记是否过期
             if (System.currentTimeMillis() - markTime < SIN_MARK_DURATION_TICKS * 50) {
                 return true;
             } else {
                 // 移除过期标记
                 sinMarkedTargets.remove(targetUUID);
             }
         }
         return false;
     }
     
     /**
      * 施加罪印标记
      */
     private void applySinMark(LivingEntity target, Level level, Player player) {
         UUID targetUUID = target.getUUID();
         
         // 记录标记时间
         sinMarkedTargets.put(targetUUID, System.currentTimeMillis());
         
         // 应用罪印效果
         target.addEffect(new MobEffectInstance(ModEffects.SIN_MARK.get(), SIN_MARK_DURATION_TICKS, 0));
     }
     
     // 粒子效果生成方法
     private void spawnRuneChainParticles(Level level, Player player) {
         if (level.isClientSide()) {
             Vec3 startPos = player.getEyePosition();
             Vec3 baseDirection = player.getLookAngle();
             
             // 生成三条锁链的粒子效果
             for (int chainIndex = 0; chainIndex < CHAIN_COUNT; chainIndex++) {
                 Vec3 chainDirection = calculateChainDirection(baseDirection, chainIndex);
                 
                 // 生成锁链粒子
                 for (int i = 0; i < 20; i++) {
                     double progress = i * 0.3;
                     double offset = (Math.random() - 0.5) * 0.2;
                     
                     level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                             startPos.x + chainDirection.x * progress + offset,
                             startPos.y + chainDirection.y * progress + offset,
                             startPos.z + chainDirection.z * progress + offset,
                             chainDirection.x * 0.1, chainDirection.y * 0.1, chainDirection.z * 0.1);
                 }
             }
         }
     }
     
     private void spawnChainDamageParticles(Level level, LivingEntity target) {
         if (level.isClientSide()) {
             Vec3 pos = target.getEyePosition();
             
             // 生成锁链伤害粒子
             for (int i = 0; i < 8; i++) {
                 level.addParticle(ParticleTypes.FLAME,
                         pos.x + (Math.random() - 0.5) * 0.5,
                         pos.y + (Math.random() - 0.5) * 0.5,
                         pos.z + (Math.random() - 0.5) * 0.5,
                         0, 0.05, 0);
             }
         }
     }
     
     // 魔力相关方法
     private boolean hasEnoughMana(Player player) {
         // 使用ManaSystem检查玩家魔力值是否足够
         return ManaSystem.hasEnoughMana(player, MANA_COST);
     }
     
     private void consumeMana(Player player) {
         // 使用ManaSystem消耗玩家魔力
         if (ManaSystem.safeConsumeMana(player, MANA_COST)) {
             // 显示魔力消耗信息
             if (player.level().isClientSide()) {
                 player.displayClientMessage(
                         net.minecraft.network.chat.Component.literal("§b消耗了 " + MANA_COST + " 点魔力"),
                         true
                 );
             }
         }
     }
     
     /**
      * 检查是否为Boss
      */
     private boolean isBoss(LivingEntity entity) {
         // 检查是否继承BaseBoss类
         return entity instanceof BaseBoss;
     }
     
     /**
      * 定期清理过期的罪印标记
      */
     @Override
     public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
         super.inventoryTick(stack, level, entity, slotId, isSelected);
         
         if (!level.isClientSide() && entity instanceof Player player && isSelected) {
             // 清理超过持续时间的罪印标记
             long currentTime = System.currentTimeMillis();
             sinMarkedTargets.entrySet().removeIf(entry -> {
                 long markTime = entry.getValue();
                 return currentTime - markTime >= SIN_MARK_DURATION_TICKS * 50;
             });
         }
     }
}