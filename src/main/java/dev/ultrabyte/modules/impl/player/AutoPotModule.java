package dev.ultrabyte.modules.impl.player;

import dev.ultrabyte.UltraByte;
import dev.ultrabyte.events.SubscribeEvent;
import dev.ultrabyte.events.impl.PlayerUpdateEvent;
import dev.ultrabyte.modules.Module;
import dev.ultrabyte.modules.RegisterModule;
import dev.ultrabyte.modules.impl.movement.PhaseModule;
import dev.ultrabyte.settings.impl.BooleanSetting;
import dev.ultrabyte.settings.impl.ModeSetting;
import dev.ultrabyte.utils.minecraft.InventoryUtils;
import dev.ultrabyte.utils.minecraft.NetworkUtils;
import dev.ultrabyte.utils.system.Timer;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

/**
 * @author NiuRen0827
 * Time:21:24
 */
@RegisterModule(name = "AutoPot", description = "auto throw pot", category = Module.Category.PLAYER)
public class AutoPotModule
extends Module {
    public final BooleanSetting groundCheck = new BooleanSetting("GroundCheck", "ground check", false);
    public final BooleanSetting resistance = new BooleanSetting("TurtleMaster", "resistance", true);
    public final BooleanSetting onlyWall = new BooleanSetting("OnlyWall", "only wall", true);
    public final BooleanSetting strength = new BooleanSetting("Strength", "strength", true);
    public final BooleanSetting inventory = new BooleanSetting("InventorySwap", "inventory swap", true);
    public ModeSetting autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);

   private int tick = 0;

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (getNull()) {
            return;
        }
        tick += 1;
        if (groundCheck.getValue() && !mc.player.isOnGround()) {
            return;
        }
        if (!(tick >= 10)) {
            return;
        } else tick = 0;

        if (resistance.getValue() && !(mc.player.hasStatusEffect(StatusEffects.SLOWNESS) || mc.player.hasStatusEffect(StatusEffects.RESISTANCE))) {
            if (onlyWall.getValue()) {
                if (PhaseModule.isInsideBlock()) {
                    throwPot(POT.RESISTANT, false);
                }
            }
            else throwPot(POT.RESISTANT, true);
        }
        if (strength.getValue() && !(mc.player.hasStatusEffect(StatusEffects.STRENGTH))) {
            if (onlyWall.getValue()) {
                if (PhaseModule.isInsideBlock()) {
                    throwPot(POT.STRENGTH, false);
                }
            }
            else throwPot(POT.STRENGTH, true);
        }

    }

    private void throwPot(POT pot, boolean rotate) {
        if (pot == POT.STRENGTH) {
            int prevSlot = mc.player.getInventory().selectedSlot;
            int slot = InventoryUtils.findPotionInventorySlot(StatusEffects.STRENGTH.value());
            boolean search = slot > 9;
            if (slot == -1) {
                return;
            }
            if (rotate) {
                //我是最好的三元表达式使用者
                UltraByte.ROTATION_MANAGER.packetRotate(mc.player.getYaw(), 90);
            }
            if (search && inventory.getValue()) {
                mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(0, mc.player.getInventory().selectedSlot + 36, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.PICKUP, mc.player);
            } else InventoryUtils.switchSlot(autoSwitch.getValue(), slot, prevSlot);
            NetworkUtils.sendSequencedPacket(sequence -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, sequence, mc.player.getYaw(),
                    rotate ? 90 : mc.player.getPitch()));
            if (search && inventory.getValue()) {
                mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(0, mc.player.getInventory().selectedSlot + 36, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.PICKUP, mc.player);
            }  else InventoryUtils.switchBack(autoSwitch.getValue(), slot, prevSlot);

        }
        if (pot == POT.RESISTANT) {
            int prevSlot = mc.player.getInventory().selectedSlot;
            int slot = InventoryUtils.findPotionInventorySlot(StatusEffects.RESISTANCE.value());
            boolean search = slot > 9;
            if (slot == -1) {
                return;
            }
            if (rotate) {
                //我是最好的三元表达式使用者
                UltraByte.ROTATION_MANAGER.packetRotate(mc.player.getYaw(), 90);
            }
            if (search && inventory.getValue()) {
                mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(0, mc.player.getInventory().selectedSlot + 36, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.PICKUP, mc.player);
            } else InventoryUtils.switchSlot(autoSwitch.getValue(), slot, prevSlot);
            NetworkUtils.sendSequencedPacket(sequence -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, sequence, mc.player.getYaw(),
                    rotate ? 90 : mc.player.getPitch()));
            if (search && inventory.getValue()) {
                mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(0, mc.player.getInventory().selectedSlot + 36, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.PICKUP, mc.player);
            }  else InventoryUtils.switchBack(autoSwitch.getValue(), slot, prevSlot);

        }
    }
    enum POT {
        RESISTANT,
        STRENGTH
    }
}
