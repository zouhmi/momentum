package org.zouhmi.momentum.features;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ProfileManagerTest {

    @TempDir
    static Path tempDir;

    @BeforeAll
    static void stubConfigAndPath() {
        // Inject a real enabled config and a safe persistence path for tests.
        org.zouhmi.momentum.config.MomentumConfig cfg =
                new org.zouhmi.momentum.config.MomentumConfig();
        org.zouhmi.momentum.MomentumClient.setConfig(cfg);
        org.zouhmi.momentum.config.MomentumConfig.setPathOverride(
                tempDir.resolve("momentum.json"));
    }

    @Test
    void lowEndProfileEnablesAllConservativeOptions() {
        org.zouhmi.momentum.config.MomentumConfig cfg =
                new org.zouhmi.momentum.config.MomentumConfig();
        cfg.battery.maxFps = 90;
        cfg.memory.trimCachesOnUnload = false;

        ProfileManager.Preview p = ProfileManager.preview(ProfileManager.Profile.LOW_END, cfg);
        assertTrue(p.entityCulling);
        assertTrue(p.chunkCoalescing);
        assertTrue(p.trimOnUnload);
        assertTrue(p.batterySaver);
        assertEquals(30, p.batteryMaxFps);
    }

    @Test
    void lowLatencyProfileKeepsFpsCapUnchanged() {
        org.zouhmi.momentum.config.MomentumConfig cfg =
                new org.zouhmi.momentum.config.MomentumConfig();
        cfg.battery.maxFps = 144;
        cfg.battery.enabled = true;

        ProfileManager.Preview p = ProfileManager.preview(ProfileManager.Profile.LOW_LATENCY, cfg);
        assertFalse(p.batterySaver);
        assertEquals(144, p.batteryMaxFps);
    }

    @Test
    void revertRestoresCapturedBaseline() {
        org.zouhmi.momentum.config.MomentumConfig cfg =
                new org.zouhmi.momentum.config.MomentumConfig();
        cfg.battery.maxFps = 240;
        cfg.battery.enabled = false;
        cfg.entities.entityCulling = true;

        org.zouhmi.momentum.config.MomentumConfig baseline =
                new org.zouhmi.momentum.config.MomentumConfig();
        baseline.battery.maxFps = 240;
        baseline.battery.enabled = false;
        baseline.entities.entityCulling = true;

        ProfileManager.apply(ProfileManager.Profile.LOW_END, cfg);
        assertLowEndApplied(cfg);
        ProfileManager.revert(baseline, cfg);
        assertEquals(240, cfg.battery.maxFps);
        assertFalse(cfg.battery.enabled);
        assertTrue(cfg.entities.entityCulling);
    }

    private void assertLowEndApplied(org.zouhmi.momentum.config.MomentumConfig cfg) {
        assertEquals(30, cfg.battery.maxFps, "low-end apply must cap fps");
        assertTrue(cfg.battery.enabled);
        assertTrue(cfg.memory.trimCachesOnUnload);
    }
}
