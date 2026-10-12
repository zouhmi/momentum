package org.zouhmi.momentum.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MomentumConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    // Resolved lazily so unit tests can redirect it to a temp directory.
    private static Path pathOverride = null;

    private static Path path() {
        if (pathOverride != null) return pathOverride;
        return FabricLoader.getInstance().getConfigDir().resolve("momentum.json");
    }

    public RenderingSection rendering = new RenderingSection();
    public EntitiesSection entities = new EntitiesSection();
    public ChunksSection chunks = new ChunksSection();
    public MemorySection memory = new MemorySection();
    public DiagnosticsSection diagnostics = new DiagnosticsSection();
    public WindowSection window = new WindowSection();
    public BatterySection battery = new BatterySection();
    public BlockEntitiesSection blockEntities = new BlockEntitiesSection();
    public PlayersSection players = new PlayersSection();

    public static class PlayersSection {
        public boolean enabled = true;
        public boolean deduplicateRenderState = true;
    }

    public static class BlockEntitiesSection {
        public boolean enabled = true;
        public boolean blockEntityCulling = true;
    }

    public static class WindowSection {
        public boolean borderlessFullscreen = false;
    }

    public static class BatterySection {
        public boolean enabled = false;
        public int maxFps = 30;
        public int appliedFpsCap = 0;
    }

    public static class RenderingSection {
        public boolean enabled = true;
        public boolean reduceOptionalParticles = false;
    }

    public static class EntitiesSection {
        public boolean enabled = true;
        public boolean entityCulling = true;
    }

    public static class ChunksSection {
        public boolean enabled = true;
        public boolean coalesceDuplicateRebuilds = true;
    }

    public static class MemorySection {
        public boolean enabled = true;
        public boolean trimCachesOnUnload = true;
        public int frameSampleCapacity = 300;
    }

    public static class DiagnosticsSection {
        public boolean enabled = true;
        public boolean hud = false;
        public int frameSampleCapacity = 300;
        public int startupLogLevelCount = 10;

        public void runStartupDiagnostics(org.slf4j.Logger log) {
            Runtime rt = Runtime.getRuntime();
            log.info("Processors: {}", rt.availableProcessors());
            log.info("JVM max heap: {} MiB", rt.maxMemory() / (1024 * 1024));
            log.info("Heap in use at startup: {} MiB", (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024));
            String os = System.getProperty("os.name", "Unknown");
            String jvm = System.getProperty("java.version", "Unknown");
            log.info("OS: {}, Java: {}", os, jvm);
        }
    }

    public static MomentumConfig load() {
        try {
            Path p = path();
            if (Files.exists(p)) {
                String json = Files.readString(p);
                MomentumConfig cfg = GSON.fromJson(json, MomentumConfig.class);
                if (cfg != null) {
                    cfg.validate();
                    return cfg;
                }
            }
        } catch (IOException | com.google.gson.JsonParseException e) {
            // Corrupt config: fall back to defaults.
        }
        MomentumConfig fresh = new MomentumConfig();
        fresh.save();
        return fresh;
    }

    void validate() {
        if (diagnostics.frameSampleCapacity < 60) diagnostics.frameSampleCapacity = 60;
        if (diagnostics.frameSampleCapacity > 3600) diagnostics.frameSampleCapacity = 3600;
        if (memory.frameSampleCapacity < 60) memory.frameSampleCapacity = 60;
        if (memory.frameSampleCapacity > 3600) memory.frameSampleCapacity = 3600;
        if (battery.maxFps < 5) battery.maxFps = 5;
        if (battery.maxFps > 260) battery.maxFps = 260;
    }

    public void save() {
        try {
            Files.writeString(path(), GSON.toJson(this));
        } catch (IOException e) {
            org.zouhmi.momentum.MomentumClient.LOGGER.warn("Failed to save Momentum config", e);
        }
    }

    public List<String> describeActiveFeatures() {
        List<String> out = new ArrayList<>();
        // Only list features actually wired into runtime hooks today.
        if (entities.enabled && entities.entityCulling) out.add("entityFrustumCulling");
        if (blockEntities.enabled && blockEntities.blockEntityCulling) out.add("blockEntityCulling");
        if (players.enabled && players.deduplicateRenderState) out.add("playerRenderDedup");
        if (chunks.enabled && chunks.coalesceDuplicateRebuilds) out.add("chunkRebuildCoalescing");
        if (rendering.enabled && rendering.reduceOptionalParticles) out.add("particleThrottling");
        if (memory.enabled && memory.trimCachesOnUnload) out.add("cacheTrimOnUnload");
        if (diagnostics.enabled) out.add("frameMetrics");
        if (window.borderlessFullscreen) out.add("borderlessFullscreen");
        if (battery.enabled) out.add("batterySaver");
        return out;
    }

    /**
     * Test hook: redirect persistence to a custom path, null restores default.
     */
    public static void setPathOverride(Path p) {
        pathOverride = p;
    }
}
