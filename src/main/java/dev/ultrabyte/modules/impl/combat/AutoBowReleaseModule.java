package dev.ultrabyte.modules.impl.combat;

import dev.ultrabyte.UltraByte;
import dev.ultrabyte.events.SubscribeEvent;
import dev.ultrabyte.events.impl.PlayerUpdateEvent;
import dev.ultrabyte.modules.Module;
import dev.ultrabyte.modules.RegisterModule;
import dev.ultrabyte.settings.impl.NumberSetting;
import dev.ultrabyte.utils.minecraft.NetworkUtils;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

@RegisterModule(name = "AutoBowRelease", description = "Automatically releases your bow after a certain amount of time has passed.", category = Module.Category.COMBAT)
public class AutoBowReleaseModule extends Module {
    public NumberSetting ticks = new NumberSetting("Ticks", "The number of ticks that have to be waited for before releasing the bow.", 3, 0, 20);

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (UltraByte.MODULE_MANAGER.getModule(SelfBowModule.class).isToggled()) return;
        if ((mc.player.getOffHandStack().getItem() == Items.BOW || mc.player.getMainHandStack().getItem() == Items.BOW) && mc.player.isUsingItem()) {
            if (mc.player.getItemUseTime() >= ticks.getValue().intValue()) {
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
                NetworkUtils.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(mc.player.getOffHandStack().getItem() == Items.BOW ? Hand.OFF_HAND : Hand.MAIN_HAND, id, mc.player.getYaw(), mc.player.getPitch()));
                mc.player.stopUsingItem();
            }
        }
    }

    @Override
    public String getMetaData() {
        return String.valueOf(ticks.getValue().intValue());
    }
}
