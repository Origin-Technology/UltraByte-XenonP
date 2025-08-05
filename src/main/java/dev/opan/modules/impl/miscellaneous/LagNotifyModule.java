package dev.opan.modules.impl.miscellaneous;

import dev.opan.UltraByte;
import dev.opan.events.SubscribeEvent;
import dev.opan.events.impl.PacketReceiveAsyncEvent;
import dev.opan.events.impl.RenderOverlayEvent;
import dev.opan.modules.Module;
import dev.opan.modules.RegisterModule;
import dev.opan.settings.impl.BooleanSetting;
import dev.opan.settings.impl.ColorSetting;
import dev.opan.utils.color.ColorUtils;
import dev.opan.utils.system.MathUtils;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;

@RegisterModule(name = "LagNotify", description = "Notifies you when you lag.", category = Module.Category.MISCELLANEOUS)
public class LagNotifyModule extends Module {
    BooleanSetting server = new BooleanSetting("Server", "Notifies you when the server stops responding.", true);
    BooleanSetting lagback = new BooleanSetting("Lagback", "Notifies you when you lagback.", true);
    ColorSetting color = new ColorSetting("Color", "The color of the notification text.", ColorUtils.getDefaultOutlineColor());

    Vec3d lagPos = null;
    double lagDistance;
    long lagTime = System.currentTimeMillis();

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveAsyncEvent event) {
        if(getNull()) return;

        if(event.getPacket() instanceof PlayerPositionLookS2CPacket packet) {
            lagPos = new Vec3d(packet.change().position().getX(), packet.change().position().getY(), packet.change().position().getZ());
            lagDistance = mc.player.getPos().distanceTo(lagPos);
            lagTime = System.currentTimeMillis();
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderOverlayEvent event) {
        if(getNull()) return;

        int width = mc.getWindow().getScaledWidth() / 2, height = mc.getWindow().getScaledHeight() / 4;
        boolean flag = false;

        if(server.getValue() && UltraByte.SERVER_MANAGER.getResponseTimer().hasTimeElapsed(1000)) {
            String text = "Detected server not responding for " + MathUtils.round(UltraByte.SERVER_MANAGER.getResponseTimer().timeElapsed()/1000f, 1) + "s.";
            UltraByte.FONT_MANAGER.drawTextWithShadow(event.getContext(),text, width - UltraByte.FONT_MANAGER.getWidth(text) / 2, height - UltraByte.FONT_MANAGER.getHeight(), color.getColor());
            flag = true;
        }

        if(lagback.getValue() && System.currentTimeMillis() - lagTime < 3000) {
            String text = "Detected lagback of " + MathUtils.round(lagDistance, 1) + " blocks " + MathUtils.round((System.currentTimeMillis() - lagTime) / 1000f, 1) + "s.";
            UltraByte.FONT_MANAGER.drawTextWithShadow(event.getContext(),text, width - UltraByte.FONT_MANAGER.getWidth(text) / 2, height - UltraByte.FONT_MANAGER.getHeight() + (flag ? UltraByte.FONT_MANAGER.getHeight() : 0), color.getColor());
        }
    }
}
