package org.zouhmi.momentum;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zouhmi.momentum.config.MomentumConfig;
import org.zouhmi.momentum.diagnostics.MetricsCollector;

public class MomentumClient implements ClientModInitializer {
    public static final String MOD_ID = "momentum";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static MomentumConfig config;
    private static MetricsCollector metrics;

    public static MomentumConfig getConfig() {
        return config;
    }

    public static MetricsCollector getMetrics() {
        return metrics;
    }

    @Override
    public void onInitializeClient() {
        config = MomentumConfig.load();
        metrics = new MetricsCollector(config.diagnostics);
        config.diagnostics.runStartupDiagnostics(LOGGER);
        LOGGER.info("Momentum initialized, {} optimizations active",
                String.join(", ", config.describeActiveFeatures()));
    }
}
