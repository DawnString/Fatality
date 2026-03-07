package cn.dawnstring.fatality.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

/**
 * 星光标记效果
 * 被标记的目标更容易受到伤害，连续标记3次后触发星爆
 */
public class StarlightMarkEffect extends MobEffect {
    
    // 伤害增加属性修改器的UUID
    private static final UUID DAMAGE_INCREASE_UUID = UUID.fromString("1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d");
    
    public StarlightMarkEffect() {
        super(MobEffectCategory.HARMFUL, 0x87CEEB); // 天蓝色，代表星光
        
        // 添加伤害增加属性修改器（每层标记增加10%受到的伤害）
        this.addAttributeModifier(Attributes.ARMOR, DAMAGE_INCREASE_UUID.toString(), -0.1, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 星光标记效果在应用时已经通过属性修改器生效
        // 这里可以添加视觉效果或音效
        super.applyEffectTick(entity, amplifier);
        
        // 生成星光粒子效果
        if (entity.level().isClientSide()) {
            spawnStarlightParticles(entity);
        }
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每2秒执行一次效果（40 ticks = 2秒）
        return duration % 40 == 0;
    }
    
    /**
     * 生成星光粒子效果
     */
    private void spawnStarlightParticles(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            double x = entity.getX();
            double y = entity.getY() + entity.getEyeHeight();
            double z = entity.getZ();
            
            // 生成蓝色星光粒子
            for (int i = 0; i < 3; i++) {
                double offsetX = (Math.random() - 0.5) * 1.5;
                double offsetY = (Math.random() - 0.5) * 1.5;
                double offsetZ = (Math.random() - 0.5) * 1.5;
                
                entity.level().addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD,
                        x + offsetX, y + offsetY, z + offsetZ,
                        0, 0.05, 0);
            }
            
            // 生成闪烁粒子
            if (entity.tickCount % 10 == 0) {
                entity.level().addParticle(net.minecraft.core.particles.ParticleTypes.GLOW,
                        x, y, z,
                        0, 0, 0);
            }
        }
    }
    
    /**
     * 获取效果描述
     */
    @Override
    public String getDescriptionId() {
        return "effect.fatality.starlight_mark";
    }
    
    /**
     * 星光标记效果是否可被清除
     */
    @Override
    public boolean isBeneficial() {
        return false; // 有害效果
    }
}