package org.zouhmi.momentum.features;

import org.zouhmi.momentum.MomentumClient;
import org.zouhmi.momentum.config.MomentumConfig;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Tracks player render-state update counts for diagnostics and enables a
 * frame-coherent skip for repeated identical player render state preparation
 * within the same frame. Safe: only reporting and skip-hints are cached,
 * vanilla still performs the render itself.
 */
public class PlayerRenderOptimizer {

    private static final Map<Integer, Long> LAST_RENDERED_FRAME = new ConcurrentHashMap<>();
    private static volatile long frameCounter = 0L;
    private static long skippedPreparations = 0L;

    public static void onFrameStart() {
        frameCounter++;
        // Bounded map, clear every 128 frames if it grows large.
        if ((frameCounter & 0x7f) == 0 && LAST_RENDERED_FRAME.size() > 512) {
            LAST_RENDERED_FRAME.clear();
        }
    }

    /**
     * Returns true when a player's render-state update should run this frame.
     * Only skips when the player was already prepared within this exact frame.
     */
    public static boolean shouldPrepareRenderState(int entityId) {
        MomentumConfig cfg = MomentumClient.getConfig();
        if (cfg == null || !cfg.players.enabled || !cfg.players.deduplicateRenderState) {
            return true;
        }
        long now = frameCounter;
        Long prev = LAST_RENDERED_FRAME.put(entityId, now);
        if (prev != null && prev == now) {
            skippedPreparations++;
            return false;
        }
        return true;
    }

    public static long getSkippedPreparations() {
        return skippedPreparations;
    }

    public static void reset() {
        LAST_RENDERED_FRAME.clear();
        frameCounter = 0L;
        skippedPreparations = 0L;
    }
}
