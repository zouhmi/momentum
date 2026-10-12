package org.zouhmi.momentum.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.zouhmi.momentum.MomentumClient;
import org.zouhmi.momentum.diagnostics.MetricsCollector;
import org.zouhmi.momentum.features.ChunkRebuildOptimizer;
import org.zouhmi.momentum.features.MemoryOptimizer;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    private long momentum$renderStartNanos = 0L;

    @Inject(method = "render", at = @At("HEAD"))
    private void momentum$onRenderStart(CallbackInfo ci) {
        this.momentum$renderStartNanos = System.nanoTime();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void momentum$onRenderEnd(CallbackInfo ci) {
        if (this.momentum$renderStartNanos == 0L) return;
        MetricsCollector metrics = MomentumClient.getMetrics();
        if (metrics != null) {
            metrics.recordFrame((System.nanoTime() - this.momentum$renderStartNanos) / 1_000_000.0);
        }
        this.momentum$renderStartNanos = 0L;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void momentum$onTickStart(CallbackInfo ci) {
        MetricsCollector metrics = MomentumClient.getMetrics();
        if (metrics != null) {
            metrics.onTickStart();
        }
        try {
            ChunkRebuildOptimizer.onClientTick();
        } catch (Throwable t) {
            // Never crash the client from optimizer bookkeeping.
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void momentum$onTickEnd(CallbackInfo ci) {
        MetricsCollector metrics = MomentumClient.getMetrics();
        if (metrics != null) {
            metrics.onTickEnd();
        }
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;ZZ)V", at = @At("TAIL"))
    private void momentum$onDisconnect(CallbackInfo ci) {
        try {
            MemoryOptimizer.onWorldUnload();
        } catch (Throwable t) {
            // Never block vanilla disconnect with cleanup errors.
        }
    }
}
