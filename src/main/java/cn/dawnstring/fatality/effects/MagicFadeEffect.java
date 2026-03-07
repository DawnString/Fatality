package cn.dawnstring.fatality.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import java.util.UUID;

public class MagicFadeEffect extends MobEffect {
    
    // 魔法伤害减少20%的UUID
    private static final UUID MAGIC_DAMAGE_UUID = UUID.fromString("1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d");
    // 魔法恢复减少20%的UUID
    private static final UUID MANA_REGEN_UUID = UUID.fromString("2b3c4d5e-6f7a-8b9c-0d1e-2f3a4b5c6d7e");
    
    public MagicFadeEffect() {
        super(MobEffectCategory.NEUTRAL, 0x8A2BE2); // 蓝紫色
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 每tick应用效果，减少魔法伤害和魔法恢复
        if (entity.level().isClientSide) {
            return;
        }
        
        // 这里不需要手动修改属性，因为属性修改器会在addAttributeModifiers中自动应用
    }
    
    @Override
    public void addAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(entity, attributeMap, amplifier);
        
        // 添加魔法伤害减少20%的修饰符
        var attackDamageAttribute = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamageAttribute != null) {
            attackDamageAttribute.removeModifier(MAGIC_DAMAGE_UUID);
            attackDamageAttribute.addTransientModifier(new AttributeModifier(
                MAGIC_DAMAGE_UUID, 
                "Magic Fade Damage Reduction", 
                -0.2, 
                AttributeModifier.Operation.MULTIPLY_TOTAL
            ));
        }
        
        // 注意：魔法恢复速率需要通过其他方式处理，因为AttributeSystem中计算的是基础值
        // 魔力衰减对魔法恢复的影响将在ManaSystem中处理
    }
    
    @Override
    public void removeAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        
        // 移除属性修饰符
        var attackDamageAttribute = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamageAttribute != null) {
            attackDamageAttribute.removeModifier(MAGIC_DAMAGE_UUID);
        }
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每tick都更新效果
        return true;
    }
}