package cn.dawnstring.fatality.items.weapon.ranged;

import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import cn.dawnstring.fatality.registry.ModEffects;
import cn.dawnstring.fatality.entity.projectile.AnnihilationBulletProjectile;
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
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 寂灭裁决
 * 远程武器，狙击枪
 * 功能：
 * 1. 单击右键发射子弹造成伤害并留下星光标记（下次对标记目标伤害+50%并清除标记）
 * 2. 长按右键蓄力3秒发射终末粒子生成2格半径寂灭领域（持续伤害0.5倍，黑色粒子环绕）
 * 属性：3750伤害，30%暴击率，38暴击伤害，0.3浮动值，1s攻击速度
 */
public class AnnihilationJudgment extends BaseWeapon {
    
    // 武器属性
    private static final int BASE_DAMAGE = 3750;
    private static final float ATTACK_SPEED = 1.0f;
    private static final float BASE_DAMAGE_MULTIPLIER = 1.0f;
    private static final float CRITICAL_CHANCE = 0.3f;
    private static final float CRITICAL_DAMAGE = 0.38f;
    private static final float DAMAGE_FLUCTUATION = 0.3f;
    
    // 蓄力相关参数
    private static final int CHARGE_TIME_TICKS = 60; // 3秒蓄力（20 ticks/秒 * 3秒）
    private static final float ANNIHILATION_FIELD_RADIUS = 2.0f; // 寂灭领域半径
    private static final float ANNIHILATION_DAMAGE_MULTIPLIER = 0.5f; // 寂灭领域伤害倍率
    private static final int ANNIHILATION_DURATION_TICKS = 100; // 寂灭领域持续时间（5秒）
    
    // 星光标记相关参数
    private static final float MARKED_DAMAGE_BONUS = 0.5f; // 标记目标伤害加成
    private static final int MARK_DURATION_TICKS = 200; // 标记持续时间（10秒）
    
    // 存储标记的目标
    private final Map<UUID, Long> markedTargets = new HashMap<>();
    
