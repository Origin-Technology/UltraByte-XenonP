package dev.opan.events.impl;

import dev.opan.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.packet.Packet;

@Getter
@AllArgsConstructor
public class PacketReceiveSyncEvent extends Event{
    private final Packet<?> packet;
}
