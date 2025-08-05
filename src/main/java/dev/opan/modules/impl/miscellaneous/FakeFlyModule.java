package dev.opan.modules.impl.miscellaneous;

import dev.opan.UltraByte;
import dev.opan.events.SubscribeEvent;
import dev.opan.events.impl.PlayerUpdateEvent;
import dev.opan.modules.Module;
import dev.opan.modules.RegisterModule;
import dev.opan.modules.impl.player.ThrowFireworkModule;
import dev.opan.settings.impl.BooleanSetting;
import dev.opan.settings.impl.ModeSetting;
import dev.opan.utils.minecraft.FindItemResult;
import dev.opan.utils.minecraft.InvUtils;
import dev.opan.utils.minecraft.InventoryUtils;
import dev.opan.utils.minecraft.NetworkUtils;
import dev.opan.utils.system.Timer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;


@RegisterModule(name = "VanillaFakeFly", description = "Fakes your fly.", category = Module.Category.MISCELLANEOUS)
public class FakeFlyModule extends Module {

    public final BooleanSetting cc = new BooleanSetting("FakeFly", "FakeFly", true);

    public ModeSetting autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);

    private boolean hasTriggered = false; // 状态追踪
    private final Timer timer = new Timer();

    // 在类作用域添加状态追踪变量
    @Override
    public void onEnable() {
        // 模块被激活时初始化状态
        hasTriggered = false;
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
      //  if (!.isEnabled()) return; // 确保模块处于激活状态
        if (mc.player == null || mc.player.isOnGround()) return;
        silentSwapEquipElytra();
        mc.player.networkHandler.sendPacket(
                new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING)
        );
        // 核心逻辑（仅执行一次）
        if (!hasTriggered) {
            UltraByte.MODULE_MANAGER.getModule(ThrowFireworkModule.class).onEnable();
            hasTriggered = true; // 标记已执行
        }
        silentSwapEquipChestplate();
        setToggled(true);
    }

    @Override
    public void onDisable() {
        // 模块禁用时重置状态
        hasTriggered = false;
    }

    // 保持 po() 方法不变
    public void po() {
        if (mc.player != null) {
            if (autoSwitch.getValue().equalsIgnoreCase("None") && mc.player.getMainHandStack().getItem() != Items.FIREWORK_ROCKET) {
                UltraByte.CHAT_MANAGER.tagged("You are currently not holding any fireworks.", getName());
                setToggled(false);
                return;
            }

            if (mc.player.getItemCooldownManager().isCoolingDown(new ItemStack(Items.FIREWORK_ROCKET))) {
                setToggled(false);
                return;
            }

            int slot = InventoryUtils.find(Items.FIREWORK_ROCKET, 0, autoSwitch.getValue().equalsIgnoreCase("AltSwap") || autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8);
            int previousSlot = mc.player.getInventory().selectedSlot;

            if (slot == -1) {
                UltraByte.CHAT_MANAGER.tagged("No fireworks could be found in your hotbar.", getName());
                setToggled(false);
                return;
            }
            InventoryUtils.switchSlot(autoSwitch.getValue(), slot, previousSlot);
            NetworkUtils.sendSequencedPacket(sequence -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, sequence, mc.player.getYaw(), mc.player.getPitch()));
            InventoryUtils.switchBack(autoSwitch.getValue(), slot, previousSlot);
        }
    }
    public static boolean silentSwapEquipChestplate() {
        if (mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem()
                .equals(Items.DIAMOND_CHESTPLATE)
                || mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem()
                .equals(Items.NETHERITE_CHESTPLATE)) {
            return false;
        }

        FindItemResult hotbarChestplateSlot = InvUtils.findInHotbar(Items.NETHERITE_CHESTPLATE);
        if (!hotbarChestplateSlot.found()) {
            hotbarChestplateSlot = InvUtils.findInHotbar(Items.DIAMOND_CHESTPLATE);
        }

        // If we have a chestplate in our hotbar, we can immediately swap
        if (hotbarChestplateSlot.found()) {
            mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, 6,
                    hotbarChestplateSlot.slot(), SlotActionType.SWAP, mc.player);
            return true;
        }

        // Search for the chestplate in the inventory
        FindItemResult inventorySlot = InvUtils.find(Items.NETHERITE_CHESTPLATE);
        if (!inventorySlot.found()) {
            inventorySlot = InvUtils.find(Items.DIAMOND_CHESTPLATE);
        }

        if (!inventorySlot.found()) {
            return false;
        }

        // Pick a good slot in the hotbar that isn't a totem (try to prevent tfails while dhanding?)
        FindItemResult hotbarSlot = InvUtils.findInHotbar(x -> {
            if (x.getItem() == Items.TOTEM_OF_UNDYING) {
                return false;
            }
            return true;
        });

        // Move chestplate to hotbarSlot
        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, inventorySlot.slot(),
                hotbarSlot.found() ? hotbarSlot.slot() : 0, SlotActionType.SWAP, mc.player);

        // Equip chestplate
        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, 6,
                hotbarSlot.found() ? hotbarSlot.slot() : 0, SlotActionType.SWAP, mc.player);

        // Move old item back to hotbar slot
        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, inventorySlot.slot(),
                hotbarSlot.found() ? hotbarSlot.slot() : 0, SlotActionType.SWAP, mc.player);

        return true;
    }

    public static boolean silentSwapEquipElytra() {
        if (mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem().equals(Items.ELYTRA)) {
            return false;
        }
        FindItemResult inventorySlot = InvUtils.findInHotbar(Items.ELYTRA);

        if (inventorySlot.found()) {
            mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, 6,
                    inventorySlot.slot(), SlotActionType.SWAP, mc.player);
            return true;
        }

        inventorySlot = InvUtils.find(Items.ELYTRA);

        if (!inventorySlot.found()) {
            return false;
        }

        FindItemResult hotbarSlot = InvUtils.findInHotbar(x -> {
            if (x.getItem() == Items.TOTEM_OF_UNDYING) {
                return false;
            }
            return true;
        });

        // Move elytra to hotbarSlot
        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, inventorySlot.slot(),
                hotbarSlot.found() ? hotbarSlot.slot() : 0, SlotActionType.SWAP, mc.player);

        // Equip elytra
        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, 6,
                hotbarSlot.found() ? hotbarSlot.slot() : 0, SlotActionType.SWAP, mc.player);

        // Move old item back to hotbar slot
        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, inventorySlot.slot(),
                hotbarSlot.found() ? hotbarSlot.slot() : 0, SlotActionType.SWAP, mc.player);

        return true;
    }

}
