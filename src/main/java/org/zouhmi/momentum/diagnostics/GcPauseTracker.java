package org.zouhmi.momentum.diagnostics;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregates GC pause counts and durations from platform MXBeans.
 * Deltas are taken per collector, all data stays local.
 */
public class GcPauseTracker {
    private final Map<String, Long> lastCount = new HashMap<>();
    private final Map<String, Long> lastTime = new HashMap<>();

    private long totalPauses = 0L;
    private long totalPauseMillis = 0L;

    public synchronized void refresh() {
        try {
            List<GarbageCollectorMXBean> beans = ManagementFactory.getGarbageCollectorMXBeans();
            for (GarbageCollectorMXBean bean : beans) {
                long count = bean.getCollectionCount();
                long time = bean.getCollectionTime();
                if (count < 0 || time < 0) continue;
                String name = bean.getName();
                Long prevCount = lastCount.get(name);
                Long prevTime = lastTime.get(name);
                if (prevCount != null && count > prevCount) {
                    long pauses = count - prevCount;
                    totalPauses += pauses;
                    if (prevTime != null && time > prevTime) {
                        totalPauseMillis += time - prevTime;
                    } else {
                        // New collectors or reset beans: count pauses without time.
                    }
                }
                lastCount.put(name, count);
                lastTime.put(name, time);
            }
        } catch (Throwable t) {
            // JMX unavailable: leave totals at zero rather than crashing.
        }
    }

    public synchronized long getTotalPauses() {
        return totalPauses;
    }

    public synchronized long getTotalPauseMillis() {
        return totalPauseMillis;
    }
}
