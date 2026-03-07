package cn.dawnstring.fatality.mixins;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RangedAttribute.class)
public class AttributeMixin {

    /**
     * 移除属性上限限制
     * 当获取最大生命值属性的上限时，返回Double.MAX_VALUE而不是1024
     */
    @Inject(method = "sanitizeValue", at = @At("HEAD"), cancellable = true)
    private void removeAttributeLimit(double value, CallbackInfoReturnable<Double> cir) {
        RangedAttribute attribute = (RangedAttribute) (Object) this;

        // 检查是否是最大生命值属性
        if (attribute.getDescriptionId().equals("attribute.name.generic.max_health")) {
            // 完全移除限制，返回原始值
            cir.setReturnValue(value);
        }
    }

    /**
     * 移除属性上限限制
     * 当获取最大生命值属性的上限时，返回Double.MAX_VALUE而不是1024
     */
    @Inject(method = "getMaxValue", at = @At("HEAD"), cancellable = true)
    private void removeMaxValueLimit(CallbackInfoReturnable<Double> cir) {
        RangedAttribute attribute = (RangedAttribute) (Object) this;

        // 检查是否是最大生命值属性
        if (attribute.getDescriptionId().equals("attribute.name.generic.max_health")) {
            // 返回Double.MAX_VALUE来完全移除1024的限制
            cir.setReturnValue(Double.MAX_VALUE);
        }
    }
}