package org.zouhmi.momentum.diagnostics;

import org.junit.jupiter.api.Test;
import org.zouhmi.momentum.config.MomentumConfig;

import static org.junit.jupiter.api.Assertions.*;

class MetricsCollectorTest {

    private MomentumConfig.DiagnosticsSection section(int capacity) {
        MomentumConfig.DiagnosticsSection s = new MomentumConfig.DiagnosticsSection();
        s.frameSampleCapacity = capacity;
        return s;
    }

    @Test
    void medianOfKnownSamplesIsExact() {
        MetricsCollector m = new MetricsCollector(section(5));
        double[] frames = {10.0, 20.0, 30.0, 40.0, 50.0};
        for (double f : frames) m.recordFrame(f);
        assertEquals(30.0, m.median(), 1e-9);
    }

    @Test
    void p99RoundsToNearMaxSample() {
        MetricsCollector m = new MetricsCollector(section(10));
        for (int i = 1; i <= 10; i++) m.recordFrame(i);
        assertEquals(10.0, m.p99(), 1e-9);
        assertEquals(10.0, m.p95(), 1e-9);
    }

    @Test
    void p1LowPicksSmallestSample() {
        MetricsCollector m = new MetricsCollector(section(10));
        for (int i = 1; i <= 10; i++) m.recordFrame(i);
        assertEquals(1.0, m.p1Low(), 1e-9);
    }

    @Test
    void circularBufferWrapsAndKeepsNewest() {
        MetricsCollector m = new MetricsCollector(section(3));
        m.recordFrame(1.0);
        m.recordFrame(2.0);
        m.recordFrame(3.0);
        m.recordFrame(4.0);
        m.recordFrame(5.0);
        // Oldest 1 and 2 were overwritten, newest sample set is {3,4,5}.
        assertEquals(3, m.getSampleCount());
        assertEquals(4.0, m.median(), 1e-9);
        assertEquals(3.0, m.p1Low(), 1e-9);
        assertEquals(5.0, m.p99(), 1e-9);
    }

    @Test
    void tickDurationMeasuredBetweenStartAndEnd() {
        MetricsCollector m = new MetricsCollector(section(60));
        m.onTickStart();
        m.onTickEnd();
        assertTrue(m.getLastTickMs() >= 0.0);
        // A second end without a start must not corrupt the last value.
        double before = m.getLastTickMs();
        m.onTickEnd();
        assertEquals(before, m.getLastTickMs(), 1e-9);
    }
}
