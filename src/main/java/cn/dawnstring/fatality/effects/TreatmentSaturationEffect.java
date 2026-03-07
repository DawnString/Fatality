package cn.dawnstring.fatality.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class TreatmentSaturationEffect extends MobEffect {
    
    public TreatmentSaturationEffect() {
        super(MobEffectCategory.NEUTRAL, 0xFF6B6B); // 浅红色
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每tick都更新效果
        return true;
    }
}