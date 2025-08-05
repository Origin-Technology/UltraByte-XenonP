package dev.ultrabyte.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import dev.ultrabyte.events.Event;
import net.minecraft.util.math.BlockPos;

@Getter @AllArgsConstructor
public class PlayerMineEvent extends Event {
    private final int actorID;
    private final BlockPos position;
}
