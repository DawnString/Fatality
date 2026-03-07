package cn.dawnstring.fatality.items.weapon.ranged;

import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import cn.dawnstring.fatality.registry.ModEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 圣炎裁决 - 双模式远程武器
 * 模式一·净化射击：对未标记目标造成伤害+点燃效果（5秒持续伤害）并留下星光标记
 * 模式二·圣炎引爆：对已有星光标记的目标射击，目标处产生爆炸，半径3格
 * 伤害1830 暴击率20 暴击伤害30 浮动0.3 攻击速度1s
 */
public class HolyFlameJudgment extends BaseWeapon {
    
    private static final int COOLDOWN_TICKS = 20; // 冷却时间20tick（1秒）
    private static final float BASE_BULLET_DAMAGE = 1830.0f; // 基础子弹伤害1830
    private static final int STARMARK_DURATION = 160; // 星光标记持续时间8秒（160ticks）
    private static final int BURN_DURATION = 100; // 点燃效果持续时间5秒（100ticks）
    private static final float EXPLOSION_RADIUS = 3.0f; // 爆炸半径3格
    private static final float EXPLOSION_DAMAGE_MULTIPLIER = 0.5f; // 爆炸伤害为基础伤害的50%
    
    // 模式切换状态
    private boolean isExplosionMode = false;
    
