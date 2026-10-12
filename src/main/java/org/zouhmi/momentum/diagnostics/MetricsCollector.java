package org.zouhmi.momentum.diagnostics;

import org.zouhmi.momentum.config.MomentumConfig;

public class MetricsCollector {
    private final double[] samples;
    private int head = 0;
    private int count = 0;
    private double lastFrameMs = 0;
    private long lastTickStartNanos = 0;
    private double lastTickDurationMs = 0;

    public MetricsCollector(MomentumConfig.DiagnosticsSection config) {
        this.samples = new double[config.frameSampleCapacity];
    }

    public void recordFrame(double frameMs) {
        samples[head] = frameMs;
        head = (head + 1) % samples.length;
        if (count < samples.length) count++;
        lastFrameMs = frameMs;
    }

    public void onTickStart() {
        lastTickStartNanos = System.nanoTime();
    }

    public void onTickEnd() {
        if (lastTickStartNanos != 0) {
            lastTickDurationMs = (System.nanoTime() - lastTickStartNanos) / 1_000_000.0;
            lastTickStartNanos = 0;
        }
    }

    public double getLastFrameMs() {
        return lastFrameMs;
    }

    public double getLastTickMs() {
        return lastTickDurationMs;
    }

    public int getSampleCount() {
        return count;
    }

    private double percentile(double p) {
        if (count == 0) return 0;
        double[] copy = new double[count];
        System.arraycopy(samples, 0, copy, 0, count);
        java.util.Arrays.sort(copy);
        int idx = (int) Math.round(p / 100.0 * (copy.length - 1));
        return copy[idx];
    }

    public double median() {
        return percentile(50);
    }

    public double p95() {
        return percentile(95);
    }

    public double p99() {
        return percentile(99);
    }

    public double p1Low() {
        return percentile(1);
    }
}
