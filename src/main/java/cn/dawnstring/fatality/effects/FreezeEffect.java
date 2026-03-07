package cn.dawnstring.fatality.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import java.util.UUID;

public class FreezeEffect extends MobEffect
{
    // 移动速度减少的UUID（每个等级使用不同的UUID）
    private static final UUID[] SPEED_MODIFIER_UUIDS = {
        UUID.fromString("12345678-1234-1234-1234-123456789001"),
        UUID.fromString("12345678-1234-1234-1234-123456789002"),
        UUID.fromString("12345678-1234-1234-1234-123456789003"),
        UUID.fromString("12345678-1234-1234-1234-123456789004"),
        UUID.fromString("12345678-1234-1234-1234-123456789005")
    };

    public FreezeEffect()
    {
        super(MobEffectCategory.HARMFUL, 0x87CEEB); // 浅蓝色
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 检查是否为boss，如果是boss则无效
        if (isBoss(entity)) {
            return;
        }

        // 根据效果等级计算移动速度减少百分比
        float speedReduction = calculateSpeedReduction(amplifier);
        
        // 移除旧的移动速度修改器（如果有）
        if (amplifier < SPEED_MODIFIER_UUIDS.length) {
            entity.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPEED_MODIFIER_UUIDS[amplifier]);
        }
        
        // 应用新的移动速度修改器
        if (amplifier < SPEED_MODIFIER_UUIDS.length) {
            AttributeModifier speedModifier = new AttributeModifier(
                SPEED_MODIFIER_UUIDS[amplifier],
                "Freeze speed reduction",
                -speedReduction,
                AttributeModifier.Operation.MULTIPLY_TOTAL
            );
            entity.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(speedModifier);
        }
        
        // 如果达到5级，完全冻结目标
        if (amplifier >= 4) {
            // 完全冻结：设置移动速度为0
            entity.setDeltaMovement(0, entity.getDeltaMovement().y, 0);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每tick都更新效果
        return true;
    }

    /**
     * 检查实体是否为boss
     */
    private boolean isBoss(LivingEntity entity) {
        // 检查常见的boss类型
        String entityName = entity.getType().getDescriptionId();
        return entityName.contains("wither") || 
               entityName.contains("ender_dragon") ||
               entityName.contains("elder_guardian") ||
               entityName.contains("warden");
    }

    /**
     * 根据效果等级计算移动速度减少百分比
     */
    private float calculateSpeedReduction(int amplifier) {
        // 每级减少20%移动速度，最大5级（100%）
        return Math.min(1.0f, (amplifier + 1) * 0.2f);
    }

    @Override
    public String getDescriptionId() {
        return "effect.fatality.freeze";
    }
}