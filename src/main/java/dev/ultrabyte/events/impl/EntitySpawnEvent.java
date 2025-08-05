package dev.ultrabyte.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import dev.ultrabyte.events.Event;
import net.minecraft.entity.Entity;

@AllArgsConstructor @Getter
public class EntitySpawnEvent extends Event {
    private final Entity entity;
}
