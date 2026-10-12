package org.zouhmi.momentum.features;

import org.zouhmi.momentum.config.MomentumConfig;

/**
 * Profile presets mapping to settings that are actually wired today.
 * apply() confirms the caller's intent; revert() restores a captured baseline.
 */
public class ProfileManager {

    public enum Profile { BALANCED, LOW_END, LOW_LATENCY }

    public static class Preview {
        public final boolean entityCulling;
        public final boolean chunkCoalescing;
        public final boolean trimOnUnload;
        public final boolean batterySaver;
        public final int batteryMaxFps;

        Preview(boolean c, boolean r, boolean t, boolean b, int f) {
            this.entityCulling = c;
            this.chunkCoalescing = r;
            this.trimOnUnload = t;
            this.batterySaver = b;
            this.batteryMaxFps = f;
        }
    }

    public static Preview preview(Profile p, MomentumConfig baseline) {
        switch (p) {
            case LOW_END:
                // Conservative for weak laptops, lowest overhead.
                return new Preview(true, true, true, true, Math.min(30, baseline.battery.maxFps));
            case LOW_LATENCY:
                // Reduce optional overhead, no fps cap change.
                return new Preview(true, true, false, false, baseline.battery.maxFps);
            case BALANCED:
            default:
                return new Preview(true, true, false, false, baseline.battery.maxFps);
        }
    }

    public static void apply(Profile p, MomentumConfig cfg) {
        Preview src = preview(p, cfg);
        cfg.entities.entityCulling = src.entityCulling;
        cfg.chunks.coalesceDuplicateRebuilds = src.chunkCoalescing;
        cfg.memory.trimCachesOnUnload = src.trimOnUnload;
        cfg.battery.enabled = src.batterySaver;
        cfg.battery.maxFps = src.batteryMaxFps;
        cfg.save();
    }

    /**
     * Restores the exact snapshot captured before apply.
     */
    public static void revert(MomentumConfig baseline, MomentumConfig cfg) {
        cfg.entities.entityCulling = baseline.entities.entityCulling;
        cfg.chunks.coalesceDuplicateRebuilds = baseline.chunks.coalesceDuplicateRebuilds;
        cfg.memory.trimCachesOnUnload = baseline.memory.trimCachesOnUnload;
        cfg.battery.enabled = baseline.battery.enabled;
        cfg.battery.maxFps = baseline.battery.maxFps;
        cfg.save();
    }
}