    public HolyFlameJudgment() {
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
                return 0;
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.of(Items.NETHERITE_INGOT); // 修复材料：下界合金锭
            }
        }, new Properties().stacksTo(1).fireResistant(), 
              0, // 基础攻击伤害（由子弹伤害决定）
              1.0f, // 攻击速度
              1.0f, // 基础伤害倍率
              0.20f, // 暴击率20%
              0.30f, // 暴击伤害倍率30%
              0.3f, // 伤害浮动
              WeaponEnum.RANGED // 武器类型：远程
        );
        
        setStory("圣炎裁决是一把拥有双模式的神圣狙击枪。净化模式能够标记并点燃敌人，而引爆模式则能引爆标记造成毁灭性爆炸。");
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        // 检查是否按下Shift键切换模式
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                isExplosionMode = !isExplosionMode;
                String modeName = isExplosionMode ? "引爆模式" : "净化模式";
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("圣炎裁决已切换至：" + modeName));
                
                // 播放模式切换音效
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.5F, 1.2F);
                
                // 生成模式切换粒子效果
                spawnModeSwitchParticles(level, player);
            }
            return InteractionResultHolder.success(itemstack);
        }
        
        if (!level.isClientSide()) {
            // 计算子弹伤害
            float bulletDamage = calculateBulletDamage(player, itemstack);
            
            // 查找射击目标
            LivingEntity target = findTarget(player, level, 50.0f);
            
            if (target != null) {
                // 检查目标是否有星光标记
                boolean hasStarlightMark = target.hasEffect(ModEffects.STARLIGHT_MARK.get());
                
                if (isExplosionMode && hasStarlightMark) {
                    // 模式二：圣炎引爆
                    triggerHolyFlameExplosion(level, target, bulletDamage, player);
                } else {
                    // 模式一：净化射击
                    performPurificationShot(level, target, bulletDamage, player, itemstack);
                }
                
                // 播放射击音效
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.0F, 0.8F);
                
                // 生成射击粒子效果
                spawnShotParticles(level, player, target);
            }
            
            // 设置冷却时间
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        }
        
        return InteractionResultHolder.success(itemstack);
    }
    
    /**
     * 查找射击目标
     */
    private LivingEntity findTarget(Player player, Level level, double range) {
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, 
                player.getBoundingBox().inflate(range));
        
        LivingEntity nearestTarget = null;
        double nearestDistance = Double.MAX_VALUE;
        
        for (LivingEntity entity : entities) {
            // 排除玩家自己和友好生物
            if (entity == player || entity.isAlliedTo(player)) {
                continue;
            }
            
            // 检查视线
            if (player.hasLineOfSight(entity)) {
                double distance = player.distanceToSqr(entity);
                if (distance < nearestDistance && distance <= range * range) {
                    nearestTarget = entity;
                    nearestDistance = distance;
                }
            }
        }
        
        return nearestTarget;
    }
    
    /**
     * 执行净化射击（模式一）
     */
    private void performPurificationShot(Level level, LivingEntity target, float damage, Player player, ItemStack weapon) {
        // 应用直接伤害
        boolean damageApplied = target.hurt(target.damageSources().playerAttack(player), damage);
        
        if (damageApplied) {
            // 应用点燃效果
            target.setSecondsOnFire(5); // 5秒点燃
            
            // 应用星光标记效果
            MobEffectInstance starlightMark = new MobEffectInstance(ModEffects.STARLIGHT_MARK.get(), STARMARK_DURATION, 0);
            target.addEffect(starlightMark);
            
            // 播放净化音效
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 0.8F, 1.0F);
            
            // 生成净化粒子效果
            spawnPurificationParticles(level, target);
        }
    }
    
    /**
     * 触发圣炎引爆（模式二）
     */
    private void triggerHolyFlameExplosion(Level level, LivingEntity target, float baseDamage, Player player) {
        Vec3 explosionCenter = target.position();
        float explosionDamage = baseDamage * EXPLOSION_DAMAGE_MULTIPLIER;
        
        // 播放爆炸音效
        level.playSound(null, explosionCenter.x, explosionCenter.y, explosionCenter.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 1.5F, 0.7F);
        
        // 生成爆炸粒子效果
        spawnExplosionParticles(level, explosionCenter);
        
        // 对周围敌人造成范围伤害
        AABB explosionBox = new AABB(
                explosionCenter.x - EXPLOSION_RADIUS, explosionCenter.y - EXPLOSION_RADIUS, explosionCenter.z - EXPLOSION_RADIUS,
                explosionCenter.x + EXPLOSION_RADIUS, explosionCenter.y + EXPLOSION_RADIUS, explosionCenter.z + EXPLOSION_RADIUS
        );
        
        List<LivingEntity> entitiesInRange = level.getEntitiesOfClass(
                LivingEntity.class, explosionBox,
                entity -> entity.isAlive() && entity != player
        );
        
        for (LivingEntity entity : entitiesInRange) {
            // 计算距离衰减
            double distance = explosionCenter.distanceTo(entity.position());
            float distanceMultiplier = (float) Math.max(0, 1.0 - distance / EXPLOSION_RADIUS);
            float finalDamage = explosionDamage * distanceMultiplier;
            
            if (finalDamage > 0) {
                entity.hurt(entity.damageSources().playerAttack(player), finalDamage);
                
                // 击退效果
                Vec3 knockback = entity.position().subtract(explosionCenter).normalize();
                entity.setDeltaMovement(knockback.scale(0.5));
                
                // 对爆炸范围内的敌人也应用点燃效果
                entity.setSecondsOnFire(3); // 3秒点燃
            }
        }
        
        // 移除目标的星光标记
        target.removeEffect(ModEffects.STARLIGHT_MARK.get());
    }
    
    /**
     * 生成模式切换粒子效果
     */
    private void spawnModeSwitchParticles(Level level, Player player) {
        if (level.isClientSide()) {
            Vec3 pos = player.getEyePosition();
            
            // 生成神圣火焰粒子
            for (int i = 0; i < 15; i++) {
                level.addParticle(ParticleTypes.FLAME,
                        pos.x + (Math.random() - 0.5) * 1.0,
                        pos.y + (Math.random() - 0.5) * 1.0,
                        pos.z + (Math.random() - 0.5) * 1.0,
                        0, 0.1, 0);
            }
            
            // 生成星光粒子
            for (int i = 0; i < 10; i++) {
                level.addParticle(ParticleTypes.END_ROD,
                        pos.x + (Math.random() - 0.5) * 0.8,
                        pos.y + (Math.random() - 0.5) * 0.8,
                        pos.z + (Math.random() - 0.5) * 0.8,
                        0, 0.05, 0);
            }
        }
    }
    
    /**
     * 生成射击粒子效果
     */
    private void spawnShotParticles(Level level, Player player, LivingEntity target) {
        if (level.isClientSide()) {
            Vec3 startPos = player.getEyePosition();
            Vec3 endPos = target.getEyePosition();
            
            // 生成弹道粒子效果
            for (int i = 0; i < 20; i++) {
                double progress = (double) i / 19;
                double x = startPos.x + (endPos.x - startPos.x) * progress;
                double y = startPos.y + (endPos.y - startPos.y) * progress;
                double z = startPos.z + (endPos.z - startPos.z) * progress;
                
                level.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0.02, 0);
            }
        }
    }
    
    /**
     * 生成净化粒子效果
     */
    private void spawnPurificationParticles(Level level, LivingEntity target) {
        if (level.isClientSide()) {
            Vec3 pos = target.getEyePosition();
            
            // 生成神圣净化粒子
            for (int i = 0; i < 12; i++) {
                level.addParticle(ParticleTypes.END_ROD,
                        pos.x + (Math.random() - 0.5) * 1.5,
                        pos.y + (Math.random() - 0.5) * 1.5,
                        pos.z + (Math.random() - 0.5) * 1.5,
                        0, 0.08, 0);
            }
            
            // 生成火焰粒子
            for (int i = 0; i < 8; i++) {
                level.addParticle(ParticleTypes.FLAME,
                        pos.x + (Math.random() - 0.5) * 1.2,
                        pos.y + (Math.random() - 0.5) * 1.2,
                        pos.z + (Math.random() - 0.5) * 1.2,
                        0, 0.05, 0);
            }
        }
    }
    
    /**
     * 生成爆炸粒子效果
     */
    private void spawnExplosionParticles(Level level, Vec3 center) {
        if (level.isClientSide()) {
            // 生成爆炸火焰粒子
            for (int i = 0; i < 25; i++) {
                double angle = Math.random() * Math.PI * 2;
                double radius = Math.random() * EXPLOSION_RADIUS;
                double x = center.x + Math.cos(angle) * radius;
                double z = center.z + Math.sin(angle) * radius;
                double y = center.y + (Math.random() - 0.5) * EXPLOSION_RADIUS;
                
                level.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0.1, 0);
            }
            
            // 生成爆炸烟雾粒子
            for (int i = 0; i < 15; i++) {
                level.addParticle(ParticleTypes.SMOKE,
                        center.x + (Math.random() - 0.5) * EXPLOSION_RADIUS,
                        center.y + (Math.random() - 0.5) * EXPLOSION_RADIUS,
                        center.z + (Math.random() - 0.5) * EXPLOSION_RADIUS,
                        0, 0.05, 0);
            }
        }
    }
    
    /**
     * 计算子弹伤害（使用BaseWeapon的伤害计算逻辑）
     */
    private float calculateBulletDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }
    
    @Override
    public String getSpecialEffectDescription() {
        String currentMode = isExplosionMode ? "引爆模式" : "净化模式";
        return "当前模式：" + currentMode + 
               " | 星光标记：8秒" + 
               " | 爆炸半径：3格" + 
               " | 点燃效果：5秒";
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // 始终显示附魔光效
    }
}