package cn.dawnstring.fatality.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import cn.dawnstring.fatality.damage.CurseFireDamageSource;

public class CurseFireBurningEffect extends MobEffect
{
    public CurseFireBurningEffect()
    {
        super(MobEffectCategory.HARMFUL, 0x8B0000); // 深红色，代表诅咒火焰效果
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 每tick造成1点伤害（每秒20点伤害）
        if (!entity.level().isClientSide()) {
            // 使用自定义诅咒火焰伤害源，显示自定义死亡消息
            CurseFireDamageSource damageSource = CurseFireDamageSource.curseFire(entity, null);
            entity.hurt(damageSource, 1.0F);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每tick都执行效果（20 ticks = 1秒）
        return true;
    }
}