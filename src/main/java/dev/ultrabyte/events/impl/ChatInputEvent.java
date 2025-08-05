package dev.ultrabyte.events.impl;

import lombok.*;
import dev.ultrabyte.events.Event;

@Getter @Setter @AllArgsConstructor
public class ChatInputEvent extends Event {
    private String message;
}
