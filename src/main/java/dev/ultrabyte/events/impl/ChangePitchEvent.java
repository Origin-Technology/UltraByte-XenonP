package dev.ultrabyte.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import dev.ultrabyte.events.Event;

@Getter @AllArgsConstructor
public class ChangePitchEvent extends Event {
    private final float pitch;
}
