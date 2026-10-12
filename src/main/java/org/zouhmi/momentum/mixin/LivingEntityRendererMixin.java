package org.zouhmi.momentum.mixin;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.zouhmi.momentum.features.PlayerRenderOptimizer;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V", at = @At("HEAD"), cancellable = true)
    private <T extends LivingEntity, S extends LivingEntityRenderState> void momentum$dedupeRenderState(
            T entity, S state, float tickDelta, CallbackInfo ci) {
        try {
            if (entity == null) return;
            // Only apply to remote or local players by entity id.
            if (!PlayerRenderOptimizer.shouldPrepareRenderState(entity.getId())) {
                ci.cancel();
            }
        } catch (Throwable t) {
            // Fail safe: run vanilla behavior on internal error.
        }
    }
}
