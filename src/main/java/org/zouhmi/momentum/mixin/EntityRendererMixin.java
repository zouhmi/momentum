package org.zouhmi.momentum.mixin;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.zouhmi.momentum.features.VisibilityOptimizer;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    @Inject(method = "shouldRender(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/Frustum;DDD)Z", at = @At("HEAD"), cancellable = true)
    private <T extends Entity> void momentum$frustumCull(T entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (!VisibilityOptimizer.shouldEntityRender(entity, frustum)) {
                cir.setReturnValue(false);
            }
        } catch (Throwable t) {
            // Fail safe: always render on internal error.
        }
    }
}
