package org.zouhmi.momentum.features;

import org.zouhmi.momentum.MomentumClient;
import org.zouhmi.momentum.config.MomentumConfig;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Throttles optional particle spawns by simple random drop when enabled.
 * Never drops gameplay-relevant events, affects ambient visuals only.
 */
public class ParticleOptimizer {

    /**
     * Returns true when the caller should keep the particle.
     */
    public static boolean keepParticle() {
        MomentumConfig cfg = MomentumClient.getConfig();
        if (cfg == null || !cfg.rendering.enabled || !cfg.rendering.reduceOptionalParticles) {
            return true;
        }
        // Drop roughly half of optional particles under reduced mode.
        return ThreadLocalRandom.current().nextInt(2) != 0;
    }

    public static boolean isReduced() {
        MomentumConfig cfg = MomentumClient.getConfig();
        return cfg != null && cfg.rendering.enabled && cfg.rendering.reduceOptionalParticles;
    }
}
