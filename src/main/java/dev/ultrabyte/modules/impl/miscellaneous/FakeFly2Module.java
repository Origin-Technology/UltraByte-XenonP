package dev.ultrabyte.modules.impl.miscellaneous;

import dev.ultrabyte.events.SubscribeEvent;
import dev.ultrabyte.events.impl.PlayerUpdateEvent;
import dev.ultrabyte.modules.Module;
import dev.ultrabyte.modules.RegisterModule;
import dev.ultrabyte.settings.impl.BooleanSetting;
import dev.ultrabyte.settings.impl.ModeSetting;
import dev.ultrabyte.settings.impl.NumberSetting;
import dev.ultrabyte.utils.minecraft.InventoryUtils;
import dev.ultrabyte.utils.minecraft.NetworkUtils;
import dev.ultrabyte.utils.system.Timer;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import static dev.ultrabyte.modules.impl.miscellaneous.FakeFlyModule.silentSwapEquipChestplate;
import static dev.ultrabyte.modules.impl.miscellaneous.FakeFlyModule.silentSwapEquipElytra;

/**
 * @author NiuRen0827
 * Time:23:31
 */
@RegisterModule(name = "VanillaFakeFly+", description = "Fakes your fly.", category = Module.Category.MISCELLANEOUS)
public class FakeFly2Module
        extends Module {
    public ModeSetting autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);
    public final BooleanSetting motion = new BooleanSetting("MotionControl", "control your motion", true);
    public final BooleanSetting blockJump = new BooleanSetting("BlockJump", "jump out block awa", true);
    public final BooleanSetting armorSwap = new BooleanSetting("ArmorSwap", "armor swap but silent", true);
    public final BooleanSetting groundNoSwap = new BooleanSetting("GroundNoSwap", "no swap when ground", true);

    public final NumberSetting fireworkDelay = new NumberSetting("FireworkDelay", "the delay of firework", 0, 0, 2000 );
    private boolean isElytra = false;
    private Timer fireworkTimer = new Timer();

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        if (blockJump.getValue()) {
            if (!(mc.world.getBlockState(getFacePos()).getBlock() == Blocks.AIR)
                    && !(mc.world.getBlockState(getFacePos()).getBlock() == Blocks.WATER || (mc.world.getBlockState(getFacePos()).getBlock() == Blocks.LAVA))) {
                int prevSlot = mc.player.getInventory().selectedSlot;
                int pearl = InventoryUtils.find(Items.ENDER_PEARL);
                if (pearl != -1) {
                    InventoryUtils.switchSlot(autoSwitch.getValue(), pearl, prevSlot);
                    NetworkUtils.sendSequencedPacket(seq -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, seq, mc.player.getYaw(), mc.player.getPitch()));
                    InventoryUtils.switchBack(autoSwitch.getValue(), pearl, prevSlot);
                }
            }
        }
            // mc.options.forwardKey.isPressed()  mc.options.backKey.isPressed()  mc.options.leftKey.isPressed()
            // mc.options.rightKey.isPressed() mc.options.jumpKeyPressssssss motherfucker
        boolean trigger = false;
        if (motion.getValue()) {


            // 检测用户按键状态(空格和前进都行 )
            if (mc.options.forwardKey.isPressed()) {
                trigger = true;
            }
            if (mc.options.jumpKey.isPressed()) {
                trigger = true;
            }
        }
        if (!(groundNoSwap.getValue() && mc.player.isOnGround())) {
            // 切甲
            if (silentSwapEquipElytra()) {
                isElytra = true;
                int firework = InventoryUtils.find(Items.FIREWORK_ROCKET);
                if (!motion.getValue()) {
                    if (firework != -1
                            && fireworkTimer.passedMs(fireworkDelay.getValue().intValue())) {
                        mc.player.networkHandler.sendPacket
                                (new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                        useFirework(firework);
                        fireworkTimer.reset();
                    }
                } else {
                    if (trigger) {
                        //  event.setYaw(targetYaw);
                        //  event.setPitch(targetPitch);
                        //   Managers.NETWORK.sendSequencedPacket(seq -> new PlayerMoveC2SPacket.LookAndOnGround(targetYaw, targetPitch, mc.player.isOnGround()));
                        if (firework != -1
                                && fireworkTimer.passedMs(fireworkDelay.getValue().intValue())) {
                            mc.player.networkHandler.sendPacket
                                    (new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));

                            useFirework(firework);
                            fireworkTimer.reset();
                        }
                    }
                    if (armorSwap.getValue()) {
                        if (silentSwapEquipChestplate()) {
                            isElytra = false;
                            //      Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                            //   mc.player.startFallFlying();
                        }
                    }
                }

            }
        }
        if (!(mc.options.forwardKey.isPressed() || mc.options.backKey.isPressed() || mc.options.leftKey.isPressed() || mc.options.rightKey.isPressed() || mc.options.jumpKey.isPressed()) && motion.getValue()
                && !mc.player.isOnGround()) {
            mc.player.setVelocity(mc.player.getVelocity().multiply(0, 0, 0));
        }
    }
    private void useFirework(int slot) {
        int prevSlot = mc.player.getInventory().selectedSlot;
        InventoryUtils.switchSlot(autoSwitch.getValue(), slot, prevSlot);
        NetworkUtils.sendSequencedPacket(seq -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, seq, mc.player.getYaw(), mc.player.getPitch()));
        InventoryUtils.switchBack(autoSwitch.getValue(), slot, prevSlot);

    }
    private BlockPos getFacePos() {
        return mc.player.getBlockPos().up();
    }

}
