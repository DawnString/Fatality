package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.registry.ModEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 星辰裁决投射物 - 实现星光标记和星爆功能
 */
public class StarryJudgmentProjectile extends AbstractArrow {
    
    // 跟踪每个目标的星光标记次数
    private static final Map<UUID, Integer> starlightMarkCounts = new HashMap<>();
    
    // 星光标记持续时间（8秒，以ticks为单位）
    private static final int STARLIGHT_MARK_DURATION = 160; // 8秒 * 20 ticks/秒
    
    // 星爆触发所需的标记次数
    private static final int STARBURST_TRIGGER_COUNT = 3;
    
    // 星爆范围
    private static final float STARBURST_RANGE = 4.0f;
    
    // 星爆伤害倍率
    private static final float STARBURST_DAMAGE_MULTIPLIER = 0.5f;
    
    private final float damage;
    private final Player shooter;
    
    public StarryJudgmentProjectile(EntityType<? extends StarryJudgmentProjectile> type, Level level) {
        super(type, level);
        this.damage = 0;
        this.shooter = null;
    }
    
    public StarryJudgmentProjectile(Level level, Player shooter, ItemStack weapon, float damage) {
        super(EntityType.ARROW, shooter, level);
        this.damage = damage;
        this.shooter = shooter;
        
        // 设置投射物属性
        this.setBaseDamage(damage);
        this.setPierceLevel((byte) 0); // 不穿透
        this.setCritArrow(true); // 总是暴击（狙击枪特性）
    }
    
    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        
        if (!this.level().isClientSide()) {
            // 播放命中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.8F, 0.6F);
            
            // 生成命中粒子效果
            spawnHitParticles();
            
            this.discard();
        }
    }
    
    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();
        
        if (entity instanceof LivingEntity target && shooter != null) {
            // 应用星光标记效果
            applyStarlightMark(target);
            
            // 检查是否触发星爆
            checkStarburstTrigger(target);
            
            // 播放实体命中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6F, 0.8F);
        }
    }
    
    /**
     * 应用星光标记效果
     */
    private void applyStarlightMark(LivingEntity target) {
        UUID targetId = target.getUUID();
        
        // 获取当前标记次数
        int currentCount = starlightMarkCounts.getOrDefault(targetId, 0);
        int newCount = currentCount + 1;
        
        // 更新标记次数
        starlightMarkCounts.put(targetId, newCount);
        
        // 应用星光标记效果（8秒持续时间）
        MobEffectInstance starlightMark = new MobEffectInstance(
                ModEffects.STARLIGHT_MARK.get(),
                STARLIGHT_MARK_DURATION,
                Math.min(newCount - 1, 2), // 效果等级（最多2级）
                false, // 不显示粒子效果（我们自己处理）
                true   // 显示图标
        );
        
        target.addEffect(starlightMark);
        
        // 生成星光标记粒子效果
        spawnStarlightMarkParticles(target);
    }
    
    /**
     * 检查是否触发星爆
     */
    private void checkStarburstTrigger(LivingEntity target) {
        UUID targetId = target.getUUID();
        int currentCount = starlightMarkCounts.getOrDefault(targetId, 0);
        
        if (currentCount >= STARBURST_TRIGGER_COUNT) {
            // 触发星爆
            triggerStarburst(target);
            
            // 重置标记次数
            starlightMarkCounts.remove(targetId);
        }
    }
    
    /**
     * 触发星爆效果
     */
    private void triggerStarburst(LivingEntity target) {
        // 计算星爆伤害（基础伤害的50%）
        float starburstDamage = damage * STARBURST_DAMAGE_MULTIPLIER;
        
        // 播放星爆音效
        this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 1.0F, 0.5F);
        
        // 生成星爆粒子效果
        spawnStarburstParticles(target);
        
        // 对周围敌人造成AOE伤害
        for (LivingEntity entity : target.level().getEntitiesOfClass(LivingEntity.class, 
                target.getBoundingBox().inflate(STARBURST_RANGE))) {
            
            // 排除玩家自己和目标本身
            if (entity == shooter || entity == target || entity.isAlliedTo(shooter)) {
                continue;
            }
            
            // 造成星爆伤害
            entity.hurt(entity.damageSources().playerAttack(shooter), starburstDamage);
        }
    }
    
    /**
     * 生成星光标记粒子效果
     */
    private void spawnStarlightMarkParticles(LivingEntity target) {
        if (target.level().isClientSide()) {
            Vec3 pos = target.getEyePosition();
            
            // 生成蓝色星光粒子
            for (int i = 0; i < 8; i++) {
                target.level().addParticle(ParticleTypes.END_ROD,
                        pos.x + (Math.random() - 0.5) * 1.0,
                        pos.y + (Math.random() - 0.5) * 1.0,
                        pos.z + (Math.random() - 0.5) * 1.0,
                        0, 0.1, 0);
            }
            
            // 生成闪烁粒子
            for (int i = 0; i < 5; i++) {
                target.level().addParticle(ParticleTypes.GLOW,
                        pos.x + (Math.random() - 0.5) * 0.8,
                        pos.y + (Math.random() - 0.5) * 0.8,
                        pos.z + (Math.random() - 0.5) * 0.8,
                        0, 0.05, 0);
            }
        }
    }
    
    /**
     * 生成星爆粒子效果
     */
    private void spawnStarburstParticles(LivingEntity target) {
        if (target.level().isClientSide()) {
            Vec3 pos = target.getEyePosition();
            
            // 生成星爆爆炸粒子
            for (int i = 0; i < 20; i++) {
                double angle = Math.random() * Math.PI * 2;
                double distance = Math.random() * STARBURST_RANGE;
                double offsetX = Math.cos(angle) * distance;
                double offsetZ = Math.sin(angle) * distance;
                double offsetY = (Math.random() - 0.5) * STARBURST_RANGE;
                
                target.level().addParticle(ParticleTypes.GLOW_SQUID_INK,
                        pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                        (Math.random() - 0.5) * 0.3,
                        (Math.random() - 0.5) * 0.3,
                        (Math.random() - 0.5) * 0.3);
            }
            
            // 生成金色星光粒子
            for (int i = 0; i < 15; i++) {
                target.level().addParticle(ParticleTypes.END_ROD,
                        pos.x + (Math.random() - 0.5) * STARBURST_RANGE,
                        pos.y + (Math.random() - 0.5) * STARBURST_RANGE,
                        pos.z + (Math.random() - 0.5) * STARBURST_RANGE,
                        (Math.random() - 0.5) * 0.5,
                        (Math.random() - 0.5) * 0.5,
                        (Math.random() - 0.5) * 0.5);
            }
        }
    }
    
    /**
     * 生成命中粒子效果
     */
    private void spawnHitParticles() {
        if (this.level().isClientSide()) {
            Vec3 pos = this.position();
            
            for (int i = 0; i < 10; i++) {
                this.level().addParticle(ParticleTypes.GLOW,
                        pos.x + (Math.random() - 0.5) * 0.5,
                        pos.y + (Math.random() - 0.5) * 0.5,
                        pos.z + (Math.random() - 0.5) * 0.5,
                        0, 0.1, 0);
            }
        }
    }
    
    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }
}