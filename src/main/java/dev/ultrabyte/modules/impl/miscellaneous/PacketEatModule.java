package dev.ultrabyte.modules.impl.miscellaneous;

import dev.ultrabyte.events.SubscribeEvent;
import dev.ultrabyte.events.impl.PacketSendEvent;
import dev.ultrabyte.events.impl.PlayerUpdateEvent;
import dev.ultrabyte.modules.Module;
import dev.ultrabyte.modules.RegisterModule;
import dev.ultrabyte.settings.impl.BooleanSetting;
import dev.ultrabyte.utils.minecraft.NetworkUtils;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

@RegisterModule(name = "PacketEat", category = Module.Category.MISCELLANEOUS)
public class PacketEatModule extends Module {

    public BooleanSetting deSync = new BooleanSetting("deSync", "Sync.", false);

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (deSync.getValue() && mc.player.isUsingItem()){
            NetworkUtils.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, mc.player.getYaw(), mc.player.getPitch()));
        }
    }


    @SubscribeEvent
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacket() instanceof PlayerActionC2SPacket packet && packet.getAction() == PlayerActionC2SPacket.Action.RELEASE_USE_ITEM) {
            event.setCancelled(true);
        }
    }
}
