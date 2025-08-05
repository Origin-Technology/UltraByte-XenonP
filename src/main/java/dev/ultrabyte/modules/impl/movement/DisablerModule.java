package dev.ultrabyte.modules.impl.movement;

import dev.ultrabyte.events.SubscribeEvent;
import dev.ultrabyte.events.impl.PacketSendEvent;
import dev.ultrabyte.mixins.accessors.PlayerMoveC2SPacketAccessor;
import dev.ultrabyte.modules.Module;
import dev.ultrabyte.modules.RegisterModule;
import dev.ultrabyte.settings.impl.BooleanSetting;
import dev.ultrabyte.utils.minecraft.NetworkUtils;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

@RegisterModule(name = "Disabler", description = "Disables Grim.", category = Module.Category.MOVEMENT)
public class DisablerModule extends Module {
    public BooleanSetting Grim = new BooleanSetting("Grim", "Test Grim", false);

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacket() instanceof PlayerMoveC2SPacket packet) {
            if (mc.player != null && Grim.getValue() && mc.player.hurtTime > 0) {
                if (packet.changesLook()) {
                    if (packet.changesPosition())
                        NetworkUtils.sendIgnoredPacket(new PlayerMoveC2SPacket.Full(packet.getX(mc.player.getX()), packet.getY(mc.player.getY()), packet.getZ(mc.player.getZ()), packet.getYaw(mc.player.getYaw()) + 11813401, packet.getPitch(mc.player.getPitch()), packet.isOnGround(), packet.horizontalCollision()));
                    else
                        NetworkUtils.sendIgnoredPacket(new PlayerMoveC2SPacket.LookAndOnGround(packet.getYaw(mc.player.getYaw()) + 11813401, packet.getPitch(mc.player.getPitch()), packet.isOnGround(), packet.horizontalCollision()));

                    ((PlayerMoveC2SPacketAccessor) packet).setYaw(packet.getYaw(mc.player.getYaw()) + 11813400);
                } else {
                    if (packet.changesPosition()) {
                        NetworkUtils.sendIgnoredPacket(new PlayerMoveC2SPacket.Full(packet.getX(mc.player.getX()), packet.getY(mc.player.getY()), packet.getZ(mc.player.getZ()), packet.getYaw(mc.player.getYaw()) + 11813401, packet.getPitch(mc.player.getPitch()), packet.isOnGround(), packet.horizontalCollision()));
                        NetworkUtils.sendIgnoredPacket(new PlayerMoveC2SPacket.Full(packet.getX(mc.player.getX()), packet.getY(mc.player.getY()), packet.getZ(mc.player.getZ()), packet.getYaw(mc.player.getYaw()) + 11813400, packet.getPitch(mc.player.getPitch()), packet.isOnGround(), packet.horizontalCollision()));
                        event.setCancelled(true);
                    } else {
                        NetworkUtils.sendIgnoredPacket(new PlayerMoveC2SPacket.LookAndOnGround(packet.getYaw(mc.player.getYaw()) + 11813401, packet.getPitch(mc.player.getPitch()), packet.isOnGround(), packet.horizontalCollision()));
                        NetworkUtils.sendIgnoredPacket(new PlayerMoveC2SPacket.LookAndOnGround(packet.getYaw(mc.player.getYaw()) + 11813400, packet.getPitch(mc.player.getPitch()), packet.isOnGround(), packet.horizontalCollision()));
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
