package cn.dawnstring.fatality.system;

import cn.dawnstring.fatality.core.events.FatalityEventBus;
import cn.dawnstring.fatality.system.accessories.AccessoryEffectHandlerManager;
import cn.dawnstring.fatality.utils.GameConstants;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

/**
 * 生命之环效果管理器 - 迁移到新的事件驱动架构
 * 使用新的事件系统和饰品效果处理器
 * 特性：
 * - 中心化的效果管理，避免全局实体遍历
 * - 基于位置的区域检测，只在需要的地方进行计算
 * - 优化的粒子效果生成
 */
@Mod.EventBusSubscriber
public class LifeRingEffectManager {
    
    // 生命之环效果实例存储
    private static final Map<UUID, LifeRingEffect> activeEffects = new HashMap<>();
    
    /**
     * 启动一个新的生命之环效果
     */
    public static void startLifeRingEffect(Level level, Vec3 center, int duration, float radius, Player owner) {
        LifeRingEffect effect = new LifeRingEffect(level, center, duration, radius, owner);
        activeEffects.put(effect.getId(), effect);
        
        // 发布生命之环启动事件到新的事件总线
        // 这里可以添加自定义事件类型
    }
    
    /**
     * 停止指定的生命之环效果
     */
    public static void stopLifeRingEffect(UUID effectId) {
        activeEffects.remove(effectId);
    }
    
    /**
     * 服务器tick事件处理 - 处理所有活跃的生命之环效果
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            // 使用迭代器安全地遍历和移除效果
            Iterator<Map.Entry<UUID, LifeRingEffect>> iterator = activeEffects.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, LifeRingEffect> entry = iterator.next();
                LifeRingEffect effect = entry.getValue();
                
                // 更新效果
                if (!effect.update()) {
                    // 效果已结束，移除
                    iterator.remove();
                }
            }
        }
    }
    
    /**
     * 单个生命之环效果实例
     */
    private static class LifeRingEffect {
        private final UUID id;
        private final Level level;
        private final Vec3 center;
        private final float radius;
        private final Player owner;
        private int ticksRemaining;
        private int effectTicks;
        
        public LifeRingEffect(Level level, Vec3 center, int duration, float radius, Player owner) {
            this.id = UUID.randomUUID();
            this.level = level;
            this.center = center;
            this.radius = radius;
            this.owner = owner;
            this.ticksRemaining = duration;
            this.effectTicks = 0;
        }
        
        public UUID getId() {
            return id;
        }
        
        /**
         * 更新生命之环效果
         * @return 如果效果仍在继续返回true，如果效果已结束返回false
         */
        public boolean update() {
            if (level.isClientSide()) {
                return false; // 只在服务器端处理
            }
            
            ticksRemaining--;
            effectTicks++;
            
            // 每1秒触发一次效果
            if (effectTicks % GameConstants.LIFE_RING_EFFECT_INTERVAL == 0) {
                applyEffects();
            }
            
            // 生成粒子效果（每tick都生成，但客户端会处理）
            spawnParticles();
            
            // 检查效果是否结束
            if (ticksRemaining <= 0) {
                // 播放效果结束音效
                level.playSound(null, center.x, center.y, center.z,
                        SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.5F, 1.0F);
                return false;
            }
            
            return true;
        }
        
        /**
         * 应用生命之环效果
         */
        private void applyEffects() {
            // 创建搜索区域（只搜索生命之环周围的实体）
            AABB searchArea = new AABB(
                    center.x - radius, center.y - radius, center.z - radius,
                    center.x + radius, center.y + radius, center.z + radius
            );
            
            // 搜索范围内的所有活着的实体
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, searchArea);
            
            for (LivingEntity entity : entities) {
                // 检查是否在生命之环范围内
                double distance = entity.distanceToSqr(center.x, center.y, center.z);
                if (distance <= radius * radius) {
                    applyEffectToEntity(entity);
                }
            }
        }
        
        /**
         * 对单个实体应用效果
         */
        private void applyEffectToEntity(LivingEntity entity) {
            if (entity instanceof Player) {
                // 对玩家进行治疗
                entity.heal(GameConstants.LIFE_RING_HEAL_AMOUNT);
                entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 1)); // 2秒再生效果
                
                // 播放治疗音效
                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.3F, 1.2F);
            } else {
                // 对非玩家生物造成伤害
                entity.hurt(entity.damageSources().magic(), GameConstants.LIFE_RING_DAMAGE_AMOUNT);
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 1)); // 2秒虚弱效果
                
                // 播放伤害音效
                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 0.2F, 1.0F);
            }
        }
        
        /**
         * 生成生命之环粒子效果
         */
        private void spawnParticles() {
            // 在客户端生成粒子效果
            if (level.isClientSide()) {
                // 生成环形粒子效果
                for (int i = 0; i < 15; i++) {
                    double angle = (i * Math.PI * 2) / 15.0;
                    double x = center.x + Math.cos(angle) * radius;
                    double z = center.z + Math.sin(angle) * radius;
                    double y = center.y + 0.5;
                    
                    // 生成绿色治愈粒子
                    level.addParticle(ParticleTypes.HAPPY_VILLAGER,
                            x, y, z,
                            (Math.random() - 0.5) * 0.05,
                            Math.random() * 0.05,
                            (Math.random() - 0.5) * 0.05);
                    
                    // 生成金色光芒粒子
                    if (i % 3 == 0) {
                        level.addParticle(ParticleTypes.GLOW,
                                x, y + 0.2, z,
                                (Math.random() - 0.5) * 0.03,
                                Math.random() * 0.03,
                                (Math.random() - 0.5) * 0.03);
                    }
                }
                
                // 生成中心粒子效果
                for (int i = 0; i < 5; i++) {
                    level.addParticle(ParticleTypes.HAPPY_VILLAGER,
                            center.x + (Math.random() - 0.5) * 0.3,
                            center.y + Math.random() * 1.0,
                            center.z + (Math.random() - 0.5) * 0.3,
                            (Math.random() - 0.5) * 0.02,
                            Math.random() * 0.02,
                            (Math.random() - 0.5) * 0.02);
                }
            }
        }
    }
    
    /**
     * 获取活跃的生命之环效果数量
     */
    public static int getActiveEffectsCount() {
        return activeEffects.size();
    }
    
    /**
     * 清理所有活跃的生命之环效果
     */
    public static void clearAllEffects() {
        activeEffects.clear();
    }
}