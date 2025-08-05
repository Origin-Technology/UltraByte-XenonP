package dev.opan.modules.impl.miscellaneous;

import dev.opan.UltraByte;
import dev.opan.events.SubscribeEvent;
import dev.opan.events.impl.PacketReceiveAsyncEvent;
import dev.opan.events.impl.PacketSendEvent;
import dev.opan.modules.Module;
import dev.opan.modules.RegisterModule;
import dev.opan.settings.impl.BooleanSetting;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.world.gen.feature.EndSpikeFeature;
import net.minecraft.world.gen.feature.EndSpikeFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

@RegisterModule(name = "AntiPacketKick", description = "Prevents you from getting kicked by packet errors.", category = Module.Category.MISCELLANEOUS)
public class AntiPacketKickModule extends Module {

    public BooleanSetting explosions = new BooleanSetting("Explosions", ".",  true);
    public BooleanSetting c0f = new BooleanSetting("Spike", ".",  true);

    public BooleanSetting Look = new BooleanSetting("Look", ".",  true);
    public BooleanSetting Looks = new BooleanSetting("LookS", ".",  true);
    public BooleanSetting move = new BooleanSetting("Move", ".",  true);
    public BooleanSetting moves = new BooleanSetting("MoveS", ".",  true);
    public BooleanSetting Velocity = new BooleanSetting("Velocity", ".",  true);

    public BooleanSetting Tick = new BooleanSetting("Tick", ".",  true);

    public BooleanSetting Ticks = new BooleanSetting("TickS", ".",  true);
    public int tick;
    public int tickc;
    @SubscribeEvent
    public void onPacketReceive(PacketReceiveAsyncEvent event) {
        if (event.getPacket() instanceof ExplosionS2CPacket packet && explosions.getValue()) {
            tick = 6;
            if(Velocity.getValue()) {
                if (mc.player != null) {
                    mc.player.setVelocityClient(mc.player.getVelocity().getX() * 0, mc.player.getVelocity().getY() * 0, mc.player.getVelocity().getZ() * 0);
                }
            }
            event.setCancelled(true);
        }
        if (event.getPacket() instanceof EndSpikeFeature packet && c0f.getValue()) {
            if(tick == 6) {
                event.setCancelled(true);
            }
        }
        if (event.getPacket() instanceof EndSpikeFeatureConfig packet && c0f.getValue()) {
            if(tick == 6) {
                UltraByte.CHAT_MANAGER.tagged("Explosion = " + mc.player.getVelocity().getY(), "OpanBeta",getName());

                event.setCancelled(true);
            }
        }
        if (event.getPacket() instanceof PlayerMoveC2SPacket packet && move.getValue()) {
            if(tick == 6) {
                UltraByte.CHAT_MANAGER.tagged("Explosion = " + mc.player.getVelocity().getY(), "OpanBeta",getName());

                event.setCancelled(true);
            }
        }

        // val packet1 = ((ExplosionS2CPacket) event.getPacket()).explosionSound();
        if (event.getPacket() instanceof PlayerInputC2SPacket && Tick.getValue()) {
            if(tick == 6) {
                mc.player.tick();
                event.setCancelled(true);
            }
        }
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket&& Look.getValue()) {
            if(tick == 6){
                event.setCancelled(true);
                UltraByte.CHAT_MANAGER.tagged("Explosion = " + mc.player.getVelocity().getY(), "OpanBeta",getName());

                tick = 0;
            }
        }

    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacket() instanceof ExplosionS2CPacket packet && explosions.getValue()) {
            tickc = 6;
            event.setCancelled(true);
        }
        if (event.getPacket() instanceof Feature packet && c0f.getValue()) {
            if(tickc == 6) {
                event.setCancelled(true);
            }
        }
        if (event.getPacket() instanceof EndSpikeFeature packet && c0f.getValue()) {
            if(tickc == 6) {
                event.setCancelled(true);
            }
        }
        if (event.getPacket() instanceof EndSpikeFeatureConfig packet && c0f.getValue()) {
            if(tickc == 6) {
                event.setCancelled(true);
            }
        }
        if (event.getPacket() instanceof PlayerInputC2SPacket && Ticks.getValue()) {
            if(tickc == 6) {
                mc.player.tick();
                event.setCancelled(true);
            }
        }
        if (event.getPacket() instanceof PlayerMoveC2SPacket packet && moves.getValue()) {
            if(tickc == 6) {
                UltraByte.CHAT_MANAGER.tagged("Explosion = " + mc.player.getVelocity().getY(), "OpanBeta",getName());

                event.setCancelled(true);
            }
        }
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket && Looks.getValue()) {
            if(tickc == 6){
                event.setCancelled(true);
                tickc = 0;
            }
        }


    }
}
