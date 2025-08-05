package dev.opan.modules.impl.miscellaneous;

import dev.opan.UltraByte;
import dev.opan.events.SubscribeEvent;
import dev.opan.events.impl.PacketReceiveAsyncEvent;
import dev.opan.events.impl.PacketSendEvent;
import dev.opan.events.impl.TickEvent;
import dev.opan.modules.Module;
import dev.opan.modules.RegisterModule;
import dev.opan.settings.impl.BooleanSetting;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.MovementType;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;


@RegisterModule(name = "PreventGuard", description = "The feature is designed to prevent something from happening.", category = Module.Category.MISCELLANEOUS)
public class PreventGuardModule extends Module {
    public BooleanSetting pauseOnEat = new BooleanSetting("PauseOnEat", "PauseOnEat", false);
    public BooleanSetting pauseOnInventory = new BooleanSetting("PauseOnInventory", "PauseOnInventory", false);
    public BooleanSetting overdrive = new BooleanSetting("Guard", "Grim,Stop", false);
    public BooleanSetting grimDisable = new BooleanSetting("GrimV2", "Velocity", false);
    private int slot = 0;


    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (mc.player == null) return;
        if (pauseOnEat.getValue() && mc.player.isUsingItem()) return;
        if (pauseOnInventory.getValue() && !mc.player.currentScreenHandler.getCursorStack().isEmpty()) return;
        for (int i = 0; i < (overdrive.getValue() ? 2 : 1); i++) {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), UltraByte.ROTATION_MANAGER.getServerYaw(), UltraByte.ROTATION_MANAGER.getServerPitch(), mc.player.isOnGround(), mc.player.horizontalCollision));
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, mc.player.isCrawling() ? mc.player.getBlockPos() : mc.player.getBlockPos().up(), Direction.DOWN));

        }

        if (!grimDisable.getValue()) {
            float f = mc.player.getYaw();
            float g = mc.player.getPitch();
            float h = -MathHelper.sin(f * (float) (Math.PI / 180.0)) * MathHelper.cos(g * (float) (Math.PI / 180.0));
            float k = -MathHelper.sin(g * (float) (Math.PI / 180.0));
            float l = MathHelper.cos(f * (float) (Math.PI / 180.0)) * MathHelper.cos(g * (float) (Math.PI / 180.0));
            float m = MathHelper.sqrt(h * h + k * k + l * l);
            float n = 3.0F;
            h *= n / m;
            k *= n / m;
            l *= n / m;
            mc.player.addVelocity(h, k, l);
            if (mc.player.isOnGround()) {
                //mc.player.move(MovementType.SELF, new Vec3d(0.0, -1.1999999F, 0.0));
                mc.player.move(MovementType.SELF, new Vec3d(0.0, 1.1999999F, 0.0));
            }
        }
    }
    @SubscribeEvent
    public void onPacketReceive(PacketReceiveAsyncEvent event) {
        if (event.getPacket() instanceof UpdateSelectedSlotS2CPacket packet) {
            slot = ((UpdateSelectedSlotS2CPacket) event.getPacket()).slot();
        } else if (event.getPacket() instanceof CloseScreenS2CPacket && mc.currentScreen instanceof InventoryScreen && pauseOnInventory.getValue()) {
            event.setCancelled(true);
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket packet) {
            slot = packet.getSelectedSlot();
        }
    }
}