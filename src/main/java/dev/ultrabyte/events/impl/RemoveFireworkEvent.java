package dev.ultrabyte.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import dev.ultrabyte.events.Event;
import net.minecraft.entity.projectile.FireworkRocketEntity;

@Getter @AllArgsConstructor
public class RemoveFireworkEvent extends Event {
    private final FireworkRocketEntity entity;
}
