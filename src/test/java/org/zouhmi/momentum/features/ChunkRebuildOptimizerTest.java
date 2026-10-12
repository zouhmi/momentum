package org.zouhmi.momentum.features;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChunkRebuildOptimizerTest {

    @BeforeAll
    static void stubConfigEnabled() {
        // Outside the game there is no loaded config; a real instance enables coalescing.
        org.zouhmi.momentum.MomentumClient.setConfig(new org.zouhmi.momentum.config.MomentumConfig());
    }

    @Test
    void allowsFirstScheduleAndSuppressesSecondInSameTick() {
        ChunkRebuildOptimizer.reset();
        assertTrue(ChunkRebuildOptimizer.allowChunkRenderSchedule(1, 2, 3));
        assertFalse(ChunkRebuildOptimizer.allowChunkRenderSchedule(1, 2, 3),
                "duplicate schedule in the same tick must be suppressed");
        long suppressed = ChunkRebuildOptimizer.getSuppressionCount();
        assertEquals(1, suppressed);
    }

    @Test
    void differentPositionsAreIndependent() {
        ChunkRebuildOptimizer.reset();
        assertTrue(ChunkRebuildOptimizer.allowChunkRenderSchedule(0, 0, 0));
        assertTrue(ChunkRebuildOptimizer.allowChunkRenderSchedule(1, 0, 0),
                "a distinct chunk must never be suppressed");
    }

    @Test
    void tickAdvanceAllowsReschedule() {
        ChunkRebuildOptimizer.reset();
        assertTrue(ChunkRebuildOptimizer.allowChunkRenderSchedule(5, 5, 5));
        // One client tick passes.
        ChunkRebuildOptimizer.onClientTick();
        assertTrue(ChunkRebuildOptimizer.allowChunkRenderSchedule(5, 5, 5),
                "the same chunk must be schedulable again on a later tick");
    }

    @Test
    void resetClearsEverything() {
        ChunkRebuildOptimizer.onClientTick();
        assertTrue(ChunkRebuildOptimizer.allowChunkRenderSchedule(2, 2, 2));
        assertFalse(ChunkRebuildOptimizer.allowChunkRenderSchedule(2, 2, 2),
                "second schedule in the same tick must suppress");
        ChunkRebuildOptimizer.reset();
        assertEquals(0, ChunkRebuildOptimizer.getSuppressionCount());
        assertTrue(ChunkRebuildOptimizer.allowChunkRenderSchedule(2, 2, 2),
                "after reset, first schedule must pass again");
    }
}
