package dev.opan.events.impl;

import lombok.*;
import dev.opan.events.Event;

@EqualsAndHashCode(callSuper = true) @Data
public class KeyInputEvent extends Event {
    private final int key, modifiers;
}
