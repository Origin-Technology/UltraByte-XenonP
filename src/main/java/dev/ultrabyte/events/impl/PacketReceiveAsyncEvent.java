package dev.ultrabyte.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import dev.ultrabyte.events.Event;
import net.minecraft.network.packet.Packet;

@Getter @AllArgsConstructor
public class PacketReceiveAsyncEvent extends Event {
    private final Packet<?> packet;
}
