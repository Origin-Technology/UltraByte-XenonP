package dev.ultrabyte.modules.impl.miscellaneous;

import dev.ultrabyte.events.SubscribeEvent;
import dev.ultrabyte.events.impl.RenderWorldEvent;
import dev.ultrabyte.events.impl.TickEvent;
import dev.ultrabyte.modules.Module;
import dev.ultrabyte.modules.RegisterModule;
import dev.ultrabyte.settings.impl.NumberSetting;
import dev.ultrabyte.utils.graphics.Renderer3D;
import dev.ultrabyte.utils.minecraft.WorldUtils;
import dev.ultrabyte.utils.system.Timer;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.awt.*;
import java.util.ArrayList;

@RegisterModule(name = "AnvilDupe", description = "Automatically places anvils in whitelisted positions.", category = Module.Category.MISCELLANEOUS)
public class AnvilDupeModule extends Module {
    public NumberSetting delay = new NumberSetting("Delay", "The delay for placing anvils.", 10, 0, 100);

    private final ArrayList<BlockPos> whitelist = new ArrayList<>();
    private final ArrayList<BlockPos> whitelistCopy = new ArrayList<>();
    private final Timer timer = new Timer();
    private boolean placeAnvils = false;
    private boolean holding = false;

    @Override
    public void onEnable() {
        whitelist.clear();
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (getNull()) return;

        if(mc.player.getMainHandStack().getItem() == Items.ANVIL && mc.options.sneakKey.isPressed() && !placeAnvils) {
            placeAnvils = true;
            whitelistCopy.clear();
            whitelistCopy.addAll(whitelist);
        }

        // Making your whitelist
        if (mc.player.getMainHandStack().getItem() instanceof SwordItem) {
            if (!(mc.crosshairTarget instanceof BlockHitResult hitResult)) return;
            BlockPos pos = hitResult.getBlockPos().offset(hitResult.getSide());

            if (mc.options.useKey.isPressed() && !holding) {
                if (whitelist.contains(pos)) whitelist.remove(pos);
                else whitelist.add(pos);
                holding = true;
            } else {
                holding = false;
            }
        } else if (mc.player.getMainHandStack().getItem() == Items.ANVIL && !whitelistCopy.isEmpty() && placeAnvils) {
            synchronized (whitelistCopy) {
                for (BlockPos pos : new ArrayList<>(whitelistCopy)) {
                    if(!timer.hasTimeElapsed(delay.getValue().intValue() * 10)) break;
                    WorldUtils.placeBlock(pos, WorldUtils.getDirection(pos, false), Hand.MAIN_HAND, true, false);
                    whitelistCopy.remove(pos);
                    timer.reset();
                }
            }

            if(whitelistCopy.isEmpty()) placeAnvils = false;
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        if(getNull() || whitelist.isEmpty()) return;

        for(BlockPos pos : whitelist) {
            Renderer3D.renderBox(event.getMatrices(), new Box(pos), new Color(255, 0, 0, 40));
            Renderer3D.renderBoxOutline(event.getMatrices(), new Box(pos), new Color(255, 0, 0, 120));
        }
    }
}
