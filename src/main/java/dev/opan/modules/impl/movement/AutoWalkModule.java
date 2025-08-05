package dev.opan.modules.impl.movement;

import dev.opan.events.SubscribeEvent;
import dev.opan.events.impl.TickEvent;
import dev.opan.modules.Module;
import dev.opan.modules.RegisterModule;

@RegisterModule(name = "AutoWalk", description = "Automatically walks at all times.", category = Module.Category.MOVEMENT)
public class AutoWalkModule extends Module {
    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;
        mc.options.forwardKey.setPressed(true);
    }

    @Override
    public void onDisable() {
        if (mc.player == null || mc.world == null) return;
        mc.options.forwardKey.setPressed(false);
    }
}
