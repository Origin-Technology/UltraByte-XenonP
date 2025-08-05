package dev.opan.events.impl;

import lombok.*;
import dev.opan.events.Event;

@Getter @Setter @AllArgsConstructor
public class ChatInputEvent extends Event {
    private String message;
}
