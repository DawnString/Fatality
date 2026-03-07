package cn.dawnstring.fatality.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 罪印标记效果
 * 使目标受到的伤害提升30%
 */
public class SinMarkEffect extends MobEffect {
    
    public SinMarkEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B0000); // 暗红色
        
        // 添加属性修改器，增加受到的伤害
        this.addAttributeModifier(Attributes.ARMOR, 
                "c4c4c4c4-4c4c-4c4c-4c4c-c4c4c4c4c4c4", 
                0.30, // 增加30%受到的伤害
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 这里可以添加额外的视觉效果或逻辑
        // 例如：生成罪印粒子效果
        if (entity.level().isClientSide()) {
            // 生成罪印粒子效果
            for (int i = 0; i < 2; i++) {
                entity.level().addParticle(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                        entity.getX() + (entity.level().random.nextDouble() - 0.5) * 0.8,
                        entity.getY() + entity.level().random.nextDouble() * 2.0,
                        entity.getZ() + (entity.level().random.nextDouble() - 0.5) * 0.8,
                        0, 0.02, 0);
            }
        }
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每20ticks（1秒）执行一次效果
        return duration % 20 == 0;
    }
    
    @Override
    public String getDescriptionId() {
        return "effect.fatality.sin_mark";
    }
}