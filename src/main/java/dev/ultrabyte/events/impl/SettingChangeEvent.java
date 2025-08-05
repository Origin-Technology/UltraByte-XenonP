package dev.ultrabyte.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import dev.ultrabyte.events.Event;
import dev.ultrabyte.settings.Setting;

@Getter @AllArgsConstructor
public class SettingChangeEvent extends Event {
    private final Setting setting;
}
