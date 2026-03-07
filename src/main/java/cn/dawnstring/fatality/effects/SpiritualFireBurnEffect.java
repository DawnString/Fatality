package cn.dawnstring.fatality.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import cn.dawnstring.fatality.damage.SpiritualFireDamageSource;

public class SpiritualFireBurnEffect extends MobEffect
{
    public SpiritualFireBurnEffect()
    {
        super(MobEffectCategory.HARMFUL, 0xFF4500); // 橙色，代表火焰效果
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 每tick造成4点伤害
        if (!entity.level().isClientSide()) {
            // 使用自定义伤害源，显示自定义死亡消息
            SpiritualFireDamageSource damageSource = SpiritualFireDamageSource.spiritualFire(entity, null);
            entity.hurt(damageSource, 4.0F);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每tick都执行效果（20 ticks = 1秒）
        return true;
    }
}