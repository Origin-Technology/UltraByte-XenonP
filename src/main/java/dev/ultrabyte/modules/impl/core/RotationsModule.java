package dev.ultrabyte.modules.impl.core;

import dev.ultrabyte.events.SubscribeEvent;
import dev.ultrabyte.mixins.accessors.PlayerMoveC2SPacketAccessor;
import dev.ultrabyte.modules.Module;
import dev.ultrabyte.modules.RegisterModule;
import dev.ultrabyte.settings.impl.BooleanSetting;
import dev.ultrabyte.settings.impl.ModeSetting;
import dev.ultrabyte.settings.impl.NumberSetting;
import dev.ultrabyte.utils.system.Timer;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

@RegisterModule(name = "Rotations", description = "Manages the client's rotation system.", category = Module.Category.CORE, persistent = true, drawn = false)
public class RotationsModule extends Module {
    public BooleanSetting movementFix = new BooleanSetting("MovementFix", "Makes your movement in accordance with your yaw.", false);
    public BooleanSetting snapBack = new BooleanSetting("SnapBack", "Reverts rotations to previous values after rotating.", false);
    public ModeSetting snapBackMode = new ModeSetting("Mode", "Snap Back Mode Reverts rotations to previous values after rotating.",  new BooleanSetting.Visibility(snapBack, true),"ServerFull", new String[]{"ServerFull", "ServerLook", "ClientFull", "ClientLook","ServerYaw-pitch"});

    //RotationManager

    // 新增平滑配置项
    //Spam
    public BooleanSetting lowVersion = new BooleanSetting("LowVersion", "Piston Kick.", false);
    public BooleanSetting useSmoothRotation= new BooleanSetting("SmoothRotation", "Spam Fov.", false);
    public NumberSetting maxJitter = new NumberSetting("max Jitter", "随机抖动.",  4.0, 0.0, 16.0);
    public NumberSetting smoothFactor = new NumberSetting("SmoothFactor", "平滑视角.",  0.01f, 0.0f, 1.0f);
    public BooleanSetting Spam_grimRotation = new BooleanSetting("GrimRotation", "Spam Fov.", false);
    public NumberSetting fov = new NumberSetting("FOVThreshold", "Spam Fov Check.", new BooleanSetting.Visibility(Spam_grimRotation, true), 10.0, 0.0, 180.0);

    //PacketMode
    public ModeSetting GrimRotations = new ModeSetting("PacketMode", "Grim Rotations Bypass system.",  "WcAx3ps", new String[]{"WcAx3ps", "Catty", "3ar", "AI24","asphyxia"});
    public BooleanSetting Strict_grimRotation = new BooleanSetting("StrictRotation", "Funk FFFFFFFF : ).", new ModeSetting.Visibility(GrimRotations, "WcAx3ps"),false);
    public BooleanSetting Rotations = new BooleanSetting("ServerRotation", "CalculateAngle.", new ModeSetting.Visibility(GrimRotations, "AI24"),false);
    public BooleanSetting Look = new BooleanSetting("Look", "asphyxia.", new ModeSetting.Visibility(GrimRotations, "asphyxia"),false);

    //RaytraceBypass MioV2
    public BooleanSetting RaytraceBypass = new BooleanSetting("RaytraceBypass", "Raytrace Bypass GrimAc2.", false);

    public ModeSetting mode = new ModeSetting("RaytraceMode", "The method.", new BooleanSetting.Visibility(RaytraceBypass, true), "Motion", new String[]{"Motion", "Packet", "Client"});
    public BooleanSetting Always = new BooleanSetting("Always", "Motion Mode Always.", new ModeSetting.Visibility(mode, "Motion"), false);
    public NumberSetting Keep = new NumberSetting("Keep", "Keep pitch.", new BooleanSetting.Visibility(RaytraceBypass, true), 2.0, 0.0, 10.0);

    public NumberSetting Delay = new NumberSetting("Delay", "Raytrace  Delay.", new BooleanSetting.Visibility(RaytraceBypass, true), 250, 0, 1000);

    public NumberSetting offset = new NumberSetting("Offset", "Offset Raytrace.", new BooleanSetting.Visibility(RaytraceBypass, true), 15.0, 0.0, 40.0);

    Timer timer = new Timer();

    private float pitch = -91;
    @SubscribeEvent
    public void onPacketSend(Packet<?> packet) {
        if (RaytraceBypass.getValue()) {
            switch (mode.getValue()) {
                case "Packet" -> {
                    if (packet instanceof PlayerInteractBlockC2SPacket && timer.passedMs(Delay.getValue().intValue())) {
                        if (mc.player != null && mc.world != null && mc.world.isSpaceEmpty(mc.player.getBoundingBox().stretch((Keep.getValue().intValue()), (.0 + offset.getValue().intValue()), (Keep.getValue().intValue())))) {
                            pitch = (float) -75;
                            timer.reset();
                        }
                    }
                }
                case "Motion" -> {
                    if (packet instanceof PlayerMoveC2SPacket movePacket && pitch != -91) {
                        if (Always.getValue()) {
                            ((PlayerMoveC2SPacketAccessor) movePacket).setPitch(pitch);
                        }
                        ((PlayerMoveC2SPacketAccessor) movePacket).setPitch(pitch);
                        pitch = -91;
                    }
                }
                case "Client" -> {
                    if (packet instanceof PlayerInteractBlockC2SPacket && timer.passedMs(Delay.getValue().intValue())) {
                        if (mc.player != null && mc.world != null && mc.world.isSpaceEmpty(mc.player.getBoundingBox().stretch((Keep.getValue().intValue()), (.0 + offset.getValue().intValue()), (Keep.getValue().intValue())))) {
                            pitch = -75;
                            timer.reset();
                        }
                    }
                }
            }
        }
    }
}

