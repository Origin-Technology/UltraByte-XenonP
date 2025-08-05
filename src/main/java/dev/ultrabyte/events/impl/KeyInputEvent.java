package dev.ultrabyte.events.impl;

import lombok.*;
import dev.ultrabyte.events.Event;

@EqualsAndHashCode(callSuper = true) @Data
public class KeyInputEvent extends Event {
    private final int key, modifiers;
}
