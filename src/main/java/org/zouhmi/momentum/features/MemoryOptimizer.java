package org.zouhmi.momentum.features;

import org.zouhmi.momentum.MomentumClient;
import org.zouhmi.momentum.config.MomentumConfig;

/**
 * Clears Momentum-owned caches when a world disconnects.
 * Only touches Momentum's own state, never vanilla world data.
 */
public class MemoryOptimizer {

    public static void onWorldUnload() {
        MomentumConfig cfg = MomentumClient.getConfig();
        if (cfg == null || !cfg.memory.enabled || !cfg.memory.trimCachesOnUnload) {
            return;
        }
        try {
            ChunkRebuildOptimizer.reset();
        } catch (Throwable t) {
            // Never block vanilla disconnect with cleanup errors.
        }
    }
}
