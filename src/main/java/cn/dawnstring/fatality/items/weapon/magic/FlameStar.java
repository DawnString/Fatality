package cn.dawnstring.fatality.items.weapon.magic;

import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
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
 * 炎辰
 * 魔法武器
 * 消耗少量魔力，向前方锥形区域喷射液态火焰,每秒造成基础火焰伤害，持续4秒
 * 被点燃的敌人死亡时产生"余烬爆燃" 对周围2米内敌人施加1层"灼痕"印记（可叠加3层）每层"灼痕"使目标受到的火焰伤害提升15%
 * 伤害462 暴击率24 暴击伤害30 浮动0.4 攻击速度0.25s
 */
public class FlameStar extends BaseWeapon {
    
    // 武器属性
    private static final int BASE_DAMAGE = 462;
    private static final float ATTACK_SPEED = 4.0f; // 0.25秒攻击速度
    private static final float BASE_DAMAGE_MULTIPLIER = 1.0f;
    private static final float CRITICAL_CHANCE = 0.24f;
    private static final float CRITICAL_DAMAGE = 0.30f;
    private static final float DAMAGE_FLUCTUATION = 0.4f;
    
    // 火焰喷射参数
    private static final float FLAME_RANGE = 8.0f; // 喷射范围
    private static final float FLAME_ANGLE = 45.0f; // 锥形角度
    private static final int FLAME_DURATION_TICKS = 80; // 持续4秒（20 ticks/秒 * 4秒）
    private static final int FLAME_DAMAGE_INTERVAL = 20; // 每秒造成一次伤害
    
    // 灼痕印记参数
    private static final float BURN_MARK_DAMAGE_BONUS = 0.15f; // 每层灼痕伤害加成
    private static final int MAX_BURN_MARK_STACKS = 3; // 最大灼痕层数
    private static final int BURN_MARK_DURATION_TICKS = 100; // 灼痕持续时间（5秒）
    private static final float EMBER_EXPLOSION_RADIUS = 2.0f; // 余烬爆燃半径
    
    // 魔力消耗
    private static final int MANA_COST = 10;
    
    // 存储灼痕印记
    private final Map<UUID, Integer> burnMarkStacks = new HashMap<>();
    
    public FlameStar() {
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
      * 右键使用方法 - 喷射液态火焰
      */
     @Override
     public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
         ItemStack itemstack = player.getItemInHand(hand);
         
         if (!level.isClientSide()) {
             if (!ManaSystem.safeConsumeMana(player, MANA_COST)) {
                 if (level.isClientSide()) {
                     player.displayClientMessage(
                             net.minecraft.network.chat.Component.literal("§c魔力不足！"),
                             true
                     );
                 }
                 return InteractionResultHolder.fail(itemstack);
             }
             
             sprayLiquidFlame(player, level, itemstack);
             
             player.getCooldowns().addCooldown(this, 5);
             
             return InteractionResultHolder.success(itemstack);
         }
         
         return InteractionResultHolder.pass(itemstack);
     }
     
