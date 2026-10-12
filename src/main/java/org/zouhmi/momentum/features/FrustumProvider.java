package org.zouhmi.momentum.features;

import net.minecraft.client.render.Frustum;

/**
 * Holds the current camera frustum, refreshed each frame by WorldRendererMixin.
 * Null means unknown, callers must fall back to rendering.
 */
public final class FrustumProvider {
    private static volatile Frustum current;

    private FrustumProvider() {
    }

    public static void set(Frustum frustum) {
        current = frustum;
    }

    public static Frustum current() {
        return current;
    }

    public static void clear() {
        current = null;
    }
}
