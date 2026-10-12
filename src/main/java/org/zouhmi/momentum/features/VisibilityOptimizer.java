package org.zouhmi.momentum.features;

import net.minecraft.client.render.Frustum;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import org.zouhmi.momentum.MomentumClient;
import org.zouhmi.momentum.config.MomentumConfig;

/**
 * Real frustum-based entity culling helper using box visibility.
 * Never occlusion: it only skips entities whose bounds are truly outside the camera frustum.
 */
public class VisibilityOptimizer {

    /**
     * Decides whether the entity should be rendered. Returns true when vanilla would render it.
     * Called from the EntityRenderer.shouldRender inject.
     */
    public static boolean shouldEntityRender(Entity entity, Frustum frustum) {
        MomentumConfig cfg = MomentumClient.getConfig();
        if (cfg == null || !cfg.entities.enabled || !cfg.entities.entityCulling) {
            return true;
        }
        if (entity == null || frustum == null) return true;
        Box box = entity.getBoundingBox();
        if (box == null) return true;
        // Conservatively skip only when the frustum definitively excludes the box.
        return frustum.isVisible(box);
    }
}
