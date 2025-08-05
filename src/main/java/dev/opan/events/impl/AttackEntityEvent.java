package dev.opan.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import dev.opan.events.Event;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

@Getter @AllArgsConstructor
public class AttackEntityEvent extends Event {
    private final PlayerEntity player;
    private final Entity target;
}
