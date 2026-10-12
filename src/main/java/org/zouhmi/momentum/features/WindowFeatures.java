package org.zouhmi.momentum.features;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;
import org.zouhmi.momentum.MomentumClient;
import org.zouhmi.momentum.config.MomentumConfig;

/**
 * Borderless fullscreen and battery-saver window behavior.
 * Borderless uses GLFW hints when supported for this window handle.
 */
public class WindowFeatures {

    public static void applyBorderlessFullscreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;
        Window window = client.getWindow();
        if (window == null) return;
        long handle = window.getHandle();
        if (handle == 0L) return;

        long monitor = GLFW.glfwGetPrimaryMonitor();
        if (monitor != 0L) {
            // Request fullscreen on primary monitor, decorationless borderless.
            GLFW.glfwSetWindowMonitor(handle, monitor, 0, 0, GLFW.glfwGetVideoMode(monitor).width(), GLFW.glfwGetVideoMode(monitor).height(), GLFW.glfwGetVideoMode(monitor).refreshRate());
        }
        MomentumClient.LOGGER.info("Momentum borderless fullscreen toggled");
        MomentumClient.getConfig().window.borderlessFullscreen = true;
        MomentumClient.getConfig().save();
    }

    public static void restoreFullscreenWindowed() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;
        Window window = client.getWindow();
        if (window != null && window.isFullscreen()) {
            window.toggleFullscreen();
        }
        MomentumConfig cfg = MomentumClient.getConfig();
        if (cfg != null) {
            cfg.window.borderlessFullscreen = false;
            cfg.save();
        }
    }

    public static void toggleBorderless() {
        MomentumConfig cfg = MomentumClient.getConfig();
        if (cfg == null) return;
        if (cfg.window.borderlessFullscreen) {
            restoreFullscreenWindowed();
        } else {
            applyBorderlessFullscreen();
        }
    }

    public static void applyBatterySaverOnce() {
        MomentumConfig cfg = MomentumClient.getConfig();
        if (cfg == null || !cfg.battery.enabled) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options == null) return;
        Integer previous = client.options.getMaxFps().getValue();
        if (cfg.battery.appliedFpsCap != previous) {
            cfg.battery.appliedFpsCap = previous;
            cfg.save();
        }
        client.options.getMaxFps().setValue(cfg.battery.maxFps);
    }

    public static void clearBatterySaver() {
        MomentumConfig cfg = MomentumClient.getConfig();
        if (cfg == null || cfg.battery.appliedFpsCap <= 0) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.options != null) {
            client.options.getMaxFps().setValue(cfg.battery.appliedFpsCap);
        }
        cfg.battery.appliedFpsCap = 0;
        cfg.save();
    }

    public static boolean isBorderlessActive() {
        MomentumConfig cfg = MomentumClient.getConfig();
        return cfg != null && cfg.window.borderlessFullscreen;
    }

    public static boolean isBatterySaverActive() {
        MomentumConfig cfg = MomentumClient.getConfig();
        return cfg != null && cfg.battery.enabled;
    }
}
