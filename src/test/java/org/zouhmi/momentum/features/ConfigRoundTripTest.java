package org.zouhmi.momentum.features;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfigRoundTripTest {
    @TempDir
    Path tempDir;

    @Test
    void saveThenLoadPreservesValues() throws Exception {
        Path file = tempDir.resolve("momentum.json");
        org.zouhmi.momentum.config.MomentumConfig.setPathOverride(file);
        try {
            org.zouhmi.momentum.config.MomentumConfig original =
                    new org.zouhmi.momentum.config.MomentumConfig();
            original.battery.enabled = true;
            original.battery.maxFps = 45;
            original.window.borderlessFullscreen = true;
            original.entities.entityCulling = false;
            original.save();
            assertTrue(Files.exists(file));

            org.zouhmi.momentum.config.MomentumConfig loaded =
                    org.zouhmi.momentum.config.MomentumConfig.load();
            assertEquals(45, loaded.battery.maxFps);
            assertTrue(loaded.battery.enabled);
            assertTrue(loaded.window.borderlessFullscreen);
            assertFalse(loaded.entities.entityCulling);
        } finally {
            org.zouhmi.momentum.config.MomentumConfig.setPathOverride(null);
        }
    }

    @Test
    void loadOnCorruptFileFallsBackToDefaults() throws Exception {
        Path file = tempDir.resolve("momentum.json");
        Files.writeString(file, "{ not valid json !!!");
        org.zouhmi.momentum.config.MomentumConfig.setPathOverride(file);
        try {
            org.zouhmi.momentum.config.MomentumConfig loaded =
                    org.zouhmi.momentum.config.MomentumConfig.load();
            assertNotNull(loaded, "corrupt config must not crash, fall back to defaults");
            assertEquals(30, loaded.battery.maxFps, "defaults must apply after corruption");
            assertTrue(loaded.entities.entityCulling);
            assertTrue(Files.exists(file), "fresh default config should be re-saved");
        } finally {
            org.zouhmi.momentum.config.MomentumConfig.setPathOverride(null);
        }
    }

    @Test
    void loadOnMissingFileCreatesDefaultConfig() {
        Path file = tempDir.resolve("nonexistent.json");
        org.zouhmi.momentum.config.MomentumConfig.setPathOverride(file);
        try {
            org.zouhmi.momentum.config.MomentumConfig loaded =
                    org.zouhmi.momentum.config.MomentumConfig.load();
            assertNotNull(loaded);
            assertTrue(Files.exists(file), "missing config should be created with defaults");
        } finally {
            org.zouhmi.momentum.config.MomentumConfig.setPathOverride(null);
        }
    }
}
