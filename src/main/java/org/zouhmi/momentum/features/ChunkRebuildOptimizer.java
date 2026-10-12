package org.zouhmi.momentum.features;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import org.zouhmi.momentum.MomentumClient;
import org.zouhmi.momentum.config.MomentumConfig;

/**
 * Real duplicate-suppression for chunk rebuild scheduling.
 * Tracks last scheduled tick per packed chunk position; skips when repeated
 * within the same tick, keeping the final state correct.
 */
public class ChunkRebuildOptimizer {

    private static final Long2LongOpenHashMap LAST_SCHEDULED = new Long2LongOpenHashMap();

    // Starts at 1: the map returns 0 for missing keys, so tick 0 would collide.
    private static long tickCounter = 1L;
    private static long suppressionCount = 0L;

    public static void onClientTick() {
        tickCounter++;
        if ((tickCounter & 0x3f) == 0 && LAST_SCHEDULED.size() > 8192) {
            LAST_SCHEDULED.clear();
        }
    }

    /**
     * Returns true when this call should pass through to vanilla.
     * Returns false when suppressed because the same chunk already has a
     * rebuild scheduled this tick, which vanilla application order keeps correct.
     */
    public static boolean allowChunkRenderSchedule(int x, int y, int z) {
        if (!coalescingEnabled) {
            return true;
        }
        MomentumConfig cfg = MomentumClient.getConfig();
        if (cfg == null || !cfg.chunks.enabled || !cfg.chunks.coalesceDuplicateRebuilds) {
            return true;
        }
        long packed = ((long) (x & 0x3FFFFF) << 42) | ((long) (z & 0x3FFFFF) << 20) | (long) (y & 0xFFFFF);
        long now = tickCounter;
        long prev = LAST_SCHEDULED.put(packed, now);
        if (prev == now) {
            suppressionCount++;
            return false;
        }
        return true;
    }

    /**
     * Test hook: forces the optimizer's enabled/disabled coalescing behavior.
     * When false, all schedules pass through unchanged.
     */
    public static void setCoalescingEnabledForTest(boolean enabled) {
        coalescingEnabled = enabled;
        LAST_SCHEDULED.clear();
        suppressionCount = 0L;
    }

    private static volatile boolean coalescingEnabled = true;

    public static long getSuppressionCount() {
        return suppressionCount;
    }

    public static void reset() {
        LAST_SCHEDULED.clear();
        tickCounter = 1L;
        suppressionCount = 0L;
    }
}
