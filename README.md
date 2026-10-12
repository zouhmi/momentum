# Momentum

Client-side Minecraft performance mod for Fabric 1.21.11.

Developer: @Zouhmi. Website: https://zouhmi.com.

## Status

Feature-complete for the current scope. Wired features:
- Frame and tick timing metrics (bounded circular buffer, percentiles).
- Validated JSON config with safe defaults, lazy path resolution, and tests.
- Real frustum-based entity culling wired into `EntityRenderer.shouldRender`.
- Real frustum-based block entity culling wired into `BlockEntityRenderer.isInRenderDistance`, driven by a per-frame frustum captured in `WorldRenderer.renderMain`.
- Player render-state deduplication wired into `LivingEntityRenderer.updateRenderState`, skipping repeated identical preparation within the same frame.
- Particle throttling wired into `ParticleManager.addParticle`, dropping about half of spawned particles when Reduced mode is on.
- Chunk rebuild coalescing wired into `WorldRenderer.scheduleChunkRender` (duplicate requests suppressed, important passes always kept).
- Memory cache trim on world unload, wired into `MinecraftClient.disconnect`.
- Borderless fullscreen toggle.
- Battery saver mode with FPS cap.
- Four Momentum buttons inside the vanilla Video and Graphics screen (Borderless, Battery Saver, Low-End profile, Low-Latency profile).
- Low-End, Low-Latency, and Balanced profiles with preview, apply, and revert.

## Features

### Entity frustum culling
Skips rendering entities whose bounding box lies definitively outside the camera frustum. Does not skip entity ticking, logic, or server behavior. Recovered gracefully if the visibility calculation fails.

### Block entity culling
Skips rendering block entities outside the camera frustum using their block volume. Does not skip block entity ticking, so furnace smelting, chest logic, and redstone behavior are untouched. Recovered gracefully on internal errors.

### Player render dedup
When `deduplicateRenderState` is on, a player's render-state update is skipped if it was already prepared in the same frame. Cosmetic only: entity ticking and interpolation are untouched.

### Particle throttling
When `reduceOptionalParticles` is on, roughly half of newly created particles are dropped. Cosmetic only: gameplay particles still spawn, and no entities or blocks are affected.

### Chunk rebuild coalescing
Suppresses duplicate non-important chunk rebuild scheduling for the same packed chunk position within a single tick. Important passes always go through. Tracking map is bounded and periodically cleaned.

### Memory trim on world unload
Clears Momentum's own staged caches when a world disconnects. Does not touch vanilla world data.

### Borderless fullscreen
Switches the game window to a decoration-less fullscreen on the primary monitor. Toggled from Video and Graphics, state persists in config.

### Battery saver
Caps FPS (default 30, range 5 to 260) and remembers your previous cap so toggling off restores it. Toggled from Video and Graphics.

### Profiles
Low-End, Low-Latency, and Balanced presets map to settings that are actually wired. `ProfileManager.apply` commits, `revert` restores a captured baseline.

### Diagnostics
Frame time distribution, tick duration, and JVM heap info collected locally at startup. Samples bounded, default 300 frames.

## Install

1. Install Fabric Loader 0.19.5 or newer for Minecraft 1.21.11.
2. Drop `momentum-0.1.0.jar` in your mods folder with Fabric API.

## Build from source

Requires Java 25 or 21. Run:

```
./gradlew build
```

Built JAR appears in `build/libs`.

## Toolchain

- Minecraft 1.21.11
- Yarn mappings 1.21.11+build.6
- Fabric Loader 0.19.5
- Fabric API 0.141.6+1.21.11
- Fabric Loom 1.19.0-alpha.2
- Gradle 9.8.1
- Java 25 (targets 21 bytecode)

## Tests

JUnit 5 suite covers config clamping, serialization, and wired-feature listing. Run with `gradle test`.

## Known limitations

- Vanilla `MinecraftClient.render` framing wraps, not yet true input latency metrics.
- Particle throttling uses a plain random drop; more precise per-type filtering is future work.
- Particle culling (not just throttling) is future work, and so is occlusion culling beyond conservative frustum checks.
- Headless benchmark scripts were removed per user request; no automated performance claims are made.
