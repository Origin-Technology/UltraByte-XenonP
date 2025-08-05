package dev.ultrabyte.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import dev.ultrabyte.events.Event;

import java.util.UUID;

@AllArgsConstructor @Getter
public class PlayerConnectEvent extends Event {
    private final UUID id;
}
