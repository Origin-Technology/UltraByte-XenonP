package dev.opan.utils.mixins;

import net.minecraft.client.render.RenderPhase;

public interface IMultiPhaseParameters {
    RenderPhase.Target opan$getTarget();
}
