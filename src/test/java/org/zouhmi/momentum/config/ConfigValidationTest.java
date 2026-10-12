package org.zouhmi.momentum.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.BeforeEach;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfigValidationTest {
    @TempDir
    Path tempDir;

    @BeforeEach
    void setup() {
        MomentumConfig.setPathOverride(tempDir.resolve("momentum.json"));
    }

    @Test
    void validateClampsFrameCapacityBelowMinimum() {
        MomentumConfig cfg = new MomentumConfig();
        cfg.diagnostics.frameSampleCapacity = 1;
        cfg.memory.frameSampleCapacity = 0;
        try {
            var m = MomentumConfig.class.getDeclaredMethod("validate");
            m.setAccessible(true);
            m.invoke(cfg);
        } catch (ReflectiveOperationException e) {
            fail(e.getMessage());
        }
        assertEquals(60, cfg.diagnostics.frameSampleCapacity);
        assertEquals(60, cfg.memory.frameSampleCapacity);
    }

    @Test
    void validateClampsBatteryMaxFps() {
        MomentumConfig cfg = new MomentumConfig();
        cfg.battery.maxFps = 1;
        try {
            var m = MomentumConfig.class.getDeclaredMethod("validate");
            m.setAccessible(true);
            m.invoke(cfg);
        } catch (ReflectiveOperationException e) {
            fail(e.getMessage());
        }
        assertEquals(5, cfg.battery.maxFps);

        cfg.battery.maxFps = 99999;
        try {
            var m = MomentumConfig.class.getDeclaredMethod("validate");
            m.setAccessible(true);
            m.invoke(cfg);
        } catch (ReflectiveOperationException e) {
            fail(e.getMessage());
        }
        assertEquals(260, cfg.battery.maxFps);
    }

    @Test
    void saveWritesValidJson() throws Exception {
        MomentumConfig cfg = new MomentumConfig();
        cfg.battery.enabled = true;
        cfg.battery.maxFps = 45;
        cfg.save();

        Path file = tempDir.resolve("momentum.json");
        assertTrue(Files.exists(file), "config file must be persisted");
        String json = Files.readString(file);
        assertTrue(json.contains("45"), "saved config should contain the fps cap value");
    }

    @Test
    void describeActiveFeaturesListsOnlyWiredFeatures() {
        MomentumConfig cfg = new MomentumConfig();
        cfg.entities.enabled = true;
        cfg.entities.entityCulling = true;
        List<String> features = cfg.describeActiveFeatures();
        assertTrue(features.contains("entityFrustumCulling"));
        assertTrue(features.contains("chunkRebuildCoalescing"));
        assertTrue(features.contains("frameMetrics"));
        assertTrue(features.contains("blockEntityCulling"), "block entity culling is wired via mixin");
        assertTrue(features.contains("cacheTrimOnUnload"), "cache trim is wired via disconnect hook");
        assertFalse(features.contains("rendering"), "generic labels must not be listed");
    }
}
