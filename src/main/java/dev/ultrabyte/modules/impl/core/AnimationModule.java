package dev.ultrabyte.modules.impl.core;

import dev.ultrabyte.modules.Module;
import dev.ultrabyte.modules.RegisterModule;
import dev.ultrabyte.settings.impl.BooleanSetting;
import dev.ultrabyte.settings.impl.NumberSetting;
import dev.ultrabyte.utils.animations.Animations;
import dev.ultrabyte.utils.animations.FadeUtils;

@RegisterModule(name = "AnimationModule", description = "Manages the client world renders.", category = Module.Category.CORE, persistent = true, drawn = false)
public class AnimationModule extends Module {
    public BooleanSetting hotbar = new BooleanSetting("Hotbar", "Adds whichever entity you middle click to your friends list.", true);
    public NumberSetting hotbarTime = new NumberSetting("hotbarTime", "The duration for the place render.", 300, 0, 1000);

    public static final FadeUtils inventoryFade = new FadeUtils(500);
    public static final Animations animation = new Animations();
}
