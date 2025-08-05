package dev.ultrabyte.mixins.accessors;

import net.minecraft.client.model.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ModelPart.Cuboid.class)
public interface ModelPartCuboidAccessor {
    @Accessor("sides")
    ModelPart.Quad[] getSides();
}
