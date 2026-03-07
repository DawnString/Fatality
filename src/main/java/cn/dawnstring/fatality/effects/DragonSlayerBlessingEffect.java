package cn.dawnstring.fatality.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class DragonSlayerBlessingEffect extends MobEffect
{
    public DragonSlayerBlessingEffect()
    {
        super(MobEffectCategory.BENEFICIAL, 0x00FF00); // 绿色，代表祝福效果
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide()) {
            // 添加生命恢复效果（每级增加1级恢复）
            if (amplifier >= 0) {
                entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    MobEffects.REGENERATION, 
                    40, // 持续2秒
                    amplifier, // 恢复等级与放大器相关
                    false, 
                    false
                ));
            }
            
            // 添加速度提升效果（每级增加1级速度）
            if (amplifier >= 0) {
                entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    MobEffects.MOVEMENT_SPEED, 
                    40, // 持续2秒
                    amplifier, // 速度等级与放大器相关
                    false, 
                    false
                ));
            }
            
            // 添加跳跃增强效果（每级增加1级跳跃）
            if (amplifier >= 0) {
                entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    MobEffects.JUMP, 
                    40, // 持续2秒
                    amplifier, // 跳跃等级与放大器相关
                    false, 
                    false
                ));
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每2秒执行一次效果（40 ticks = 2秒）
        return duration % 40 == 0;
    }
}