package dev.opan.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import dev.opan.events.Event;

import java.util.UUID;

@AllArgsConstructor @Getter
public class PlayerConnectEvent extends Event {
    private final UUID id;
}
