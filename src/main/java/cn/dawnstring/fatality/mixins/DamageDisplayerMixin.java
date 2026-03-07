package cn.dawnstring.fatality.mixins;

import cn.dawnstring.fatality.client.DamageIndicatorManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 伤害显示Mixin（修复特殊攻击伤害显示问题）
 */
@Mixin(LivingEntity.class)
public class DamageDisplayerMixin {

    @Inject(method = "hurt", at = @At("RETURN"))
    private void onHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        // 只在客户端执行
        if (((LivingEntity)(Object)this).level().isClientSide()) {
            LivingEntity target = (LivingEntity)(Object)this;

            // 检查伤害是否成功造成
            boolean damageApplied = cir.getReturnValue();

            // 使用null作为攻击者（移除攻击者限制）
            DamageIndicatorManager.addDamageIndicator(target, amount, null);
        }
    }
}