package dev.opan.modules.impl.miscellaneous;

import dev.opan.events.SubscribeEvent;
import dev.opan.events.impl.PacketSendEvent;
import dev.opan.events.impl.PlayerUpdateEvent;
import dev.opan.modules.Module;
import dev.opan.modules.RegisterModule;
import dev.opan.settings.impl.BooleanSetting;
import dev.opan.utils.minecraft.NetworkUtils;
import net.minecraft.item.Item;
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
