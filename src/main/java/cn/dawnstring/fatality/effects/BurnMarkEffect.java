package cn.dawnstring.fatality.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 灼痕印记效果
 * 每层使目标受到的火焰伤害提升15%，最多叠加3层
 */
public class BurnMarkEffect extends MobEffect {
    
    public BurnMarkEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF4500); // 橙色
        
        // 添加属性修改器，增加受到的火焰伤害
        this.addAttributeModifier(Attributes.ARMOR, 
                "b3b3b3b3-3b3b-3b3b-3b3b-b3b3b3b3b3b3", 
                0.15, // 每层增加15%火焰伤害
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 这里可以添加额外的视觉效果或逻辑
        // 例如：生成灼痕粒子效果
        if (entity.level().isClientSide()) {
            // 生成灼痕粒子效果
            for (int i = 0; i < 3; i++) {
                entity.level().addParticle(net.minecraft.core.particles.ParticleTypes.FLAME,
                        entity.getX() + (entity.level().random.nextDouble() - 0.5) * 0.5,
                        entity.getY() + entity.level().random.nextDouble() * 1.5,
                        entity.getZ() + (entity.level().random.nextDouble() - 0.5) * 0.5,
                        0, 0.05, 0);
            }
        }
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每10ticks（0.5秒）执行一次效果
        return duration % 10 == 0;
    }
    
    @Override
    public String getDescriptionId() {
        return "effect.fatality.burn_mark";
    }
}