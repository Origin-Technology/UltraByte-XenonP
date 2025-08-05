package dev.ultrabyte.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import dev.ultrabyte.events.Event;
import net.minecraft.util.math.BlockPos;

@AllArgsConstructor @Getter
public class DestroyBlockEvent extends Event {
    private final BlockPos position;
}
