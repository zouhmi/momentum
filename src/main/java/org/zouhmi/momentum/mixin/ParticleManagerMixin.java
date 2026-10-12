package org.zouhmi.momentum.mixin;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.zouhmi.momentum.features.ParticleOptimizer;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

    @Inject(method = "addParticle(Lnet/minecraft/client/particle/Particle;)V", at = @At("HEAD"), cancellable = true)
    private void momentum$throttleParticles(Particle particle, CallbackInfo ci) {
        try {
            if (!ParticleOptimizer.keepParticle()) {
                ci.cancel();
            }
        } catch (Throwable t) {
            // Fail safe: spawn normally on internal error.
        }
    }
}
