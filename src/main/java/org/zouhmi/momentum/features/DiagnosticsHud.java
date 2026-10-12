package org.zouhmi.momentum.features;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.font.TextRenderer;
import org.zouhmi.momentum.MomentumClient;
import org.zouhmi.momentum.config.MomentumConfig;
import org.zouhmi.momentum.diagnostics.GcPauseTracker;
import org.zouhmi.momentum.diagnostics.MetricsCollector;
import org.zouhmi.momentum.memory.HeapMonitor;

/**
 * Tiny overlay showing FPS, frame percentiles, tick and heap usage.
 * Only active when diagnostics.hud is enabled. Impl purely local.
 */
public class DiagnosticsHud {

    private static final GcPauseTracker GC_TRACKER = new GcPauseTracker();
    private static long lastRefreshMillis = 0L;

    public static void render(DrawContext context) {
        MomentumConfig cfg = MomentumClient.getConfig();
        if (cfg == null || !cfg.diagnostics.enabled || !cfg.diagnostics.hud) return;
        MetricsCollector metrics = MomentumClient.getMetrics();
        if (metrics == null) return;

        long now = System.currentTimeMillis();
        if (now - lastRefreshMillis > 1000L) {
            GC_TRACKER.refresh();
            lastRefreshMillis = now;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;
        TextRenderer tr = client.textRenderer;
        if (tr == null) return;

        double fps = metrics.getLastFrameMs() > 0 ? 1000.0 / metrics.getLastFrameMs() : 0.0;
        String[] lines = new String[]{
                "Momentum",
                String.format("FPS: %.0f", fps),
                String.format("Frame p95: %.1f ms, p99: %.1f ms", metrics.p95(), metrics.p99()),
                String.format("1%% Low: %.1f ms", metrics.p1Low()),
                String.format("Tick: %.1f ms", metrics.getLastTickMs()),
                "Heap: " + HeapMonitor.describe(),
                String.format("GC pauses: %d (%d ms)", GC_TRACKER.getTotalPauses(), GC_TRACKER.getTotalPauseMillis())
        };

        int x = 4;
        int y = 4;
        int color = 0xFFFFFF;
        int bg = 0x80101010;
        int maxW = 0;
        for (String line : lines) maxW = Math.max(maxW, tr.getWidth(line));
        context.fill(x - 2, y - 2, x + maxW + 2, y + lines.length * 9 + 2, bg);
        for (String line : lines) {
            context.drawTextWithShadow(tr, line, x, y, color);
            y += 9;
        }
    }
}
