package dev.ultrabyte.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import dev.ultrabyte.events.Event;
import dev.ultrabyte.modules.Module;

@AllArgsConstructor @Getter
public class ToggleModuleEvent extends Event {
    private final Module module;
    private final boolean state;
}
