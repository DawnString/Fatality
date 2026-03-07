package cn.dawnstring.fatality.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class ArmorErosionEffect extends MobEffect
{
    private static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("118B338A-C7D8-4E4B-B6D3-9F1A2D47BE12");
    private static final UUID ARMOR_TOUGHNESS_MODIFIER_UUID = UUID.fromString("229C449B-D8E9-5F5C-C7E4-A02B3E58CF23");

    public ArmorErosionEffect()
    {
        super(MobEffectCategory.HARMFUL, 	0x00FF00);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(entity, attributeMap, amplifier);

        // 获取护甲属性实例
        AttributeInstance armorAttribute = entity.getAttribute(Attributes.ARMOR);
        if (armorAttribute != null) {
            // 移除可能存在的旧修饰符
            armorAttribute.removeModifier(ARMOR_MODIFIER_UUID);

            // 添加新的修饰符，将护甲值乘以0（完全消除护甲）
            AttributeModifier armorModifier = new AttributeModifier(
                    ARMOR_MODIFIER_UUID,
                    "Armor Shred",
                    -1.0, // 乘以0的效果（1.0 - 1.0 = 0）
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            );
            armorAttribute.addTransientModifier(armorModifier);
        }

        // 获取护甲韧性属性实例
        AttributeInstance toughnessAttribute = entity.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (toughnessAttribute != null) {
            // 移除可能存在的旧修饰符
            toughnessAttribute.removeModifier(ARMOR_TOUGHNESS_MODIFIER_UUID);

            // 添加新的修饰符，将护甲韧性乘以0（完全消除护甲韧性）
            AttributeModifier toughnessModifier = new AttributeModifier(
                    ARMOR_TOUGHNESS_MODIFIER_UUID,
                    "Armor Toughness Shred",
                    -1.0, // 乘以0的效果（1.0 - 1.0 = 0）
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            );
            toughnessAttribute.addTransientModifier(toughnessModifier);
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);

        // 移除护甲修饰符
        AttributeInstance armorAttribute = entity.getAttribute(Attributes.ARMOR);
        if (armorAttribute != null) {
            armorAttribute.removeModifier(ARMOR_MODIFIER_UUID);
        }

        // 移除护甲韧性修饰符
        AttributeInstance toughnessAttribute = entity.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (toughnessAttribute != null) {
            toughnessAttribute.removeModifier(ARMOR_TOUGHNESS_MODIFIER_UUID);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每tick都更新效果，确保属性修改器持续生效
        return true;
    }
}
