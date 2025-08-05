package dev.opan.utils.mixins;

import net.minecraft.client.render.RenderLayer;

public interface IMultiPhase {
    RenderLayer.MultiPhaseParameters opan$getParameters();
}
