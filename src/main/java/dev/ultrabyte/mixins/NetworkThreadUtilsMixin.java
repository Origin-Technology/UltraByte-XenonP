package dev.ultrabyte.mixins;

import dev.ultrabyte.UltraByte;
import dev.ultrabyte.events.impl.PacketReceiveSyncEvent;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.OffThreadException;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.thread.ThreadExecutor;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import static net.minecraft.network.NetworkThreadUtils.createCrashException;

@Mixin(NetworkThreadUtils.class)
public class NetworkThreadUtilsMixin {
    @Shadow
    private static final Logger LOGGER = null;
    /**
     * @author
     * @reason
     */
    @Overwrite
    public static <T extends PacketListener> void forceMainThread(Packet<T> packet, T listener, ThreadExecutor<?> engine) throws OffThreadException {
        if (!engine.isOnThread()) {
            engine.executeSync(() -> {
                if (listener.accepts(packet)) {
                    try {
                        PacketReceiveSyncEvent packetReceiveSyncEvent = new PacketReceiveSyncEvent(packet);
                        UltraByte.EVENT_HANDLER.post(packetReceiveSyncEvent);

                        if (packet instanceof BundleS2CPacket bundleS2CPacket) {
                            for (Packet<?> subPacket : bundleS2CPacket.getPackets()) {
                                UltraByte.EVENT_HANDLER.post(new PacketReceiveSyncEvent(subPacket));
                            }
                        }

                        if (packetReceiveSyncEvent.isCancelled())
                            return;

                        packet.apply(listener);
                    } catch (Exception var4) {
                        if (var4 instanceof CrashException) {
                            CrashException crashException = (CrashException)var4;
                            if (crashException.getCause() instanceof OutOfMemoryError) {
                                throw createCrashException(var4, packet, listener);
                            }
                        }

                        listener.onPacketException(packet, var4);
                    }
                } else {
                    LOGGER.debug("Ignoring packet due to disconnection: {}", packet);
                }

            });
            throw OffThreadException.INSTANCE;
        }
    }
}
