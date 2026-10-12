package org.zouhmi.momentum.features;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Box;
import org.zouhmi.momentum.MomentumClient;
import org.zouhmi.momentum.config.MomentumConfig;

/**
 * Frustum-based block entity culling helper.
 * Rendering-only: never touches block entity ticking or world behavior.
 */
public class BlockEntityOptimizer {

    /**
     * Returns true when the block entity should render this frame.
     */
    public static boolean shouldBlockEntityRender(BlockEntity blockEntity, Frustum frustum) {
        MomentumConfig cfg = MomentumClient.getConfig();
        if (cfg == null || !cfg.blockEntities.enabled || !cfg.blockEntities.blockEntityCulling) {
            return true;
        }
        if (blockEntity == null || frustum == null) return true;
        Box box = new Box(blockEntity.getPos());
        // Conservative: skip only when the frustum definitively excludes the block volume.
        return frustum.isVisible(box);
    }
}
