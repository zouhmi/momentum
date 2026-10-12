package org.zouhmi.momentum.mixin;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.state.WorldRenderState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.zouhmi.momentum.features.ChunkRebuildOptimizer;
import org.zouhmi.momentum.features.FrustumProvider;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Inject(method = "renderMain(Lnet/minecraft/client/render/FrameGraphBuilder;Lnet/minecraft/client/render/Frustum;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;ZLnet/minecraft/client/render/state/WorldRenderState;Lnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/util/profiler/Profiler;)V", at = @At("HEAD"))
    private void momentum$captureFrustum(net.minecraft.client.render.FrameGraphBuilder frameGraphBuilder,
            Frustum frustum,
            Matrix4f matrix4f,
            com.mojang.blaze3d.buffers.GpuBufferSlice gpuBufferSlice,
            boolean bl,
            WorldRenderState worldRenderState,
            net.minecraft.client.render.RenderTickCounter tickCounter,
            net.minecraft.util.profiler.Profiler profiler, CallbackInfo ci) {
        try {
            FrustumProvider.set(frustum);
        } catch (Throwable t) {
            // Never break rendering for bookkeeping.
        }
    }

    @Inject(method = "scheduleChunkRender(IIIZ)V", at = @At("HEAD"), cancellable = true)
    private void momentum$coalesceChunkSchedule(int x, int y, int z, boolean important, CallbackInfo ci) {
        try {
            // Never suppress important rebuilds so gameplay state stays correct.
            if (!important && !ChunkRebuildOptimizer.allowChunkRenderSchedule(x, y, z)) {
                ci.cancel();
            }
        } catch (Throwable t) {
            // Fail safe: fall through to vanilla behavior on internal error.
        }
    }
}