    public AnnihilationJudgment() {
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
              WeaponEnum.RANGED // 武器类型：远程
         );
     }
     
     /**
      * 右键使用方法 - 实现单击和蓄力功能
      */
     @Override
     public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
         ItemStack itemstack = player.getItemInHand(hand);
         
         // 开始蓄力，返回consume表示开始使用物品
         return InteractionResultHolder.consume(itemstack);
     }
     
     /**
      * 获取使用持续时间（蓄力时间）
      */
     @Override
     public int getUseDuration(ItemStack stack) {
         return CHARGE_TIME_TICKS; // 3秒蓄力时间
     }
     
     /**
      * 使用动画类型
      */
     @Override
     public UseAnim getUseAnimation(ItemStack stack) {
         return UseAnim.BOW; // 使用弓的动画
     }
     
     /**
      * 释放右键时触发（蓄力完成或提前释放）
      */
     @Override
     public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
         if (!level.isClientSide() && entity instanceof Player player) {
             // 计算实际蓄力时间
             int chargeTime = getUseDuration(stack) - timeCharged;
             
             if (chargeTime >= CHARGE_TIME_TICKS) {
                 // 蓄力完成，发射终末粒子
                 fireAnnihilationParticle(player, level, stack);
             } else {
                 // 提前释放，发射普通子弹
                 fireNormalShot(player, level, stack);
             }
             
             // 设置冷却时间
             player.getCooldowns().addCooldown(this, 20); // 1秒冷却
         }
     }
     
     /**
     * 发射普通子弹攻击
     */
    private void fireNormalShot(Player player, Level level, ItemStack weapon) {
        // 计算伤害
        float damage = calculateFinalDamage(player, weapon, null);
        
        // 创建子弹投射物（移除射线检测，让子弹命中时处理标记逻辑）
        AnnihilationBulletProjectile bullet = new AnnihilationBulletProjectile(level, player, weapon, damage, null, false);
        
        // 添加到世界
        level.addFreshEntity(bullet);
        
        // 播放射击音效
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 0.8F, 1.2F);
        
        // 生成射击粒子效果
        spawnShootParticles(level, player);
    }
     
     /**
      * 发射终末粒子生成寂灭领域
      */
     private void fireAnnihilationParticle(Player player, Level level, ItemStack weapon) {
         // 查找目标位置
         Vec3 targetPos = findTargetPosition(player, level, 30.0f);
         
         if (targetPos != null) {
             // 创建寂灭领域
             createAnnihilationField(level, targetPos, player, weapon);
             
             // 播放领域生成音效
             level.playSound(null, targetPos.x, targetPos.y, targetPos.z,
                     SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0F, 0.5F);
             
             // 显示领域生成信息
             if (level.isClientSide) {
                 player.displayClientMessage(
                         net.minecraft.network.chat.Component.literal("§4寂灭领域已生成！"),
                         true
                 );
             }
         }
         
         // 生成终末粒子效果
         spawnAnnihilationParticles(level, player, targetPos);
     }
     
     /**
      * 创建寂灭领域
      */
     private void createAnnihilationField(Level level, Vec3 center, Player player, ItemStack weapon) {
         // 获取领域内的所有实体
         AABB fieldBounds = new AABB(
                 center.x - ANNIHILATION_FIELD_RADIUS, center.y - ANNIHILATION_FIELD_RADIUS, center.z - ANNIHILATION_FIELD_RADIUS,
                 center.x + ANNIHILATION_FIELD_RADIUS, center.y + ANNIHILATION_FIELD_RADIUS, center.z + ANNIHILATION_FIELD_RADIUS
         );
         
         // 为领域内的实体应用持续伤害效果
         for (Entity entity : level.getEntities(player, fieldBounds)) {
             if (entity instanceof LivingEntity target && target != player) {
                 // 应用星光标记效果（作为领域效果的视觉指示）
                 target.addEffect(new MobEffectInstance(ModEffects.STARLIGHT_MARK.get(), ANNIHILATION_DURATION_TICKS, 0));
                 
                 // 设置伤害来源信息
                 target.getPersistentData().putFloat("annihilation_damage", calculateFinalDamage(player, weapon, target) * ANNIHILATION_DAMAGE_MULTIPLIER);
                 target.getPersistentData().putUUID("annihilation_source", player.getUUID());
             }
         }
         
         // 生成领域边界粒子
         spawnFieldBoundaryParticles(level, center);
         
         // 启动持续伤害计时器
         startAnnihilationFieldTimer(level, center, player, weapon);
     }
     
     /**
      * 启动寂灭领域持续伤害计时器
      */
     private void startAnnihilationFieldTimer(Level level, Vec3 center, Player player, ItemStack weapon) {
         // 每20ticks（1秒）造成一次伤害，持续5秒
         for (int i = 1; i <= 5; i++) {
             final int tickDelay = i * 20;
             level.getServer().execute(() -> {
                 try {
                     Thread.sleep(tickDelay * 50); // 转换为毫秒
                     
                     if (!level.isClientSide() && player.isAlive()) {
                         // 获取领域内的所有实体
                         AABB fieldBounds = new AABB(
                                 center.x - ANNIHILATION_FIELD_RADIUS, center.y - ANNIHILATION_FIELD_RADIUS, center.z - ANNIHILATION_FIELD_RADIUS,
                                 center.x + ANNIHILATION_FIELD_RADIUS, center.y + ANNIHILATION_FIELD_RADIUS, center.z + ANNIHILATION_FIELD_RADIUS
                         );
                         
                         for (Entity entity : level.getEntities(player, fieldBounds)) {
                             if (entity instanceof LivingEntity target && target != player) {
                                 // 计算领域伤害
                                 float damage = calculateFinalDamage(player, weapon, target) * ANNIHILATION_DAMAGE_MULTIPLIER;
                                 
                                 // 造成持续伤害
                                 target.hurt(target.damageSources().magic(), damage);
                                 
                                 // 生成领域伤害粒子
                                 spawnFieldDamageParticles(level, target);
                             }
                         }
                     }
                 } catch (InterruptedException e) {
                     Thread.currentThread().interrupt();
                 }
             });
         }
     }
     
     /**
      * 应用星光标记效果
      */
     private void applyStarlightMark(LivingEntity target, Player player) {
         // 应用星光标记效果
         target.addEffect(new MobEffectInstance(ModEffects.STARLIGHT_MARK.get(), MARK_DURATION_TICKS, 0));
         
         // 记录标记目标
         markedTargets.put(target.getUUID(), System.currentTimeMillis());
         
         // 显示标记信息
         if (player.level().isClientSide) {
             player.displayClientMessage(
                     net.minecraft.network.chat.Component.literal("§b星光标记已应用！"),
                     true
             );
         }
     }
    
    /**
     * 查找目标位置（用于寂灭领域）
     */
    private Vec3 findTargetPosition(Player player, Level level, double range) {
         Vec3 startPos = player.getEyePosition();
         Vec3 lookVec = player.getLookAngle();
         Vec3 endPos = startPos.add(lookVec.scale(range));
         
         // 使用射线检测查找碰撞点
         BlockHitResult blockResult = level.clip(new ClipContext(
                 startPos, endPos, ClipContext.Block.COLLIDER,
                 ClipContext.Fluid.NONE, player
         ));
         
         return blockResult.getLocation();
     }
     
     // 粒子效果生成方法
     private void spawnShootParticles(Level level, Player player) {
         if (level.isClientSide()) {
             Vec3 pos = player.getEyePosition();
             Vec3 lookVec = player.getLookAngle();
             
             // 生成射击轨迹粒子
             for (int i = 0; i < 10; i++) {
                 double progress = i * 0.3;
                 level.addParticle(ParticleTypes.END_ROD,
                         pos.x + lookVec.x * progress,
                         pos.y + lookVec.y * progress,
                         pos.z + lookVec.z * progress,
                         lookVec.x * 0.1, lookVec.y * 0.1, lookVec.z * 0.1);
             }
         }
     }
     
     private void spawnHitParticles(Level level, LivingEntity target) {
         if (level.isClientSide()) {
             Vec3 pos = target.getEyePosition();
             
             // 生成命中爆炸粒子
             for (int i = 0; i < 15; i++) {
                 level.addParticle(ParticleTypes.GLOW,
                         pos.x + (Math.random() - 0.5) * 1.0,
                         pos.y + (Math.random() - 0.5) * 1.0,
                         pos.z + (Math.random() - 0.5) * 1.0,
                         0, 0.1, 0);
             }
         }
     }
     
     private void spawnChargingParticles(Level level, Player player) {
         if (level.isClientSide()) {
             Vec3 pos = player.getEyePosition();
             
             // 生成蓄力环绕粒子
             for (int i = 0; i < 20; i++) {
                 double angle = i * Math.PI * 2 / 20;
                 double radius = 1.0;
                 
                 level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                         pos.x + Math.cos(angle) * radius,
                         pos.y + 0.5,
                         pos.z + Math.sin(angle) * radius,
                         0, 0.05, 0);
             }
         }
     }
     
     private void spawnAnnihilationParticles(Level level, Player player, Vec3 targetPos) {
         if (level.isClientSide() && targetPos != null) {
             // 生成终末粒子轨迹
             Vec3 startPos = player.getEyePosition();
             Vec3 direction = targetPos.subtract(startPos).normalize();
             
             for (int i = 0; i < 30; i++) {
                 double progress = i * 0.1;
                 level.addParticle(ParticleTypes.SMOKE,
                         startPos.x + direction.x * progress,
                         startPos.y + direction.y * progress,
                         startPos.z + direction.z * progress,
                         0, 0, 0);
             }
         }
     }
     
     private void spawnFieldDamageParticles(Level level, LivingEntity target) {
         if (level.isClientSide()) {
             Vec3 pos = target.getEyePosition();
             
             // 生成领域伤害粒子
             for (int i = 0; i < 5; i++) {
                 level.addParticle(ParticleTypes.SMOKE,
                         pos.x + (Math.random() - 0.5) * 0.5,
                         pos.y + (Math.random() - 0.5) * 0.5,
                         pos.z + (Math.random() - 0.5) * 0.5,
                         0, 0.05, 0);
             }
         }
     }
     
     private void spawnFieldBoundaryParticles(Level level, Vec3 center) {
         if (level.isClientSide()) {
             // 生成领域边界环绕粒子
             for (int i = 0; i < 50; i++) {
                 double angle = i * Math.PI * 2 / 50;
                 double radius = ANNIHILATION_FIELD_RADIUS;
                 
                 level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                         center.x + Math.cos(angle) * radius,
                         center.y + 0.5,
                         center.z + Math.sin(angle) * radius,
                         0, 0.05, 0);
             }
         }
     }
     
     /**
     * 子弹命中实体时的处理逻辑
     */
    public static void onBulletHit(LivingEntity target, Level level, Player player, UUID targetUUID, boolean isMarkedTarget, float baseDamage) {
        if (target == null || player == null) return;
        
        // 检查目标是否已被标记
        if (isMarkedTarget) {
            // 对标记目标造成额外伤害
            float damage = baseDamage * (1.0f + MARKED_DAMAGE_BONUS);
            
            // 清除标记
            target.removeEffect(ModEffects.STARLIGHT_MARK.get());
            
            // 显示标记清除信息
            if (level.isClientSide()) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§b星光标记已清除！造成额外伤害"),
                        true
                );
            }
            
            // 造成伤害
            target.hurt(target.damageSources().playerAttack(player), damage);
            
            // 显示伤害信息
            if (level.isClientSide()) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§b造成 " + String.format("%.1f", damage) + " 伤害"),
                        true
                );
            }
        } else {
            // 对未标记目标应用星光标记
            target.addEffect(new MobEffectInstance(ModEffects.STARLIGHT_MARK.get(), MARK_DURATION_TICKS, 0));
            
            // 显示标记信息
            if (level.isClientSide()) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§b星光标记已应用！"),
                        true
                );
            }
        }
        
        // 播放命中音效
        level.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.0F);
        
        // 生成命中粒子效果
        if (level.isClientSide()) {
            Vec3 pos = target.getEyePosition();
            for (int i = 0; i < 15; i++) {
                level.addParticle(ParticleTypes.GLOW,
                        pos.x + (Math.random() - 0.5) * 1.0,
                        pos.y + (Math.random() - 0.5) * 1.0,
                        pos.z + (Math.random() - 0.5) * 1.0,
                        0, 0.1, 0);
            }
        }
    }
    
    /**
     * 定期清理过期的标记
     */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        
        if (!level.isClientSide() && entity instanceof Player player && isSelected) {
            long currentTime = System.currentTimeMillis();
            
            // 清理超过标记持续时间的记录
            markedTargets.entrySet().removeIf(entry -> 
                currentTime - entry.getValue() > MARK_DURATION_TICKS * 50
            );
        }
    }
}