     /**
      * 喷射液态火焰
      */
     private void sprayLiquidFlame(Player player, Level level, ItemStack weapon) {
         level.playSound(null, player.getX(), player.getY(), player.getZ(),
                 SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
         
         spawnSprayParticles(level, player);
         
         Vec3 startPos = player.getEyePosition();
         Vec3 lookVec = player.getLookAngle();
         
         AABB coneBounds = calculateConeBounds(startPos, lookVec, FLAME_RANGE, FLAME_ANGLE);
         
         for (Entity entity : level.getEntities(player, coneBounds)) {
             if (entity instanceof LivingEntity target && target != player) {
                 applyLiquidFlameEffect(target, level, player, weapon);
             }
         }
     }
     
     /**
      * 应用液态火焰效果
      */
     private void applyLiquidFlameEffect(LivingEntity target, Level level, Player player, ItemStack weapon) {
         target.setSecondsOnFire(FLAME_DURATION_TICKS / 20);
         
         float baseDamage = calculateFinalDamage(player, weapon, target);
         float burnMarkBonus = getBurnMarkBonus(target);
         float damage = baseDamage * (1.0f + burnMarkBonus);
         
         target.hurt(target.damageSources().onFire(), damage);
         
         target.getPersistentData().putUUID("flame_source", player.getUUID());
         target.getPersistentData().putBoolean("liquid_flame", true);
         
         spawnFlameDamageParticles(level, target);
     }
     
     /**
      * 计算锥形区域边界
      */
     private AABB calculateConeBounds(Vec3 startPos, Vec3 direction, float range, float angle) {
         Vec3 endPos = startPos.add(direction.scale(range));
         
         // 计算锥形半径
         double radius = range * Math.tan(Math.toRadians(angle / 2));
         
         return new AABB(
                 Math.min(startPos.x, endPos.x) - radius, Math.min(startPos.y, endPos.y) - radius, Math.min(startPos.z, endPos.z) - radius,
                 Math.max(startPos.x, endPos.x) + radius, Math.max(startPos.y, endPos.y) + radius, Math.max(startPos.z, endPos.z) + radius
         );
     }
     
     /**
      * 获取灼痕伤害加成
      */
     private float getBurnMarkBonus(LivingEntity target) {
         UUID targetUUID = target.getUUID();
         if (burnMarkStacks.containsKey(targetUUID)) {
             int stacks = burnMarkStacks.get(targetUUID);
             return stacks * BURN_MARK_DAMAGE_BONUS;
         }
         return 0.0f;
     }
     
     /**
      * 余烬爆燃效果（敌人死亡时触发）
      */
     public static void onEnemyDeath(LivingEntity deadEntity, Level level) {
         // 检查是否为液态火焰杀死的敌人
         if (deadEntity.getPersistentData().getBoolean("liquid_flame")) {
             // 获取火焰来源玩家
             UUID flameSourceUUID = deadEntity.getPersistentData().getUUID("flame_source");
             Player sourcePlayer = level.getPlayerByUUID(flameSourceUUID);
             
             if (sourcePlayer != null) {
                 // 播放爆燃音效
                 level.playSound(null, deadEntity.getX(), deadEntity.getY(), deadEntity.getZ(),
                         SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.5F, 1.0F);
                 
                 // 生成爆燃粒子效果
                 spawnEmberExplosionParticles(level, deadEntity);
                 
                 // 对周围敌人施加灼痕印记
                 AABB explosionBounds = new AABB(
                         deadEntity.getX() - EMBER_EXPLOSION_RADIUS, deadEntity.getY() - EMBER_EXPLOSION_RADIUS, deadEntity.getZ() - EMBER_EXPLOSION_RADIUS,
                         deadEntity.getX() + EMBER_EXPLOSION_RADIUS, deadEntity.getY() + EMBER_EXPLOSION_RADIUS, deadEntity.getZ() + EMBER_EXPLOSION_RADIUS
                 );
                 
                 for (Entity entity : level.getEntities(deadEntity, explosionBounds)) {
                     if (entity instanceof LivingEntity nearbyTarget && nearbyTarget != deadEntity) {
                         // 应用灼痕印记
                         applyBurnMark(nearbyTarget, level, sourcePlayer);
                         
                         // 显示灼痕施加信息
                         if (level.isClientSide()) {
                             sourcePlayer.displayClientMessage(
                                     net.minecraft.network.chat.Component.literal("§6余烬爆燃对目标施加了灼痕印记！"),
                                     true
                             );
                         }
                     }
                 }
             }
         }
     }
     
     /**
      * 应用灼痕印记（静态方法供外部调用）
      */
     public static void applyBurnMark(LivingEntity target, Level level, Player player) {
         // 这里需要获取FlameStar实例来访问burnMarkStacks
         // 在实际使用中，应该通过玩家当前持有的武器来获取实例
         if (player.getMainHandItem().getItem() instanceof FlameStar flameStar) {
             flameStar.applyBurnMarkInternal(target, level, player);
         }
     }
     
     /**
      * 内部应用灼痕印记方法
      */
     private void applyBurnMarkInternal(LivingEntity target, Level level, Player player) {
         UUID targetUUID = target.getUUID();
         int currentStacks = burnMarkStacks.getOrDefault(targetUUID, 0);
         
         if (currentStacks < MAX_BURN_MARK_STACKS) {
             // 增加灼痕层数
             burnMarkStacks.put(targetUUID, currentStacks + 1);
             
             // 应用灼痕效果
             target.addEffect(new MobEffectInstance(ModEffects.BURN_MARK.get(), BURN_MARK_DURATION_TICKS, currentStacks));
             
             // 显示灼痕信息
             if (level.isClientSide()) {
                 player.displayClientMessage(
                         net.minecraft.network.chat.Component.literal("§6灼痕印记已叠加至 " + (currentStacks + 1) + " 层！"),
                         true
                 );
             }
         }
     }
     
     // 粒子效果生成方法
     private void spawnSprayParticles(Level level, Player player) {
         if (level.isClientSide()) {
             Vec3 startPos = player.getEyePosition();
             Vec3 lookVec = player.getLookAngle();
             
             // 生成液态火焰喷射粒子
             for (int i = 0; i < 30; i++) {
                 double progress = i * 0.3;
                 double spread = (Math.random() - 0.5) * 0.5;
                 
                 level.addParticle(ParticleTypes.FLAME,
                         startPos.x + lookVec.x * progress + spread,
                         startPos.y + lookVec.y * progress + spread,
                         startPos.z + lookVec.z * progress + spread,
                         lookVec.x * 0.1, lookVec.y * 0.1, lookVec.z * 0.1);
             }
         }
     }
     
     private void spawnFlameDamageParticles(Level level, LivingEntity target) {
         if (level.isClientSide()) {
             Vec3 pos = target.getEyePosition();
             
             // 生成火焰伤害粒子
             for (int i = 0; i < 10; i++) {
                 level.addParticle(ParticleTypes.FLAME,
                         pos.x + (Math.random() - 0.5) * 0.5,
                         pos.y + (Math.random() - 0.5) * 0.5,
                         pos.z + (Math.random() - 0.5) * 0.5,
                         0, 0.05, 0);
             }
         }
     }
     
     private static void spawnEmberExplosionParticles(Level level, LivingEntity deadEntity) {
         if (level.isClientSide()) {
             Vec3 pos = deadEntity.getEyePosition();
             
             // 生成余烬爆燃粒子
             for (int i = 0; i < 50; i++) {
                 level.addParticle(ParticleTypes.LAVA,
                         pos.x + (Math.random() - 0.5) * 2.0,
                         pos.y + (Math.random() - 0.5) * 2.0,
                         pos.z + (Math.random() - 0.5) * 2.0,
                         (Math.random() - 0.5) * 0.2,
                         Math.random() * 0.2,
                         (Math.random() - 0.5) * 0.2);
             }
         }
     }
     
     // 魔力相关方法
     private boolean hasEnoughMana(Player player) {// 使用ManaSystem检查玩家魔力值是否足够
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
         //TODO
         return false;
     }
     
     /**
      * 定期清理过期的灼痕印记
      */
     @Override
     public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
         super.inventoryTick(stack, level, entity, slotId, isSelected);
         
         if (!level.isClientSide() && entity instanceof Player player && isSelected) {
             // 清理超过持续时间的灼痕印记
             burnMarkStacks.entrySet().removeIf(entry -> {
                 // 这里需要实现灼痕持续时间检查
                 // 暂时保留所有印记
                 return false;
             });
         }
     }
}