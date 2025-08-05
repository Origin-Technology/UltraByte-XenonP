package dev.opan.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import dev.opan.events.Event;

@Getter
@AllArgsConstructor
public class MouseInputEvent extends Event {
    private final int button;
}
