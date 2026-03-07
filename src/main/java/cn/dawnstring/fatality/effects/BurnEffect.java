package cn.dawnstring.fatality.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import cn.dawnstring.fatality.damage.BurnDamageSource;

public class BurnEffect extends MobEffect
{
    public BurnEffect()
    {
        super(MobEffectCategory.HARMFUL, 0xFF4500); // 橙色，代表火焰效果
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 每tick造成1点伤害（每秒20点伤害）
        if (!entity.level().isClientSide()) {
            // 使用自定义灼烧伤害源，显示自定义死亡消息
            BurnDamageSource damageSource = BurnDamageSource.burn(entity, null);
            entity.hurt(damageSource, 1.0F);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每tick都执行效果（20 ticks = 1秒）
        return true;
    }
}