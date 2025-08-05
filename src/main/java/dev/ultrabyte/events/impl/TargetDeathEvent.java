package dev.ultrabyte.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import dev.ultrabyte.events.Event;
import net.minecraft.entity.player.PlayerEntity;

@AllArgsConstructor @Getter
public class TargetDeathEvent extends Event {
    private final PlayerEntity player;
}
