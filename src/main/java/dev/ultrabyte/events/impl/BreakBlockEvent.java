package dev.ultrabyte.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import dev.ultrabyte.events.Event;
import net.minecraft.util.math.BlockPos;

@Getter @AllArgsConstructor
public class BreakBlockEvent extends Event {
    private final BlockPos pos;
}
