package cn.dawnstring.fatality.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 护甲粉碎效果
 * 目标护甲值减少20%，目标受到伤害增加20%
 */
public class ArmorBreakEffect extends MobEffect
{
    private static final String ARMOR_MODIFIER_UUID = "7f6c9d8e-5a4b-3c2d-1e0f-9a8b7c6d5e4f";
    private static final String ARMOR_TOUGHNESS_MODIFIER_UUID = "8e7d6c5b-4a3b-2c1d-0e9f-8b7a6c5d4e3f";

    public ArmorBreakEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B4513); // 棕色，表示护甲破坏
        
        // 添加护甲减少属性修改器
        this.addAttributeModifier(Attributes.ARMOR, ARMOR_MODIFIER_UUID, -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);
        
        // 添加护甲韧性减少属性修改器
        this.addAttributeModifier(Attributes.ARMOR_TOUGHNESS, ARMOR_TOUGHNESS_MODIFIER_UUID, -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        // 护甲粉碎效果在应用时已经通过属性修改器生效
        // 这里可以添加额外的视觉效果或音效
        super.applyEffectTick(livingEntity, amplifier);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每tick都应用效果
        return true;
    }

    /**
     * 获取效果描述
     */
    @Override
    public String getDescriptionId() {
        return "effect.fatality.armor_break";
    }

    /**
     * 护甲粉碎效果是否可被清除
     */
    @Override
    public boolean isBeneficial() {
        return false; // 有害效果
    }
}