package cn.dawnstring.fatality.mixins;

import cn.dawnstring.fatality.client.DamageIndicatorManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
public class DamageIndicatorMixin {

    /**
     * 注入到createParticle方法，当创建伤害指示器粒子时返回null来阻止生成
     */
    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
    private void disableDamageIndicatorParticle(ParticleOptions particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, CallbackInfoReturnable<Particle> cir) {
         if (particleData.getType() == ParticleTypes.DAMAGE_INDICATOR) {
             cir.setReturnValue(null); // 返回null来阻止粒子生成
        }
    }
}