package cn.dawnstring.fatality.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import cn.dawnstring.fatality.damage.DragonfireDamageSource;

public class DragonfireBurnEffect extends MobEffect
{
    public DragonfireBurnEffect()
    {
        super(MobEffectCategory.HARMFUL, 0xDC143C); // 深红色，代表龙炎效果
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide()) {
            // 每tick造成2点伤害（每秒40点伤害）
            DragonfireDamageSource damageSource = DragonfireDamageSource.dragonfire(entity, null);
            entity.hurt(damageSource, 2.0F);
            
            // 添加缓慢效果（每级增加1级缓慢）
            if (amplifier >= 0) {
                entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN, 
                    20, // 持续1秒
                    amplifier, // 缓慢等级与放大器相关
                    false, 
                    false
                ));
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每tick都执行效果（20 ticks = 1秒）
        return true;
    }
}