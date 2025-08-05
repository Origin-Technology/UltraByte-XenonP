package dev.ultrabyte.managers;

import dev.ultrabyte.UltraByte;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import lombok.Getter;
import dev.ultrabyte.events.SubscribeEvent;
import dev.ultrabyte.events.impl.*;
import dev.ultrabyte.modules.impl.miscellaneous.FastLatencyModule;
import dev.ultrabyte.utils.IMinecraft;
import dev.ultrabyte.utils.system.Timer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

@Getter
public class ServerManager implements IMinecraft {
    private final Timer setbackTimer = new Timer();
    private final Timer responseTimer = new Timer();

    private final float[] tickRates = new float[20];
    private int nextIndex = 0;
    private long lastUpdate = -1;
    private long timeJoined;
    private final ConcurrentLinkedQueue<Long> pingMeasurements = new ConcurrentLinkedQueue<>();
    private long lastKeepAliveSendTime;
    private Pair<ServerAddress, ServerInfo> lastConnection;

    public ServerManager() {
        UltraByte.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveAsyncEvent event) {
        responseTimer.reset();

        if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
            setbackTimer.reset();
        }

        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
            tickRates[nextIndex] = Math.clamp(20.0f / ((System.currentTimeMillis() - lastUpdate) / 1000.0F), 0.0f, 20.0f);
            nextIndex = (nextIndex + 1) % tickRates.length;
            lastUpdate = System.currentTimeMillis();
        }
    }

    @SubscribeEvent
    public void onClientConnect(ClientConnectEvent event) {
        Arrays.fill(tickRates, 0);
        nextIndex = 0;
        timeJoined = System.currentTimeMillis();
        lastUpdate = System.currentTimeMillis();
    }

    @SubscribeEvent
    public void handleConnections(PacketReceiveAsyncEvent event) {
        if (mc.world == null) return;

        if (event.getPacket() instanceof PlayerListS2CPacket packet) {
            if (packet.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER)) {
                for(PlayerListS2CPacket.Entry entry : packet.getPlayerAdditionEntries()) {
                    UltraByte.EVENT_HANDLER.post(new PlayerConnectEvent(entry.profile().getId()));
                }
            }
        } else if(event.getPacket() instanceof PlayerRemoveS2CPacket packet) {
            for(UUID id : packet.profileIds()) {
                UltraByte.EVENT_HANDLER.post(new PlayerDisconnectEvent(id));
            }
        }
    }

    @SubscribeEvent
    public void onServerConnect(ServerConnectEvent event) {
        lastConnection = new ObjectObjectImmutablePair<>(event.getAddress(), event.getInfo());
    }

    public float getTickRate() {
        if (mc.player == null) return 0;
        if (System.currentTimeMillis() - timeJoined < 4000) return 20;

        int ticks = 0;
        float tickRates = 0.0f;

        for (float tickRate : this.tickRates) {
            if (tickRate > 0) {
                tickRates += tickRate;
                ticks++;
            }
        }

        return tickRates / ticks;
    }

    public int getPingDelay() {
        return (int) (getPing() / 25.0f);
    }

    public int getPing() {
        if (UltraByte.MODULE_MANAGER.getModule(FastLatencyModule.class).isToggled()) {
            return UltraByte.MODULE_MANAGER.getModule(FastLatencyModule.class).getLatency();
        }

        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        return entry == null ? 0 : entry.getLatency();
    }

    public String getServerBrand() {
        if (mc.getCurrentServerEntry() == null || mc.getNetworkHandler() == null || mc.getNetworkHandler().getBrand() == null) return "Vanilla";
        return mc.getNetworkHandler().getBrand();
    }

    public String getServer() {
        return mc.isInSingleplayer() ? "Singleplayer" : ServerAddress.parse(mc.getCurrentServerEntry().address).getAddress();
    }

    public long getCurrentPing() {
        if(pingMeasurements.isEmpty()) {
            return -1; // 表示无有效数据
        }

        long sum = 0;
        int count = 0;
        for(Long ping : pingMeasurements) {
            if(ping != null) {
                sum += ping;
                count++;
            }
        }
        return count > 0 ? sum / count : -1;
    }
}
