package dev.opan.modules.impl.core;

import dev.opan.modules.Module;
import dev.opan.modules.RegisterModule;
import dev.opan.settings.impl.BooleanSetting;
import dev.opan.settings.impl.NumberSetting;
import dev.opan.utils.animations.Animations;
import dev.opan.utils.animations.FadeUtils;

@RegisterModule(name = "AnimationModule", description = "Manages the client world renders.", category = Module.Category.CORE, persistent = true, drawn = false)
public class AnimationModule extends Module {
    public BooleanSetting hotbar = new BooleanSetting("Hotbar", "Adds whichever entity you middle click to your friends list.", true);
    public NumberSetting hotbarTime = new NumberSetting("hotbarTime", "The duration for the place render.", 300, 0, 1000);

    public static final FadeUtils inventoryFade = new FadeUtils(500);
    public static final Animations animation = new Animations();
}
