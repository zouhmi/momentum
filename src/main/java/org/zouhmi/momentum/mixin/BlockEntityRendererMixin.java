package org.zouhmi.momentum.mixin;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.zouhmi.momentum.features.BlockEntityOptimizer;
import org.zouhmi.momentum.features.FrustumProvider;

@Mixin(BlockEntityRenderer.class)
public abstract class BlockEntityRendererMixin {

    @Inject(method = "isInRenderDistance(Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/util/math/Vec3d;)Z", at = @At("HEAD"), cancellable = true)
    private <T extends BlockEntity> void momentum$cullBlockEntity(T blockEntity, Vec3d pos, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (blockEntity != null && !BlockEntityOptimizer.shouldBlockEntityRender(blockEntity, FrustumProvider.current())) {
                cir.setReturnValue(false);
            }
        } catch (Throwable t) {
            // Fail safe: always render on internal error.
        }
    }
}
