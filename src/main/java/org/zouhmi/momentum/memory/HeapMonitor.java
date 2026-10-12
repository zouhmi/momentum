package org.zouhmi.momentum.memory;

public final class HeapMonitor {
    private HeapMonitor() {
    }

    public static long usedHeapBytes() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    public static long maxHeapBytes() {
        return Runtime.getRuntime().maxMemory();
    }

    public static String describe() {
        long used = usedHeapBytes() / (1024 * 1024);
        long max = maxHeapBytes() / (1024 * 1024);
        return used + " / " + max + " MiB";
    }
}
